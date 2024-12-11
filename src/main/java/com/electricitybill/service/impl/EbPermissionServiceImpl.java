package com.electricitybill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.entity.po.EbAdmin;
import com.electricitybill.entity.po.EbPermission;
import com.electricitybill.entity.po.EbRolePermission;
import com.electricitybill.mapper.EbAdminMapper;
import com.electricitybill.mapper.EbPermissionMapper;
import com.electricitybill.mapper.EbRolePermissionMapper;
import com.electricitybill.service.IEbPermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.service.IEbRoleService;
import com.electricitybill.utils.UserContextUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
public class EbPermissionServiceImpl extends ServiceImpl<EbPermissionMapper, EbPermission> implements IEbPermissionService {
    @Resource
    private IEbRoleService ebRoleService;
    @Resource
    private EbAdminMapper ebAdminMapper;
    @Resource
    private EbRolePermissionMapper ebRolePermissionMapper;
    @Override
    public Boolean roleCheck(HttpServletRequest request) {
        AtomicReference<Boolean> isValid = new AtomicReference<>(false);
        String requestURI = request.getRequestURI();
        String[] split = requestURI.split("/");
        String module = split[1];
        String actions;
        if(split.length >=3) {
            actions = split[2];
        } else {
            actions = "";
        }

        EbAdmin ebAdmin = ebAdminMapper.selectById(UserContextUtils.getUser());
        List<EbRolePermission> ebRolePermissions = ebRolePermissionMapper.selectList(new LambdaQueryWrapper<EbRolePermission>().eq(EbRolePermission::getRoleId, ebAdmin.getRoleId()));
        List<Long> list = ebRolePermissions.stream().map(EbRolePermission::getPermissionId).collect(Collectors.toList());
        Map<Long, List<Long>> permissionRoleIdToMap = ebRoleService.permissionRoleIdToMap();
        Map<Long, List<Long>> currentPermissionRoleMap = ebRoleService.currentPermissionRoleMap(permissionRoleIdToMap, list);
        currentPermissionRoleMap.forEach((key,value)->{
            //拿到key的模块名称
            EbPermission ebPermission = baseMapper.selectById(key);
            if(ebPermission.getPermissionCode().equals(module)){
                if (value.isEmpty()) {
                    isValid.set(true);
                }else{
                    for (Long childrenId : value) {
                        EbPermission childrenEbPermission = baseMapper.selectById(childrenId);
                        String[] childrenCode = childrenEbPermission.getPermissionCode().split(":");
                        String childrenAction = childrenCode[1];
                        if(childrenAction.equals(actions)){
                            isValid.set(true);
                        }
                    }
                }
            }
        });
        return isValid.get();
    }
}
