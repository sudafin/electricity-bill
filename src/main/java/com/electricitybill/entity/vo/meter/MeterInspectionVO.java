package com.electricitybill.entity.vo.meter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeterInspectionVO {

    @ApiModelProperty(value = "检测类型: routine（常规检查）/fault（故障检修）/calibration（校准）")
    private String inspectionType;


    private String inspectionResult;

    private String faultDescription;

    private String solution;

    private String inspectorName;

    private LocalDateTime inspectionTime;

    private String userName;

    private String remark;

    private String status;

}
