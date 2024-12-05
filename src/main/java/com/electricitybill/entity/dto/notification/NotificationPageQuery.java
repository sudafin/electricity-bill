package com.electricitybill.entity.dto.notification;

import com.electricitybill.entity.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NotificationPageQuery extends PageQuery {
    private String title;
    private String type;
}
