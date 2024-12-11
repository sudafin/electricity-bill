package com.electricitybill.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.electricitybill.entity.po.EbPermission;
import com.electricitybill.entity.po.EbRolePermission;
import com.electricitybill.mapper.EbPermissionMapper;
import com.electricitybill.mapper.EbRolePermissionMapper;
import com.electricitybill.utils.StringUtils;
import io.swagger.util.Json;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.*;
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

    @Test
    void testSpilt(){
        String str ="/user/page";
        String[] split = str.split("/");
        System.out.println(Arrays.toString(split));
    }

    @Test
    void testJson(){
        String str = "PageDTO(total=8, pages=2, list=[UserPageVO(id=1, username=张三, phone=13812345678, address=上海市黄浦区人民广场123号, meterNo=METER1001, accountStatus=正常, electricityUsage=500.00, userType=商业用户, balance=1380.50, lastPaymentDate=2023-05-01T10:00), UserPageVO(id=2, username=李四, phone=13912345678, address=上海市徐汇区虹桥路456号, meterNo=METER1002, accountStatus=正常, electricityUsage=800.00, userType=商业用户, balance=50.00, lastPaymentDate=2023-04-15T15:30), UserPageVO(id=3, username=王五, phone=13712345678, address=上海市长宁区延安西路789号, meterNo=METER1003, accountStatus=正常, electricityUsage=200.00, userType=居民用户, balance=300.00, lastPaymentDate=2023-05-10T09:15), UserPageVO(id=6, username=丽丽, phone=1231231312, address=3123123123, meterNo=3213, accountStatus=正常, electricityUsage=0.00, userType=居民用户, balance=0.00, lastPaymentDate=2024-12-02T14:30:34), UserPageVO(id=7, username=哈, phone=123123, address=123123123, meterNo=12313, accountStatus=欠费, electricityUsage=0.00, userType=居民用户, balance=0.00, lastPaymentDate=2024-12-02T14:31:12)])";
        System.out.println(JSONUtil.parse(str));
    }
}