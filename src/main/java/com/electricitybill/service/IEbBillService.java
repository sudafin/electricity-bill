package com.electricitybill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.R;
import com.electricitybill.entity.po.EbBill;
import com.electricitybill.entity.vo.user.UserBillVO;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2025-03-17
 */
public interface IEbBillService extends IService<EbBill> {

    List<UserBillVO> queryUserBill(@NotNull Long userId);

}
