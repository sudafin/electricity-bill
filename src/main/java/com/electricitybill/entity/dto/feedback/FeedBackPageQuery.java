package com.electricitybill.entity.dto.feedback;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

//EqualsAndHashCode(callSuper = true) 生成equals和hashCode方法，同时调用父类的equals和hashCode方法
@EqualsAndHashCode(callSuper = true)
@Data
public class FeedBackPageQuery extends PageQuery {
    private String feedbackId;
    private String feedbackStatus;
    private String feedbackType;
}
