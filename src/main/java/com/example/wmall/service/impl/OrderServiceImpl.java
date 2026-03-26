package com.example.wmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.wmall.common.Result;
import com.example.wmall.dto.OrderCreateDTO;
import com.example.wmall.dto.OrderItemCreateDTO;
import com.example.wmall.dto.OrderItemVO;
import com.example.wmall.dto.OrderQueryDTO;
import com.example.wmall.dto.OrderVO;
import com.example.wmall.entity.Goods;
import com.example.wmall.entity.Order;
import com.example.wmall.entity.OrderItem;
import com.example.wmall.enums.ResultCodeEnum;
import com.example.wmall.mapper.GoodsMapper;
import com.example.wmall.mapper.OrderItemMapper;
import com.example.wmall.mapper.OrderMapper;
import com.example.wmall.mapper.UserMapper;
import com.example.wmall.service.OrderService;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private GoodsMapper goodsMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderVO> create(OrderCreateDTO dto) {
        log.info("开始创建订单，商品明细数量：{}", dto.getItems().size());
        
        List<OrderItemCreateDTO> items = dto.getItems();
        
        // 1. 校验商品并收集商品信息
        List<Goods> goodsList = new ArrayList<>();
        for (OrderItemCreateDTO item : items) {
            QueryWrapper<Goods> queryGoods = new QueryWrapper<>();
            queryGoods.eq("id", item.getGoodsId())
                      .eq("deleted", 0)
                      .eq("status", 1); // 仅查询上架商品
            Goods goods = goodsMapper.selectOne(queryGoods);
            
            if (goods == null) {
                log.warn("商品不存在或已下架，商品ID：{}", item.getGoodsId());
                return Result.fail("商品不存在或已下架，商品ID：" + item.getGoodsId());
            }
            
            if (goods.getStock() < item.getQuantity()) {
                log.warn("商品库存不足，商品名称：{}，当前库存：{}，购买数量：{}", 
                        goods.getName(), goods.getStock(), item.getQuantity());
                return Result.fail("商品【" + goods.getName() + "】库存不足，当前库存：" + goods.getStock());
            }
            int quantity = item.getQuantity();
            goods.setStock(goods.getStock() - quantity);
            
            goodsList.add(goods);
        }
        
        // 更新商品库存到数据库
        for (Goods goods : goodsList) {
            int updateResult = goodsMapper.updateById(goods);
            if (updateResult <= 0) {
                log.error("库存更新失败，商品ID：{}", goods.getId());
                throw new RuntimeException("库存更新失败");
            }
        }
        log.info("库存更新成功，涉及商品数量：{}", goodsList.size());
        
        // 2. 生成订单号
        String orderNo = generateOrderNo();
        log.info("生成订单号：{}", orderNo);
        
        // 3. 获取当前登录用户ID
        Long userId = getCurrentUserId();
        log.info("当前用户ID：{}", userId);
        
        // 4. 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (int i = 0; i < items.size(); i++) {
            OrderItemCreateDTO itemDTO = items.get(i);
            Goods goods = goodsList.get(i);
            
            // 计算小计金额
            BigDecimal subtotal = goods.getPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            
            // 构建订单明细对象（订单ID稍后设置）
            OrderItem orderItem = new OrderItem();
            orderItem.setGoodsId(goods.getId());
            orderItem.setGoodsName(goods.getName());
            orderItem.setGoodsPrice(goods.getPrice());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setSubtotalAmount(subtotal);
            orderItems.add(orderItem);
        }
        
        log.info("订单总金额计算完成：{}", totalAmount);
        
        // 5. 保存订单主表
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 0-待支付
        order.setRemark(dto.getRemark());
        
        int orderResult = orderMapper.insert(order);
        if (orderResult <= 0) {
            log.error("订单主表保存失败");
            return Result.fail("订单创建失败");
        }
        
        log.info("订单主表保存成功，订单ID：{}", order.getId());
        
        // 6. 保存订单明细表
        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            int itemResult = orderItemMapper.insert(orderItem);
            if (itemResult <= 0) {
                log.error("订单明细保存失败，商品ID：{}", orderItem.getGoodsId());
                throw new RuntimeException("订单明细保存失败");
            }
        }
        
        log.info("订单明细保存成功，明细数量：{}", orderItems.size());
        
        // 7. 组装返回VO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        
        // 组装明细VO列表
        List<OrderItemVO> itemVOList = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem, itemVO);
            itemVOList.add(itemVO);
        }
        orderVO.setItems(itemVOList);
        
        log.info("订单创建成功，订单号：{}，订单ID：{}", orderNo, order.getId());
        
        return Result.success(orderVO);
    }
    
    /**
     * 生成订单号：yyyyMMddHHmmss + 6位随机数
     */
    private String generateOrderNo() {
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(900000) + 100000; // 生成6位随机数
        return timeStr + random;
    }
    
    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("用户未登录");
            throw new RuntimeException("用户未登录");
        }
        
        String username = authentication.getName();
        // 从数据库查询用户ID
        QueryWrapper<com.example.wmall.entity.User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        com.example.wmall.entity.User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            log.error("用户不存在，username：{}", username);
            throw new RuntimeException("用户不存在");
        }
        
        return user.getId();
    }

    @Override
    public Result<List<OrderVO>> getMyOrders(OrderQueryDTO dto) {
        log.info("获取我的订单，查询条件：{}", dto);
        Long userId = getCurrentUserId();
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("user_id", userId)
                    .eq("deleted", 0);

        if (dto.getStatus() != null) {
            if (dto.getStatus() != null){
                queryWrapper.eq("status", dto.getStatus());
            }
            if(dto.getStartTime() != null){
                queryWrapper.ge("create_time", dto.getStartTime());
            }
            if(dto.getEndTime() != null){
                queryWrapper.le("create_time", dto.getEndTime());
            }
        }
        queryWrapper.orderByDesc("create_time");
        List<Order> orderList = orderMapper.selectList(queryWrapper);

        List<OrderVO> orderVOList = new ArrayList<>();
        for (Order order : orderList) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVOList.add(orderVO);
        }
        
        return Result.success(orderVOList);
    }

    @Override
    public Result<OrderVO> getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || order.getDeleted() == 1) {
            log.warn("订单不存在或已删除，订单ID：{}", id);
            return Result.fail("订单不存在");
        }
//TODO: 缺少订单项明细（items）        
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        QueryWrapper<OrderItem> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("order_id",id)
                    .eq("deleted",0);
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        
        List<OrderItemVO> itemOrderVOs=new ArrayList<>();
        for(OrderItem orderItem : orderItems){
            OrderItemVO itemVO=new OrderItemVO();
            BeanUtils.copyProperties(orderItem, itemVO);
            itemOrderVOs.add(itemVO);
        }
        vo.setItems(itemOrderVOs);

        return Result.success(vo);
    }
}
