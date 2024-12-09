package com.electricitybill.controller.config;


import com.electricitybill.entity.R;
import com.electricitybill.entity.vo.rate.RateInfoVO;
import com.electricitybill.service.IEbRateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@RestController
@RequestMapping("/rate")
public class EbRateController {
    @Resource
    private IEbRateService ebRateService;

    @GetMapping
    public List<RateInfoVO> getRate() {
        return ebRateService.getRate();
    }
    
    @PutMapping("edit/{id}")
    public R editRate(@PathVariable Long id, @RequestParam("rateValue") BigDecimal rateValue) {
        return ebRateService.editRate(id, rateValue);
    }
}
