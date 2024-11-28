package com.electricitybill.service.impl;

import com.electricitybill.constants.Constant;
import com.electricitybill.entity.dto.AdminDTO;
import com.electricitybill.entity.dto.AdminFormDTO;
import com.electricitybill.entity.po.EbAdmin;
import com.electricitybill.entity.vo.LoginVO;
import com.electricitybill.enums.StatusType;
import com.electricitybill.expcetions.UnauthorizedException;
import com.electricitybill.mapper.EbAdminMapper;
import com.electricitybill.service.IEbAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.JwtUtils;
import com.electricitybill.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    @Override
    public LoginVO login(AdminFormDTO adminFormDTO) {
        log.info("前端登录信息：{}", adminFormDTO);
        //根据账号密码查询用户
        String account = adminFormDTO.getAccount();
        String password = adminFormDTO.getPassword();
        EbAdmin admin = lambdaQuery().eq(EbAdmin::getAccount, account).one();
        //判断账号是否存在
        if (ObjectUtils.isEmpty(admin)) {
            throw new UnauthorizedException(Constant.ACCOUNT_NOT_EXIST);
        }
        //判断密码是否正确,通过bcrypt加密的匹配password是真秘密, admin.getPassword()是加密后的密码,返回值判断是否匹配
        boolean matches = passwordEncoder.matches(password, admin.getPassword());
        if (!matches) {
            throw new UnauthorizedException(Constant.ACCOUNT_PASSWORD_ERROR);
        }
        //判断是否启用
        if (admin.getStatus() == StatusType.DISABLE.getValue()) {
            throw new UnauthorizedException(Constant.ACCOUNT_DISABLED);
        }
        AdminDTO adminDTO = AdminDTO.builder().id(admin.getId()).build();
        String token;
        try {
            token = jwtUtils.createToken(adminDTO);
        } catch (Exception e) {
            log.error("生成token失败", e);
            throw new UnauthorizedException(Constant.TOKEN_GENERATE_FAILED);
        }
        return LoginVO.builder().adminDTO(adminDTO).token(token).build();
    }
}
