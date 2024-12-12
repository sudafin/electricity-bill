package com.electricitybill.controller.admin;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.admin.AdminFormDTO;
import com.electricitybill.entity.vo.admin.LoginVO;
import com.electricitybill.service.IEbAdminService;
import com.electricitybill.utils.RSAUtils;

import io.swagger.annotations.ApiOperation;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyPair;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@RestController
@RequestMapping("/admin")
public class EbAdminController {
    @Resource
    private IEbAdminService ebAdminService;

    //生成RSA密钥对
    public static final KeyPair keyPair = RSAUtils.generateKeyPair();


    //将生成的RSA公钥接口暴露给前端,前端的密码需要加密传递
    @GetMapping
    public String getPublicKey() {
        return RSAUtils.getPublicKey(keyPair);
    }

    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody @Validated AdminFormDTO adminFormDTO) throws Exception {
        //检查验证码
        if(!ebAdminService.checkCaptcha(adminFormDTO.getKey(), adminFormDTO.getCode())){
            return R.error(4002,"验证码错误");
        }
        //将前端传递过来的加密密码解密
        String decryptedPassword = RSAUtils.decrypt(adminFormDTO.getPassword(), RSAUtils.getPrivateKey(keyPair));
        adminFormDTO.setPassword(decryptedPassword);
        return ebAdminService.login(adminFormDTO);
    }
    @PostMapping("/logout")
    public void logout(HttpServletRequest httpServletRequest){
        //清除session
        httpServletRequest.getSession().invalidate();
    }
    @ApiOperation(value = "验证码", notes = "验证码")
    @GetMapping(value = "/captcha", produces = "image/png")
    public void captcha(@RequestParam(value = "key") String key, HttpServletResponse response) throws IOException {
        ebAdminService.create(key, response);
    }
  
}
