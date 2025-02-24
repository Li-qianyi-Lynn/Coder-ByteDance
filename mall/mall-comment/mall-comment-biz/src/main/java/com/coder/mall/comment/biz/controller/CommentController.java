package com.coder.mall.comment.biz.controller;

import com.coder.framework.common.response.PageResponse;
import com.coder.framework.common.response.Response;
import com.coder.framework.biz.operationlog.aspect.ApiOperationLog;
import com.coder.mall.comment.biz.model.vo.*;
import com.coder.mall.comment.biz.service.CommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 发布评论
     * @param publishCommentReqVO
     * @return
     */
    @PostMapping("/publish")
    @ApiOperationLog("发布评论")
    public Response<?> publishComment(@Validated @RequestBody PublishCommentReqVO publishCommentReqVO) {
        return commentService.publishComment(publishCommentReqVO);
    }

    /**
     * 评论分页查询
     * @param findCommentPageListReqVO
     * @return
     */
    @PostMapping("/list")
    @ApiOperationLog("评论分页查询")
    public PageResponse<FindCommentItemRspVO> findCommentPageList(@Validated @RequestBody FindCommentPageListReqVO findCommentPageListReqVO) {
        return commentService.findCommentPageList(findCommentPageListReqVO);
    }

    /**
     * 二级评论分页查询
     * @param findChildCommentPageListReqVO
     * @return
     */
    @PostMapping("/child/list")
    @ApiOperationLog("二级评论分页查询")
    public PageResponse<FindChildCommentItemRspVO> findChildCommentPageList(@Validated @RequestBody FindChildCommentPageListReqVO findChildCommentPageListReqVO) {
        return commentService.findChildCommentPageList(findChildCommentPageListReqVO);
    }

    /**
     * 删除评论
     * @param deleteCommentReqVO
     * @return
     */
    @PostMapping("/delete")
    @ApiOperationLog("删除评论")
    public Response<?> deleteComment(@Validated @RequestBody DeleteCommentReqVO deleteCommentReqVO) {
        return commentService.deleteComment(deleteCommentReqVO);
    }

}
