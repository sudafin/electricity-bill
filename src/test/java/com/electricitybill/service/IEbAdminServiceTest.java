package com.electricitybill.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.entity.po.EbPermission;
import com.electricitybill.entity.po.EbRolePermission;
import com.electricitybill.mapper.EbPermissionMapper;
import com.electricitybill.mapper.EbRolePermissionMapper;
import com.electricitybill.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class IEbAdminServiceTest {
    @Resource
     private PasswordEncoder passwordEncoder;
    @Resource
    private EbPermissionMapper ebPermissionMapper;
    @Resource
    private EbRolePermissionMapper ebRolePermissionMapper;
    @Test
    void login() {
        System.out.println(passwordEncoder.matches("admin123", "$2a$10$4FUHuxcpYOIomnc3CIJfcOnCYk0P0corhysvagSvIqy234vm3hj9u"));
    }
    @Test
    void permissionIdToMap() {
        List<EbPermission> ebPermissionList = ebPermissionMapper.selectList(new LambdaQueryWrapper<EbPermission>());
        HashMap<Long, List<Long>> permissionRoleMap = new HashMap<>();
        ebPermissionList.forEach(ebPermission -> {
            if (ebPermission.getParentId()  == null) {
                permissionRoleMap.putIfAbsent(ebPermission.getId(), new ArrayList<>());
            }else {
                permissionRoleMap.get(ebPermission.getParentId()).add(ebPermission.getId());
            }
        });
        List<EbRolePermission> ebRolePermissions = ebRolePermissionMapper.selectList(new LambdaQueryWrapper<EbRolePermission>().eq(EbRolePermission::getRoleId, 3L));
        List<Long> permissionIds = ebRolePermissions.stream().map(EbRolePermission::getPermissionId).collect(Collectors.toList());
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
        //打印结果
        permissionRoleMap.forEach((k,v)->{
            System.out.println(k);
            v.forEach(System.out::println);
            System.out.println("-------------------------------------");
        });
    }
    @Test
    void testStr(){
        String role = " ";
        //都对null为true
        System.out.println(StringUtils.isEmpty(role)); //false
        System.out.println(StringUtils.isBlank(role)); //true
    }
}