package com.electricitybill.service;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.po.EbUser;
import com.electricitybill.entity.po.EbUserType;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserPageVO;

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

    R insertUser(UserDTO userDTO);

    R deleteUser(List<Long> userIds);

    R updateUser(UserDTO userDTO);

    List<String> getUserTypeList();

    R addUserType(EbUserType ebUserType);
}
