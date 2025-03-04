package com.coder.mall.kv.biz.controller;

import com.coder.framework.biz.operationlog.aspect.ApiOperationLog;
import com.coder.framework.common.response.Response;
import com.coder.mall.kv.biz.service.CommentContentService;
import com.coder.mall.kv.dto.req.BatchAddCommentContentReqDTO;
import com.coder.mall.kv.dto.req.BatchFindCommentContentReqDTO;
import com.coder.mall.kv.dto.req.DeleteCommentContentReqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论内容
 */
@RestController
@RequestMapping("/kv")
@Slf4j
public class CommentContentController {

    @Resource
    private CommentContentService commentContentService;

    /**
     * 批量存储评论内容
     * @param batchAddCommentContentReqDTO
     * @return
     */
    @PostMapping(value = "/comment/content/batchAdd")
    @ApiOperationLog("批量存储评论内容")
    public Response<?> batchAddCommentContent(@Validated @RequestBody BatchAddCommentContentReqDTO batchAddCommentContentReqDTO) {
        return commentContentService.batchAddCommentContent(batchAddCommentContentReqDTO);
    }

    /**
     * 批量查询评论内容
     * @param batchFindCommentContentReqDTO
     * @return
     */
    @PostMapping(value = "/comment/content/batchFind")
    @ApiOperationLog("批量查询评论内容")
    public Response<?> batchFindCommentContent(@Validated @RequestBody BatchFindCommentContentReqDTO batchFindCommentContentReqDTO) {
        return commentContentService.batchFindCommentContent(batchFindCommentContentReqDTO);
    }

    /**
     * 删除评论内容
     * @param deleteCommentContentReqDTO
     * @return
     */
    @PostMapping(value = "/comment/content/delete")
    @ApiOperationLog("删除评论内容")
    public Response<?> deleteCommentContent(@Validated @RequestBody DeleteCommentContentReqDTO deleteCommentContentReqDTO) {
        return commentContentService.deleteCommentContent(deleteCommentContentReqDTO);
    }

}
