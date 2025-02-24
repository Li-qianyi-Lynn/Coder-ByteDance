package com.coder.mall.kv.biz.service;

import com.coder.framework.common.response.Response;
import com.coder.mall.kv.dto.req.BatchAddCommentContentReqDTO;
import com.coder.mall.kv.dto.req.BatchFindCommentContentReqDTO;
import com.coder.mall.kv.dto.req.DeleteCommentContentReqDTO;

public interface CommentContentService {


    /**
     * 批量添加评论内容
     * @param batchAddCommentContentReqDTO
     * @return
     */
    Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);

    /**
     * 批量查询评论内容
     * @param batchFindCommentContentReqDTO
     * @return
     */
    Response<?> batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO);

    /**
     * 删除评论内容
     * @param deleteCommentContentReqDTO
     * @return
     */
    Response<?> deleteCommentContent(DeleteCommentContentReqDTO deleteCommentContentReqDTO);

}

