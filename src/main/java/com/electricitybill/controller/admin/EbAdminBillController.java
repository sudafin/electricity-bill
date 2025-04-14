package com.electricitybill.controller.admin;

import com.electricitybill.entity.vo.user.UserBillVO;
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
@Api(tags = "账单管理")
public class EbAdminBillController {
    @Resource
    private IEbBillService ebBillService;
    @ApiOperation("管理端查询用户账单")
    @GetMapping("{userId}")
    public List<UserBillVO> queryUserBill(@PathVariable @NotNull Long userId){
        return ebBillService.queryUserBill(userId);
    }
    
}
