package com.electricitybill.service.impl;

import com.electricitybill.entity.po.EbUsageSummary;
import com.electricitybill.entity.vo.electricity.ElectricityUserVO;
import com.electricitybill.enums.DateType;
import com.electricitybill.mapper.EbUsageSummaryMapper;
import com.electricitybill.service.IEbUsageSummaryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.CollUtils;
import com.electricitybill.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 每日用电量及电费汇总表 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-27
 */
@Service
public class EbUsageSummaryServiceImpl extends ServiceImpl<EbUsageSummaryMapper, EbUsageSummary> implements IEbUsageSummaryService {
    public static void main(String[] args) {
        LocalDate now = LocalDate.now();
        System.out.println(DateUtils.getMonthBeginTime(now));
        System.out.println(now);
    }
    @Override
    public ElectricityUserVO getElectricityRecords() {
        //拿到天的然后是当前一个月到现在的值
        List<EbUsageSummary> ebDailyUsageSummaries = lambdaQuery()
                .eq(EbUsageSummary::getDateType, DateType.DAILY.getDesc())
                .gt(EbUsageSummary::getSummaryDateStart, DateUtils.getMonthBeginTime(LocalDate.now()))
                //如果不要用lt,会是大于等于当天的数据,可当天数据暂时还没有,虽然加lt也不会有变化,但为了代码的理论性,还是用le
                .le(EbUsageSummary::getSummaryDateStart, LocalDateTime.now())
                .list();
        //设置数据
        ElectricityUserVO electricityUserVO = new ElectricityUserVO();
        EbUsageSummary lastEbUsageSummary = CollUtils.getLast(ebDailyUsageSummaries);
        electricityUserVO.setYesterdayElectricity(lastEbUsageSummary.getTotalUsage());
        electricityUserVO.setYesterdayElectricityFee(lastEbUsageSummary.getTotalCost());
        //设置当月到目前的数据,reduce将数据归集,第一个参数是初始值,第二个参数是每次迭代的函数
        electricityUserVO.setCurrentMonthElectricity(ebDailyUsageSummaries.stream().map(EbUsageSummary::getTotalUsage).reduce(BigDecimal.ZERO,BigDecimal::add));
        electricityUserVO.setCurrentMonthElectricityFee(ebDailyUsageSummaries.stream().map(EbUsageSummary::getTotalCost).reduce(BigDecimal.ZERO,BigDecimal::add));
        //返回数据
        return electricityUserVO;
    }
}
