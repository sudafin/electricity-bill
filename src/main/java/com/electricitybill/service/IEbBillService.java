package com.electricitybill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.dto.AliPay;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.bill.BillPageQuery;
import com.electricitybill.entity.po.EbBill;
import com.electricitybill.entity.vo.bill.BillPageVO;
import com.electricitybill.entity.vo.user.UserBillVO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

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

    PageDTO<BillPageVO> query(BillPageQuery billPageQuery);

    UserBillVO detailBill(Long billId);

    Map<String, Object> pay(AliPay aliPay);

    String payNotify(HttpServletRequest request);
}
