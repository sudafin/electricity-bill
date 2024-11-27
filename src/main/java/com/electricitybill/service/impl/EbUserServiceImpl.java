package com.electricitybill.service.impl;

import com.electricitybill.entity.model.EbUser;
import com.electricitybill.mapper.EbUserMapper;
import com.electricitybill.service.IEbUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author huangdada
 * @since 2024-11-26
 */
@Service
public class EbUserServiceImpl extends ServiceImpl<EbUserMapper, EbUser> implements IEbUserService {

}
