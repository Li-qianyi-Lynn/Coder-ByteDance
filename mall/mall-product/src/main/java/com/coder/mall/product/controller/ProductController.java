package com.coder.mall.product.controller;

import com.coder.mall.product.model.ProductDTO;
import com.coder.mall.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 获取商品详情
    @GetMapping("/{productId}")
    public ProductDTO getProduct(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }

    // 获取商品列表，支持分页
    @GetMapping("/properties")
    public List<ProductDTO> listProductsByProperties(@RequestParam(required = false) Long dealerId,
                                                     @RequestParam(required = false) List<String> categories,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return productService.listProductsByProperties(dealerId, categories, page, pageSize);
    }

    // 通过多个 productId 获取商品信息
    @PostMapping("/listByIds")
    public List<ProductDTO> listProductsByIds(@RequestBody List<Long> productIds) {
        return productService.listProductsByIds(productIds);
    }

    // 创建商品
    @PostMapping("/")
    public ProductDTO createProduct(@RequestBody ProductDTO productDTO) {
        return productService.createProduct(productDTO);
    }

    // 更新商品
    @PutMapping("/{productId}")
    public ProductDTO updateProduct(@PathVariable Long productId, @RequestBody ProductDTO productDTO) {
        return productService.updateProduct(productId, productDTO);
    }

    // 删除商品
    @DeleteMapping("/{productId}")
    public boolean deleteProduct(@PathVariable Long productId) {
        return productService.deleteProduct(productId);
    }

    // 获取商家的商品列表
    @GetMapping("/dealer/{dealerId}")
    public List<ProductDTO> listProductsByDealerId(@PathVariable Long dealerId,
                                                   @RequestParam int page,
                                                   @RequestParam int pageSize) {
        return productService.listProductsByDealerId(dealerId, page, pageSize);
    }
}
