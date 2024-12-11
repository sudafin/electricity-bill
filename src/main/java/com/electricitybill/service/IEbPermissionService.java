package com.electricitybill.service;

import com.electricitybill.entity.po.EbPermission;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbPermissionService extends IService<EbPermission> {

    Boolean roleCheck(HttpServletRequest request);
}
