package com.electricitybill.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.admin.LoginDTO;
import com.electricitybill.entity.dto.admin.LoginFormDTO;
import com.electricitybill.entity.dto.user.UserCreateDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.electricitybill.entity.dto.usertype.UserTypeCreateDTO;
import com.electricitybill.entity.po.*;
import com.electricitybill.entity.vo.admin.LoginVO;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.enums.*;
import com.electricitybill.expcetions.*;
import com.electricitybill.mapper.*;
import com.electricitybill.service.IEbUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.service.IEbUserTypeService;
import com.electricitybill.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
@Slf4j
public class EbUserServiceImpl extends ServiceImpl<EbUserMapper, EbUser> implements IEbUserService {
    @Resource
    private EbMeterMapper ebMeterMapper;

    @Resource
    private EbBillMapper ebBillMapper;

    @Resource
    private IEbUserTypeService ebUserTypeService;
    @Resource
    private EbScheduledTaskMapper ebScheduledTaskMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtils jwtUtils;

    private static List<String> getStringList(EbScheduledTask ebScheduledTask, Long userId, Boolean userEditDTO) {
        String userIds = ebScheduledTask.getUserIds();
        //,逗号切割,转为list
        List<String> userIdList = Arrays.asList(userIds.split(","));
        if (userIdList.contains(String.valueOf(userId)) && !userEditDTO) {
            userIdList.remove(String.valueOf(userId));
        } else if (!userIdList.contains(String.valueOf(userId)) && userEditDTO) {
            userIdList.add(String.valueOf(userId));
        }
        return userIdList;
    }

    @Override
    public PageDTO<UserPageVO> queryUserPage(UserPageQuery userPageQuery) {
        log.debug("userPageQuery:{}", userPageQuery);

        /**
         * new Page<>(PageNo, PageSize); 其中第一个参数是当前页，第二个参数是每页显示的条数
         */

        // 分页查询条件
        Page<EbUser> ebUserPage = new Page<>(userPageQuery.getPageNo(), userPageQuery.getPageSize());
        // 查询数据库条件
        Page<EbUser> page = lambdaQuery().eq(EbUser::getValidType, ValidType.VALID.getValue()).eq(StrUtil.isNotBlank(userPageQuery.getUserType()), EbUser::getUserType, userPageQuery.getUserType()).eq(StrUtil.isNotBlank(userPageQuery.getPhone()), EbUser::getPhone, userPageQuery.getPhone()).like(StrUtil.isNotBlank(userPageQuery.getName()), EbUser::getUsername, userPageQuery.getName()).eq(StrUtil.isNotBlank(userPageQuery.getMeterId()), EbUser::getMeterId, userPageQuery.getMeterId()).ge(userPageQuery.getStartDate() != null, EbUser::getLastPaymentDate, userPageQuery.getStartDate()).le(userPageQuery.getEndDate() != null, EbUser::getLastPaymentDate, userPageQuery.getEndDate()).page(ebUserPage);
        //判断数据是否为空
        /**
         * page.getSize(): 每页的记录数 一页10条数据大小
         * page.getTotal(): 总记录数  一共100条数据大小
         * page.getRecords(): 当前页的数据列表 当前页的10条数据
         * page.getCurrent(): 当前页码 当前显示的页码
         * page.getPages(): 总页数 总共多少页
         */
        List<EbUser> ebUserList = page.getRecords();
        if (CollUtils.isEmpty(ebUserList)) {
            //如果为空，则返回空分页
            return PageDTO.empty(page);
        }
        List<UserPageVO> userPageVOList = BeanUtils.copyList(ebUserList, UserPageVO.class);
        log.debug("userPageVOList的数据:{}", userPageVOList);
        return PageDTO.of(page, userPageVOList);
    }

