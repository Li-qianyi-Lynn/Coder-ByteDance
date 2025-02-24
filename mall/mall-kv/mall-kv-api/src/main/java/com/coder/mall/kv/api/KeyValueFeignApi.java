package com.coder.mall.kv.api;

import com.coder.framework.common.response.Response;
import com.coder.mall.kv.constant.ApiConstants;
import com.coder.mall.kv.dto.req.BatchAddCommentContentReqDTO;
import com.coder.mall.kv.dto.req.BatchFindCommentContentReqDTO;
import com.coder.mall.kv.dto.req.DeleteCommentContentReqDTO;
import com.coder.mall.kv.dto.rsp.FindCommentContentRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface KeyValueFeignApi {

    String PREFIX = "/kv";

    @PostMapping(value = PREFIX + "/comment/content/batchAdd")
    Response<?> batchAddCommentContent(@RequestBody BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);

    @PostMapping(value = PREFIX + "/comment/content/batchFind")
    Response<List<FindCommentContentRspDTO>> batchFindCommentContent(@RequestBody BatchFindCommentContentReqDTO batchFindCommentContentReqDTO);

    @PostMapping(value = PREFIX + "/comment/content/delete")
    Response<?> deleteCommentContent(@RequestBody DeleteCommentContentReqDTO deleteCommentContentReqDTO);


}