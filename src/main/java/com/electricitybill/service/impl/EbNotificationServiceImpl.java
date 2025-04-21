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
import com.electricitybill.entity.vo.notification.NotificationUserDetailVO;
import com.electricitybill.entity.vo.notification.NotificationUserPageVO;
import com.electricitybill.enums.NotificationType;
import com.electricitybill.enums.ReadStatusType;
import com.electricitybill.enums.ValidType;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.*;
import com.electricitybill.service.IEbNotificationRecipientService;
import com.electricitybill.service.IEbNotificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
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
    private IEbNotificationRecipientService ebNotificationRecipientService;
    @Resource
    private EbUserMapper ebUserMapper;

    @Override
    public PageDTO<NotificationPageVO> queryPage(NotificationPageQuery notificationPageQuery) {
        // 获取当前用户的通知接收信息
        List<EbNotificationRecipient> ebNotificationRecipientList = ebNotificationRecipientService.lambdaQuery()
                .eq(EbNotificationRecipient::getRecipientId, AdminContextUtils.getAdminId())
                .eq(StringUtils.isNotBlank(notificationPageQuery.getReadStatus()), EbNotificationRecipient::getReadStatus, notificationPageQuery.getReadStatus())
                .list();

        // 获取所有通知（不分页）
        List<EbNotification> records = lambdaQuery()
                .like(StringUtils.isNotBlank(notificationPageQuery.getTitle()), EbNotification::getTitle, notificationPageQuery.getTitle())
                .ne(EbNotification::getType, NotificationType.FEEDBACK_NOTIFICATION.getDesc())
                .eq(EbNotification::getValidType, ValidType.VALID.getValue())
                .list();

        // 联接两张表，通过通知ID进行过滤
        List<EbNotification> filteredRecords = records.stream()
                .filter(ebNotification -> ebNotificationRecipientList.stream()
                        .anyMatch(ebNotificationRecipient -> ebNotificationRecipient.getNotificationId().equals(ebNotification.getId())))
                .collect(Collectors.toList());

        // 如果过滤后的数据为空，直接返回空分页
        if (CollUtils.isEmpty(filteredRecords)) {
            Page<NotificationPageVO> emptyPage = new Page<>(notificationPageQuery.getPageNo(), notificationPageQuery.getPageSize());
            emptyPage.setTotal(0);  // 总数为0
            emptyPage.setRecords(Collections.emptyList());  // 数据为空
            return PageDTO.empty(emptyPage);
        }

        // 创建分页对象，手动设置分页逻辑
        int total = filteredRecords.size();  // 总记录数
        int pageSize = notificationPageQuery.getPageSize();  // 每页条数
        int pageNo = notificationPageQuery.getPageNo();  // 当前页数

        // 计算分页范围
        int fromIndex = (pageNo - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        // 获取当前页的数据
        List<EbNotification> pageData = filteredRecords.subList(fromIndex, toIndex);

        // 转换为 VO 对象
        List<NotificationPageVO> notificationPageVOS = new ArrayList<>(pageData.size());
        pageData.forEach(ebNotification -> {
            NotificationPageVO notificationPageVO = new NotificationPageVO();
            notificationPageVO.setTitle(ebNotification.getTitle());
            notificationPageVO.setId(ebNotification.getId());
            notificationPageVO.setType(ebNotification.getType());
            notificationPageVO.setContent(ebNotification.getContent());
            notificationPageVO.setCreateTime(ebNotification.getCreatedAt());
            ebNotificationRecipientList.forEach(ebNotificationRecipient -> {
                if (ebNotificationRecipient.getNotificationId().equals(ebNotification.getId())) {
                    notificationPageVO.setReadStatus(ebNotificationRecipient.getReadStatus());
                }
            });
            notificationPageVO.setExpireTime(ebNotification.getExpireTime());
            notificationPageVOS.add(notificationPageVO);
        });

        // 创建并设置返回的分页对象
        Page<NotificationPageVO> resultPage = new Page<>(pageNo, pageSize);
        resultPage.setTotal(total);  // 设置总记录数
        resultPage.setRecords(notificationPageVOS);  // 设置当前页数据

        return PageDTO.of(resultPage, notificationPageVOS);
    }


    @Override
    public NotificationDetailVO queryNotificationDetail(Long notificationId) {
        EbNotification ebNotification = baseMapper.selectById(notificationId);
        if (ObjectUtils.isEmpty(ebNotification) || ebNotification.getValidType().equals(ValidType.INVALID.getValue())) {
            throw new DbException(Constant.NOTIFICATION_NOT_FOUND);
        }
        //将该通知所在的用户改为已读
        EbNotificationRecipient ebNotificationRecipient = ebNotificationRecipientService.getOne(new LambdaQueryWrapper<EbNotificationRecipient>().eq(EbNotificationRecipient::getNotificationId, notificationId)
                .eq(EbNotificationRecipient::getRecipientId, AdminContextUtils.getAdminId()));
        ebNotificationRecipient.setReadStatus(ReadStatusType.READ.getValue());
        ebNotificationRecipientService.updateById(ebNotificationRecipient);
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
        // 确保 id 唯一
        while (getById(id) != null) {
            id = IdUtil.getSnowflakeNextId();
        }
        ebNotification.setId(id);
        ebNotification.setTitle(notificationDTO.getTitle());
        ebNotification.setContent(notificationDTO.getContent());
        ebNotification.setType(notificationDTO.getType());
        ebNotification.setSenderType(notificationDTO.getSenderType());
        ebNotification.setSenderId(AdminContextUtils.getAdminId());
        ebNotification.setValidType(ValidType.VALID.getValue());
        ebNotification.setExpireTime(notificationDTO.getExpireTime());
        int insert = baseMapper.insert(ebNotification);
        if (insert != 1) {
            throw new DbException(Constant.DB_INSERT_FAILURE);
        }

        List<String> senderList = notificationDTO.getSenderList();
        // 获取发送者的列表
        if (CollUtils.isEmpty(senderList)) {
            throw new DbException(Constant.NOTIFICATION_SENDER_LIST_EMPTY);
        }

        // 去重 senderList，避免重复插入
        Set<String> senderSet = new HashSet<>(senderList);
        Set<EbNotificationRecipient> ebNotificationRecipients = new HashSet<>();
        long finalId = id;
        for (String sender : senderSet) {
            // 获取对应角色
            EbRole ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getRoleName, sender));
            if (ObjectUtils.isEmpty(ebRole)) {
                throw new DbException(Constant.ROLE_NOT_EXIST);
            }
            // 获取对应角色的管理员列表
            List<EbAdmin> ebAdminList = ebAdminMapper.selectList(new LambdaQueryWrapper<EbAdmin>().eq(EbAdmin::getRoleId, ebRole.getId()));
            if (CollUtils.isEmpty(ebAdminList)) {
                continue;
            }
            ebAdminList.forEach(ebAdmin -> {
                //需要在最内层里循环内创建对象不然数据会被覆盖
                EbNotificationRecipient ebNotificationRecipient = new EbNotificationRecipient();
                ebNotificationRecipient.setNotificationId(finalId);
                ebNotificationRecipient.setRecipientType(sender);
                ebNotificationRecipient.setReadStatus(ReadStatusType.UNREAD.getValue());
                ebNotificationRecipient.setRecipientId(ebAdmin.getId());
                ebNotificationRecipients.add(ebNotificationRecipient);
            });
        }
        if (CollUtils.isNotEmpty(ebNotificationRecipients)) {
            ebNotificationRecipientService.saveBatch(ebNotificationRecipients);
        }else if (CollUtils.isEmpty(ebNotificationRecipients)) {
            throw new DbException(Constant.NOTIFICATION_RECIPIENT_LIST_EMPTY);
        }
        return R.ok();
    }


    @Override
    @Transactional
    public R deleteNotification(List<Long> ids) {
        //通知表主键id与接受通知表的通知id有外键关系,所以先删除接受通知表的数据再删除通知表数据
        ebNotificationRecipientService.remove(new LambdaQueryWrapper<EbNotificationRecipient>()
                .in(EbNotificationRecipient::getNotificationId, ids));
        List<EbNotification> ebNotifications = listByIds(ids);
        ebNotifications.forEach(ebNotification -> ebNotification.setValidType(ValidType.INVALID.getValue()));
        updateBatchById(ebNotifications);
        return R.ok();
    }

    @Override
    public PageDTO<NotificationUserPageVO> getNewNotificationList(PageQuery pageQuery) {
        Page<EbNotification> page = new Page<>(pageQuery.getPageNo(), pageQuery.getPageSize());
        Long userId = UserContextUtils.getUserId() == null ? 1 : UserContextUtils.getUserId();
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
                )
                .orderByDesc(EbNotification::getCreatedAt);
        Page<EbNotification> ebNotificationPage = page(page, wrapper);
        if (ebNotificationPage.getTotal() == 0) {
            return PageDTO.empty(ebNotificationPage);
        }
        //如果为空返回空的集合
        if (CollUtils.isEmpty(ebNotificationPage.getRecords())) {
            return PageDTO.empty(ebNotificationPage);
        }
        List<EbNotification> list = ebNotificationPage.getRecords();
        //设置数据
        List<NotificationUserPageVO> notificationUserPageVOS = list.stream().map(ebNotification -> {
            NotificationUserPageVO notificationUserPageVO = new NotificationUserPageVO();
            notificationUserPageVO.setId(ebNotification.getId());
            notificationUserPageVO.setContent(ebNotification.getContent());
            notificationUserPageVO.setTitle(ebNotification.getTitle());
            notificationUserPageVO.setCreateTime(ebNotification.getCreatedAt());
            //查询当前用户是否在接收表插入了一条数据,插入说明已读
            Optional.ofNullable(
                    ebNotificationRecipientService.getOne(
                            new LambdaQueryWrapper<EbNotificationRecipient>()
                                    .eq(EbNotificationRecipient::getNotificationId, ebNotification.getId())
                                    .eq(EbNotificationRecipient::getRecipientId, userId)
                                    .eq(EbNotificationRecipient::getRecipientType, ebNotification.getSenderType())
                    )
            ).ifPresentOrElse(
                    // 存在时的处理逻辑,说明有数据
                    ebNotificationRecipient -> notificationUserPageVO.setReadStatus(ebNotificationRecipient.getReadStatus()),
                    // 不存在时的处理逻辑
                    () -> notificationUserPageVO.setReadStatus(ReadStatusType.UNREAD.getValue()) // 假设默认未读状态
            );
            return notificationUserPageVO;
        }).collect(Collectors.toList());
        return PageDTO.of(ebNotificationPage, notificationUserPageVOS);
    }

    @Override
    public NotificationUserDetailVO getNotificationDetail(Long id) {
        EbNotification ebNotification = getById(id);
        if(ebNotification.getValidType() == ValidType.INVALID.getValue()){
            throw new BizIllegalException(Constant.NOTIFICATION_INVALID);
        }
        if (ObjectUtils.isEmpty(ebNotification)) {
            throw new BizIllegalException(Constant.NOTIFICATION_NOT_EXIST);
        }
        EbNotificationRecipient ebNotificationRecipient = new EbNotificationRecipient();
        ebNotificationRecipient.setNotificationId(id);
        ebNotificationRecipient.setRecipientType("user");
        ebNotificationRecipient.setRecipientId(UserContextUtils.getUserId());
        ebNotificationRecipient.setReadStatus(ReadStatusType.READ.getValue());
        ebNotificationRecipient.setReadTime(LocalDateTime.now());
        ebNotificationRecipientService.save(ebNotificationRecipient);
        return BeanUtils.copyBean(ebNotification, NotificationUserDetailVO.class);
    }
}
