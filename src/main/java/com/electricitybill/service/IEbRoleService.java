package com.electricitybill.service;

import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.role.PermissionDTO;
import com.electricitybill.entity.dto.role.RoleCreateDTO;
import com.electricitybill.entity.dto.role.RoleEditDTO;
import com.electricitybill.entity.dto.role.RolePageQuery;
import com.electricitybill.entity.po.EbRole;
import com.baomidou.mybatisplus.extension.service.IService;
import com.electricitybill.entity.vo.role.PermissionDetailVO;
import com.electricitybill.entity.vo.role.RoleInfoVO;
import com.electricitybill.entity.vo.role.RolePageVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
public interface IEbRoleService extends IService<EbRole> {

    PermissionDetailVO editRoleAndAdminDetail(Long id);

    PageDTO<RolePageVO> queryPage(RolePageQuery rolePageQuery);

    R deleteAdmins(List<Long> ids);

    R editRole(Long id, RoleEditDTO roleEditDTO);

    R createRole(RoleCreateDTO roleCreateDTO);

    R status(Long id);

    List<PermissionDTO> getPermissionList();

    List<RoleInfoVO> roleList();
    Map<Long, List<Long>> permissionRoleIdToMap();
    Map<Long, List<Long>> currentPermissionRoleMap(Map<Long, List<Long>> permissionRoleMap, List<Long> permissionIds);
}
