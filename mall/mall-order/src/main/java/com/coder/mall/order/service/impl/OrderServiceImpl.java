package com.coder.mall.order.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coder.framework.common.exception.BizException;
import com.coder.mall.order.constant.OrderErrorEnum;
import com.coder.mall.order.constant.OrderStatus;
import com.coder.mall.order.constant.RedisKeyConstant;
import com.coder.mall.order.feign.CartFeignClient;
import com.coder.mall.order.feign.ProductFeignClient;
import com.coder.mall.order.mapper.CustomerOrderMapper;
import com.coder.mall.order.model.dto.DealerGetDTO;
import com.coder.mall.order.model.dto.ListCustomerHistoryDTO;
import com.coder.mall.order.model.dto.ListDealerHistoryDTO;
import com.coder.mall.order.model.dto.OrderCancelDTO;
import com.coder.mall.order.model.dto.OrderCreateDTO;
import com.coder.mall.order.model.dto.OrderGetDTO;
import com.coder.mall.order.model.dto.OrderPlaceDTO;
import com.coder.mall.order.model.dto.OrderUpdateDTO;
import com.coder.mall.order.model.dto.ProductDTO;
import com.coder.mall.order.model.entity.CustomerOrder;
import com.coder.mall.order.model.entity.DealerOrder;
import com.coder.mall.order.model.entity.OrderItem;
import com.coder.mall.order.model.entity.PageResult;
import com.coder.mall.order.model.message.OrderMessage;
import com.coder.mall.order.service.OrderMessageService;
// import com.coder.mall.order.repository.CustomerOrderRepository;
import com.coder.mall.order.service.OrderScheduleService;
import com.coder.mall.order.service.OrderService;
import com.coder.mall.order.utils.OrderNoGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {



    @Autowired  
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private OrderNoGenerator orderNoGenerator;

    @Autowired
    private CustomerOrderMapper customerOrderMapper;

    @Autowired
    private ObjectMapper objectMapper;  // JSON 处理

    private final OrderScheduleService orderScheduleService;

    @Autowired
    private CartFeignClient cartFeignClient;


    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private OrderMessageService orderMessageService;


    @Autowired
    public OrderServiceImpl(OrderScheduleService orderScheduleService) {
        this.orderScheduleService = orderScheduleService;
    }

    private BigDecimal calculateTotalCost(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return items.stream()
                .map(item -> {
                    // 如果没有折扣，默认为1
                    BigDecimal discount = item.getDiscount() != null ? 
                        item.getDiscount() : BigDecimal.ONE;
                    
                    // 计算实际价格 = 单价 * 折扣 * 数量
                    return item.getUnitPrice()
                            .multiply(discount)
                            .multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public CustomerOrder getCustomerOrder(OrderGetDTO orderGetDTO) {
        if (orderGetDTO == null || orderGetDTO.getOrderNo() == null) {
            log.warn("订单查询参数为空");
            throw new BizException(OrderErrorEnum.ORDER_GET_FAILED);
        }
        
        log.info("开始查询订单，参数：{}", orderGetDTO);
        
        try {
            CustomerOrder order = customerOrderMapper.selectByOrderNoAndUserId(
                orderGetDTO.getOrderNo(), 
                orderGetDTO.getUserId()
            );
            
            if (order == null) {
                log.warn("未找到订单，订单号：{}，用户ID：{}", orderGetDTO.getOrderNo(), orderGetDTO.getUserId());
                throw new BizException(OrderErrorEnum.ORDER_NOT_FOUND);  // 使用枚举
            }
            
            // 即使订单已取消也返回
            log.info("查询到订单信息：{}", order);
            return order;
            
        } catch (BizException e) {
            throw e;  // 直接抛出业务异常
        } catch (Exception e) {
            log.error("查询订单异常，参数：{}，错误：{}", orderGetDTO, e.getMessage(), e);
            throw new BizException(OrderErrorEnum.ORDER_GET_FAILED);  // 使用枚举
        }
    }


    @Override
    public PageResult<CustomerOrder> listCustomerHistoryOrders(ListCustomerHistoryDTO listDTO) {
        try {
            log.info("开始查询用户订单列表：{}", listDTO);
            
            // 计算偏移量
            int offset = (listDTO.getPage() - 1) * listDTO.getPageSize();
            
            // 查询总数
            int total = customerOrderMapper.countCustomerOrders(listDTO.getUserId().longValue());
            
            // 查询订单列表
            List<CustomerOrder> orders = customerOrderMapper.selectCustomerOrders(
                listDTO.getUserId(), 
                offset, 
                listDTO.getPageSize()
            );
            
            PageResult<CustomerOrder> result = new PageResult<CustomerOrder>(orders, total, listDTO.getPage(), listDTO.getPageSize());
            result.setTotal(total);
            result.setPageNum(listDTO.getPage());
            result.setPageSize(listDTO.getPageSize());
            result.setContent(orders);
            result.setTotalPages((int) Math.ceil((double) total / listDTO.getPageSize()));

            
            log.info("查询用户订单列表成功，总数：{}", total);
            return result;
            
        } catch (Exception e) {
            log.error("查询用户订单列表失败：{}", listDTO, e);
            throw new BizException(OrderErrorEnum.ORDER_QUERY_FAILED);
        }
    }


    @Override
    public CustomerOrder updateOrder(OrderUpdateDTO orderUpdateDTO) {
        // 获取订单并验证
        CustomerOrder order = getCustomerOrder(new OrderGetDTO());
        if (order == null) {
            throw new BizException(OrderErrorEnum.ORDER_NOT_FOUND);
        }
        
        // 只有CREATED状态的订单才能修改
        if (!OrderStatus.CREATED.equals(order.getStatus())) {
            throw new BizException(OrderErrorEnum.ORDER_STATUS_INVALID);
        }
        
        try {
            // 更新订单项，如果有的话
            if (orderUpdateDTO.getOrderItems() != null && !orderUpdateDTO.getOrderItems().isEmpty()) {
                // 重新计算总价
                BigDecimal newTotalCost = calculateTotalCost(orderUpdateDTO.getOrderItems());
                order.setTotalCost(newTotalCost);
                // 将订单项转换为JSON存储
                order.setOrderItems(objectMapper.writeValueAsString(orderUpdateDTO.getOrderItems()));
            }
            
            // 更新收件人信息
            if (orderUpdateDTO.getRecipientInfo() != null) {
                order.setRecipientInfo(orderUpdateDTO.getRecipientInfo());
            }
            
            // 更新支付信息
            if (orderUpdateDTO.getPaymentInfo() != null) {
                order.setPaymentInfo(objectMapper.writeValueAsString(orderUpdateDTO.getPaymentInfo()));
            }
            
            // 更新额外信息
            if (orderUpdateDTO.getExtraInfo() != null) {
                order.setExtraInfo(orderUpdateDTO.getExtraInfo());
            }
            
            // 更新时间
            order.setUpdateTime(new Date());
            
            // 更新数据库
            customerOrderMapper.updateOrder(order);
            
            // 更新Redis缓存
            String cacheKey = RedisKeyConstant.ORDER_CACHE + order.getOrderNo();
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(order));
            
            return order;
        } catch (Exception e) {
            log.error("更新订单失败: {}", e.getMessage(), e);
            throw new BizException(OrderErrorEnum.ORDER_UPDATE_FAILED);
        }
    }

    @Override
    @Transactional
    public CustomerOrder placeOrder(OrderPlaceDTO orderPlaceDTO) {
        try {
            log.info("开始确认订单: {}", orderPlaceDTO.getOrderNo());
            
            // 1. 参数校验
            if (orderPlaceDTO == null || orderPlaceDTO.getOrderNo() == null || orderPlaceDTO.getUserId() == null) {
                log.warn("订单确认参数为空");
                throw new BizException(OrderErrorEnum.PARAM_ERROR);
            }
            
            // 2. 获取并验证订单
            CustomerOrder order = customerOrderMapper.selectByOrderNoAndUserId(
                orderPlaceDTO.getOrderNo(), 
                orderPlaceDTO.getUserId()
            );
            
            if (order == null) {
                log.warn("订单不存在, orderNo: {}, userId: {}", 
                    orderPlaceDTO.getOrderNo(), orderPlaceDTO.getUserId());
                throw new BizException(OrderErrorEnum.ORDER_NOT_FOUND);
            }
            
            if (!OrderStatus.CREATED.equals(order.getStatus())) {
                log.warn("订单状态不正确, orderNo: {}, status: {}", 
                    orderPlaceDTO.getOrderNo(), order.getStatus());
                throw new BizException(OrderErrorEnum.ORDER_STATUS_INVALID);
            }

            // 3. 从购物车删除商品
            try {
                List<OrderItem> orderItems = objectMapper.readValue(
                    order.getOrderItems(), 
                    new TypeReference<List<OrderItem>>() {}
                );
                
                for (OrderItem item : orderItems) {
                    if (item.getCartItem() != null) {  // 如果是从购物车来的商品
                        cartFeignClient.deleteItemFromCart(
                            order.getUserId(),
                            Long.valueOf(item.getProductId()),
                            orderPlaceDTO.getToken()
                        );
                        log.info("从购物车删除商品: productId={}, userId={}", 
                            item.getProductId(), order.getUserId());
                    }
                }
            } catch (Exception e) {
                log.error("从购物车删除商品失败: {}", e.getMessage(), e);
                throw new BizException(OrderErrorEnum.CART_UPDATE_FAILED);
            }

            // 4. 更新订单状态
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            customerOrderMapper.updateStatus(order.getOrderNo(), order.getStatus());

            // 5. 发送订单消息到结算服务
            try {
                OrderMessage orderMessage = new OrderMessage();
                orderMessage.setOrderNo(order.getOrderNo());
                orderMessage.setUserId(order.getUserId());
                orderMessage.setStatus(order.getStatus());
                orderMessage.setTimestamp(new Date());
                
                // 添加额外数据
                Map<String, Object> extraData = new HashMap<>();
                extraData.put("totalAmount", order.getTotalCost());
                extraData.put("currency", "CNY");
                orderMessage.setExtraData(extraData);

                // 发送消息并等待支付链接返回
                String paymentUrl = orderMessageService.sendOrderPlacedMessageAndWaitForPaymentUrl(orderMessage);
                
                if (paymentUrl == null) {
                    throw new BizException(OrderErrorEnum.PAYMENT_URL_GENERATION_FAILED);
                }
                
                // 将支付链接保存到订单中
                order.setPaymentInfo(objectMapper.writeValueAsString(
                    Collections.singletonMap("paymentUrl", paymentUrl)));
                customerOrderMapper.updatePaymentStatus(order.getOrderNo(), OrderStatus.PENDING_PAYMENT);
                
                log.info("订单已确认并获取支付链接: orderNo={}, paymentUrl={}", 
                    order.getOrderNo(), paymentUrl);
            } catch (Exception e) {
                log.error("发送订单消息失败: {}", e.getMessage(), e);
                throw new BizException(OrderErrorEnum.ORDER_MESSAGE_SEND_FAILED);
            }

            return order;
            
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("确认订单失败: {}", e.getMessage(), e);
            throw new BizException(OrderErrorEnum.ORDER_PLACE_FAILED);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(OrderCancelDTO orderCancelDTO) {
        CustomerOrder order = customerOrderMapper.selectByOrderNoAndUserId(orderCancelDTO.getOrderNo(), orderCancelDTO.getUserId());
        if (order == null) {
            throw new BizException(OrderErrorEnum.ORDER_NOT_FOUND);
        }
        
        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT) && !order.getStatus().equals(OrderStatus.CREATED)) {
            throw new BizException(OrderErrorEnum.ORDER_STATUS_ERROR);
        }
        
        int rows = customerOrderMapper.updateStatus(orderCancelDTO.getOrderNo(), OrderStatus.CANCELLED);
        if (rows != 1) {
            throw new BizException(OrderErrorEnum.ORDER_UPDATE_FAILED);
        }
    }



    @Override
    public DealerOrder getDealerOrder(DealerGetDTO dealerGetDTO) {
        try {
            log.info("开始获取经销商订单, dealerGetDTO: {}", dealerGetDTO);
            
            // 1. 参数校验
            if (dealerGetDTO == null || dealerGetDTO.getDealerId() == null 
                || dealerGetDTO.getOrderNo() == null || dealerGetDTO.getUserId() == null) {
                throw new BizException(OrderErrorEnum.PARAM_ERROR);
            }
            
            // 2. 获取原始订单
            CustomerOrder customerOrder = customerOrderMapper.selectByOrderNoAndUserId(
                dealerGetDTO.getOrderNo(), 
                dealerGetDTO.getUserId()
            );
            
            if (customerOrder == null) {
                log.warn("订单不存在, orderNo: {}", dealerGetDTO.getOrderNo());
                throw new BizException(OrderErrorEnum.ORDER_NOT_FOUND);
            }
            
            // 3. 获取经销商商品信息
            List<ProductDTO> dealerProducts;
            try {
                dealerProducts = productFeignClient.listProductsByDealerId(
                    dealerGetDTO.getDealerId(),
                    1,  // 页码
                    1000  // 每页大小
                );
                

                if (dealerProducts == null) {
                    log.warn("获取经销商商品列表为空, dealerId: {}", dealerGetDTO.getDealerId());
                    return null;
                }
            } catch (Exception e) {
                log.error("调用商品服务失败, dealerId: {}, error: {}", dealerGetDTO.getDealerId(), e.getMessage());
                throw new BizException(OrderErrorEnum.PRODUCT_SERVICE_ERROR);
            }
            
            // 4. 解析订单项
            List<OrderItem> allOrderItems;
            try {
                allOrderItems = objectMapper.readValue(
                    customerOrder.getOrderItems(), 
                    new TypeReference<List<OrderItem>>() {}
                );
            } catch (Exception e) {
                log.error("解析订单项失败, orderItems: {}", customerOrder.getOrderItems(), e);
                throw new BizException(OrderErrorEnum.ORDER_PARSE_ERROR);
            }
            
            // 5. 创建商品ID映射并筛选订单项
            Set<String> dealerProductIds = dealerProducts.stream()
                .map(ProductDTO::getProductId)
                .collect(Collectors.toSet());
                
            List<OrderItem> dealerOrderItems = allOrderItems.stream()
                .filter(item -> dealerProductIds.contains(item.getProductId()))
                .collect(Collectors.toList());
            
            if (dealerOrderItems.isEmpty()) {
                log.info("该订单中没有此经销商的商品, dealerId: {}, orderNo: {}", 
                    dealerGetDTO.getDealerId(), dealerGetDTO.getOrderNo());
                return null;
            }
            
            // 6. 构建经销商订单
            DealerOrder dealerOrder = new DealerOrder();
            dealerOrder.setOrderNo(customerOrder.getOrderNo());
            dealerOrder.setDealerId(dealerGetDTO.getDealerId());
            dealerOrder.setCreateTime(customerOrder.getCreateTime());
            dealerOrder.setStatus(customerOrder.getStatus());
            dealerOrder.setAmount(calculateTotalCost(dealerOrderItems));
            dealerOrder.setOrderItems(dealerOrderItems);
            dealerOrder.setRecipientInfo(customerOrder.getRecipientInfo());
            
            log.info("获取经销商订单成功, dealerOrder: {}", dealerOrder);
            return dealerOrder;
            
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取经销商订单失败: {}", e.getMessage(), e);
            throw new BizException(OrderErrorEnum.ORDER_GET_FAILED);
        }
    }




    public void testOrderData(long userId) {
        // 分页参数
        int page = 0;
        int size = 10;
        int offset = page * size;
        
        // 查询订单数据
        List<CustomerOrder> orders = customerOrderMapper.selectByUserIdWithPage(userId, offset, size);
        int total = customerOrderMapper.countByUserId(userId);
        int totalPages = (int) Math.ceil((double) total / size);
        
        // 记录日志
        log.info("Total elements: {}", total);
        log.info("Total pages: {}", totalPages);
        log.info("Current page number: {}", page);
        
        orders.forEach(order -> {
            log.info("Order ID: {}", order.getOrderId());
            log.info("Total Cost: {}", order.getTotalCost());
            log.info("Status: {}", order.getStatus());
            log.info("Items count: {}", order.getOrderItems().length());
        });
    }

 
    @Override
    @Transactional
    public CustomerOrder createCustomerOrder(OrderCreateDTO orderCreateDTO) {
        try {
            log.info("开始创建订单，入参：{}", orderCreateDTO);  // 添加入参日志
            
            CustomerOrder order = new CustomerOrder();
            order.setOrderNo(orderNoGenerator.generateOrderNo());
            order.setUserId(orderCreateDTO.getUserId());
            order.setTotalCost(calculateTotalCost(orderCreateDTO.getOrderItems()));
            
            order.setStatus(OrderStatus.CREATED);
            
            // 添加收件人信息日志
            log.info("收件人信息：{}", orderCreateDTO.getRecipientInfo());
            order.setRecipientInfo(orderCreateDTO.getRecipientInfo());
            
            // JSON转换日志
            String orderItemsJson = objectMapper.writeValueAsString(orderCreateDTO.getOrderItems());
            log.info("订单项JSON：{}", orderItemsJson);
            order.setOrderItems(orderItemsJson);
            
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            
            // 数据库操作日志
            log.info("开始保存订单到数据库");
            customerOrderMapper.insert(order);
            
            // Redis操作日志
            log.info("开始保存订单到Redis");
            String cacheKey = RedisKeyConstant.ORDER_CACHE + order.getOrderNo();
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(order));
            
            // // 购物车操作日志
            // log.info("开始从购物车删除商品");
            // for (OrderItem item : orderCreateDTO.getOrderItems()) {
            //     cartFeignClient.deleteItemFromCart(
            //         orderCreateDTO.getUserId(),
            //         Long.parseLong(item.getProductId()),
            //         orderCreateDTO.getToken()
            //     );
            // }
            
            orderScheduleService.addOrderToTimeoutQueue(order.getOrderNo(), 30, TimeUnit.MINUTES);
            log.info("订单创建成功：{}", order.getOrderNo());
            
            return order;
        } catch (Exception e) {
            log.error("创建订单失败，具体原因：", e);  // 添加详细错误日志
            throw new BizException(OrderErrorEnum.ORDER_CREATE_FAILED);
        }
    }


    @Override
    public PageResult<DealerOrder> listDealerHistoryOrders(ListDealerHistoryDTO listDealerHistoryDTO) {
        try {
            log.info("开始查询经销商历史订单, 参数: {}", listDealerHistoryDTO);
            
            // 1. 先获取经销商的所有商品（只调用一次商品服务）
            List<ProductDTO> dealerProducts;
            try {
                dealerProducts = productFeignClient.listProductsByDealerId(
                    listDealerHistoryDTO.getDealerId(),
                    1,
                    1000
                );
                
                if (dealerProducts == null || dealerProducts.isEmpty()) {
                    log.warn("经销商没有商品, dealerId: {}", listDealerHistoryDTO.getDealerId());
                    return new PageResult<>(new ArrayList<>(), 
                        listDealerHistoryDTO.getPage(), 
                        listDealerHistoryDTO.getPageSize(), 
                        0);
                }
            } catch (Exception e) {
                log.error("获取经销商商品列表失败, dealerId: {}, error: {}", 
                    listDealerHistoryDTO.getDealerId(), e.getMessage(), e);
                throw new BizException(OrderErrorEnum.PRODUCT_SERVICE_ERROR);
            }

            // 2. 创建商品ID集合，用于快速查找
            Set<String> dealerProductIds = dealerProducts.stream()
                .map(ProductDTO::getProductId)
                .collect(Collectors.toSet());
            
            // 3. 获取时间范围内的所有订单
            int offset = (listDealerHistoryDTO.getPage() - 1) * listDealerHistoryDTO.getPageSize();
            List<CustomerOrder> allOrders = customerOrderMapper.selectHistoryOrders(
                null,
                listDealerHistoryDTO.getStartDate().toString(),
                listDealerHistoryDTO.getEndDate().toString(),
                offset,
                listDealerHistoryDTO.getPageSize()
            );

            // 4. 筛选并转换订单
            List<DealerOrder> dealerOrders = allOrders.stream()
                .map(customerOrder -> {
                    try {
                        // 解析订单项
                        List<OrderItem> orderItems = objectMapper.readValue(
                            customerOrder.getOrderItems(), 
                            new TypeReference<List<OrderItem>>() {}
                        );
                        
                        // 筛选该经销商的订单项
                        List<OrderItem> dealerOrderItems = orderItems.stream()
                            .filter(item -> dealerProductIds.contains(item.getProductId()))
                            .collect(Collectors.toList());
                        
                        // 如果没有该经销商的商品，返回null
                        if (dealerOrderItems.isEmpty()) {
                            return null;
                        }
                        
                        // 创建经销商订单
                        DealerOrder dealerOrder = new DealerOrder();
                        dealerOrder.setOrderNo(customerOrder.getOrderNo());
                        dealerOrder.setDealerId(listDealerHistoryDTO.getDealerId());
                        dealerOrder.setStatus(customerOrder.getStatus());
                        dealerOrder.setCreateTime(customerOrder.getCreateTime());
                        dealerOrder.setAmount(calculateTotalCost(dealerOrderItems));
                        dealerOrder.setOrderItems(dealerOrderItems);
                        dealerOrder.setRecipientInfo(customerOrder.getRecipientInfo());
                        
                        return dealerOrder;
                    } catch (Exception e) {
                        log.error("处理订单失败, orderNo: {}, error: {}", 
                            customerOrder.getOrderNo(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            // 5. 获取总记录数
            int total = customerOrderMapper.countHistoryOrdersForDealer(
                listDealerHistoryDTO.getDealerId(),
                listDealerHistoryDTO.getStartDate().toString(),
                listDealerHistoryDTO.getEndDate().toString()
            );
            
            log.info("查询经销商历史订单完成, dealerId: {}, 订单数量: {}", 
                listDealerHistoryDTO.getDealerId(), dealerOrders.size());
                
            return new PageResult<>(dealerOrders, 
                listDealerHistoryDTO.getPage(), 
                listDealerHistoryDTO.getPageSize(), 
                total);
            
        } catch (Exception e) {
            log.error("获取经销商历史订单失败: {}", e.getMessage(), e);
            throw new BizException(OrderErrorEnum.ORDER_LIST_FAILED);
        }
    }

    @Override
    public  int updateOrderPaymentStatus(String orderNo, OrderStatus paymentStatus) {
        int rows = customerOrderMapper.updatePaymentStatus(orderNo, paymentStatus);
        if (rows == 0) {
            log.warn("订单状态更新失败，未找到订单：{}", orderNo);
        } else if (rows > 1) {
            log.error("订单状态更新异常，影响了多条记录，订单号：{}", orderNo);
        }
        return rows;
    }

}