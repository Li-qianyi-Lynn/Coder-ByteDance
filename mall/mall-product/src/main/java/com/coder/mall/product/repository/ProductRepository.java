package com.coder.mall.product.repository;

import com.coder.mall.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

        // 根据 dealerId 查询商品，支持分页
    Page<Product> findByDealerId(Long dealerId, Pageable pageable);



    // 根据产品ID查询商品
    Product findByProductId(Long productId);

    // 预留根据类别查询商品的接口
    List<Product> findByCategory(String category);

    List<Product> findByCategoryIn(List<String> categories);

    //Slice<Object> findByDealerId(Long , java.awt.print.Pageable );
}
