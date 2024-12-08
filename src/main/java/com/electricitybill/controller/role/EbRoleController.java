package com.electricitybill.controller.role;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.role.PermissionDTO;
import com.electricitybill.entity.dto.role.RoleCreateDTO;
import com.electricitybill.entity.dto.role.RoleEditDTO;
import com.electricitybill.entity.dto.role.RolePageQuery;
import com.electricitybill.entity.vo.role.PermissionDetailVO;
import com.electricitybill.entity.vo.role.RoleInfoVO;
import com.electricitybill.entity.vo.role.RolePageVO;
import com.electricitybill.service.IEbRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@RestController
@RequestMapping("/role")
public class EbRoleController {
    @Resource
    private IEbRoleService ebRoleService;

    @ApiOperation("分页")
    @GetMapping("page")
    private PageDTO<RolePageVO> queryPage(RolePageQuery rolePageQuery){
        return ebRoleService.queryPage(rolePageQuery);
    }


    @ApiOperation("获取当前管理人员的信息")
    @GetMapping("detail/{id}")
    private PermissionDetailVO editRoleAndAdminDetail(@PathVariable(value = "id") Long id){
        return ebRoleService.editRoleAndAdminDetail(id);
    }

    @ApiOperation("删除系统人员")
    @DeleteMapping("delete")
    private R deleteAdmins(@RequestParam("ids") List<Long> ids){
        return ebRoleService.deleteAdmins(ids);
    }

    @ApiOperation("编辑系统人员")
    @PutMapping("edit/{id}")
    private R editRole(@PathVariable(value = "id") Long id, @RequestBody RoleEditDTO roleEditDTO){
        return ebRoleService.editRole(id, roleEditDTO);
    }

    @ApiOperation("创建角色或系统人员")
    @PostMapping("create")
    private R createRole(@RequestBody RoleCreateDTO roleCreateDTO){
        return ebRoleService.createRole(roleCreateDTO);
    }
    @ApiOperation("获取权限列表")
    @GetMapping("list")
    private List<PermissionDTO> getPermissionList(){
        return ebRoleService.getPermissionList();
    }
    @ApiOperation("状态")
    @PutMapping("status/{id}")
    private R status(@PathVariable(value = "id") Long id){
        return ebRoleService.status(id);
    }

    @ApiOperation("获取角色列表")
    @GetMapping("roleList")
    public List<RoleInfoVO> roleList(){
        return ebRoleService.roleList();
    }
}
