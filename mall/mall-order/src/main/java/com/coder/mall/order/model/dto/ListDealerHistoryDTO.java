package com.coder.mall.order.model.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ListDealerHistoryDTO {
    @NotNull(message = "经销商ID不能为空")
    private Long dealerId;
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startDate;
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endDate;
    @NotNull(message = "页码不能为空")
    private int page;
    @NotNull(message = "每页大小不能为空")
    private int pageSize;
    @NotNull(message = "token不能为空")
    private String token;
}
