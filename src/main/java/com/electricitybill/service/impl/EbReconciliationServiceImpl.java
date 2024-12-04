package com.electricitybill.service.impl;
import java.math.BigDecimal;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.reconciliation.ApprovalDTO;
import com.electricitybill.entity.po.EbAdmin;
import com.electricitybill.entity.vo.reconciliation.ApprovalDetailVO;
import com.electricitybill.entity.vo.reconciliation.ApprovalRecordVO;
import com.electricitybill.entity.vo.user.UserPaymentRecordVO;
import com.electricitybill.enums.UserStatusType;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.mapper.EbAdminMapper;
import com.electricitybill.utils.UserContextUtils;
import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.reconciliation.ReconciliationPageQuery;
import com.electricitybill.entity.po.EbPayment;
import com.electricitybill.entity.po.EbReconciliation;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.reconciliation.ReconciliationDetailVO;
import com.electricitybill.entity.vo.reconciliation.ReconciliationPageVO;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbPaymentMapper;
import com.electricitybill.mapper.EbReconciliationMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbReconciliationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
@Slf4j
public class EbReconciliationServiceImpl extends ServiceImpl<EbReconciliationMapper, EbReconciliation> implements IEbReconciliationService {
    @Resource
    private EbUserMapper ebUserMapper;
    @Resource
    private EbPaymentMapper ebPaymentMapper;
    @Resource
    private EbAdminMapper adminMapper;
    @Override
    public PageDTO<ReconciliationPageVO> queryPage(ReconciliationPageQuery reconciliationPageQuery) {
        log.debug("queryPage:{}",reconciliationPageQuery);
        Page<EbReconciliation> ebReconciliationPage = new Page<>(reconciliationPageQuery.getPageNo(), reconciliationPageQuery.getPageSize());
        //reconciliation只有userId如果想要通过用户名查找需要先查询用户的信息
        List<EbUser> ebUserList = ebUserMapper.selectList(
                new LambdaQueryWrapper<EbUser>()
                        .eq(StringUtils.isNotBlank(reconciliationPageQuery.getMeterNo()), EbUser::getMeterNo, reconciliationPageQuery.getMeterNo())
                        .eq(StringUtils.isNotBlank(reconciliationPageQuery.getUsername()), EbUser::getUsername, reconciliationPageQuery.getUsername())
                        .eq(StringUtils.isNotBlank(reconciliationPageQuery.getUserType()), EbUser::getUserType, reconciliationPageQuery.getUserType()
                        ));
        if(CollUtils.isEmpty(ebUserList)&&StringUtils.isNotBlank(reconciliationPageQuery.getUsername())){
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        //收集id
        Set<Long> ids = ebUserList.stream().mapToLong(EbUser::getId).boxed().collect(Collectors.toSet());
        //正式查询
        Page<EbReconciliation> reconciliationPage = lambdaQuery()
                //假如其中有不为空的字段就走这个查询, 如果都为空就不走这个查询
                .in(!StringUtils.isAllBlank(reconciliationPageQuery.getMeterNo(), reconciliationPageQuery.getUsername(), reconciliationPageQuery.getUserType())
                        , EbReconciliation::getUserId, ids)
                .eq(StringUtils.isNotBlank(reconciliationPageQuery.getReconciliationNo()), EbReconciliation::getReconciliationNo, reconciliationPageQuery.getReconciliationNo())
                .eq(StringUtils.isNotBlank(reconciliationPageQuery.getReconciliationStatus()), EbReconciliation::getStatus, reconciliationPageQuery.getReconciliationStatus())
                .ge(reconciliationPageQuery.getStartDate() != null, EbReconciliation::getStartDate, reconciliationPageQuery.getStartDate())
                .le(reconciliationPageQuery.getEndDate() != null, EbReconciliation::getStartDate, reconciliationPageQuery.getEndDate())
                .page(ebReconciliationPage);
        List<EbReconciliation> records = reconciliationPage.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(ebReconciliationPage);
        }
        List<ReconciliationPageVO> reconciliationPageVOList = records.stream().map(reconciliation -> {
            ReconciliationPageVO reconciliationPageVO = new ReconciliationPageVO();
            reconciliationPageVO.setReconciliationNo(reconciliation.getReconciliationNo());
            reconciliationPageVO.setReconciliationStatus(reconciliation.getStatus());
            reconciliationPageVO.setReconciliationTime(reconciliation.getStartDate());
            reconciliationPageVO.setBalance(reconciliation.getTotalAmount());
            ebUserList.stream().filter(ebUser -> ebUser.getId().equals(reconciliation.getUserId())).findFirst().ifPresent(ebUser -> {
                reconciliationPageVO.setUsername(ebUser.getUsername());
                reconciliationPageVO.setMeterNo(ebUser.getMeterNo());
                reconciliationPageVO.setUserType(ebUser.getUserType());
            });
            return reconciliationPageVO;
        }).collect(Collectors.toList());
        return new PageDTO<>(reconciliationPage.getTotal(), reconciliationPage.getPages(),reconciliationPageVOList);
    }

