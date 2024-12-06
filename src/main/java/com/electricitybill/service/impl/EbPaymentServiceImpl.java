package com.electricitybill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.paymennt.PaymentPageQuery;
import com.electricitybill.entity.po.EbPayment;
import com.electricitybill.entity.po.EbReconciliation;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.payment.PaymentDetailVO;
import com.electricitybill.entity.vo.payment.PaymentPageVO;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbPaymentMapper;
import com.electricitybill.mapper.EbReconciliationMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbPaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
public class EbPaymentServiceImpl extends ServiceImpl<EbPaymentMapper, EbPayment> implements IEbPaymentService {
    @Resource
    private EbReconciliationMapper reconciliationMapper;
    @Resource
    private EbUserMapper ebUserMapper;
    @Override
    public PageDTO<PaymentPageVO> queryPage(PaymentPageQuery paymentPageQuery) {
        Page<EbPayment> ebPaymentPage = new Page<>(paymentPageQuery.getPageNo(), paymentPageQuery.getPageSize());
        Page<EbPayment> paymentPage = lambdaQuery()
                .eq(StringUtils.isNotBlank(paymentPageQuery.getPaymentMethod()), EbPayment::getPaymentMethod, paymentPageQuery.getPaymentMethod())
                .eq(StringUtils.isNotBlank(paymentPageQuery.getStatus()), EbPayment::getStatus, paymentPageQuery.getStatus())
                .eq(paymentPageQuery.getPaymentId() != null, EbPayment::getId, paymentPageQuery.getPaymentId())
                .ge(paymentPageQuery.getStartDate() != null, EbPayment::getPaymentTime, paymentPageQuery.getStartDate())
                .le(paymentPageQuery.getEndDate() != null, EbPayment::getPaymentTime, paymentPageQuery.getEndDate())
                .page(ebPaymentPage);
        List<EbPayment> records = paymentPage.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(paymentPage);
        }
        ArrayList<PaymentPageVO> pageVOArrayList = new ArrayList<>();
        records.forEach(ebPayment -> {
            PaymentPageVO paymentPageVO = new PaymentPageVO();
            paymentPageVO.setPaymentMethod(ebPayment.getPaymentMethod());
            paymentPageVO.setPaymentTime(ebPayment.getPaymentTime());
            paymentPageVO.setPaymentId(ebPayment.getId());
            paymentPageVO.setStatus(ebPayment.getStatus());
            EbUser ebUser = ebUserMapper.selectById(ebPayment.getUserId());
            paymentPageVO.setUsername(ebUser.getUsername());
            paymentPageVO.setBalance(ebUser.getBalance());
            pageVOArrayList.add(paymentPageVO);
        });
        return PageDTO.of(paymentPage, pageVOArrayList);
    }

    @Override
    public PaymentDetailVO queryUserPayment(Long paymentId) {
        EbPayment ebPayment = baseMapper.selectById(paymentId);
        if(ObjectUtils.isEmpty(ebPayment)){
            throw new DbException(Constant.PAYMENT_NOT_EXIST);
        }
        PaymentDetailVO paymentDetailVO = new PaymentDetailVO();
        paymentDetailVO.setPaymentId(ebPayment.getId());
        EbUser ebUser = ebUserMapper.selectById(ebPayment.getUserId());
        paymentDetailVO.setUsername(ebUser.getUsername());
        paymentDetailVO.setUserStatus(ebUser.getAccountStatus());
        paymentDetailVO.setBalance(ebUser.getBalance());
        paymentDetailVO.setPaymentMethod(ebPayment.getPaymentMethod());
        paymentDetailVO.setStatus(ebPayment.getStatus());
        paymentDetailVO.setPaymentTime(ebPayment.getPaymentTime());
        if(ebPayment.getReconciliationId() != null) {
            EbReconciliation ebReconciliation = reconciliationMapper.selectOne(new LambdaQueryWrapper<EbReconciliation>().eq(EbReconciliation::getReconciliationNo, ebPayment.getReconciliationId()));
            paymentDetailVO.setReconciliationId(ebPayment.getId());
            paymentDetailVO.setReconciliationRemark(ebReconciliation.getComment());
            paymentDetailVO.setReconciliationStatus(ebReconciliation.getStatus());
        }
        paymentDetailVO.setIsReconciliate(ebPayment.getReconciliationId() != null);
        return paymentDetailVO;
    }

    @Override
    public R deleteUser(List<Long> ids) {
        int deleteBatchIds = baseMapper.deleteBatchIds(ids);
        if (deleteBatchIds != ids.size()) {
            throw new DbException(Constant.DB_DELETE_FAILURE);
        }
        return R.ok();
    }

    @Override
    public R refundPayment(Long paymentId) {
        EbPayment ebPayment = baseMapper.selectById(paymentId);
        if(ObjectUtils.isEmpty(ebPayment)){
            throw new DbException(Constant.PAYMENT_NOT_EXIST);
        }
        EbReconciliation ebReconciliation = reconciliationMapper.selectOne(new LambdaQueryWrapper<EbReconciliation>().eq(EbReconciliation::getReconciliationNo, ebPayment.getReconciliationId()));
        if(!ebReconciliation.getStatus().equals("退回")){
            throw new BadRequestException("该账单未审批");
        }
        ebPayment.setStatus("退款");
        ebPayment.setRefundAmount(ebPayment.getAmount());
        ebPayment.setRefundTime(LocalDateTime.now());
        int updateById = baseMapper.updateById(ebPayment);
        if (updateById != 1) {
            throw new DbException(Constant.DB_UPDATE_FAILURE);
        }
        return R.ok();
    }
}
