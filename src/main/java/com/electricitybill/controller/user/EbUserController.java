package com.electricitybill.controller.user;


import com.electricitybill.entity.R;
import com.electricitybill.entity.dto.PageDTO;
import com.electricitybill.entity.dto.user.UserDTO;
import com.electricitybill.entity.dto.user.UserPageQuery;
import com.electricitybill.entity.vo.user.UserDetailVO;
import com.electricitybill.entity.vo.user.UserPageVO;
import com.electricitybill.entity.vo.user.UserPaymentVO;
import com.electricitybill.service.IEbUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
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
@RequestMapping("/user")
@Api(tags = "用户管理")
public class EbUserController {
    @Resource
    private IEbUserService ebUserService;

    @ApiOperation("分页查询用户")
    @GetMapping("/page")
    public PageDTO<UserPageVO> queryUserPage( UserPageQuery userPageQuery){
        return ebUserService.queryUserPage(userPageQuery);
    }

    @ApiOperation("查询用户详情")
    @GetMapping("/detail/{userId}")
    public UserDetailVO queryUserDetail(@PathVariable @NotNull Long userId){
        return ebUserService.queryUserDetail(userId);
    }

    @ApiOperation("插入用户")
    @PostMapping("/create")
    public R insertUser(@RequestBody @NotNull UserDTO userDTO){
        return ebUserService.insertUser(userDTO);
    }
    @ApiOperation("删除用户")
    @DeleteMapping("/delete")
    public R deleteUser(@RequestParam("userIds")  List<Long> userIds){
        return ebUserService.deleteUser(userIds);
    }
    @ApiOperation("更新用户信息")
    @PutMapping("/edit")
    public R updateUser(@RequestBody @NotNull UserDTO userDTO){
        return ebUserService.updateUser(userDTO);
    }

    @ApiOperation("缴费")
    @PutMapping("/pay")
    //要用RequestBody就用前端传数据就要用data类型,如果要用RequestParams前端传数据就要用params类型
    //@RequestParam适合表单提交，@RequestBody适合json提交 ,@PathVariable适合路径参数 , 不用参数只能是get请求
    public R pay(@RequestParam("userId") Long userId, @RequestParam("money") Double money, @RequestParam("paymentMethod") String paymentMethod){
        return ebUserService.pay(userId,money);
    }

    @ApiOperation("查询用户缴费详情")
    @GetMapping("/bill/{userId}")
    public UserPaymentVO queryUserPayment(@PathVariable @NotNull Long userId){
        return ebUserService.queryUserPayment(userId);
    }
}
