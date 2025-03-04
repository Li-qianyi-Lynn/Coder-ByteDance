package com.coder.mall.comment.biz.domain.mapper;

import com.coder.mall.comment.biz.domain.dataobject.CommentDO;
import com.coder.mall.comment.biz.model.bo.CommentBO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentDOMapper {

    /**
     * 查询要插入的评论 ID
     */
    Long getCommentIdToInsert();

    /**
     * 根据评论 ID 批量查询
     * @param commentIds
     * @return
     */
    List<CommentDO> selectByCommentIds(@Param("commentIds") List<Long> commentIds);

    /**
     * 批量插入评论
     * @param comments
     * @return
     */
    int batchInsert(@Param("comments") List<CommentBO> comments);

    /**
     * 查询评论分页数据
     * @param productId
     * @param offset
     * @param pageSize
     * @return
     */
    List<CommentDO> selectPageList(@Param("productId") Long productId,
                                   @Param("offset") long offset,
                                   @Param("pageSize") long pageSize);

    /**
     * 批量查询二级评论
     * @param commentIds
     * @return
     */
    List<CommentDO> selectTwoLevelCommentByIds(@Param("commentIds") List<Long> commentIds);

    /**
     * 查询一级评论下子评论总数
     * @param commentId
     * @return
     */
    Long selectChildCommentTotalById(Long commentId);

    /**
     * 查询二级评论分页数据
     * @param parentId
     * @param offset
     * @param pageSize
     * @return
     */
    List<CommentDO> selectChildPageList(@Param("parentId") Long parentId,
                                        @Param("offset") long offset,
                                        @Param("pageSize") long pageSize);

    Long selectCommentTotalByProductId(Long productId);

    void updateChildCommentTotal(Long parentId);

    CommentDO selectByPrimaryKey(Long commentId);

    void deleteByPrimaryKey(Long commentId);
}
