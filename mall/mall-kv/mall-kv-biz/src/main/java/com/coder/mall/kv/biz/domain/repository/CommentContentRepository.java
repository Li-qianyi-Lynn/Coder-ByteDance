package com.coder.mall.kv.biz.domain.repository;

import com.coder.mall.kv.biz.domain.dataobject.CommentContentDO;
import com.coder.mall.kv.biz.domain.dataobject.CommentContentPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.UUID;

public interface CommentContentRepository extends CassandraRepository<CommentContentDO, CommentContentPrimaryKey> {

    /**
     * 批量查询评论内容
     * @param productId
     * @param yearMonths
     * @param contentIds
     * @return
     */
    List<CommentContentDO> findByPrimaryKeyProductIdAndPrimaryKeyYearMonthInAndPrimaryKeyContentIdIn(
            Long productId, List<String> yearMonths, List<UUID> contentIds
    );

    /**
     * 删除评论正文
     * @param productId
     * @param yearMonth
     * @param contentId
     */
    void deleteByPrimaryKeyProductIdAndPrimaryKeyYearMonthAndPrimaryKeyContentId(Long productId, String yearMonth, UUID contentId);

}