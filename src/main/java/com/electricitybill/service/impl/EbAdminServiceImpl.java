package com.electricitybill.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ICaptcha;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.admin.AdminDTO;
import com.electricitybill.entity.dto.admin.AdminFormDTO;
import com.electricitybill.entity.po.*;
import com.electricitybill.entity.vo.admin.LoginVO;
import com.electricitybill.entity.vo.dashboard.DashboardVO;
import com.electricitybill.enums.AdminStatusType;
import com.electricitybill.enums.FeedbackStatusType;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.expcetions.ForbiddenException;
import com.electricitybill.expcetions.UnauthorizedException;
import com.electricitybill.mapper.*;
import com.electricitybill.service.IEbAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.service.IEbUserTypeService;
import com.electricitybill.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

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
    @Resource
    private EbElectricityUsageMapper ebElectricityUsageMapper;
    @Resource
    private EbBillMapper ebBillMapper;
    @Resource
    private EbMeterMapper ebMeterMapper;
    @Resource
    private EbUserFeedbackMapper ebUserFeedbackMapper;
    @Resource
    private EbUserMapper ebUserMapper;
    @Resource
    private IEbUserTypeService ebUserTypeService;
    @Resource
    private EbReconciliationMapper ebReconciliationMapper;
    @Override
    public DashboardVO getAdminDashboardInfo() {
        /**
         * 拿到所有用户数量,用电量,支付金额,账单,用户类型和最近7天的用电量
         */
        DashboardVO dashboardVO = DashboardVO.builder().build();
        List<EbElectricityUsage> ebElectricityUsageList = ebElectricityUsageMapper.selectList(new LambdaQueryWrapper<>());
        //补充总用电量和最近7天的用电量
        if (CollUtils.isEmpty(ebElectricityUsageList)) {
            //如果没有数据,则默认为0或空,不用抛错误
            dashboardVO.setTotalElectricityUsage(0L);
            dashboardVO.setElectricityWeekUsageList(CollUtils.emptyList());
        } else {
            double totalElectricity = ebElectricityUsageList.stream().mapToDouble(usage -> new BigDecimal(String.valueOf(usage.getUsageAmount())).doubleValue()).sum();
            //将数据倒序,按时间降序,时间最新的在前面方便获取最新的7个数据
            List<EbElectricityUsage> ebElectricityUsages = ebElectricityUsageList.stream().sorted(Comparator.comparing(EbElectricityUsage::getEndTime).reversed()).collect(Collectors.toList());
            //拿到最近7天的数据,设置为0
            // 生成最近7天的日期列表
            List<LocalDate> recentDates = IntStream.range(0, 7).mapToObj(LocalDate.now()::minusDays).sorted().collect(Collectors.toList());
            // 生成 Map<LocalDate, BigDecimal>，这里假设 BigDecimal 的值为每个日期的日期差的 BigDecimal 表示
            Map<LocalDate, BigDecimal> dateToValueMap = recentDates.stream().collect(Collectors.toMap(date -> date, // 使用日期作为 key
                    date -> BigDecimal.ZERO, // 初始值为 0
                    (existing, replacement) -> existing, // 处理键冲突的策略
                    LinkedHashMap::new // 使用 LinkedHashMap 保持插入顺序
            ));
            ebElectricityUsages.stream().limit(7)
                    //再次把最新的日期放到列表最后面
                    .sorted(Comparator.comparing(EbElectricityUsage::getEndTime)).forEach(usage -> {
                        //拿到当前用电量的日期
                        LocalDate endDate = usage.getEndTime().toLocalDate();
                        //如果是最近7天,就加入到list中,否则就跳过
                        if (dateToValueMap.containsKey(endDate)) {
                            dateToValueMap.put(endDate, usage.getUsageAmount());
                        }
                    });
            //将dateToValueMap的value转为list
            List<Double> electricityWeekUsageList = dateToValueMap.values().stream()
                    //日期排序
                    .map(BigDecimal::doubleValue).collect(toList());
            log.debug("总用电量:{}", totalElectricity);
            log.debug("最近7天的用电量:{}", electricityWeekUsageList);
            dashboardVO.setTotalElectricityUsage((long) totalElectricity);
            dashboardVO.setElectricityWeekUsageList(electricityWeekUsageList);
        }
        //补充账单总数和总金额
        List<EbPayment> ebPaymentList = new ArrayList<>();
        if (CollUtils.isEmpty(ebPaymentList)) {
            dashboardVO.setTotalPaymentBill(0L);
            dashboardVO.setTotalAmount(0L);
        } else {
            int totalPaymentBill = ebPaymentList.size();
            int totalAmount = ebPaymentList.stream().mapToInt(payment -> new BigDecimal(String.valueOf(payment.getAmount())).intValue()).sum();
            log.debug("总账单数:{}", totalPaymentBill);
            log.debug("总金额:{}", totalAmount);
            dashboardVO.setTotalAmount((long) totalAmount);
            dashboardVO.setTotalPaymentBill((long) totalPaymentBill);
        }
        //补充用户类型和用户总数
        List<EbUserType> ebUserTypeList = ebUserTypeService.lambdaQuery().eq(EbUserType::getStatus, 1).list();
        int totalUser = lambdaQuery().list().size();
        Map<String, Long> userTypeMap = new HashMap<>();
        ebUserTypeList.forEach(userType -> {
            Long userTypeCount = ebUserMapper.selectCount(new LambdaQueryWrapper<EbUser>().eq(EbUser::getUserType, userType.getTypeName()));
            log.debug("用户类型{}的数量:{}", userType, userTypeCount);
            userTypeMap.put(userType.getTypeName(), userTypeCount);
        });
        dashboardVO.setUserTypeMap(userTypeMap);
        dashboardVO.setTotalUser((long) totalUser);
        List<EbUserFeedback> ebUserFeedbacks = ebUserFeedbackMapper.selectList(new LambdaQueryWrapper<>());
        Long UnProcessedFeedbackCount = ebUserFeedbacks.stream().filter(feedback -> feedback.getFeedbackStatus().equals(FeedbackStatusType.PENDING.getDesc())).count();
        Long ProcessedFeedbackCount = ebUserFeedbacks.stream().filter(feedback -> feedback.getFeedbackStatus().equals(FeedbackStatusType.PROCESSED.getDesc())).count();
        dashboardVO.setUnprocessedFeedbackCount(UnProcessedFeedbackCount);
        dashboardVO.setProcessedFeedbackCount(ProcessedFeedbackCount);
        int reconciliationSize = ebReconciliationMapper.selectList(new LambdaQueryWrapper<>()).size();
        dashboardVO.setTotalReconciliation((long) reconciliationSize);
        log.info("dashboardVO的对象数据:{}", dashboardVO);
        return dashboardVO;
    }

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
                .rememberMe(adminFormDTO.getRememberMe())
                .build();
        String token;
        try {
            //生成token
            token = jwtUtils.createToken(adminDTO);
            //生成refreshToken
            String refreshToken = jwtUtils.createRefreshToken(adminDTO);
            //生成refreshToken在cookie的最大有效期
            int maxAge = Math.toIntExact(BooleanUtils.isTrue(adminDTO.getRememberMe()) ?
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
        return R.ok(LoginVO.builder().adminDTO(adminDTO).token(token).build());
    }

    @Override
    @Transactional
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
    public R checkCaptcha(String key, String code) {
        String captchaCode = stringRedisTemplate.opsForValue().get(key);
        if(captchaCode == null){
            //验证码过期
            return R.error(4001,"验证码过期");
        }
        if(!StringUtils.equalsIgnoreCase(code,captchaCode)){
            //验证码不对
            return R.error(4001,"验证码错误");
        }
        //到现在增加验证码的有效时间,防止后面可能用户再次输入本验证码
        stringRedisTemplate.expire(key, 5, TimeUnit.MINUTES);
        return R.ok();
    }

    @Override
    public String refreshToken(String token) {
        // 1.校验refresh-token,校验JTI
        AdminDTO adminDTO = jwtUtils.parseRefreshToken(token);
        // 2.生成新的access-token、refresh-token
        return generateToken(adminDTO);
    }

    @Override
    public void logout() {
        // 删除jti
        jwtUtils.cleanJtiCache();
        // 删除cookie
        WebUtils.cookieBuilder()
                .name(Constant.REFRESH_HEADER)
                .value("")
                .maxAge(0)
                .httpOnly(true)
                .build();
    }


    private String generateToken(AdminDTO adminDTO) {
        // 2.2.生成access-token
        String token = jwtUtils.createToken(adminDTO);
        // 2.3.生成refresh-token，将refresh-token的JTI 保存到Redis
        String refreshToken = jwtUtils.createRefreshToken(adminDTO);
        // 2.4.将refresh-token写入用户cookie，并设置HttpOnly为true
        int maxAge = BooleanUtils.isTrue(adminDTO.getRememberMe()) ?
                (int) Constant.JWT_REMEMBER_ME_TTL.toSeconds() : -1;
        WebUtils.cookieBuilder()
                .name(Constant.REFRESH_HEADER)
                .value(refreshToken)
                .maxAge(maxAge)
                .httpOnly(true)
                .build();
        return token;
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
