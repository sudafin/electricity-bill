package com.electricitybill.controller.user;

import com.electricitybill.entity.dto.AliPay;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.bill.BillPageQuery;
import com.electricitybill.entity.vo.bill.BillPageVO;
import com.electricitybill.entity.vo.bill.BillUserDetailVO;
import com.electricitybill.service.IEbBillService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author huangdada
 * @version 1.0
 * 2025/03/26/09:40
 */

@RestController
@RequestMapping("/user/bill")
@Slf4j
@Api(tags = "用户端账单管理")
public class EbUserBillController {
    @Resource
    private IEbBillService ebBillService;



    @GetMapping("page")
    public PageDTO<BillPageVO> query(BillPageQuery billPageQuery){
        return ebBillService.query(billPageQuery);
    }

    @GetMapping("detailBill/{id}")
    public BillUserDetailVO detailBill(@PathVariable("id") Long billId){
        return ebBillService.detailBill(billId);
    }

    @GetMapping("/pay")
    public Map<String, Object> pay(AliPay aliPay) {
        return ebBillService.pay(aliPay);
    }


    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
       return ebBillService.payNotify(request);
    }
}
