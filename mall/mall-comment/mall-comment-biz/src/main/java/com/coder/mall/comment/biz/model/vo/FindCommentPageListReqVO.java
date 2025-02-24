package com.coder.mall.comment.biz.model.vo;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评论分页数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindCommentPageListReqVO {

    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    @NotNull(message = "页码不能为空")
    private Integer pageNo = 1;
}
