package com.electricitybill.entity.dto.user;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

//EqualsAndHashCode(callSuper = true) 生成equals和hashCode方法，同时调用父类的equals和hashCode方法
@EqualsAndHashCode(callSuper = true)
@Data
public class UserPageQuery extends PageQuery {
    private String name;
    private String phone;
    private String userType;
    private String meterNumber;

}
