package com.electricitybill.service;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.admin.AdminFormDTO;
import com.electricitybill.entity.po.EbAdmin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.admin.LoginVO;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbAdminService extends IService<EbAdmin> {

    R<LoginVO> login(AdminFormDTO adminFormDTO);

    void create(String key, HttpServletResponse response) throws IOException;

    R checkCaptcha(@NotNull(message = "验证码key不能为空") String key, @NotNull(message = "验证码不能为空") String code);

    String refreshToken(String token);

    void logout();
}
