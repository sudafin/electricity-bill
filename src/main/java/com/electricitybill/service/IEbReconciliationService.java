package com.electricitybill.service;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.reconciliation.ApprovalDTO;
import com.electricitybill.entity.dto.reconciliation.ReconciliationPageQuery;
import com.electricitybill.entity.po.EbReconciliation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.reconciliation.ApprovalDetailVO;
import com.electricitybill.entity.vo.reconciliation.ReconciliationDetailVO;
import com.electricitybill.entity.vo.reconciliation.ReconciliationPageVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbReconciliationService extends IService<EbReconciliation> {

    PageDTO<ReconciliationPageVO> queryPage(ReconciliationPageQuery reconciliationPageQuery);

    ReconciliationDetailVO queryReconciliationDetail(Long reconciliationId);

    R approveReconciliation(Long reconciliationId, ApprovalDTO approvalDTO);

    ApprovalDetailVO queryApprovalReconciliationDetail(Long reconciliationId);

    void export(HttpServletResponse response) throws IOException;
}
