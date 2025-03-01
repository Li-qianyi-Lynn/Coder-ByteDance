package com.coder.mall.order.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DealerGetDTO {
    @NotNull(message = "经销商ID不能为空")
    private Long dealerId;
    @NotNull(message = "订单号不能为空")
    private String orderNo;
    @NotNull(message = "token不能为空")
    private String token;
    @NotNull(message = "用户ID不能为空")
    private Long userId;
  
}
