package com.electricitybill.entity.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.utils.StringUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
//Accessors注解，该注解用于设置getter和setter方法的访问级别,chain = true表示setter方法返回当前对象
@Accessors(chain = true)
public class PageQuery {
    public static final Integer DEFAULT_PAGE_SIZE = 20;
    public static final Integer DEFAULT_PAGE_NUM = 1;

    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = DEFAULT_PAGE_NUM;

    @Min(value = 1, message = "每页查询数量不能小于1")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    private Boolean isAsc = true;

    private String sortBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 计算起始位置
     * @return 返回起始位置
     */
    public int from(){
        return (pageNo - 1) * pageSize;
    }

    /**
     * 根据前端传入的排序字段和排序方式，进行排序
     * @param orderItems 前端传入的排序字段和排序方式
     * @return 返回分页对象
     * @param <T>   空
     */
    public <T> Page<T> toMpPage(OrderItem ... orderItems) {
        Page<T> page = new Page<>(pageNo, pageSize);
        // 是否手动指定排序方式
        if (orderItems != null && orderItems.length > 0) {
            for (OrderItem orderItem : orderItems) {
                page.addOrder(orderItem);
            }
            return page;
        }
        // 前端是否有排序字段
        if (StringUtils.isNotEmpty(sortBy)){
            OrderItem orderItem = new OrderItem();
            orderItem.setAsc(isAsc);
            orderItem.setColumn(sortBy);
            page.addOrder(orderItem);
        }
        return page;
    }

    /**
     * 根据前端传入的排序字段和排序方式，进行排序
     * @param defaultSortBy 默认排序字段
     * @param isAsc 默认排序方式 Ascending(升序) or Descending(降序)
     * @return 返回分页对象
     * @param <T> 空
     */
    public <T> Page<T> toMpPage(String defaultSortBy, boolean isAsc) {
        if (StringUtils.isBlank(sortBy)){
            sortBy = defaultSortBy;
            this.isAsc = isAsc;
        }
        Page<T> page = new Page<>(pageNo, pageSize);
        OrderItem orderItem = new OrderItem();
        orderItem.setAsc(this.isAsc);
        orderItem.setColumn(sortBy);
        page.addOrder(orderItem);
        return page;
    }

    /**
     * 默认按照创建时间倒序排序
     * @return 返回分页对象
     * @param <T> 空
     */
    public <T> Page<T> toMpPageDefaultSortByCreateTimeDesc() {
        return toMpPage(Constant.DATA_FIELD_NAME_CREATE_TIME, false);
    }
}
