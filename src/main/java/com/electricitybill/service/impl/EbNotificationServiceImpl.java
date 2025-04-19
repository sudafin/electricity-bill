package com.electricitybill.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.notification.NotificationDTO;
import com.electricitybill.entity.dto.notification.NotificationPageQuery;
import com.electricitybill.entity.po.*;
import com.electricitybill.entity.query.PageQuery;
import com.electricitybill.entity.vo.notification.NotificationDetailVO;
import com.electricitybill.entity.vo.notification.NotificationPageVO;
import com.electricitybill.entity.vo.notification.NotificationUserVO;
import com.electricitybill.enums.NotificationType;
import com.electricitybill.enums.ReadStatusType;
import com.electricitybill.enums.ValidType;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.*;
import com.electricitybill.service.IEbNotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
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
    @Resource
    private EbAnnouncementMapper ebAnnouncementMapper;
    @Resource
    private EbUserMapper ebUserMapper;

    @Override
    public PageDTO<NotificationPageVO> queryPage(NotificationPageQuery notificationPageQuery) {
        Page<EbNotification> ebNotificationPage = new Page<>(notificationPageQuery.getPageNo(), notificationPageQuery.getPageSize());
        //获取总的通知
        Page<EbNotification> page = lambdaQuery()
                .like(StringUtils.isNotBlank(notificationPageQuery.getTitle()), EbNotification::getTitle, notificationPageQuery.getTitle())
                .eq(StringUtils.isNotBlank(notificationPageQuery.getType()), EbNotification::getType, notificationPageQuery.getType())
                .page(ebNotificationPage);
        List<EbNotification> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(ebNotificationPage);
        }
        //查询admin接收的通知与总的通知过滤,过滤records中不是当前用户类型的通知
        List<EbNotificationRecipient> ebNotificationRecipients = ebNotificationRecipientMapper.selectList(new LambdaQueryWrapper<EbNotificationRecipient>().eq(EbNotificationRecipient::getRecipientId, AdminContextUtils.getAdminId()));
        ArrayList<NotificationPageVO> notificationPageVOS = new ArrayList<>();
        records.forEach(ebNotification -> {
            NotificationPageVO notificationPageVO = new NotificationPageVO();
            ebNotificationRecipients.stream()
                    .filter(ebNotificationRecipient -> ebNotificationRecipient.getNotificationId().equals(ebNotification.getId())).findFirst().ifPresent(ebNotificationRecipient -> {
                        notificationPageVO.setTitle(ebNotification.getTitle());
                        notificationPageVO.setId(ebNotification.getId());
                        notificationPageVO.setType(ebNotification.getType());
                        notificationPageVO.setContent(ebNotification.getContent());
                        notificationPageVO.setCreateTime(ebNotification.getCreatedAt());
                        notificationPageVO.setReadStatus(ebNotificationRecipient.getReadStatus());
                        notificationPageVO.setExpireTime(ebNotification.getExpireTime());
                        notificationPageVOS.add(notificationPageVO);
                    });
        });
        page.setTotal(notificationPageVOS.size());
        //计算pages的公式是总数/每页显示的数量+1
        page.setPages(notificationPageVOS.size() / page.getSize() + 1);
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
                .eq(EbNotificationRecipient::getRecipientId, AdminContextUtils.getAdminId()));
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
        notificationDetailVO.setTitle(ebNotification.getTitle());
        notificationDetailVO.setCreateTime(ebNotification.getCreatedAt());
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
        ebNotification.setSenderType(notificationDTO.getNotificationType());
        ebNotification.setSenderId(AdminContextUtils.getAdminId());
        ebNotification.setValidType(ValidType.VALID.getValue());
        ebNotification.setExpireTime(notificationDTO.getExpireTime());
        int insert = baseMapper.insert(ebNotification);
        if (insert != 1) {
            throw new DbException(Constant.DB_INSERT_FAILURE);
        }
        List<String> senderList = notificationDTO.getSenderList();
        //获取发送者的列表,其中分为管理端和用户端的通知，SendType分为系统的内部通知和用户的公告通知
        if (notificationDTO.getNotificationType().equals(NotificationType.INTERNAL_NOTIFICATION.getDesc())) {
            //如果是管理端那么发送人的类型不能为空
            if (CollUtils.isEmpty(senderList)) {
                throw new DbException(Constant.NOTIFICATION_SENDER_LIST_EMPTY);
            }
            senderList.forEach(sender -> {
                EbNotificationRecipient ebNotificationRecipient = new EbNotificationRecipient();
                //获取刚插入的数据
                ebNotificationRecipient.setNotificationId(id);
                ebNotificationRecipient.setRecipientType(sender);
                ebNotificationRecipient.setReadStatus(ReadStatusType.UNREAD.getValue());
                EbRole ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getRoleName, sender));
                if (ObjectUtils.isEmpty(ebRole)) {
                    throw new DbException(Constant.ROLE_NOT_EXIST);
                }
                List<EbAdmin> ebAdminList = ebAdminMapper.selectList(new LambdaQueryWrapper<EbAdmin>().eq(EbAdmin::getRoleId, ebRole.getId()));
                if (CollUtils.isNotEmpty(ebAdminList)) {
                    ebAdminList.forEach(ebAdmin -> {
                        ebNotificationRecipient.setRecipientId(ebAdmin.getId());
                        ebNotificationRecipientMapper.insert(ebNotificationRecipient);
                    });
                }
            });
        } else if (notificationDTO.getNotificationType().equals(NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc())) {
            //如果用户通知则默认不需要传入发送人的列表，直接写入公告表里
            EbAnnouncement ebAnnouncement = new EbAnnouncement();
            ebAnnouncement.setContent(notificationDTO.getContent());
            ebAnnouncement.setTitle(notificationDTO.getTitle());
            ebAnnouncement.setStartTime(notificationDTO.getExpireTime());
            ebAnnouncement.setEndTime(notificationDTO.getExpireTime());
            ebAnnouncement.setStatus(ValidType.INVALID.getDesc());
            ebAnnouncementMapper.insert(ebAnnouncement);
        } else {
            throw new DbException(Constant.NOTIFICATION_TYPE_ERROR);
        }
        return R.ok();
    }

    @Override
    @Transactional
    public R deleteNotification(List<Long> ids) {
        //通知表主键id与接受通知表的通知id有外键关系,所以先删除接受通知表的数据再删除通知表数据
        int delete = ebNotificationRecipientMapper.delete(new LambdaQueryWrapper<EbNotificationRecipient>()
                .in(EbNotificationRecipient::getNotificationId, ids));
        if (delete != ids.size()) {
            throw new DbException(Constant.DB_DELETE_FAILURE);
        }
        List<EbNotification> ebNotifications = listByIds(ids);
        ebNotifications.forEach(ebNotification -> ebNotification.setValidType(ValidType.INVALID.getValue()));
        updateBatchById(ebNotifications);
        return R.ok();
    }

    @Override
    public PageDTO<NotificationUserVO> getNewNotificationList(PageQuery pageQuery) {
        Page<EbNotification> page = new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize());
        Long userId = UserContextUtils.getUserId();
        EbUser ebUser = ebUserMapper.selectById(userId);
        if (ObjectUtils.isEmpty(ebUser)) {
            throw new BizIllegalException(Constant.USER_NOT_EXIST);
        }
        //拿到最新的通知,包括反馈通知和公告通知,取前10条

        LambdaQueryWrapper<EbNotification> wrapper = new LambdaQueryWrapper<EbNotification>()
                .eq(EbNotification::getValidType, ValidType.VALID.getValue())
                .and(ebNotificationLambdaQueryWrapper -> ebNotificationLambdaQueryWrapper
                        .eq(EbNotification::getType, NotificationType.FEEDBACK_NOTIFICATION.getDesc())
                        .or()
                        .eq(EbNotification::getType, NotificationType.ANNOUNCEMENT_NOTIFICATION.getDesc())
                );
        Page<EbNotification> ebNotificationPage = page(page, wrapper);
        if (ebNotificationPage.getTotal() == 0){
            return PageDTO.empty(ebNotificationPage);
        }
        //如果为空返回空的集合
        if (CollUtils.isEmpty(ebNotificationPage.getRecords())) {
            return PageDTO.empty(ebNotificationPage);
        }
        List<EbNotification> list = ebNotificationPage.getRecords();
        //设置数据
        List<NotificationUserVO> notificationUserVOS = list.stream().map(ebNotification -> {
            NotificationUserVO notificationUserVO = new NotificationUserVO();
            notificationUserVO.setId(ebNotification.getId());
            notificationUserVO.setContent(ebNotification.getContent());
            notificationUserVO.setTitle(ebNotification.getTitle());
            notificationUserVO.setCreateTime(ebNotification.getCreatedAt());
            //查询当前用户是否在接收表插入了一条数据,插入说明已读
            Optional.ofNullable(
                    ebNotificationRecipientMapper.selectOne(
                            new LambdaQueryWrapper<EbNotificationRecipient>()
                                    .eq(EbNotificationRecipient::getNotificationId, ebNotification.getId())
                                    .eq(EbNotificationRecipient::getRecipientId, userId)
                    )
            ).ifPresentOrElse(
                    // 存在时的处理逻辑,说明有数据
                    ebNotificationRecipient -> notificationUserVO.setReadStatus(ebNotificationRecipient.getReadStatus()),
                    // 不存在时的处理逻辑
                    () -> notificationUserVO.setReadStatus(ReadStatusType.UNREAD.getValue()) // 假设默认未读状态
            );
            return notificationUserVO;
        }).collect(Collectors.toList());
        return PageDTO.of(ebNotificationPage, notificationUserVOS);
    }
}
