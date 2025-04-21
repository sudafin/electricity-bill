package com.electricitybill.controller.admin;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.rate.RateCrateDTO;
import com.electricitybill.entity.dto.rate.RatePageQuery;
import com.electricitybill.entity.vo.rate.RateDetailVO;
import com.electricitybill.entity.vo.rate.RatePageVO;
import com.electricitybill.service.IEbRateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "管理端费率管理")
public class EbRateController {
    @Resource
    private IEbRateService ebRateService;

    @ApiOperation("获取费率详情")
    @GetMapping("detail/{id}")
    public RateDetailVO getRateDetail(@PathVariable Long id) {
        return ebRateService.getRateDetail(id);
    }

    @ApiOperation("编辑费率")
    @PutMapping("edit/{id}")
    public R editRate(@PathVariable Long id, @RequestBody RateCrateDTO rateCrateDTO) {
        return ebRateService.editRate(id, rateCrateDTO);
    }
    /**
     * 分页查询
     */
    @ApiOperation("分页查询")
    @GetMapping("page")
    public PageDTO<RatePageVO> queryRatePage(RatePageQuery ratePageQuery) {
        return ebRateService.queryRatePage(ratePageQuery);
    }

    /**
     * 新增费率
     */
    @ApiOperation("新增费率")
    @PostMapping("create")
    public R createRate(@RequestBody RateCrateDTO rateCrateDTO) {
        return ebRateService.createRate(rateCrateDTO);
    }

    @ApiOperation("删除费率")
    @DeleteMapping("delete")
    public R deleteRate(@RequestParam(name = "ids") List<Long> ids) {
        return ebRateService.deleteRate(ids);
    }
}
