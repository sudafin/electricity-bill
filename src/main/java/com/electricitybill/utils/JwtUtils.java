package com.electricitybill.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.electricitybill.entity.dto.AdminDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.token.expirationTime:}")
    private long expirationTime;
    @Value("${jwt.token.secret:}")
    private String secretKey;

    /**
     * 创建token
     * @param adminDTO token需要携带的用户对象信息,如用户id姓名等
     * @return token
     */
    public String createToken(AdminDTO adminDTO){

        JWTSigner jwtSigner = JWTSignerUtil.hs256(this.secretKey.getBytes());
        //将密钥算法加密生成签名,防止token被修改
        String token = JWT.create()
                .setPayload("adminDTO", adminDTO)
                .setExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .setSigner(jwtSigner)
                .sign();
        return token;
    }

    //解析token
    public AdminDTO parseToken(String token){
        Object payload = JWT.of(token).getPayload("adminDTO");
        return BeanUtil.toBean(payload, AdminDTO.class);
    }

    //验证token是否过期
    public boolean validateToken(String token){
        JWTSigner jwtSigner = JWTSignerUtil.hs256(this.secretKey.getBytes());
        try {
            // 创建 JWTValidator 验证token时间实例
            JWTValidator validator = JWTValidator.of(token);

            // 验证签名算法
            validator.validateAlgorithm(jwtSigner);

            // 验证 token 是否过期
            validator.validateDate();

            // 如果验证通过，返回 true
            return true;
        } catch (Exception e) {
            // 如果验证失败，捕获异常并返回 false
            return false;
        }
    }

    //将验证token的真假和时间的过期结合一起
    public boolean checkToken(String token){
        JWTSigner jwtSigner = JWTSignerUtil.hs256(this.secretKey.getBytes());
        return validateToken(token) && JWTUtil.verify(token, jwtSigner);
    }
}
