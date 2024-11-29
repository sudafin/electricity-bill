package com.electricitybill.service;

import com.electricitybill.entity.dto.AdminFormDTO;
import com.electricitybill.entity.po.EbAdmin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.LoginVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbAdminService extends IService<EbAdmin> {

    LoginVO login(AdminFormDTO adminFormDTO);

}
