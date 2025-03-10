package com.electricitybill.entity.vo.admin;


import com.electricitybill.entity.dto.admin.AdminDTO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LoginVO implements Serializable {
    private static final long serialVersionUID = -3124612657759050173L;
    private AdminDTO adminDTO;
    private String token;
}
