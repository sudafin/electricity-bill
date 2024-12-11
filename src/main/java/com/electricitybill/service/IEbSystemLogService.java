package com.electricitybill.service;

import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.log.LogDTO;
import com.electricitybill.entity.dto.log.LogPageQuery;
import com.electricitybill.entity.po.EbSystemLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.log.LogDetailVO;
import com.electricitybill.entity.vo.log.LogPageVO;

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
public interface IEbSystemLogService extends IService<EbSystemLog> {

    PageDTO<LogPageVO> queryPage(LogPageQuery logPageQuery);

    LogDetailVO queryDetail(Long id);

    void export(HttpServletResponse response) throws IOException;

    void saveLog(LogDTO logDTO);
}
