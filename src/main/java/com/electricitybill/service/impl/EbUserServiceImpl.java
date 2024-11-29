package com.electricitybill.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.entity.po.EbElectricityUsage;
import com.electricitybill.entity.po.EbPayment;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.DashboardVO;
import com.electricitybill.enums.UserType;
import com.electricitybill.mapper.EbElectricityUsageMapper;
import com.electricitybill.mapper.EbPaymentMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbElectricityUsageService;
import com.electricitybill.service.IEbPaymentService;
import com.electricitybill.service.IEbRoleService;
import com.electricitybill.service.IEbUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
@Slf4j
public class EbUserServiceImpl extends ServiceImpl<EbUserMapper, EbUser> implements IEbUserService {
    @Resource
    private EbElectricityUsageMapper ebElectricityUsageMapper;
    @Resource
    private EbPaymentMapper paymentMapper;
    @Override
    public DashboardVO getDashboardInfo() {
        /**
         * 拿到所有用户数量,用电量,支付金额,账单,用户类型和最近7天的用电量
         */
        DashboardVO dashboardVO = DashboardVO.builder().build();
        List<EbElectricityUsage> ebElectricityUsageList = ebElectricityUsageMapper.selectList(new LambdaQueryWrapper<>());
        //补充总用电量和最近7天的用电量
        if(CollUtils.isEmpty(ebElectricityUsageList)){
            //如果没有数据,则默认为0或空,不用抛错误
            dashboardVO.setTotalElectricityUsage(0L);
            dashboardVO.setElectricityWeekUsageList(CollUtils.emptyList());
        }else {
            double totalElectricity = ebElectricityUsageList.stream()
                    .mapToDouble(usage -> new BigDecimal(String.valueOf(usage.getUsageAmount())).doubleValue())
                    .sum();
            //最近7天的用电量
            List<Double> electricityWeekUsageList = ebElectricityUsageList.stream()
                    .mapToDouble(usage -> new BigDecimal(String.valueOf(usage.getUsageAmount())).doubleValue())
                    .limit(7)
                    .boxed()  //因为mapToDouble返回的是DoubleStream,而collect方法需要的是Stream,所以使用了boxed()方将Double流转换为Double对象的流才能调用collect方法,
                    .collect(toList());
            log.debug("总用电量:{}",totalElectricity);
            log.debug("最近7天的用电量:{}",electricityWeekUsageList);
            dashboardVO.setTotalElectricityUsage((long) totalElectricity);
            dashboardVO.setElectricityWeekUsageList(electricityWeekUsageList);
        }
        //补充账单总数和总金额
        List<EbPayment> ebPaymentList = paymentMapper.selectList(new LambdaQueryWrapper<>());
        if(CollUtils.isEmpty(ebPaymentList)){
            dashboardVO.setTotalPaymentBill(0L);
            dashboardVO.setTotalAmount(0L);
        }else {
            int totalPaymentBill = ebPaymentList.size();
            int totalAmount = ebPaymentList.stream()
                    .mapToInt(payment -> new BigDecimal(String.valueOf(payment.getAmount())).intValue())
                    .sum();
            log.debug("总账单数:{}",totalPaymentBill);
            log.debug("总金额:{}",totalAmount);
            dashboardVO.setTotalAmount((long) totalAmount);
            dashboardVO.setTotalPaymentBill((long) totalPaymentBill);
        }
        //补充用户类型和用户总数
        List<String> userTypeList = UserType.getUserTypeList();
        int totalUser = lambdaQuery().list().size();
        log.debug("用户类型列表:{}",userTypeList);
        Map<String,Integer> userTypeMap = new HashMap<>();
        userTypeList.forEach(userType -> {
            int userTypeCount = lambdaQuery().eq(EbUser::getUserType, userType).list().size();
            log.debug("用户类型{}的数量:{}",userType,userTypeCount);
            userTypeMap.put(userType,userTypeCount);
        });
        dashboardVO.setUserTypeMap(userTypeMap);
        dashboardVO.setTotalUser((long) totalUser);
        log.info("dashboardVO的对象数据:{}",dashboardVO);
        return dashboardVO;
    }

}
