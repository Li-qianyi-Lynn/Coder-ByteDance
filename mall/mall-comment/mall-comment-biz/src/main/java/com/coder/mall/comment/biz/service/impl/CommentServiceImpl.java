package com.coder.mall.comment.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.coder.framework.biz.context.holder.LoginUserContextHolder;
import com.coder.framework.common.constant.DateConstants;
import com.coder.framework.common.exception.BizException;
import com.coder.framework.common.response.PageResponse;
import com.coder.framework.common.response.Response;
import com.coder.framework.common.util.DateUtils;
import com.coder.framework.common.util.JsonUtils;
import com.coder.mall.comment.biz.constant.MQConstants;
import com.coder.mall.comment.biz.domain.dataobject.CommentDO;
import com.coder.mall.comment.biz.domain.mapper.CommentDOMapper;
import com.coder.mall.comment.biz.enums.ResponseCodeEnum;
import com.coder.mall.comment.biz.model.dto.PublishCommentMqDTO;
import com.coder.mall.comment.biz.model.vo.*;
import com.coder.mall.comment.biz.retry.SendMqRetryHelper;
import com.coder.mall.comment.biz.rpc.KeyValueRpcService;
import com.coder.mall.comment.biz.service.CommentService;
import com.coder.mall.kv.dto.req.FindCommentContentReqDTO;
import com.coder.mall.kv.dto.rsp.FindCommentContentRspDTO;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论业务
 */
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private SendMqRetryHelper sendMqRetryHelper;
    @Resource
    private CommentDOMapper commentDOMapper;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 发布评论
     *
     * @param publishCommentReqVO
     * @return
     */
    @Override
    public Response<?> publishComment(PublishCommentReqVO publishCommentReqVO) {
        // 评论正文
        String content = publishCommentReqVO.getContent();
        // 附近图片
        String imageUrl = publishCommentReqVO.getImageUrl();

        // 评论内容和图片不能同时为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl),
                "评论正文和图片不能同时为空");

        // 发布者 ID
        Long creatorId = LoginUserContextHolder.getUserId();

        // 评论 ID
        Long commentId = getCommentId() + 1;

        // 发送 MQ
        // 构建消息体 DTO
        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .productId(publishCommentReqVO.getProductId())
                .content(content)
                .imageUrl(imageUrl)
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .commentId(commentId)
                .build();

        // 发送 MQ (包含重试机制)
        sendMqRetryHelper.asyncSend(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));

        return Response.success();
    }

    /**
     * 评论列表分页查询
     *
     * @param findCommentPageListReqVO
     * @return
     */
    @Override
    public PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO) {
        // 笔记 ID
        Long productId = findCommentPageListReqVO.getProductId();
        // 当前页码
        Integer pageNo = findCommentPageListReqVO.getPageNo();
        // 每页展示一级评论数
        long pageSize = 10;

        // TODO: 先从缓存中查

        // 查询评论总数
        Long count = commentDOMapper.selectCommentTotalByProductId(productId);

        if (Objects.isNull(count)) {
            return PageResponse.success(null, pageNo, pageSize);
        }

        // 分页返参
        List<FindCommentItemRspVO> commentRspVOS = null;

        // 若评论总数大于 0
        if (count > 0) {
            commentRspVOS = Lists.newArrayList();

            // 计算分页查询的偏移量 offset
            long offset = PageResponse.getOffset(pageNo, pageSize);

            // 查询一级评论
            List<CommentDO> oneLevelCommentDOS = commentDOMapper.selectPageList(productId, offset, pageSize);

            // 调用 KV 服务需要的入参
            List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
            // 调用用户服务的入参
            List<Long> userIds = Lists.newArrayList();

            // 将一级评论和二级评论合并到一起
            List<CommentDO> allCommentDOS = Lists.newArrayList();
            CollUtil.addAll(allCommentDOS, oneLevelCommentDOS);

            // 循环提取 RPC 调用需要的入参数据
            allCommentDOS.forEach(commentDO -> {
                // 构建调用 KV 服务批量查询评论内容的入参
                boolean isContentEmpty = commentDO.getIsContentEmpty();
                if (!isContentEmpty) {
                    FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                            .contentId(commentDO.getContentUuid())
                            .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(commentDO.getCreateTime()))
                            .build();
                    findCommentContentReqDTOS.add(findCommentContentReqDTO);
                }

                // 构建调用用户服务批量查询用户信息的入参
                userIds.add(commentDO.getUserId());
            });

            // RPC: 调用 KV 服务，批量获取评论内容
            List<FindCommentContentRspDTO> findCommentContentRspDTOS =
                    keyValueRpcService.batchFindCommentContent(productId, findCommentContentReqDTOS);

            log.warn("findCommentContentRspDTOS:{}", findCommentContentRspDTOS);

            // DTO 集合转 Map, 方便后续拼装数据
            Map<String, String> commentUuidAndContentMap = null;
            if (CollUtil.isNotEmpty(findCommentContentRspDTOS)) {
                commentUuidAndContentMap = findCommentContentRspDTOS.stream()
                        .collect(Collectors.toMap(FindCommentContentRspDTO::getContentId, FindCommentContentRspDTO::getContent));
            }

            // RPC: 调用用户服务，批量获取用户信息（头像、昵称等）TODO
//            List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);

            // DTO 集合转 Map, 方便后续拼装数据
//            Map<Long, FindUserByIdRspDTO> userIdAndDTOMap = null;
//            if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
//                userIdAndDTOMap = findUserByIdRspDTOS.stream()
//                        .collect(Collectors.toMap(FindUserByIdRspDTO::getId, dto -> dto));
//            }

            // DO 转 VO, 组合拼装一二级评论数据
            for (CommentDO commentDO : oneLevelCommentDOS) {
                // 一级评论
                Long userId = commentDO.getUserId();
                FindCommentItemRspVO oneLevelCommentRspVO = FindCommentItemRspVO.builder()
                        .userId(userId)
                        .commentId(commentDO.getId())
                        .imageUrl(commentDO.getImageUrl())
                        .createTime(DateUtils.formatRelativeTime(commentDO.getCreateTime()))
                        .likeTotal(commentDO.getLikeTotal())
                        .childCommentTotal(commentDO.getChildCommentTotal())
                        .build();

                // 用户信息
//                setUserInfo(commentIdAndDOMap, userIdAndDTOMap, userId, oneLevelCommentRspVO);
                // 笔记内容
                setCommentContent(commentUuidAndContentMap, commentDO, oneLevelCommentRspVO);

                commentRspVOS.add(oneLevelCommentRspVO);
            }

        }

        return PageResponse.success(commentRspVOS, pageNo, count, pageSize);
    }

    /**
     * 设置评论内容
     * @param commentUuidAndContentMap
     * @param commentDO1
     * @param firstReplyCommentRspVO
     */
    private static void setCommentContent(Map<String, String> commentUuidAndContentMap, CommentDO commentDO1, FindCommentItemRspVO firstReplyCommentRspVO) {
        if (CollUtil.isNotEmpty(commentUuidAndContentMap)) {
            String contentUuid = commentDO1.getContentUuid();
            if (StringUtils.isNotBlank(contentUuid)) {
                firstReplyCommentRspVO.setContent(commentUuidAndContentMap.get(contentUuid));
            }
        }
    }

    /**
     * 设置用户信息
     * @param commentIdAndDOMap
     * @param userIdAndDTOMap
     * @param userId
     * @param oneLevelCommentRspVO
     */
