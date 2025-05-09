package com.electricitybill.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.po.EbScheduledTask;
import com.electricitybill.entity.vo.user.UserProfileVO;
import com.electricitybill.enums.TaskType;
import com.electricitybill.enums.UserTaskType;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.mapper.EbScheduledTaskMapper;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.UserProfileService;
import com.electricitybill.utils.RSAUtils;
import com.electricitybill.utils.UserContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.electricitybill.controller.EbLoginController.keyPair;
import static net.sf.jsqlparser.statement.select.PlainSelect.getStringList;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final EbUserMapper ebUserMapper;
    private final PasswordEncoder passwordEncoder;
    @Resource
    private final EbScheduledTaskMapper ebScheduledTaskMapper;

    private static List<String> getStringList(EbScheduledTask ebScheduledTask, Long userId, Boolean value) {
        String userIds = ebScheduledTask.getUserIds();
        if (userIds == null) {
            return new ArrayList<>();
        }
        //,逗号切割,转为list
        List<String> userIdList = new ArrayList<>(Arrays.asList(userIds.split(",")));
        if (userIdList.contains(String.valueOf(userId)) && !value) {
            userIdList.remove(String.valueOf(userId));

        } else if (!userIdList.contains(String.valueOf(userId)) && value) {
            userIdList.add(String.valueOf(userId));
        }
        return userIdList;
    }

    @Override
    public UserProfileVO getUserProfile() {
        Long userId = UserContextUtils.getUserId();
        EbUser user = ebUserMapper.selectById(userId);

        if (user == null) {
            log.error("未找到用户信息, userId: {}", userId);
            throw new BadRequestException("用户信息不存在");
        }

        UserProfileVO profileVO = new UserProfileVO();
        BeanUtils.copyProperties(user, profileVO);
        profileVO.setRegisterTime(user.getCreatedAt());
        profileVO.setUsername(user.getUsername());
        List<EbScheduledTask> ebScheduledTasks = ebScheduledTaskMapper.selectList(
                new LambdaQueryWrapper<EbScheduledTask>()
                        .eq(EbScheduledTask::getTaskType, TaskType.USER_TASK.getDesc())
        );
        if (ebScheduledTasks.isEmpty()) {
            throw new BizIllegalException(Constant.TASK_NOT_EXIST);
        }
        for (EbScheduledTask ebScheduledTask : ebScheduledTasks) {//拿到账单提醒的任务
            if (ebScheduledTask.getTaskName().equals(UserTaskType.BILL_REMINDER.getDesc())) {
                String userIds = ebScheduledTask.getUserIds();
                if (userIds == null) {
                    profileVO.setBillReminder(false);
                    continue;
                }
                //,逗号切割,转为list
                List<String> userIdList = Arrays.asList(userIds.split(","));
                profileVO.setBillReminder(userIdList.contains(String.valueOf(userId)));
            }
            if (ebScheduledTask.getTaskName().equals(UserTaskType.PAYMENT_REMINDER.getDesc())) {
                String userIds = ebScheduledTask.getUserIds();
                if (userIds == null) {
                    profileVO.setPaymentReminder(false);
                    continue;
                }
                //,逗号切割,转为list
                List<String> userIdList = Arrays.asList(userIds.split(","));
                profileVO.setPaymentReminder(userIdList.contains(String.valueOf(userId)));
            }
        }
        // 敏感信息处理，不返回密码等敏感字段
        return profileVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserProfile(JSONObject jsonObject) {
        Long userId = UserContextUtils.getUserId();
        EbUser user = ebUserMapper.selectById(userId);
        if (user == null) {
            throw new BadRequestException("用户信息不存在");
        }
        if (jsonObject.get("username") != null) {
            user.setUsername(jsonObject.getStr("username"));
        }
        if (jsonObject.get("address") != null) {
            user.setAddress(jsonObject.getStr("address"));
        }
        Boolean billReminder = jsonObject.getBool("billReminder");
        Boolean paymentReminder = jsonObject.getBool("paymentReminder");
        List<EbScheduledTask> ebScheduledTasks = ebScheduledTaskMapper.selectList(
                new LambdaQueryWrapper<EbScheduledTask>()
                        .eq(EbScheduledTask::getTaskType, TaskType.USER_TASK.getDesc())
        );
        if (ebScheduledTasks.isEmpty()) {
            throw new BizIllegalException(Constant.TASK_NOT_EXIST);
        }
        for (EbScheduledTask ebScheduledTask : ebScheduledTasks) {//拿到账单提醒的任务
            if (ebScheduledTask.getTaskName().equals(UserTaskType.BILL_REMINDER.getDesc())) {
                List<String> userIdList = getStringList(ebScheduledTask, userId, billReminder);
                if (userIdList.isEmpty() && billReminder) {
                    ebScheduledTask.setUserIds(userId.toString());
                } else if (userIdList.isEmpty()) {
                    ebScheduledTask.setUserIds("");
                } else ebScheduledTask.setUserIds(String.join(",", userIdList));
            }
            if (ebScheduledTask.getTaskName().equals(UserTaskType.PAYMENT_REMINDER.getDesc())) {
                List<String> userIdList = getStringList(ebScheduledTask, userId, paymentReminder);
                if (userIdList.isEmpty() && paymentReminder) {
                    ebScheduledTask.setUserIds(userId.toString());
                } else if (userIdList.isEmpty()) {
                    ebScheduledTask.setUserIds("");
                } else
                    ebScheduledTask.setUserIds(String.join(",", userIdList));
            }
            ebScheduledTaskMapper.updateById(ebScheduledTask);
        }

        int result = ebUserMapper.updateById(user);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(String currentPassword, String newPassword, String confirmPassword) {
        // 参数校验
        if (!StringUtils.hasText(currentPassword) || !StringUtils.hasText(newPassword) || !StringUtils.hasText(confirmPassword)) {
            log.warn("修改密码参数不完整");
            throw new BadRequestException("请填写完整的密码信息");
        }
        try {
            //接口密钥传递的密码
            currentPassword = RSAUtils.decrypt(currentPassword, RSAUtils.getPrivateKey(keyPair));
            newPassword = RSAUtils.decrypt(newPassword, RSAUtils.getPrivateKey(keyPair));
            confirmPassword = RSAUtils.decrypt(confirmPassword, RSAUtils.getPrivateKey(keyPair));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        EbUser ebUser = ebUserMapper.selectById(UserContextUtils.getUserId());
        if (ebUser == null) {
            throw new BadRequestException("用户信息不存在");
        }
        if(!passwordEncoder.matches(currentPassword, ebUser.getPassword())){
            throw new BadRequestException("当前原密码错误");
        }
        if(!newPassword.equals(confirmPassword)){
            throw new BadRequestException("新密码与确认密码不一致");
        }
        //对密码进行bcrypt加密
        String encodedPassword= passwordEncoder.encode(newPassword);
        // 更新密码
        ebUser.setPassword(encodedPassword);
        int result = ebUserMapper.updateById(ebUser);
        return result > 0;


    }

}