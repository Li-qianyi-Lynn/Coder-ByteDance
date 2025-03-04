package com.coder.mall.kv.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.util.UUID;

/**
 * 评论内容主键
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PrimaryKeyClass
public class CommentContentPrimaryKey {

    @PrimaryKeyColumn(name = "product_id", type = PrimaryKeyType.PARTITIONED)
    private Long productId; // 分区键1

    @PrimaryKeyColumn(name = "year_month", type = PrimaryKeyType.PARTITIONED)
    private String yearMonth; // 分区键2

    @PrimaryKeyColumn(name = "content_id", type = PrimaryKeyType.CLUSTERED)
    private UUID contentId; // 聚簇键

}