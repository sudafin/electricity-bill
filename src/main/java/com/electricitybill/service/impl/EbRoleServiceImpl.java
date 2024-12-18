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
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.electricitybill.controller.admin.EbAdminController.keyPair;

/**
 * <p>
 *  服务实现类
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
    public PermissionDetailVO editRoleAndAdminDetail(Long id) {
        //获取当前管理人员
        EbAdmin ebAdmin = ebAdminMapper.selectById(id);
        //判断当前管理人员是否存在
        if (ObjectUtils.isEmpty(ebAdmin)) {
            throw new BadRequestException(Constant.ACCOUNT_NOT_EXIST);
        }
        //获取当前管理人员的角色id,然后拿到角色信息
        Long roleId = ebAdmin.getRoleId();
        EbRole ebRole = ebRoleMapper.selectById(roleId);
        //获取当前管理人员拥有的所有权限列表
        List<EbRolePermission> ebRolePermissions = ebRolePermissionMapper.selectList(new LambdaQueryWrapper<EbRolePermission>().eq(EbRolePermission::getRoleId, roleId));
        if (CollUtils.isEmpty(ebRolePermissions)) {
            throw new BizIllegalException("当前角色设置错误");
        }
        //我们的eb_permission 表是分为menu还有actions,然后我们需要找到对应的menu然后再找到他们对应的actions; key是父类型的id, value是孩子的id集合
        Map<Long, List<Long>> allPermissionRoleMap = permissionRoleIdToMap();
        //拿到遍历拿到elRolePermissions里对象id的集合
        List<Long> permissionIds = ebRolePermissions.stream().map(EbRolePermission::getPermissionId).collect(Collectors.toList());
        Map<Long, List<Long>> currentPermissionRoleMap= currentPermissionRoleMap(allPermissionRoleMap, permissionIds);
        //设置返回值
        PermissionDetailVO permissionDetailVO = new PermissionDetailVO();
        permissionDetailVO.setAccount(ebAdmin.getAccount());
        permissionDetailVO.setRoleName(ebRole.getRoleName());
        permissionDetailVO.setRoleDesc(ebRole.getRoleDesc());
        //设置权限信息
        ArrayList<PermissionDTO> permissionVOArrayList = new ArrayList<>();
        currentPermissionRoleMap.forEach((key, value)->{
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
            //然后添加到总的列表中
            permissionVOArrayList.add(permissionDTO);
        });
        permissionDetailVO.setPermissionList(permissionVOArrayList);
        return permissionDetailVO;
    }

    @Override
    public PageDTO<RolePageVO> queryPage(RolePageQuery rolePageQuery) {
        Page<EbAdmin> ebAdminPage = new Page<>(rolePageQuery.getPageNo(), rolePageQuery.getPageSize());
        EbRole ebRole = new EbRole();
        if(StringUtils.isNotBlank(rolePageQuery.getRole())){
        ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>()
                .eq(EbRole::getRoleName, rolePageQuery.getRole()));
        }
        Page<EbAdmin> adminPage = ebAdminMapper.selectPage(ebAdminPage, new LambdaQueryWrapper<EbAdmin>()
                .eq(rolePageQuery.getAdminId() != null, EbAdmin::getId, rolePageQuery.getAdminId())
                .eq(StringUtils.isNotBlank(rolePageQuery.getAccount()), EbAdmin::getAccount, rolePageQuery.getAccount())
                .eq(ebRole.getId() !=null ,EbAdmin::getRoleId, ebRole.getId())
                .ge(rolePageQuery.getStartDate() != null, EbAdmin::getCreatedAt, rolePageQuery.getStartDate())
                .le(rolePageQuery.getEndDate() != null, EbAdmin::getCreatedAt, rolePageQuery.getEndDate())
        );
        List<EbAdmin> adminPageRecords = adminPage.getRecords();
        if(CollUtils.isEmpty(adminPageRecords)){
            return PageDTO.empty(adminPage);
        }
        ArrayList<RolePageVO> rolePageVOArrayList = new ArrayList<>();
        adminPageRecords.forEach(ebAdmin -> {
            RolePageVO rolePageVO = new RolePageVO();
            rolePageVO.setAccount(ebAdmin.getAccount());
            rolePageVO.setAdminId(ebAdmin.getId());
            rolePageVO.setStatus(ebAdmin.getStatus());
            EbRole role = ebRoleMapper.selectById(ebAdmin.getRoleId());
            if(ObjectUtils.isEmpty(role)){
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
    public R deleteAdmins(List<Long> ids){
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
        //处理管理员信息
        EbRole ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getRoleName, roleEditDTO.getRole()));
        if (ObjectUtils.isEmpty(ebRole)) {
            throw new BadRequestException(Constant.ROLE_NOT_EXIST);
        }
        ebAdmin.setRoleId(ebRole.getId());
        if(StringUtils.isNotBlank(roleEditDTO.getAccount())){
            ebAdmin.setAccount(roleEditDTO.getAccount());
        }
        if(StringUtils.isNotBlank(roleEditDTO.getPassword())){
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
        int update = ebAdminMapper.updateById(ebAdmin);
        if (update != 1) {
            throw new DbException(Constant.DB_UPDATE_FAILURE);
        }
        if(UserContextUtils.getUser().equals(ebAdmin.getId())){
            return R.of(401, "当前用户信息过期", null);
        }
        return R.ok();
    }

    @Override
    public R createRole(RoleCreateDTO roleCreateDTO) {
        R create;
        if (roleCreateDTO.getIsRole()) {
            create = isCreateRole(roleCreateDTO);
        }else {
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
        permissionRoleIdToMap.forEach((key, value)->{
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
        if(admin != null){
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
        ebAdmin.setAccount(roleCreateDTO.getAccount());
        EbRole ebRole = ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getRoleName, roleCreateDTO.getRole()));
        if(ebRole == null){
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
        if(ebRoleMapper.selectOne(new LambdaQueryWrapper<EbRole>().eq(EbRole::getRoleName, roleCreateDTO.getRole())) != null){
            return R.error(404, "当前角色已存在");
        }
        long ebRoleId = IdUtil.getSnowflakeNextId();
        ebRole.setId(ebRoleId);
        ebRole.setRoleDesc(roleCreateDTO.getRoleDesc());
        ebRole.setStatus(1);
        ebRole.setRoleName(roleCreateDTO.getRole());
        ebRoleMapper.insert(ebRole);
        //设置权限
        List<Long> permissionIdList = roleCreateDTO.getPermissionIdList();
        Map<Long, List<Long>> currentPermissionRoleMap = currentPermissionRoleMap(permissionRoleIdToMap(), permissionIdList);
        currentPermissionRoleMap.forEach((key,value)->{
            ebRolePermissionMapper.insert(new EbRolePermission().setRoleId(ebRoleId).setPermissionId(key));
            //如果有子节点
            if(CollUtils.isNotEmpty(value))
                value.forEach(permissionId -> ebRolePermissionMapper.insert(new EbRolePermission().setRoleId(ebRoleId).setPermissionId(permissionId)));
        });
        //删除缓存
        stringRedisTemplate.delete(Constant.PERMISSION_ROLE_MAP);
        return R.ok();
    }


    /**
     * 根据给定的权限ID列表更新权限与角色映射
     * 此方法旨在过滤和排序权限与角色的映射，确保只保留与给定权限ID列表匹配的权限
     *
     * @param permissionRoleMap 原始的权限与角色映射，其中键为权限ID，值为拥有该权限的角色ID列表
     * @param permissionIds 当前有效的权限ID列表
     * @return 返回一个新的、经过过滤和排序的权限与角色映射
     */
    public Map<Long, List<Long>> currentPermissionRoleMap(Map<Long, List<Long>> permissionRoleMap, List<Long> permissionIds) {
        //获取redis的缓存数据
        String permissionRoleMapJson = stringRedisTemplate.opsForValue().get(Constant.PERMISSION_ROLE_MAP);
        //查看当前是否有缓存数据
        if(StrUtil.isNotEmpty(permissionRoleMapJson)){
            return JSONUtil.toBean(permissionRoleMapJson, new TypeReference<Map<Long, List<Long>>>() {},false);
        }
        //没有就重新获取
            //permissionIds和permissionRoleMap进行排除,
            //把permissionRoleMap中没有permissionIds的key去掉
        permissionRoleMap.keySet().removeIf(key -> !permissionIds.contains(key));
            //把permissionRoleMap中list类型的value中没有permissionIds的值去掉
        permissionRoleMap.replaceAll((key, value) ->
                value.stream()
                        .filter(permissionIds::contains)
                        .collect(Collectors.toList())
        );
        //不使用stream流的写法
        /**
         *         for (Map.Entry<String, List<Long>> entry : permissionRoleMap.entrySet()) {
         *             List<Long> filteredValue = new ArrayList<>();
         *             //取出每个list的值然后遍历然后判断包含然后添加到新的list中
         *             for (Long permissionId : entry.getValue()) {
         *                 if (permissionIds.contains(permissionId)) {
         *                     filteredValue.add(permissionId);
         *                 }
         *             }
         *             entry.setValue(filteredValue);
         *         }
         */
        //permissionRoleMap按key进行排序,通过TreeMap排序,并存入到redis缓存中,使用hash结构
        Map<Long, List<Long>> orderMap = new TreeMap<>(permissionRoleMap);
        //序列化后存入redis
        stringRedisTemplate.opsForValue().set(Constant.PERMISSION_ROLE_MAP, JSONUtil.toJsonStr(orderMap));
        return orderMap;
    }

    /**
     * 生成权限与角色ID的映射
     * 该方法用于创建一个HashMap，其中键是权限ID，值是包含该权限下所有子权限ID的列表
     * 主要目的是为了快速查询某个权限下的所有子权限
     *
     * @return HashMap<Long, List<Long>> 返回一个HashMap，键为权限ID，值为子权限ID列表
     */
    public Map<Long, List<Long>> permissionRoleIdToMap() {
        // 查询所有权限记录
        List<EbPermission> ebPermissionList = ebPermissionMapper.selectList(new LambdaQueryWrapper<>());
        // 初始化权限映射HashMap
        HashMap<Long, List<Long>> permissionMap = new HashMap<>();
        // 遍历权限列表，为每个权限创建或更新映射
        ebPermissionList.forEach(ebPermission -> {
            // 如果权限的父ID为空，则将其作为父权限添加到映射中，并初始化其子权限列表
            if (ebPermission.getParentId()  == null) {
                permissionMap.putIfAbsent(ebPermission.getId(), new ArrayList<>());
            }else {
                // 如果权限的父ID不为空，则将其添加到对应父权限的子权限列表中
                permissionMap.get(ebPermission.getParentId()).add(ebPermission.getId());
            }
        });
        // 返回构建完成的权限映射
        return new TreeMap<>(permissionMap);
    }
}
