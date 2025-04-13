package com.electricitybill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.meter.MeterPageQuery;
import com.electricitybill.entity.po.EbMeter;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.meter.MeterPageVO;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbMeterMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbMeterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.StringUtils;
import org.springframework.stereotype.Service;
import com.electricitybill.entity.dto.PageDTO;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-25
 */
@Service
public class EbMeterServiceImpl extends ServiceImpl<EbMeterMapper, EbMeter> implements IEbMeterService {
    @Resource
    private EbUserMapper ebUserMapper;
    @Override
    public PageDTO<MeterPageVO> queryMeterPage(MeterPageQuery meterPageQuery) {
        // 分页查询
        Page<EbMeter> page = new Page<>(meterPageQuery.getPageNo(), meterPageQuery.getPageSize());
        // 查询数据库条件
        Page<EbMeter> ebMeterPage = lambdaQuery().eq(StringUtils.isNotBlank(meterPageQuery.getModel()), EbMeter::getModel, meterPageQuery.getModel())
                .eq(meterPageQuery.getId() != null, EbMeter::getId, meterPageQuery.getId())
                .eq(StringUtils.isNotBlank(meterPageQuery.getStatus()), EbMeter::getStatus, meterPageQuery.getStatus())
                .between(meterPageQuery.getStartDate() != null && meterPageQuery.getEndDate() != null, EbMeter::getInstallDate, meterPageQuery.getStartDate(), meterPageQuery.getEndDate())
                .page(page);
        // 返回分页数据
        List<EbMeter> records = ebMeterPage.getRecords();
        if(CollUtils.isNotEmpty(records)){
            return PageDTO.empty(page);
        }
        // 封装数据
        List<MeterPageVO> meterPageVOS = records.stream().map(ebMeter -> {
            MeterPageVO meterPageVO = new MeterPageVO();
            meterPageVO.setMeterId(ebMeter.getId());
            meterPageVO.setModel(ebMeter.getModel());
            meterPageVO.setStatus(ebMeter.getStatus());
            EbUser ebUser = ebUserMapper.selectById(ebMeter.getUserId());
            if (ebUser == null) {
                throw new BizIllegalException(Constant.USER_NOT_EXIST);
            }
            meterPageVO.setUserName(ebUser.getUsername());
            meterPageVO.setInstallDate(ebMeter.getInstallDate());
            meterPageVO.setLastMeterReadingDate(ebMeter.getLastMeterReadingDate());
            meterPageVO.setStartReading(ebMeter.getStartReading());
            meterPageVO.setEndingReading(ebMeter.getEndingReading());
            meterPageVO.setInstallPlace(ebMeter.getInstallPlace());
            return meterPageVO;
        }).collect(Collectors.toList());
        return PageDTO.of(ebMeterPage, meterPageVOS);
    }
}
