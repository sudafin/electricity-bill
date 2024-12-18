package com.electricitybill.service;

import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.log.LogDTO;
import com.electricitybill.entity.dto.log.LogPageQuery;
import com.electricitybill.entity.po.EbSystemLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.log.LogDetailVO;
import com.electricitybill.entity.vo.log.LogPageVO;

import java.io.IOException;
import java.util.concurrent.Future;

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

    Future<String> export() throws IOException;

    void saveLog(LogDTO logDTO);
}
