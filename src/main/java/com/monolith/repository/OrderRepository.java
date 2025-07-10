package com.monolith.repository;

import com.monolith.OrderStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * WARNING: Do not modify the interface.
 * The file does not need to be submitted, it is only for your reference.
 */
public interface OrderRepository extends CrudRepository<OrderEntity, Long> {

    List<OrderEntity> findAllByPortfolioIdAndStatus(String portfolioId, OrderStatus status);

}
