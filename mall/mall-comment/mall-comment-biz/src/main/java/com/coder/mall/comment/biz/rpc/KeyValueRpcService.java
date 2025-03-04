package com.coder.mall.comment.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.coder.framework.common.constant.DateConstants;
import com.coder.framework.common.response.Response;
import com.coder.mall.comment.biz.model.bo.CommentBO;
import com.coder.mall.kv.api.KeyValueFeignApi;
import com.coder.mall.kv.dto.req.*;
import com.coder.mall.kv.dto.rsp.FindCommentContentRspDTO;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class KeyValueRpcService {

    @Resource
    private KeyValueFeignApi keyValueFeignApi;

    /**
     * 批量存储评论内容
     * @param commentBOS
     * @return
     */
    public boolean batchSaveCommentContent(List<CommentBO> commentBOS) {
        List<CommentContentReqDTO> comments = Lists.newArrayList();

        // BO 转 DTO
        commentBOS.forEach(commentBO -> {
            CommentContentReqDTO commentContentReqDTO = CommentContentReqDTO.builder()
                    .productId(commentBO.getProductId())
                    .content(commentBO.getContent())
                    .contentId(commentBO.getContentUuid())
                    .yearMonth(commentBO.getCreateTime().format(DateConstants.DATE_FORMAT_Y_M))
                    .build();
            comments.add(commentContentReqDTO);
        });

        // 构建接口入参实体类
        BatchAddCommentContentReqDTO batchAddCommentContentReqDTO = BatchAddCommentContentReqDTO.builder()
                .comments(comments)
                .build();

        // 调用 KV 存储服务
        Response<?> response = keyValueFeignApi.batchAddCommentContent(batchAddCommentContentReqDTO);

        // 若返参中 success 为 false, 则主动抛出异常，以便调用层回滚事务
        if (!response.isSuccess()) {
            throw new RuntimeException("批量保存评论内容失败");
        }

        return true;
    }

    /**
     * 批量查询评论内容
     * @param productId
     * @param findCommentContentReqDTOS
     * @return
     */
    public List<FindCommentContentRspDTO> batchFindCommentContent(Long productId, List<FindCommentContentReqDTO> findCommentContentReqDTOS) {
        BatchFindCommentContentReqDTO bathFindCommentContentReqDTO = BatchFindCommentContentReqDTO.builder()
                .productId(productId)
                .commentContentKeys(findCommentContentReqDTOS)
                .build();

        Response<List<FindCommentContentRspDTO>> response = keyValueFeignApi.batchFindCommentContent(bathFindCommentContentReqDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }

        return response.getData();
    }

    /**
     * 删除评论内容
     * @param productId
     * @param createTime
     * @param contentId
     * @return
     */
    public boolean deleteCommentContent(Long productId, LocalDateTime createTime, String contentId) {
        DeleteCommentContentReqDTO deleteCommentContentReqDTO = DeleteCommentContentReqDTO.builder()
                .productId(productId)
                .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(createTime))
                .contentId(contentId)
                .build();

        // 调用 KV 存储服务
        Response<?> response = keyValueFeignApi.deleteCommentContent(deleteCommentContentReqDTO);

        if (!response.isSuccess()) {
            throw new RuntimeException("删除评论内容失败");
        }

        return true;
    }

}