//    private static void setUserInfo(Map<Long, CommentDO> commentIdAndDOMap, Map<Long, FindUserByIdRspDTO> userIdAndDTOMap, Long userId, FindCommentItemRspVO oneLevelCommentRspVO) {
//        if (CollUtil.isNotEmpty(commentIdAndDOMap)) {
//            FindUserByIdRspDTO findUserByIdRspDTO = userIdAndDTOMap.get(userId);
//            if (Objects.nonNull(findUserByIdRspDTO)) {
//                oneLevelCommentRspVO.setAvatar(findUserByIdRspDTO.getAvatar());
//                oneLevelCommentRspVO.setNickname(findUserByIdRspDTO.getNickName());
//            }
//        }
//    }

    /**
     * 二级评论分页查询
     *
     * @param findChildCommentPageListReqVO
     * @return
     */
    @Override
    public PageResponse<FindChildCommentItemRspVO> findChildCommentPageList(FindChildCommentPageListReqVO findChildCommentPageListReqVO) {
        // 父评论 ID
        Long parentCommentId = findChildCommentPageListReqVO.getParentCommentId();
        // 当前页码
        Integer pageNo = findChildCommentPageListReqVO.getPageNo();
        // 每页展示的二级评论数 (小红书 APP 中是一次查询 6 条)
        long pageSize = 6;

        // TODO: 先从缓存中查（后面补充）

        // 查询一级评论下子评论的总数 (直接查询 t_comment 表的 child_comment_total 字段，提升查询性能, 避免 count(*))
        Long count = commentDOMapper.selectChildCommentTotalById(parentCommentId);

        // 若该一级评论不存在，或者子评论总数为 0
        if (Objects.isNull(count) || count == 0) {
            return PageResponse.success(null, pageNo, 0);
        }

        // 分页返参 VO
        List<FindChildCommentItemRspVO> childCommentRspVOS = Lists.newArrayList();

        // 计算分页查询的偏移量 offset (需要 +1，因为最早回复的二级评论已经被展示了)
        long offset = PageResponse.getOffset(pageNo, pageSize);

        // 分页查询子评论
        List<CommentDO> childCommentDOS = commentDOMapper.selectChildPageList(parentCommentId, offset, pageSize);

        // 调用 KV 服务需要的入参
        List<FindCommentContentReqDTO> findCommentContentReqDTOS = Lists.newArrayList();
        // 调用用户服务的入参
        Set<Long> userIds = Sets.newHashSet();

        // 归属的商品 ID
        Long productId = null;

        // 循环提取 RPC 调用需要的入参数据
        for (CommentDO childCommentDO : childCommentDOS) {
            productId = childCommentDO.getProductId();
            // 构建调用 KV 服务批量查询评论内容的入参
            boolean isContentEmpty = childCommentDO.getIsContentEmpty();
            if (!isContentEmpty) {
                FindCommentContentReqDTO findCommentContentReqDTO = FindCommentContentReqDTO.builder()
                        .contentId(childCommentDO.getContentUuid())
                        .yearMonth(DateConstants.DATE_FORMAT_Y_M.format(childCommentDO.getCreateTime()))
                        .build();
                findCommentContentReqDTOS.add(findCommentContentReqDTO);
            }

            // 构建调用用户服务批量查询用户信息的入参 (包含评论发布者、回复的目标用户)
            userIds.add(childCommentDO.getUserId());

            Long parentId = childCommentDO.getParentId();
            Long replyCommentId = childCommentDO.getReplyCommentId();
            // 若当前评论的 replyCommentId 不等于 parentId，则前端需要展示回复的哪个用户，如  “回复 犬小哈：”
            if (!Objects.equals(parentId, replyCommentId)) {
                userIds.add(childCommentDO.getReplyUserId());
            }
        }

        // RPC: 调用 KV 服务，批量获取评论内容
        List<FindCommentContentRspDTO> findCommentContentRspDTOS =
                keyValueRpcService.batchFindCommentContent(productId, findCommentContentReqDTOS);

        // DTO 集合转 Map, 方便后续拼装数据
        Map<String, String> commentUuidAndContentMap = null;
        if (CollUtil.isNotEmpty(findCommentContentRspDTOS)) {
            commentUuidAndContentMap = findCommentContentRspDTOS.stream()
                    .collect(Collectors.toMap(FindCommentContentRspDTO::getContentId, FindCommentContentRspDTO::getContent));
        }

        // RPC: 调用用户服务，批量获取用户信息（头像、昵称等） TODO
//        List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds.stream().toList());


        // DTO 集合转 Map, 方便后续拼装数据 TODO
//        Map<Long, FindUserByIdRspDTO> userIdAndDTOMap = null;
//        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
//            userIdAndDTOMap = findUserByIdRspDTOS.stream()
//                    .collect(Collectors.toMap(FindUserByIdRspDTO::getId, dto -> dto));
//        }

        // DO 转 VO
        for (CommentDO childCommentDO : childCommentDOS) {
            // 构建 VO 实体类
            Long userId = childCommentDO.getUserId();
            FindChildCommentItemRspVO childCommentRspVO = FindChildCommentItemRspVO.builder()
                    .userId(userId)
                    .commentId(childCommentDO.getId())
                    .imageUrl(childCommentDO.getImageUrl())
                    .createTime(DateUtils.formatRelativeTime(childCommentDO.getCreateTime()))
                    .likeTotal(childCommentDO.getLikeTotal())
                    .build();

            // 填充用户信息(包括评论发布者、回复的用户) TODO
//            if (CollUtil.isNotEmpty(userIdAndDTOMap)) {
//                FindUserByIdRspDTO findUserByIdRspDTO = userIdAndDTOMap.get(userId);
//                // 评论发布者用户信息(头像、昵称)
//                if (Objects.nonNull(findUserByIdRspDTO)) {
//                    childCommentRspVO.setAvatar(findUserByIdRspDTO.getAvatar());
//                    childCommentRspVO.setNickname(findUserByIdRspDTO.getNickName());
//                }
//
//                // 评论回复的哪个
//                Long replyCommentId = childCommentDO.getReplyCommentId();
//                Long parentId = childCommentDO.getParentId();
//
//                if (Objects.nonNull(replyCommentId)
//                        && !Objects.equals(replyCommentId, parentId)) {
//                    Long replyUserId = childCommentDO.getReplyUserId();
//                    FindUserByIdRspDTO replyUser = userIdAndDTOMap.get(replyUserId);
//                    childCommentRspVO.setReplyUserName(replyUser.getNickName());
//                    childCommentRspVO.setReplyUserId(replyUser.getId());
//                }
//            }

            // 评论内容
            if (CollUtil.isNotEmpty(commentUuidAndContentMap)) {
                String contentUuid = childCommentDO.getContentUuid();
                if (StringUtils.isNotBlank(contentUuid)) {
                    childCommentRspVO.setContent(commentUuidAndContentMap.get(contentUuid));
                }
            }

            childCommentRspVOS.add(childCommentRspVO);
        }

        return PageResponse.success(childCommentRspVOS, pageNo, count, pageSize);
    }

    public Long getCommentId() {
        return commentDOMapper.getCommentIdToInsert();
    }

    /**
     * 删除评论
     *
     * @param deleteCommentReqVO
     * @return
     */
    @Override
    public Response<?> deleteComment(DeleteCommentReqVO deleteCommentReqVO) {
        // 被删除的评论 ID
        Long commentId = deleteCommentReqVO.getCommentId();

        // 1. 校验评论是否存在
        CommentDO commentDO = commentDOMapper.selectByPrimaryKey(commentId);

        if (Objects.isNull(commentDO)) {
            throw new BizException(ResponseCodeEnum.COMMENT_NOT_FOUND);
        }

        // 2. 校验是否有权限删除 TODO
//        Long currUserId = LoginUserContextHolder.getUserId();
//        if (!Objects.equals(currUserId, commentDO.getUserId())) {
//            throw new BizException(ResponseCodeEnum.COMMENT_CANT_OPERATE);
//        }

        // 3. 物理删除评论、评论内容
        // 编程式事务，保证多个操作的原子性
        transactionTemplate.execute(status -> {
            try {
                // 删除评论元数据
                commentDOMapper.deleteByPrimaryKey(commentId);

                // 删除评论内容
                keyValueRpcService.deleteCommentContent(commentDO.getProductId(),
                        commentDO.getCreateTime(),
                        commentDO.getContentUuid());

                return null;
            } catch (Exception ex) {
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("", ex);
                throw ex;
            }
        });

        // 4. 删除 Redis 缓存（ZSet 和 String）

        // 5. 发布广播 MQ, 将本地缓存删除

        // 6. 发送 MQ, 异步去更新计数、删除关联评论、热度值等

        return Response.success();
    }


}
