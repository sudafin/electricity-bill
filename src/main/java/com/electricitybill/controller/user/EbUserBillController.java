package com.electricitybill.controller.user;

import cn.hutool.json.JSONObject;
import com.alipay.api.AlipayApiException;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.AliPay;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.bill.BillPageQuery;
import com.electricitybill.entity.vo.bill.BillPageVO;
import com.electricitybill.entity.vo.bill.BillUserDetailVO;
import com.electricitybill.service.IEbBillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

    @GetMapping("overview")
    @ApiOperation("获取用户账单概览")
    public R<JSONObject> overview(){
        return R.ok(ebBillService.overview());
    }

    @GetMapping("page")
    public PageDTO<BillPageVO> query(@RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize") Integer pageSize){
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("pageNo",pageNo);
        jsonObject.set("pageSize",pageSize);
        return ebBillService.query(jsonObject);
    }

    @GetMapping("detail/{id}")
    public BillUserDetailVO detailBill(@PathVariable("id") Long billId){
        return ebBillService.detailBill(billId);
    }

    @GetMapping("/pay")
    public Map<String, Object> pay(Long billId) {
        AliPay aliPay = new AliPay();
        aliPay.setBillId(billId);
        return ebBillService.pay(aliPay);
    }


    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
       return ebBillService.payNotify(request);
    }


    @GetMapping("/status/{billId}")
    public String queryStatus(@PathVariable("billId") Long billId) throws AlipayApiException {
        return ebBillService.queryStatus(billId);
    }
}
