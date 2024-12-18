package com.electricitybill.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.notification.NotificationDTO;
import com.electricitybill.entity.dto.notification.NotificationPageQuery;
import com.electricitybill.entity.po.EbAdmin;
import com.electricitybill.entity.po.EbNotification;
import com.electricitybill.entity.po.EbNotificationRecipient;
import com.electricitybill.entity.po.EbRole;
import com.electricitybill.entity.vo.notification.NotificationDetailVO;
import com.electricitybill.entity.vo.notification.NotificationPageVO;
import com.electricitybill.enums.RoleType;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbAdminMapper;
import com.electricitybill.mapper.EbNotificationMapper;
import com.electricitybill.mapper.EbNotificationRecipientMapper;
import com.electricitybill.mapper.EbRoleMapper;
import com.electricitybill.service.IEbNotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import com.electricitybill.utils.UserContextUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
public class EbNotificationServiceImpl extends ServiceImpl<EbNotificationMapper, EbNotification> implements IEbNotificationService {
    @Resource
    private EbAdminMapper ebAdminMapper;
    @Resource
    private EbRoleMapper ebRoleMapper;
    @Resource
    private EbNotificationRecipientMapper ebNotificationRecipientMapper;
    @Override
    public PageDTO<NotificationPageVO> queryPage(NotificationPageQuery notificationPageQuery) {
        Page<EbNotification> ebNotificationPage = new Page<>(notificationPageQuery.getPageNo(), notificationPageQuery.getPageSize());
        //获取总的通知
        Page<EbNotification> page = lambdaQuery()
                .like(StringUtils.isNotBlank(notificationPageQuery.getTitle()), EbNotification::getTitle, notificationPageQuery.getTitle())
                .eq(StringUtils.isNotBlank(notificationPageQuery.getType()), EbNotification::getType, notificationPageQuery.getType())
                .page(ebNotificationPage);
        List<EbNotification> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(ebNotificationPage);
        }
        //查询admin接收的通知与总的通知过滤,过滤records中不是当前用户类型的通知
        List<EbNotificationRecipient> ebNotificationRecipients = ebNotificationRecipientMapper.selectList(new LambdaQueryWrapper<EbNotificationRecipient>().eq(EbNotificationRecipient::getRecipientId, UserContextUtils.getUser()));
        ArrayList<NotificationPageVO> notificationPageVOS = new ArrayList<>();
        records.forEach(ebNotification -> {
            NotificationPageVO notificationPageVO = new NotificationPageVO();
            ebNotificationRecipients.stream()
                    .filter(ebNotificationRecipient -> ebNotificationRecipient.getNotificationId().equals(ebNotification.getId())).findFirst().ifPresent(ebNotificationRecipient -> {
                        notificationPageVO.setTitle(ebNotification.getTitle());
                        notificationPageVO.setId(ebNotification.getId());
                        notificationPageVO.setLevel(ebNotification.getLevel());
                        notificationPageVO.setType(ebNotification.getType());
                        notificationPageVO.setContent(ebNotification.getContent());
                        notificationPageVO.setCreateTime(ebNotification.getCreatedAt());
                        notificationPageVO.setReadStatus(ebNotificationRecipient.getReadStatus());
                        notificationPageVOS.add(notificationPageVO);
            });
        });
        page.setTotal(notificationPageVOS.size());
        //计算pages的公式是总数/每页显示的数量+1
        page.setPages(notificationPageVOS.size() / page.getSize() + 1 );
        return PageDTO.of(page, notificationPageVOS);
    }

    @Override
    public NotificationDetailVO queryNotificationDetail(Long notificationId) {
        EbNotification ebNotification = baseMapper.selectById(notificationId);
        if (ObjectUtils.isEmpty(ebNotification)) {
            throw new DbException(Constant.NOTIFICATION_NOT_FOUND);
        }
        //将该通知所在的用户改为已读
        EbNotificationRecipient ebNotificationRecipient = ebNotificationRecipientMapper.selectOne(new LambdaQueryWrapper<EbNotificationRecipient>().eq(EbNotificationRecipient::getNotificationId, notificationId)
                .eq(EbNotificationRecipient::getRecipientId,UserContextUtils.getUser()));
        ebNotificationRecipient.setReadStatus(1);
        ebNotificationRecipientMapper.updateById(ebNotificationRecipient);
        //将列表数据返回回去
        NotificationDetailVO notificationDetailVO = new NotificationDetailVO();
        EbAdmin ebAdmin = ebAdminMapper.selectById(ebNotification.getSenderId());
        EbRole ebRole = ebRoleMapper.selectById(ebAdmin.getRoleId());
        notificationDetailVO.setSenderName(ebAdmin.getAccount());
        notificationDetailVO.setSenderRole(ebRole.getRoleName());
        notificationDetailVO.setContent(ebNotification.getContent());
        notificationDetailVO.setType(ebNotification.getType());
        notificationDetailVO.setLevel(ebNotification.getLevel());
        notificationDetailVO.setTitle(ebNotification.getTitle());
        notificationDetailVO.setCreateTime(ebNotification.getCreatedAt());
        notificationDetailVO.setExpireTime(ebNotification.getExpireTime());
        return notificationDetailVO;
    }

    @Override
    @Transactional
    public R create(NotificationDTO notificationDTO) {
        EbNotification ebNotification = new EbNotification();
        long id = IdUtil.getSnowflakeNextId();
        ebNotification.setId(id);
        ebNotification.setTitle(notificationDTO.getTitle());
        ebNotification.setContent(notificationDTO.getContent());
        ebNotification.setType(notificationDTO.getType());
        ebNotification.setLevel(notificationDTO.getLevel());
        ebNotification.setSenderId(UserContextUtils.getUser());
        ebNotification.setExpireTime(notificationDTO.getExpireTime());
        int insert = baseMapper.insert(ebNotification);
        if (insert != 1) {
            throw new DbException(Constant.DB_INSERT_FAILURE);
        }
        //获取发送者的列表
        List<String> senderList = notificationDTO.getSenderList();
        senderList.forEach(sender -> {
            EbNotificationRecipient ebNotificationRecipient = new EbNotificationRecipient();
            //获取刚插入的数据
            ebNotificationRecipient.setNotificationId(id);
            ebNotificationRecipient.setRecipientType(sender);
            ebNotificationRecipient.setReadStatus(0);
            EbRole ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getRoleName, sender));
            List<EbAdmin> ebAdminList = ebAdminMapper.selectList(new LambdaQueryWrapper<EbAdmin>().eq(EbAdmin::getRoleId, ebRole.getId()));
            ebAdminList.forEach(ebAdmin -> {
                ebNotificationRecipient.setRecipientId(ebAdmin.getId());
                ebNotificationRecipientMapper.insert(ebNotificationRecipient);
            });
        });
        return R.ok();
    }

    @Override
    public R deleteNotification(List<Long> ids) {
        //通知表主键id与接受通知表的通知id有外键关系,所以先删除接受通知表的数据再删除通知表数据
        int delete = ebNotificationRecipientMapper.delete(new LambdaQueryWrapper<EbNotificationRecipient>()
                .in(EbNotificationRecipient::getNotificationId, ids));
        int deleteBatchIds = baseMapper.deleteBatchIds(ids);
        if (deleteBatchIds != ids.size() || delete != ids.size()) {
            throw new DbException(Constant.DB_DELETE_FAILURE);
        }
        return R.ok();
    }
}
