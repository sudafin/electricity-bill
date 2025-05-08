package com.electricitybill.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.electricitybill.constants.Constant;
import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.role.PermissionDTO;
import com.electricitybill.entity.dto.role.RoleCreateDTO;
import com.electricitybill.entity.dto.role.RoleEditDTO;
import com.electricitybill.entity.dto.role.RolePageQuery;
import com.electricitybill.entity.po.EbAdmin;
import com.electricitybill.entity.po.EbPermission;
import com.electricitybill.entity.po.EbRole;
import com.electricitybill.entity.po.EbRolePermission;
import com.electricitybill.entity.vo.role.PermissionDetailVO;
import com.electricitybill.entity.vo.role.RoleInfoVO;
import com.electricitybill.entity.vo.role.RolePageVO;
import com.electricitybill.enums.ValidType;
import com.electricitybill.expcetions.BadRequestException;
import com.electricitybill.expcetions.BizIllegalException;
import com.electricitybill.expcetions.DbException;
import com.electricitybill.mapper.EbAdminMapper;
import com.electricitybill.mapper.EbPermissionMapper;
import com.electricitybill.mapper.EbRoleMapper;
import com.electricitybill.mapper.EbRolePermissionMapper;
import com.electricitybill.service.IEbRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.electricitybill.utils.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.electricitybill.controller.EbLoginController.keyPair;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
public class EbRoleServiceImpl extends ServiceImpl<EbRoleMapper, EbRole> implements IEbRoleService {
    @Resource
    private EbAdminMapper ebAdminMapper;
    @Resource
    private EbPermissionMapper ebPermissionMapper;
    @Resource
    private EbRolePermissionMapper ebRolePermissionMapper;
    @Resource
    private EbRoleMapper ebRoleMapper;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public PermissionDetailVO roleAndAdminDetail(Long id) {
        // 获取当前管理人员
        EbAdmin ebAdmin = ebAdminMapper.selectById(id);
        if (ObjectUtils.isEmpty(ebAdmin)) {
            throw new BadRequestException(Constant.ACCOUNT_NOT_EXIST);
        }

        // 获取角色信息
        Long roleId = ebAdmin.getRoleId();
        EbRole ebRole = ebRoleMapper.selectById(roleId);

        // 获取权限ID列表
        List<EbRolePermission> ebRolePermissions = ebRolePermissionMapper.selectList(
                new LambdaQueryWrapper<EbRolePermission>().eq(EbRolePermission::getRoleId, roleId));
        if (CollUtils.isEmpty(ebRolePermissions)) {
            throw new BizIllegalException("当前角色设置错误");
        }

        List<Long> permissionIds = ebRolePermissions.stream()
                .map(EbRolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 构造完整权限结构
        Map<Long, List<Long>> allPermissionRoleMap = permissionRoleIdToMap();
        Map<Long, List<Long>> currentPermissionRoleMap = currentPermissionRoleMap(allPermissionRoleMap, permissionIds);

        // 查询所有相关权限详情（避免多次调用数据库）
        Set<Long> allRelevantPermissionIds = new HashSet<>(permissionIds);
        allRelevantPermissionIds.addAll(currentPermissionRoleMap.keySet());

        Map<Long, EbPermission> permissionMap = ebPermissionMapper.selectBatchIds(allRelevantPermissionIds)
                .stream()
                .collect(Collectors.toMap(EbPermission::getId, Function.identity()));

        // 准备数据结构
        PermissionDetailVO permissionDetailVO = new PermissionDetailVO();
        permissionDetailVO.setAccount(ebAdmin.getAccount());
        permissionDetailVO.setRoleName(ebRole.getRoleName());
        permissionDetailVO.setRoleDesc(ebRole.getRoleDesc());

        List<PermissionDTO> permissionVOArrayList = new ArrayList<>();

        // 避免重复出现的子权限
        Set<Long> allChildPermissionIds = currentPermissionRoleMap.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        for (Map.Entry<Long, List<Long>> entry : currentPermissionRoleMap.entrySet()) {
            Long parentId = entry.getKey();

            // 跳过已经作为子权限出现的权限ID（防止重复）
            if (allChildPermissionIds.contains(parentId)) {
                continue;
            }

            EbPermission parentPermission = permissionMap.get(parentId);
            if (parentPermission == null) continue;

            PermissionDTO parentDTO = new PermissionDTO();
            parentDTO.setPermissionId(parentId);
            parentDTO.setPermissionName(parentPermission.getPermissionName());

            List<PermissionDTO> children = new ArrayList<>();
            for (Long childId : entry.getValue()) {
                EbPermission childPermission = permissionMap.get(childId);
                if (childPermission == null) continue;

                PermissionDTO childDTO = new PermissionDTO();
                childDTO.setPermissionId(childId);
                childDTO.setPermissionName(childPermission.getPermissionName());
                childDTO.setChildren(new ArrayList<>());
                children.add(childDTO);
            }

            parentDTO.setChildren(children);
            permissionVOArrayList.add(parentDTO);
        }

        permissionDetailVO.setPermissionList(permissionVOArrayList);
        return permissionDetailVO;
    }


    @Override
    public PageDTO<RolePageVO> queryPage(RolePageQuery rolePageQuery) {
        Page<EbAdmin> ebAdminPage = new Page<>(rolePageQuery.getPageNo(), rolePageQuery.getPageSize());
        EbRole ebRole = new EbRole();
        if (StringUtils.isNotBlank(rolePageQuery.getRole())) {
            ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>()
                    .eq(EbRole::getRoleName, rolePageQuery.getRole()));
        }
        Page<EbAdmin> adminPage = ebAdminMapper.selectPage(ebAdminPage, new LambdaQueryWrapper<EbAdmin>()
                .eq(StringUtils.isNotBlank(rolePageQuery.getAccount()), EbAdmin::getAccount, rolePageQuery.getAccount())
                .eq(ebRole.getId() != null, EbAdmin::getRoleId, ebRole.getId())
                .ge(rolePageQuery.getStartDate() != null, EbAdmin::getCreatedAt, rolePageQuery.getStartDate())
                .le(rolePageQuery.getEndDate() != null, EbAdmin::getCreatedAt, rolePageQuery.getEndDate())
                .orderByDesc(EbAdmin::getCreatedAt)
        );
        List<EbAdmin> adminPageRecords = adminPage.getRecords();
        if (CollUtils.isEmpty(adminPageRecords)) {
            return PageDTO.empty(adminPage);
        }
        ArrayList<RolePageVO> rolePageVOArrayList = new ArrayList<>();
        adminPageRecords.forEach(ebAdmin -> {
            RolePageVO rolePageVO = new RolePageVO();
            rolePageVO.setAccount(ebAdmin.getAccount());
            rolePageVO.setAdminId(ebAdmin.getId());
            rolePageVO.setStatus(ebAdmin.getStatus());
            EbRole role = ebRoleMapper.selectById(ebAdmin.getRoleId());
            if (ObjectUtils.isEmpty(role)) {
                throw new BizIllegalException("当前角色设置错误");
            }
            rolePageVO.setRole(role.getRoleName());
            rolePageVO.setCreateTime(role.getCreatedAt());
            rolePageVO.setRoleDesc(role.getRoleDesc());
            rolePageVOArrayList.add(rolePageVO);
        });
        return PageDTO.of(adminPage, rolePageVOArrayList);
    }