    @Override
    public ReconciliationDetailVO queryReconciliationDetail(Long reconciliationId)  {
        ReconciliationDetailVO reconciliationDetailVO = new ReconciliationDetailVO();
        EbReconciliation ebReconciliation = lambdaQuery().eq(EbReconciliation::getReconciliationNo, reconciliationId).one();
        if (ObjectUtils.isEmpty(ebReconciliation)) {
            throw new DbException(Constant.RECONCILIATION_NOT_EXIST);
        }
        EbUser ebUser = ebUserMapper.selectById(ebReconciliation.getUserId());
        List<EbPayment> ebPaymentList = ebPaymentMapper.selectList(new LambdaQueryWrapper<EbPayment>().eq(EbPayment::getUserId, ebReconciliation.getUserId()));
        ArrayList<UserPaymentRecordVO> recordVOArrayList = new ArrayList<>();
        if (CollUtils.isEmpty(ebPaymentList)) {
            reconciliationDetailVO.setUserPaymentRecordVOList(CollUtils.emptyList());
        }else {
            ebPaymentList.forEach(ebPayment -> {
                UserPaymentRecordVO userPaymentRecordVO = new UserPaymentRecordVO();
                userPaymentRecordVO.setPaymentAmount(ebPayment.getAmount());
                userPaymentRecordVO.setPaymentStatus(ebPayment.getStatus());
                EbAdmin ebAdmin = adminMapper.selectById(ebPayment.getOperatorId());
                if (ObjectUtils.isEmpty(ebAdmin)) {
                    throw new DbException(Constant.DATA_QUERY_EMPTY);
                } else {
                    userPaymentRecordVO.setOperator(ebAdmin.getAccount());
                    userPaymentRecordVO.setRemark(ebPayment.getRemark());
                    userPaymentRecordVO.setPaymentTime(ebPayment.getPaymentTime());
                    userPaymentRecordVO.setPaymentMethod(ebPayment.getPaymentMethod());
                    recordVOArrayList.add(userPaymentRecordVO);
                }
                log.debug("userPaymentRecordVO的数据:{}", userPaymentRecordVO);
            });
        }
        reconciliationDetailVO.setReconciliationNo(ebReconciliation.getReconciliationNo());
        reconciliationDetailVO.setUsername(ebUser.getUsername());
        reconciliationDetailVO.setUserType(ebUser.getUserType());
        reconciliationDetailVO.setReconciliationStatus(ebReconciliation.getStatus());
        reconciliationDetailVO.setMeterNo(ebUser.getMeterNo());
        reconciliationDetailVO.setCreateTime(ebReconciliation.getCreatedAt());
        reconciliationDetailVO.setBalance(ebReconciliation.getTotalAmount());
        reconciliationDetailVO.setApprovalTime(ebReconciliation.getApprovalTime());
        if(ebReconciliation.getApproverId() != null) {
            EbAdmin ebAdmin = adminMapper.selectById(ebReconciliation.getApproverId());
            reconciliationDetailVO.setApprovalOperator(ebAdmin.getAccount());
        }else reconciliationDetailVO.setApprovalOperator(null);
        reconciliationDetailVO.setApprovalComment(ebReconciliation.getComment());
        reconciliationDetailVO.setUserPaymentRecordVOList(recordVOArrayList);
        reconciliationDetailVO.setPaymentStatus(ebReconciliation.getPaymentStatus());
        return reconciliationDetailVO;
    }

