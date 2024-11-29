package com.electricitybill.service;

import com.electricitybill.entity.po.EbUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.DashboardVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbUserService extends IService<EbUser> {

    DashboardVO getDashboardInfo();
}