    @Override
    public UserDetailVO queryUserDetail(Long userId) {
        log.debug("userId:{}", userId);
        //根据id查询用户
        EbUser ebUser = getById(userId);
        if (ObjectUtils.isEmpty(ebUser)) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        if (!ebUser.getValidType().equals(ValidType.VALID.getValue())) {
            throw new BizIllegalException(Constant.USER_INVALID);
        }
        UserDetailVO userDetailVO = BeanUtils.copyBean(ebUser, UserDetailVO.class);
        if (ObjectUtils.isEmpty(userDetailVO)) {
            throw new BizIllegalException(Constant.CONVERT_ERROR);
        }
        log.debug("userDetailVO的数据:{}", userDetailVO);
        EbMeter ebMeter = ebMeterMapper.selectOne(new LambdaQueryWrapper<EbMeter>().eq(EbMeter::getUserId, userId));
        List<EbBill> ebBillList = ebBillMapper.selectList(new LambdaQueryWrapper<EbBill>().eq(EbBill::getUserId, ebUser.getId())).stream().filter(ebBill -> !ebBill.getStatus().equals(BillType.PAID.getDesc())).collect(Collectors.toList());
        userDetailVO.setOutstandingBill(ebBillList.size());
        if (ObjectUtils.isEmpty(ebMeter)) {
            userDetailVO.setLastMeterReadingDate(null);
            return userDetailVO;
        }
        userDetailVO.setLastMeterReadingDate(ebMeter.getLastMeterReadingDate());
        ArrayList<cn.hutool.json.JSONObject> billRecords = new ArrayList<>();
        ebBillList.forEach(ebBill -> {
            cn.hutool.json.JSONObject jsonObject = new JSONObject();
            jsonObject.set("billId", ebBill.getId());
            jsonObject.set("usageAmount", ebBill.getUsageAmount());
            jsonObject.set("paymentMethod", ebBill.getPaymentMethod());
            jsonObject.set("paymentDate", ebBill.getUpdatedAt());
            jsonObject.set("billTotalAmount", ebBill.getTotalAmount());
            billRecords.add(jsonObject);
        });
        userDetailVO.setBillRecords(billRecords);
        return userDetailVO;
    }

    @Override
    public R insertUser(UserCreateDTO userCreateDTO) {
        //判断用户是否存在
        EbUser ebUser = baseMapper.selectOne(new LambdaQueryWrapper<EbUser>().eq(EbUser::getIdCardNo, userCreateDTO.idCardNo));
        if (ObjectUtils.isNotEmpty(ebUser)) {
            throw new DbException(Constant.USER_EXIST);
        }

        EbUser user = BeanUtils.copyBean(userCreateDTO, EbUser.class);
        user.setAccountStatus(AccountStatus.NORMAL.getDesc());
        //TODO初始密码,后面改
        user.setAccount(userCreateDTO.getPhone());
        user.setPassword("123");
        baseMapper.insert(user);
        return R.ok();
    }

    @Override
    @Transactional
    public R deleteUser(List<Long> userIds) {
        List<EbUser> ebUsers = listByIds(userIds);
        ebUsers.forEach(user -> {
            user.setValidType(ValidType.INVALID.getValue());
        });
        updateBatchById(ebUsers);
        return R.ok();
    }

    @Override
    public R adminUpdateUser(UserCreateDTO userCreateDTO) {
        EbUser ebUser = baseMapper.selectOne(new LambdaQueryWrapper<EbUser>().eq(EbUser::getIdCardNo, userCreateDTO.idCardNo));
        if (ObjectUtils.isEmpty(ebUser)) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        ObjectUtils.assignIfNotNull(ebUser, userCreateDTO.getUserType(), EbUser::setUserType);
        ObjectUtils.assignIfNotNull(ebUser, userCreateDTO.getUsername(), EbUser::setUsername);
        ObjectUtils.assignIfNotNull(ebUser, userCreateDTO.getPhone(), EbUser::setPhone);
        ObjectUtils.assignIfNotNull(ebUser, userCreateDTO.getAddress(), EbUser::setAddress);
        ObjectUtils.assignIfNotNull(ebUser, userCreateDTO.getMeterNo(), EbUser::setMeterId);
        baseMapper.updateById(ebUser);
        return R.ok();
    }

    @Override
    public List<String> getUserTypeList() {
        List<EbUserType> res = ebUserTypeService.lambdaQuery().eq(EbUserType::getStatus, ValidType.VALID.getValue()).list();
        if (res.isEmpty()) {
            return ListUtil.empty();
        }
        return res.stream().map(EbUserType::getTypeName).collect(Collectors.toList());
    }

    @Override
    public R addUserType(@NotNull UserTypeCreateDTO userTypeCreateDTO) {
        ebUserTypeService.lambdaQuery().eq(EbUserType::getTypeName, userTypeCreateDTO.getTypeName()).oneOpt().ifPresent(type -> {
            throw new DbException(Constant.USER_TYPE_EXIST);
        });
        EbUserType ebUserType = BeanUtils.copyBean(userTypeCreateDTO, EbUserType.class);
        ebUserType.setStatus(ValidType.VALID.getValue());
        ebUserTypeService.save(ebUserType);
        return R.ok();
    }

