package com.electricitybill.entity.dto.meter;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

//EqualsAndHashCode(callSuper = true) 生成equals和hashCode方法，同时调用父类的equals和hashCode方法
@EqualsAndHashCode(callSuper = true)
@Data
public class MeterPageQuery extends PageQuery {
    private String status;
    private Long id;
    private String model;
}
