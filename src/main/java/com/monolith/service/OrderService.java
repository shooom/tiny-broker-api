package com.monolith.service;

import com.monolith.dto.OrderRequest;
import com.monolith.exception.OrderNotFoundException;
import com.monolith.repository.OrderEntity;
import com.monolith.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.monolith.OrderStatus.*;

@Service
public class OrderService {

    public static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Creates an order with the given parameters.
     * 
     * @param request the order request
     * @param price the price of the security
     * @return the created order entity
     */
    @Transactional
    public OrderEntity createOrder(OrderRequest request, BigDecimal price) {
        OrderEntity order = new OrderEntity(
                request.getPortfolioId(),
                request.getIsin(),
                CREATED,
                request.getSide(),
                request.getQuantity(),
                price);

        return orderRepository.save(order);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId the ID of the order to retrieve
     * @return the order entity
     * @throws OrderNotFoundException if the order doesn't exist
     */
    public OrderEntity getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
    }

    /**
     * Retrieves an order by its ID and verifies it's in CREATED status.
     *
     * @param orderId the ID of the order to retrieve
     * @return the order entity
     * @throws OrderNotFoundException if the order doesn't exist
     * @throws IllegalArgumentException if the order is not in CREATED status
     */
    @Transactional
    public OrderEntity getOrderForExecution(Long orderId) {
        OrderEntity order = getOrder(orderId);

        if (order.getStatus() != CREATED) {
            String exMessage = "Order " + orderId + " cannot be executed because it's in " + order.getStatus() + " status";
            log.warn(exMessage);
            throw new IllegalArgumentException(exMessage);
        }

        return order;
    }

    /**
     * @param orderId the ID of the order to cancel
     * @return the updated order entity
     * @throws OrderNotFoundException if the order doesn't exist
     * @throws IllegalArgumentException if the order cannot be canceled
     */
    @Transactional
    public OrderEntity cancelOrder(Long orderId) {
        OrderEntity order = getOrder(orderId);

        if (order.getStatus() != CREATED) {
            String exMessage = "Order " + orderId + " cannot be cancelled because it's in " + order.getStatus() + " status";
            log.warn(exMessage);
            throw new IllegalArgumentException(exMessage);
        }

        order.setStatus(CANCELLED);
        return orderRepository.save(order);
    }

    /**
     * Finalizes order execution by setting its status to EXECUTED.
     *
     * @param order the order to finalize
     * @return the updated order entity
     */
    @Transactional
    public OrderEntity finalizeOrderExecution(OrderEntity order) {
        log.info("Trying to finalize {} order", order.getId());
        order.setStatus(EXECUTED);
        return orderRepository.save(order);
    }
}
