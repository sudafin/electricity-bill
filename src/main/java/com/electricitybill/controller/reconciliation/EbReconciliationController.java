package com.electricitybill.controller.reconciliation;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.reconciliation.ApprovalDTO;
import com.electricitybill.entity.dto.reconciliation.ReconciliationPageQuery;
import com.electricitybill.entity.vo.reconciliation.ApprovalDetailVO;
import com.electricitybill.entity.vo.reconciliation.ReconciliationDetailVO;
import com.electricitybill.entity.vo.reconciliation.ReconciliationPageVO;
import com.electricitybill.service.IEbReconciliationService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@RestController
@RequestMapping("/reconciliation")
public class EbReconciliationController {
    @Resource
    private IEbReconciliationService ebReconciliationService;

    @GetMapping("/page")
    public PageDTO<ReconciliationPageVO> queryPage(ReconciliationPageQuery reconciliationPageQuery){
        return ebReconciliationService.queryPage(reconciliationPageQuery);
    }

    @GetMapping("/detail/{id}")
    public ReconciliationDetailVO queryReconciliationDetail(@PathVariable(name = "id") Long reconciliationId){
        return ebReconciliationService.queryReconciliationDetail(reconciliationId);
    }

    @GetMapping("/approve/detail/{id}")
    public ApprovalDetailVO queryApprovalReconciliationDetail(@PathVariable(name = "id") Long reconciliationId){
        return ebReconciliationService.queryApprovalReconciliationDetail(reconciliationId);
    }
    @PutMapping("approve/{id}")
    public R approveReconciliation(@PathVariable(name = "id") Long reconciliationId, @RequestBody ApprovalDTO approvalDTO){
        return ebReconciliationService.approveReconciliation(reconciliationId,approvalDTO);
    }

    @GetMapping("/export")
    @ApiOperation("导出运营数据报表")
    public void export(HttpServletResponse response) throws IOException {
        ebReconciliationService.export(response);
    }
}
