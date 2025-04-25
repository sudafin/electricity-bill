package com.electricitybill.controller.admin;

import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.bill.BillPageQuery;
import com.electricitybill.entity.vo.bill.BillAdminDetailVO;
import com.electricitybill.entity.vo.bill.BillPageAdminVO;
import com.electricitybill.entity.vo.bill.BillUserDetailVO;
import com.electricitybill.service.IEbBillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author huangdada
 * @version 1.0
 * 2025/03/26/09:40
 */

@RestController
@RequestMapping("/admin/bill")
@Slf4j
@Api(tags = "管理端账单管理")
public class EbAdminBillController {
    @Resource
    private IEbBillService ebBillService;
    @ApiOperation("管理端分页查询账单")
    @GetMapping("page")
    public PageDTO<BillPageAdminVO> query(BillPageQuery billPageQuery){
        return ebBillService.queryAdmin(billPageQuery);
    }

    @ApiOperation("管理端查询用户账单")
    @GetMapping("{id}")
    public BillAdminDetailVO queryUserBill(@PathVariable("id") @NotNull Long billId){
        return ebBillService.queryUserBillByAdmin(billId);
    }

}
