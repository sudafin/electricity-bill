package com.electricitybill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.meter.MeterCreateDTO;
import com.electricitybill.entity.dto.meter.MeterEditDTO;
import com.electricitybill.entity.dto.meter.MeterInspectionDTO;
import com.electricitybill.entity.dto.meter.MeterPageQuery;
import com.electricitybill.entity.po.EbMeter;
import com.electricitybill.entity.po.EbMeterInspection;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.meter.MeterDetailVO;
import com.electricitybill.entity.vo.meter.MeterPageVO;
import com.electricitybill.enums.*;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.mapper.EbMeterMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbMeterInspectionService;
import com.electricitybill.service.IEbMeterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.BeanUtils;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import org.springframework.stereotype.Service;
import com.electricitybill.entity.dto.PageDTO;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Resource
    private IEbMeterInspectionService ebMeterInspectionService;
    @Override
    public PageDTO<MeterPageVO> queryMeterPage(MeterPageQuery meterPageQuery) {
        // 分页查询
        Page<EbMeter> page = new Page<>(meterPageQuery.getPageNo(), meterPageQuery.getPageSize());
        // 查询数据库条件
        Page<EbMeter> ebMeterPage = lambdaQuery().eq(StringUtils.isNotBlank(meterPageQuery.getModel()), EbMeter::getModel, meterPageQuery.getModel())
                .eq(StringUtils.isNotBlank(meterPageQuery.getMeterId()), EbMeter::getId, meterPageQuery.getMeterId())
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

    @Override
    public R<Object> createMeter(MeterCreateDTO meterCreateDTO) {
        EbMeter ebMeter = BeanUtils.copyBean(meterCreateDTO, EbMeter.class);
        if (ebMeter == null) {
            throw new BizIllegalException(Constant.CONVERT_ERROR);
        }
        ebMeter.setValidType(ValidType.VALID.getValue());
        save(ebMeter);
        return R.ok(null);
    }

    @Override
    public List<String> getMeterModel() {
        return MeterModelType.getModelTypeList();
    }

    @Override
    public R<Object> editMeter(MeterEditDTO meterBindDTO) {
        Long meterId = meterBindDTO.getMeterId();
        EbMeter ebMeter = getById(meterId);
        if (ebMeter == null) {
            throw new BizIllegalException(Constant.METER_NOT_EXIST);
        }
        ObjectUtils.assignIfNotNull(ebMeter, meterBindDTO.getModel(), EbMeter::setModel);
        ObjectUtils.assignIfNotNull(ebMeter, meterBindDTO.getStatus(), EbMeter::setStatus);
        ObjectUtils.assignIfNotNull(ebMeter, meterBindDTO.getInstallPlace(), EbMeter::setInstallPlace);
        if(meterBindDTO.getIdCardNo() != null){
            EbUser ebUser = ebUserMapper.selectOne(new LambdaQueryWrapper<EbUser>().eq(EbUser::getIdCardNo, meterBindDTO.getIdCardNo()));
            if (ebUser == null) {
                throw new BizIllegalException(Constant.USER_NOT_EXIST);
            }
            ebMeter.setUserId(ebUser.getId());
            ebUser.setMeterId(ebMeter.getId());
            ebUserMapper.updateById(ebUser);
        }
        updateById(ebMeter);
        return R.ok(null);
    }

    @Override
    public R<Object> deleteMeter(String meterId) {
        EbMeter ebMeter = getById(meterId);
        if (ebMeter == null) {
            throw new BizIllegalException(Constant.METER_NOT_EXIST);
        }
        ebMeter.setValidType(ValidType.VALID.getValue());
        updateById(ebMeter);
        return R.ok(null);
    }

    @Override
    public MeterDetailVO getMeterDetail(String meterId) {
        EbMeter ebMeter = getById(meterId);
        if (ebMeter == null) {
            throw new BizIllegalException(Constant.METER_NOT_EXIST);
        }
        MeterDetailVO meterDetailVO = BeanUtils.copyBean(ebMeter, MeterDetailVO.class);
        if(ebMeter.getInspectionId() != null){
            EbMeterInspection ebMeterInspection = ebMeterInspectionService.getById(ebMeter.getInspectionId());
            if (ebMeterInspection == null){
                throw new BizIllegalException(Constant.METER_INSPECTION_NOT_EXIST);
            }
            BeanUtils.copyProperties(ebMeterInspection, meterDetailVO);
        }
        return meterDetailVO;
    }

    @Override
    public R<Object> inspectionCreate(MeterInspectionDTO meterInspectionDTO) {
        EbMeter ebMeter = getById(meterInspectionDTO.getMeterId());
        if (ebMeter == null) {
            throw new BizIllegalException(Constant.METER_NOT_EXIST);
        }
        EbMeterInspection ebMeterInspection = BeanUtils.copyBean(meterInspectionDTO, EbMeterInspection.class);
        boolean save = ebMeterInspectionService.save(ebMeterInspection);
        if (save) {
            EbMeterInspection meterInspection = ebMeterInspectionService.lambdaQuery().eq(EbMeterInspection::getMeterId, meterInspectionDTO.getMeterId()).getEntity();
            ebMeter.setInspectionId(meterInspection.getId());
            updateById(ebMeter);
        }
        return R.ok(null);
    }

    @Override
    public Map<String, List<String>> getInspectionType() {
        HashMap<String, List<String>> res = new HashMap<>();
        if(CollUtils.isNotEmpty(InspectionType.getInspectionTypeList())) {
            res.put("inspectionType", InspectionType.getInspectionTypeList());
        }
        if(CollUtils.isNotEmpty(InspectionStatus.getInspectionStatusList())) {
            res.put("inspectionStatus", InspectionStatus.getInspectionStatusList());
        }
        if (CollUtils.isNotEmpty(InspectionResult.getInspectionResultList())){
               res.put("inspectionResult", InspectionResult.getInspectionResultList());
        }
        return res;
    }
}
