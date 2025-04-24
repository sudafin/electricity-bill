package com.electricitybill.service.impl;

import cn.hutool.core.collection.ListUtil;
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
import com.electricitybill.entity.vo.meter.MeterInspectionVO;
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
import org.springframework.transaction.annotation.Transactional;

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
                .eq(EbMeter::getValidType,ValidType.VALID.getValue())
                .between(meterPageQuery.getStartDate() != null && meterPageQuery.getEndDate() != null, EbMeter::getInstallDate, meterPageQuery.getStartDate(), meterPageQuery.getEndDate())
                .orderByDesc(EbMeter::getCreatedAt)
                .page(page);
        // 返回分页数据
        List<EbMeter> records = ebMeterPage.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        // 封装数据
        List<MeterPageVO> meterPageVOS = records.stream().map(ebMeter -> {
            MeterPageVO meterPageVO = new MeterPageVO();
            meterPageVO.setId(ebMeter.getId());
            meterPageVO.setModel(ebMeter.getModel());
            meterPageVO.setStatus(ebMeter.getStatus());
            EbUser ebUser = ebUserMapper.selectById(ebMeter.getUserId());
            if (ebUser != null) {
                meterPageVO.setUserName(ebUser.getUsername());
            }
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
    @Transactional
    public R<Object> editMeter(MeterEditDTO meterEditDTO) {
        EbMeter ebMeter = lambdaQuery().eq(EbMeter::getId, meterEditDTO.getId()).eq(EbMeter::getValidType, ValidType.VALID.getValue()).one();
        if (ebMeter == null) {
            throw new BizIllegalException(Constant.METER_NOT_EXIST);
        }
        ObjectUtils.assignIfNotNull(ebMeter, meterEditDTO.getModel(), EbMeter::setModel);
        ObjectUtils.assignIfNotNull(ebMeter, meterEditDTO.getStatus(), EbMeter::setStatus);
        ObjectUtils.assignIfNotNull(ebMeter, meterEditDTO.getInstallPlace(), EbMeter::setInstallPlace);
        ObjectUtils.assignIfNotNull(ebMeter, meterEditDTO.getInstallDate(), EbMeter::setInstallDate);
        ObjectUtils.assignIfNotNull(ebMeter, meterEditDTO.getLastMeterReadingDate(), EbMeter::setLastMeterReadingDate);
        ObjectUtils.assignIfNotNull(ebMeter, meterEditDTO.getStartReading(), EbMeter::setStartReading);
        ObjectUtils.assignIfNotNull(ebMeter, meterEditDTO.getEndingReading(), EbMeter::setEndingReading);
        ObjectUtils.assignIfNotNull(ebMeter, meterEditDTO.getStartMeterReadingDate(), EbMeter::setStartMeterReadingDate);
        updateById(ebMeter);
        if(meterEditDTO.getInspectionId() != null){
            EbMeterInspection ebMeterInspection = ebMeterInspectionService.lambdaQuery().eq(EbMeterInspection::getId, meterEditDTO.getInspectionId()).one();
            ObjectUtils.assignIfNotNull(ebMeterInspection, meterEditDTO.getInspectorName(), EbMeterInspection::setInspectorName);
            ObjectUtils.assignIfNotNull(ebMeterInspection, meterEditDTO.getInspectionResult(), EbMeterInspection::setInspectionResult);
            ObjectUtils.assignIfNotNull(ebMeterInspection, meterEditDTO.getInspectionTime(), EbMeterInspection::setInspectionTime);
            ObjectUtils.assignIfNotNull(ebMeterInspection, meterEditDTO.getInspectionType(), EbMeterInspection::setInspectionType);
            ObjectUtils.assignIfNotNull(ebMeterInspection, meterEditDTO.getInspectionStatus(), EbMeterInspection::setStatus);
            ObjectUtils.assignIfNotNull(ebMeterInspection, meterEditDTO.getRemark(), EbMeterInspection::setRemark);
            ebMeterInspectionService.updateById(ebMeterInspection);
        }
        return R.ok(null);
    }

    @Override
    public R<Object> deleteMeter(String meterId) {
        EbMeter ebMeter = getById(meterId);
        if (ebMeter == null) {
            throw new BizIllegalException(Constant.METER_NOT_EXIST);
        }
        ebMeter.setValidType(ValidType.INVALID.getValue());
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
        List<EbMeterInspection> ebMeterInspections = ebMeterInspectionService.lambdaQuery().eq(EbMeterInspection::getMeterId, meterId).orderByDesc(EbMeterInspection::getInspectionTime).list();
        if(!ebMeterInspections.isEmpty()){
            EbMeterInspection ebMeterInspection = ebMeterInspections.get(0);
            BeanUtils.copyProperties(ebMeterInspection, meterDetailVO);
            meterDetailVO.setInspectionId(ebMeterInspection.getId());
            meterDetailVO.setInspectionStatus(ebMeterInspection.getStatus());
        }
        //拿到的是meter的状态，上面检修信息复制字段时会将status字赋值给它，但我们要的不是inspectionStatus
        meterDetailVO.setStatus(ebMeter.getStatus());
        EbUser ebUser = ebUserMapper.selectById(ebMeter.getUserId());
        if (ebUser != null) {
            meterDetailVO.setUsername(ebUser.getUsername());
            meterDetailVO.setUserId(ebUser.getId());
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
        ebMeterInspectionService.save(ebMeterInspection);
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

    @Override
    public List<MeterInspectionVO> getInspectionById(String meterId) {
        List<EbMeterInspection> ebMeterInspections = ebMeterInspectionService.lambdaQuery().eq(EbMeterInspection::getMeterId, meterId).list();
        if(ebMeterInspections.isEmpty()){
            return ListUtil.empty();
        }
        return ebMeterInspections.stream().map(ebMeterInspection -> {
            MeterInspectionVO meterInspectionVO = BeanUtils.copyBean(ebMeterInspection, MeterInspectionVO.class);
            EbUser ebUser = ebUserMapper.selectById(ebMeterInspection.getUserId());
            if(ebUser != null){
                meterInspectionVO.setUserName(ebUser.getUsername());
            }
            return meterInspectionVO;
        }).collect(Collectors.toList());
    }


}