    @Override
    public R approveReconciliation(Long reconciliationId, ApprovalDTO approvalDTO) {
        EbReconciliation ebReconciliation = lambdaQuery().eq(EbReconciliation::getReconciliationNo, reconciliationId).one();
        if (ObjectUtils.isEmpty(ebReconciliation)) {
            throw new DbException(Constant.RECONCILIATION_NOT_EXIST);
        }
        if (ebReconciliation.getStatus().equals(approvalDTO.getStatus())) {
            throw new BadRequestException(Constant.RECONCILIATION_STATUS_NOT_CHANGE);
        }
        ebReconciliation.setStatus(approvalDTO.getStatus());
        ebReconciliation.setComment(approvalDTO.getComment());
        ebReconciliation.setApprovalTime(LocalDateTime.now());
        Long user = UserContextUtils.getUser();
        ebReconciliation.setApproverId(UserContextUtils.getUser());
        int res = baseMapper.updateById(ebReconciliation);
        if (res <= 0) {

            throw new DbException(Constant.DATA_UPDATE_FAILURE);
        }
        return R.ok();
    }

    @Override
    public ApprovalDetailVO queryApprovalReconciliationDetail(Long reconciliationId) {
        ApprovalDetailVO approvalDetailVO = new ApprovalDetailVO();
        EbReconciliation ebReconciliation = lambdaQuery().eq(EbReconciliation::getReconciliationNo, reconciliationId).one();
        if (ObjectUtils.isEmpty(ebReconciliation)) {
            throw new DbException(Constant.RECONCILIATION_NOT_EXIST);
        }
        EbUser ebUser = ebUserMapper.selectById(ebReconciliation.getUserId());
        approvalDetailVO.setReconciliationNo(ebReconciliation.getReconciliationNo());
        approvalDetailVO.setUsername(ebUser.getUsername());
        approvalDetailVO.setBalance(ebReconciliation.getTotalAmount());
        approvalDetailVO.setComment(ebReconciliation.getComment());
        approvalDetailVO.setIsApproved(ebReconciliation.getStatus().equals("通过"));
        approvalDetailVO.setStatus(ebReconciliation.getStatus());
        List<EbReconciliation> list = lambdaQuery().eq(EbReconciliation::getUserId, ebReconciliation.getUserId()).list();
        //把当前的审批记录排除
        list.remove(ebReconciliation);
        if (CollUtils.isEmpty(list)) {
            approvalDetailVO.setApprovalRecordList(CollUtils.emptyList());
        }else{
            List<ApprovalRecordVO> approvalRecordVOList = list.stream().map(reconciliation -> {
                ApprovalRecordVO approvalRecordVO = new ApprovalRecordVO();
                approvalRecordVO.setReconciliationNo(reconciliation.getReconciliationNo());
                approvalRecordVO.setApprovalStatus(reconciliation.getStatus());
                approvalRecordVO.setApprovalTime(reconciliation.getApprovalTime());
                approvalRecordVO.setApprovalOperator(adminMapper.selectById(reconciliation.getApproverId()).getAccount());
                approvalRecordVO.setComment(reconciliation.getComment());
                return approvalRecordVO;
            }).collect(Collectors.toList());
            approvalDetailVO.setApprovalRecordList(approvalRecordVOList);
        }
        return approvalDetailVO;
    }
}
