package com.electricitybill.service;

import cn.hutool.json.JSONObject;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserCreateDTO;
import com.electricitybill.entity.dto.user.UserEditDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.dto.usertype.UserTypeCreateDTO;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserInfoVO;
import com.electricitybill.entity.vo.user.UserPageVO;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbUserService extends IService<EbUser> {

    PageDTO<UserPageVO> queryUserPage(UserPageQuery userPageQuery);

    UserDetailVO queryUserDetail(Long userId);

    R insertUser(UserCreateDTO userCreateDTO);

    R deleteUser(List<Long> userIds);

    R adminUpdateUser(UserCreateDTO userCreateDTO);

    List<String> getUserTypeList();

    R addUserType(@NotNull UserTypeCreateDTO ebUserType);

    R userEditInfo(UserEditDTO userEditDTO);

    UserInfoVO getUserInfo();

    JSONObject getUserInfoByIdCard(String idCardNo);

    R bindMeter(JSONObject jsonObject);
}
