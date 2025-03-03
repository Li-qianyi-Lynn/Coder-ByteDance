package com.coder.mall.cart.service.impl;

import com.coder.mall.cart.model.dto.ProductDTO;
import com.coder.mall.cart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private RestTemplate restTemplate;

    // TODO:需要改成localhost或者线上服务器ip
    private final String getProductsByIdsUrl = "http://api-base-url/listByIds"; // 替换为实际的 URL

    /**
     * 根据多个商品ID获取商品信息列表
     * @param productIds 商品ID列表
     * @return 商品DTO列表
     */
    @Override
    public List<ProductDTO> listProductsByIds(List<Long> productIds) {
        // 创建请求体，包含 productIds
        HttpEntity<List<Long>> request = new HttpEntity<>(productIds);

        // 使用 RestTemplate 发送 POST 请求
        ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(
                getProductsByIdsUrl,
                HttpMethod.POST,
                request,
                (Class<List<ProductDTO>>) (Object) List.class
        );

        // 返回商品列表
        return response.getBody();
    }
}