    @Override
    public JSONObject getUserInfoByIdCard(String idCardNo) {
        EbUser ebUser = lambdaQuery().eq(EbUser::getIdCardNo, idCardNo).one();
        if (ObjectUtils.isEmpty(ebUser)) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        if (ebUser.getValidType().equals(ValidType.INVALID.getValue())) {
            throw new BizIllegalException(Constant.USER_INVALID);
        }
        if (ebUser.getMeterId() != null) {
            throw new BizIllegalException(Constant.USER_HAS_METER);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("id", ebUser.getId());
        jsonObject.set("username", ebUser.getUsername());
        jsonObject.set("phone", ebUser.getPhone());
        return jsonObject;
    }

    @Override
    public R bindMeter(JSONObject jsonObject) {
        String meterId = jsonObject.getStr("meterId");
        String userId = jsonObject.getStr("userId");
        //1表示绑定用户，2表示解绑用户
        String status = jsonObject.getStr("status");
        if (StringUtils.isBlank(meterId) || StringUtils.isBlank(userId) || StringUtils.isBlank(status)) {
            throw new BadRequestException(Constant.REQUEST_PARAM_MISSING);
        }
        EbUser ebUser = getById(userId);
        EbMeter ebMeter = ebMeterMapper.selectById(meterId);
        if (ObjectUtils.isEmpty(ebUser) || ObjectUtils.isEmpty(ebMeter)) {
            throw new DbException(Constant.USER_NOT_EXIST);
        }
        if (ebUser.getValidType().equals(ValidType.INVALID.getValue())) {
            throw new BizIllegalException(Constant.USER_INVALID);
        }
        if (ebUser.getMeterId() != null && status.equals("1")) {
            throw new BizIllegalException(Constant.USER_HAS_BIND);
        }
        if (ebUser.getMeterId() == null && status.equals("2")) {
            throw new BizIllegalException(Constant.USER_HAS_UNBIND);
        }
        if (status.equals("1")) {
            ebUser.setMeterId(meterId);
            ebMeter.setUserId(Long.valueOf(userId));
        } else {
            ebUser.setMeterId(null);
            ebMeter.setUserId(null);
        }
        updateById(ebUser);
        ebMeterMapper.updateById(ebMeter);
        return R.ok();

    }

    @Override
    public R login(LoginFormDTO loginFormDTO) {
        log.info("前端登录信息：{}", loginFormDTO);
        //根据账号密码查询用户
        String account = loginFormDTO.getAccount();
        String password = loginFormDTO.getPassword();
        EbUser ebUser = lambdaQuery().eq(EbUser::getAccount, account).one();
        //判断账号是否存在
        if (ObjectUtils.isEmpty(ebUser)) {
            R.error(4001,"账号不存在");
        }
        //判断密码是否正确,通过bcrypt加密的匹配password是真秘密, admin.getPassword()是加密后的密码,返回值判断是否匹配
        boolean matches = passwordEncoder.matches(password, ebUser.getPassword());
        if (!matches) {
            R.error(4001,"账号或密码错误");
        }
        LoginDTO loginDTO = LoginDTO.builder()
                .isUser(true)
                .id(ebUser.getId())
                .userName(ebUser.getUsername())
                .rememberMe(loginFormDTO.getRememberMe())
                .build();
        String token;
        try {
            //生成token
            token = jwtUtils.createToken(loginDTO);
            //生成refreshToken
            String refreshToken = jwtUtils.createRefreshToken(loginDTO);
            //生成refreshToken在cookie的最大有效期
            int maxAge = Math.toIntExact(BooleanUtils.isTrue(loginDTO.getRememberMe()) ?
                    Constant.JWT_REMEMBER_ME_TTL.getSeconds() : -1);
            //在Cookie中设置name  = "refresh" value = refreshToken
            WebUtils.cookieBuilder()
                    .name(Constant.REFRESH_HEADER)
                    .value(refreshToken)
                    .maxAge(maxAge)
                    .httpOnly(true)
                    .build();
        } catch (Exception e) {
            log.error("生成token失败", e);
            throw new UnauthorizedException(Constant.TOKEN_GENERATE_FAILED);
        }
        log.debug("token:{}", token);
        return R.ok(LoginVO.builder().loginDTO(loginDTO).token(token).build());
    }

}