    @Override
    public R deleteAdmins(List<Long> ids) {
        int adminDeleteBatchIds = ebAdminMapper.deleteBatchIds(ids);
        if (adminDeleteBatchIds != ids.size()) {
            throw new DbException(Constant.DB_DELETE_FAILURE);
        }
        return R.ok();
    }

    @Override
    @Transactional
    public R editRole(Long id, RoleEditDTO roleEditDTO) {
        EbAdmin ebAdmin = ebAdminMapper.selectById(id);
        if (ObjectUtils.isEmpty(ebAdmin)) {
            throw new BadRequestException(Constant.ACCOUNT_NOT_EXIST);
        }
        if (roleEditDTO.getIsEditRole()) {
            //处理管理员信息
            EbRole ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getRoleName, roleEditDTO.getRole()));
            if (ObjectUtils.isEmpty(ebRole)) {
                throw new BadRequestException(Constant.ROLE_NOT_EXIST);
            }
            ebAdmin.setRoleId(ebRole.getId());
        } else {
            if (StringUtils.isNotBlank(roleEditDTO.getPassword())) {
                //对密码解密
                try {
                    //keyPair在EbAdminController中设为public static便于获取, 因为在需要用EbAdminController中用这个生成公钥密钥,我们才能使用RSA私钥解密
                    String decryptedPassword = RSAUtils.decrypt(roleEditDTO.getPassword(), RSAUtils.getPrivateKey(keyPair));
                    //对密码进行bcrypt加密
                    String encodedPassword = passwordEncoder.encode(decryptedPassword);
                    ebAdmin.setPassword(encodedPassword);
                } catch (Exception e) {
                    throw new BizIllegalException("密码安全问题");
                }
            }
        }
        ebAdminMapper.updateById(ebAdmin);
        if (StringUtils.isNotBlank(roleEditDTO.getPassword()) && AdminContextUtils.getAdminId().equals(ebAdmin.getId())) {
            return R.of(401, "当前用户登录过期", null);
        }
        return R.ok();
    }

    @Override
    public R createRole(RoleCreateDTO roleCreateDTO) {
        R create;
        if (roleCreateDTO.getIsRole()) {
            create = isCreateRole(roleCreateDTO);
        } else {
            create = isCreateAdmin(roleCreateDTO);
        }
        return create;
    }

    @Override
    public R status(Long id) {
        EbAdmin ebAdmin = ebAdminMapper.selectById(id);
        if (ObjectUtils.isEmpty(ebAdmin)) {
            throw new BadRequestException(Constant.ACCOUNT_NOT_EXIST);
        }
        ebAdmin.setStatus(ebAdmin.getStatus() == 0 ? 1 : 0);
        int update = ebAdminMapper.updateById(ebAdmin);
        if (update != 1) {
            throw new DbException(Constant.DB_UPDATE_FAILURE);
        }
        return R.ok();
    }

    @Override
    public List<PermissionDTO> getPermissionList() {
        Map<Long, List<Long>> permissionRoleIdToMap = permissionRoleIdToMap();
        ArrayList<PermissionDTO> permissionVOArrayList = new ArrayList<>();
        permissionRoleIdToMap.forEach((key, value) -> {
            ArrayList<PermissionDTO> childrenPermissionVOArrayList = new ArrayList<>();
            PermissionDTO permissionDTO = new PermissionDTO();
            permissionDTO.setPermissionId(key);
            permissionDTO.setPermissionName(ebPermissionMapper.selectById(key).getPermissionName());
            //设置儿子节点
            value.forEach(permissionId -> {
                PermissionDTO childrenPermissionDTO = new PermissionDTO();
                childrenPermissionDTO.setPermissionId(permissionId);
                childrenPermissionDTO.setPermissionName(ebPermissionMapper.selectById(permissionId).getPermissionName());
                childrenPermissionDTO.setChildren(new ArrayList<>());
                childrenPermissionVOArrayList.add(childrenPermissionDTO);
            });
            permissionDTO.setChildren(childrenPermissionVOArrayList);
            permissionVOArrayList.add(permissionDTO);
        });
        return permissionVOArrayList;
    }

    @Override
    public List<RoleInfoVO> roleList() {
        List<EbRole> list = list();
        ArrayList<RoleInfoVO> roleInfoVOS = new ArrayList<>();
        list.forEach(ebRole -> {
            RoleInfoVO roleInfoVO = new RoleInfoVO();
            roleInfoVO.setLabel(ebRole.getRoleName());
            roleInfoVO.setValue(ebRole.getRoleName());
            if ("系统管理员".equals(ebRole.getRoleName())) {
                roleInfoVO.setIcon("Management");
            } else if ("运营人员".equals(ebRole.getRoleName())) {
                roleInfoVO.setIcon("Operation");
            } else {
                roleInfoVO.setIcon("Monitor");
            }

            roleInfoVOS.add(roleInfoVO);
        });
        return roleInfoVOS;
    }

    private R isCreateAdmin(RoleCreateDTO roleCreateDTO) {
        EbAdmin ebAdmin = new EbAdmin();
        //查看当前是否有相同账号
        EbAdmin admin = ebAdminMapper.selectOne(new LambdaQueryWrapper<EbAdmin>().eq(EbAdmin::getAccount, roleCreateDTO.getAccount()));
        if (admin != null) {
            return R.error(404, "当前账号已存在");
        }
        //keyPair在EbAdminController中
        String decryptedPassword;
        try {
            decryptedPassword = RSAUtils.decrypt(roleCreateDTO.getPassword(), RSAUtils.getPrivateKey(keyPair));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //对密码进行bcrypt加密
        String encodedPassword = passwordEncoder.encode(decryptedPassword);
        ebAdmin.setPassword(encodedPassword);
        ebAdmin.setAdminName("admin");
        ebAdmin.setAccount(roleCreateDTO.getAccount());
        EbRole ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getRoleName, roleCreateDTO.getRole()));
        if (ebRole == null) {
            throw new BadRequestException(Constant.ROLE_NOT_EXIST);
        }
        ebAdmin.setRoleId(ebRole.getId());
        ebAdmin.setStatus(1);
        int insert = ebAdminMapper.insert(ebAdmin);
        if (insert != 1) {
            throw new DbException(Constant.DB_INSERT_FAILURE);
        }
        return R.ok();
    }

    private R isCreateRole(RoleCreateDTO roleCreateDTO) {
        EbRole ebRole = new EbRole();
        if (ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getRoleName, roleCreateDTO.getRole())) != null) {
            return R.error(404, "当前角色已存在");
        }
        long ebRoleId = IdUtil.getSnowflakeNextId();
        ebRole.setId(ebRoleId);
        ebRole.setRoleDesc(roleCreateDTO.getRoleDesc());
        ebRole.setStatus(ValidType.VALID.getValue());
        ebRole.setRoleName(roleCreateDTO.getRole());
        ebRoleMapper.insert(ebRole);
        //设置权限
        List<Long> permissionIdList = roleCreateDTO.getPermissionIdList();
        // 根据已有权限结构获取所有要授权的权限（包含子权限）
        Map<Long, List<Long>> currentPermissionRoleMap = currentPermissionRoleMap(permissionRoleIdToMap(), permissionIdList);
        // 用 Set 去重所有权限 ID
        Set<Long> allPermissionIds = new HashSet<>();
        currentPermissionRoleMap.forEach((parentId, childIds) -> {
            allPermissionIds.add(parentId);
            allPermissionIds.addAll(childIds);
        });
        // 批量插入
        List<EbRolePermission> rolePermissions = allPermissionIds.stream()
                .map(permissionId -> new EbRolePermission().setRoleId(ebRoleId).setPermissionId(permissionId))
                .collect(Collectors.toList());

        rolePermissions.forEach(ebRolePermissionMapper::insert);
        //删除缓存
        stringRedisTemplate.delete(Constant.PERMISSION_ROLE_MAP);
        return R.ok();
    }


    /**
     * 根据给定的权限ID列表更新权限与角色映射
     * 此方法旨在过滤和排序权限与角色的映射，确保只保留与给定权限ID列表匹配的权限
     *
     * @return 返回一个新的、经过过滤和排序的权限与角色映射
     */
    public Map<Long, List<Long>> currentPermissionRoleMap(Map<Long, List<Long>> fullPermissionMap, List<Long> selectedPermissionIds) {
        // 返回的 Map 不直接操作参数
        Map<Long, List<Long>> resultMap = new HashMap<>();

        // 构造筛选后的结构（只保留 selectedPermissionIds 中的父权限及其子权限）
        for (Long id : selectedPermissionIds) {
            if (fullPermissionMap.containsKey(id)) {
                // 是父权限
                List<Long> childIds = fullPermissionMap.get(id);
                List<Long> filteredChildren = childIds.stream()
                        .filter(selectedPermissionIds::contains)
                        .collect(Collectors.toList());
                resultMap.put(id, filteredChildren);
            } else {
                // 是孤立子权限，找它的父级（用于兼容性扩展）
                for (Map.Entry<Long, List<Long>> entry : fullPermissionMap.entrySet()) {
                    if (entry.getValue().contains(id)) {
                        resultMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(id);
                    }
                }
            }
        }

        // 不要缓存和 selectedPermissionIds 强相关的结果！否则多用户会串数据
        return new TreeMap<>(resultMap);
    }

    /**
     * 生成权限与角色ID的映射
     * 该方法用于创建一个HashMap，其中键是权限ID，值是包含该权限下所有子权限ID的列表
     * 主要目的是为了快速查询某个权限下的所有子权限
     *
     * @return HashMap<Long, List < Long>> 返回一个HashMap，键为权限ID，值为子权限ID列表
     */
    public Map<Long, List<Long>> permissionRoleIdToMap() {
        // 查询所有权限记录
        List<EbPermission> ebPermissionList = ebPermissionMapper.selectList(new LambdaQueryWrapper<>());

        // 初始化权限映射
        Map<Long, List<Long>> permissionMap = new HashMap<>();

        for (EbPermission permission : ebPermissionList) {
            Long id = permission.getId();
            Long parentId = permission.getParentId();

            // 确保每个权限都有映射列表（无论是否是父级）
            permissionMap.putIfAbsent(id, new ArrayList<>());

            // 如果是子权限，添加到父权限的列表中
            if (parentId != null) {
                permissionMap.putIfAbsent(parentId, new ArrayList<>());
                permissionMap.get(parentId).add(id);
            }
        }

        return new TreeMap<>(permissionMap); // TreeMap 可选：让结果按 key 排序
    }

}
