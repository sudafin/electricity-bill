package com.electricitybill.entity.vo.user;

import cn.hutool.json.JSONObject;
import com.alipay.api.domain.BillDetailVo;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserDetailVO extends UserPageVO{

    /**
     *  当前未支付的账单
     */
    private  Integer outstandingBill;

    /**
     * 上次抄表日期
     */
    private LocalDateTime lastMeterReadingDate;

    /**
     * 当月已用电量
     */
    private Long currentMonthlyElectricityUsage;

    List<JSONObject> billRecords;

}
