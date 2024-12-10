package com.electricitybill.controller.log;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.log.LogPageQuery;
import com.electricitybill.entity.vo.log.LogDetailVO;
import com.electricitybill.entity.vo.log.LogPageVO;
import com.electricitybill.service.IEbSystemLogService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
@RequestMapping("/log")
public class EbSystemLogController {
    @Resource
    private IEbSystemLogService ebSystemLogService;

    @GetMapping("page")
    public PageDTO<LogPageVO> queryPage(LogPageQuery  logPageQuery) {
        return ebSystemLogService.queryPage(logPageQuery);
    }
    @GetMapping("detail/{id}")
    public LogDetailVO queryDetail(@PathVariable("id") Long id) {
        return ebSystemLogService.queryDetail(id);
    }
    @DeleteMapping("delete")
    public R deleteLog(@RequestParam("ids") List<Long> ids) {
        return ebSystemLogService.removeBatchByIds(ids) ? R.ok() : R.error("删除失败");
    }
    @GetMapping("/export")
    @ApiOperation("导出运营数据报表")
    public void export(HttpServletResponse response) throws IOException {
        ebSystemLogService.export(response);
    }
}
