package com.electricitybill.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.GifCaptcha;
import cn.hutool.captcha.ICaptcha;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.admin.AdminDTO;
import com.electricitybill.entity.dto.admin.AdminFormDTO;
import com.electricitybill.entity.po.EbAdmin;
import com.electricitybill.entity.po.EbRole;
import com.electricitybill.entity.vo.admin.LoginVO;
import com.electricitybill.enums.AdminStatusType;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.expcetions.ForbiddenException;
import com.electricitybill.expcetions.UnauthorizedException;
import com.electricitybill.mapper.EbAdminMapper;
import com.electricitybill.mapper.EbRoleMapper;
import com.electricitybill.service.IEbAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.JwtUtils;
import com.electricitybill.utils.ObjectUtils;
import com.electricitybill.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
@Slf4j
public class EbAdminServiceImpl extends ServiceImpl<EbAdminMapper, EbAdmin> implements IEbAdminService {
    @Resource
    private JwtUtils jwtUtils;
    //bcrypt加密的依赖注入
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private EbRoleMapper ebRoleMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public R<LoginVO> login(AdminFormDTO adminFormDTO) {
        log.info("前端登录信息：{}", adminFormDTO);
        //根据账号密码查询用户
        String account = adminFormDTO.getAccount();
        String password = adminFormDTO.getPassword();
        EbAdmin admin = lambdaQuery().eq(EbAdmin::getAccount, account).one();
        //判断账号是否存在
        if (ObjectUtils.isEmpty(admin)) {
           R.error(4001,"账号不存在");
        }
        //判断密码是否正确,通过bcrypt加密的匹配password是真秘密, admin.getPassword()是加密后的密码,返回值判断是否匹配
        boolean matches = passwordEncoder.matches(password, admin.getPassword());
        if (!matches) {
            R.error(4001,"账号或密码错误");
        }
        //判断是否启用
        if (admin.getStatus() == AdminStatusType.DISABLE.getValue()) {
            throw new ForbiddenException(Constant.ACCOUNT_DISABLED);
        }
        EbRole ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getId, admin.getRoleId()));
        if(ObjectUtils.isEmpty(ebRole)){
            throw new UnauthorizedException(Constant.ROLE_NOT_EXIST);
        }
        AdminDTO adminDTO = AdminDTO.builder()
                .id(admin.getId())
                .userName(admin.getAccount())
                .roleName(ebRole.getRoleName())
                .build();
        String token;
        try {
            token = jwtUtils.createToken(adminDTO);
        } catch (Exception e) {
            log.error("生成token失败", e);
            throw new UnauthorizedException(Constant.TOKEN_GENERATE_FAILED);
        }
        log.debug("token:{}", token);
        return R.ok(LoginVO.builder().adminDTO(adminDTO).token(token).build());
    }

    @Override
    public void create(String key, HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(key)) {
            throw new DbException("验证码key不能为空");
        }
        //参数一是响应对象，参数二是验证码类型
        setHeader(response, "png");
        ICaptcha captcha = CaptchaUtil.createCircleCaptcha(150, 40, 4, 4);
        stringRedisTemplate.opsForValue().set(key, (captcha.getCode()), 1, TimeUnit.MINUTES);
        captcha.write(response.getOutputStream());
    }

    @Override
    public boolean checkCaptcha(String key, String code) {
        String captchaCode = stringRedisTemplate.opsForValue().get(key);
        if(captchaCode == null){
            //验证码过期
            return false;
        }
        if(!StringUtils.equalsIgnoreCase(code,captchaCode)){
            //验证码不对
            return false;
        }
        //到现在增加验证码的有效时间,防止后面可能用户再次输入本验证码
        stringRedisTemplate.expire(key, 5, TimeUnit.MINUTES);
        return true;
    }

    private void setHeader(HttpServletResponse response, String type) {
        if (StringUtils.equalsIgnoreCase(type, "gif")) {
            response.setContentType(MediaType.IMAGE_GIF_VALUE);
        } else {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
        }
        response.setHeader(HttpHeaders.PRAGMA, "No-cache");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "No-cache");
        response.setDateHeader(HttpHeaders.EXPIRES, 0L);
    }

}
