package com.electricitybill.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.log.LogPageQuery;
import com.electricitybill.entity.po.EbSystemLog;
import com.electricitybill.entity.vo.log.LogDetailVO;
import com.electricitybill.entity.vo.log.LogPageVO;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.mapper.EbSystemLogMapper;
import com.electricitybill.service.IEbSystemLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
public class EbSystemLogServiceImpl extends ServiceImpl<EbSystemLogMapper, EbSystemLog> implements IEbSystemLogService {

    @Override
    public PageDTO<LogPageVO> queryPage(LogPageQuery logPageQuery) {
        Page<EbSystemLog> ebSystemLogPage = new Page<>(logPageQuery.getPageNo(), logPageQuery.getPageSize());
        Page<EbSystemLog> systemLogPage = lambdaQuery()
                .eq(StringUtils.isNotBlank(logPageQuery.getOperationType()), EbSystemLog::getOperationType, logPageQuery.getOperationType())
                .eq(StringUtils.isNotBlank(logPageQuery.getModule()), EbSystemLog::getModule, logPageQuery.getModule())
                .like(StringUtils.isNotBlank(logPageQuery.getOperatorName()), EbSystemLog::getDescription, logPageQuery.getOperatorName())
                .ge(logPageQuery.getStartDate() != null, EbSystemLog::getCreatedAt, logPageQuery.getStartDate())
                .le(logPageQuery.getEndDate() != null, EbSystemLog::getCreatedAt, logPageQuery.getEndDate())
                .page(ebSystemLogPage);
        List<EbSystemLog> records = systemLogPage.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(systemLogPage);
        }
        ArrayList<LogPageVO> pageVOArrayList = new ArrayList<>();
        records.forEach(ebSystemLog -> {
            LogPageVO logPageVO = new LogPageVO();
            logPageVO.setId(ebSystemLog.getId());
            logPageVO.setOperatorName(ebSystemLog.getOperatorName());
            logPageVO.setOperationType(ebSystemLog.getOperationType());
            logPageVO.setModule(ebSystemLog.getModule());
            logPageVO.setDescription(ebSystemLog.getDescription());
            logPageVO.setIp(ebSystemLog.getIp());
            logPageVO.setStatus(ebSystemLog.getStatus());
            logPageVO.setCreateTime(ebSystemLog.getCreatedAt());
            pageVOArrayList.add(logPageVO);
        });
        return PageDTO.of(systemLogPage, pageVOArrayList);
    }

    @Override
    public LogDetailVO queryDetail(Long id) {
        EbSystemLog ebSystemLog = getById(id);
        if(ObjectUtils.isEmpty(ebSystemLog)){
            throw new BadRequestException(Constant.LOG_NOT_EXIST);
        }
        LogDetailVO logDetailVO = new LogDetailVO();
        logDetailVO.setOperatorName(ebSystemLog.getOperatorName());
        logDetailVO.setOperationType(ebSystemLog.getOperationType());
        logDetailVO.setModule(ebSystemLog.getModule());
        logDetailVO.setDescription(ebSystemLog.getDescription());
        logDetailVO.setIp(ebSystemLog.getIp());
        logDetailVO.setStatus(ebSystemLog.getStatus());
        logDetailVO.setRequestParams(JSONUtil.escape(ebSystemLog.getRequestParams()));
        logDetailVO.setResponseData(ebSystemLog.getResponseData());
        logDetailVO.setErrorMsg(ebSystemLog.getErrorMsg());
        logDetailVO.setCreateTime(ebSystemLog.getCreatedAt());
        return logDetailVO;
    }
}
