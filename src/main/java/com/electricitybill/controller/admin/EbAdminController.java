package com.electricitybill.controller.admin;


import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.admin.AdminFormDTO;
import com.electricitybill.entity.vo.admin.LoginVO;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.service.IEbAdminService;
import com.electricitybill.utils.RSAUtils;

import com.electricitybill.utils.WebUtils;
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
            return R.error(4002,"验证码过期或错误");
        }
        //将前端传递过来的加密密码解密
        String decryptedPassword = RSAUtils.decrypt(adminFormDTO.getPassword(), RSAUtils.getPrivateKey(keyPair));
        adminFormDTO.setPassword(decryptedPassword);
        return ebAdminService.login(adminFormDTO);
    }
    @PostMapping("/logout")
    public void logout(){
        //清除session
        ebAdminService.logout();
    }
    @ApiOperation(value = "验证码", notes = "验证码")
    @GetMapping(value = "/captcha", produces = "image/png")
    public void captcha(@RequestParam(value = "key") String key, HttpServletResponse response) throws IOException {
        ebAdminService.create(key, response);
    }

    /**
     * authToken我们需要设置短一点, 这样可以减少服务器的资源开销,而refreshToken我们可以设置长一点,用来防止用户总是登录
     * 只要authToken一过期,后端就会访问refresh接口, 通过refresh来创建新的authToken和新的refreshToken
     * @param adminToken 前端传过来的refreshToken,这个refreshToken我们在登录时已经创建
     * @return 返回新的authToken
     */
    @ApiOperation("刷新token")
    @GetMapping(value = "/refresh")
    public String refreshToken(@CookieValue(value = Constant.REFRESH_HEADER, required = false) String adminToken) {
        if(adminToken == null){
            throw new BadRequestException("登录超时");
        }
        return ebAdminService.refreshToken(WebUtils.cookieBuilder().decode(adminToken));
    }

}
