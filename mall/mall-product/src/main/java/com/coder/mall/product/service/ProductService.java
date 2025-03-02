package com.coder.mall.product.service;

import com.coder.mall.product.model.ProductDTO;
import com.coder.mall.product.model.Product;
import com.coder.mall.product.repository.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductDTO getProduct(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        return product.map(this::convertToDTO).orElse(null);
    }

    public List<ProductDTO> listProductsByIds(List<Long> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> listProductsByProperties(Long dealerId, List<String> categories, int page, int pageSize) {
        List<Product> products;
        Pageable pageable = (Pageable) PageRequest.of(page, pageSize);
        if (dealerId != null) {
            products = productRepository.findByDealerId(dealerId, pageable).getContent();
        } else if (categories != null && !categories.isEmpty()) {
            products = productRepository.findByCategoryIn(categories);
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // 通过 dealerId 获取商品列表（分页）
    public List<ProductDTO> listProductsByDealerId(Long dealerId, int page, int pageSize) {
        Pageable pageable = (Pageable) PageRequest.of(page, pageSize);  // 创建分页请求
        Page<Product> productPage = productRepository.findByDealerId(dealerId, pageable);

        return productPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        Product savedProduct = productRepository.save(product);
        return convertToDTO(savedProduct);
    }

    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            BeanUtils.copyProperties(productDTO, product, "productId");
            Product updatedProduct = productRepository.save(product);
            return convertToDTO(updatedProduct);
        }
        return null;
    }

    public boolean deleteProduct(Long productId) {
        try {
            // 检查是否存在该产品
            if (productRepository.existsById(productId)) {
                productRepository.deleteById(productId);
                return true;  // 删除成功
            } else {
                return false;  // 没有找到该产品，删除失败
            }
        } catch (Exception e) {
            // 捕获异常并返回删除失败
            return false;  // 发生异常，删除失败
        }
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        BeanUtils.copyProperties(product, productDTO);
        return productDTO;
    }
}
