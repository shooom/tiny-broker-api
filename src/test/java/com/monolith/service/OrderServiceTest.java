package com.monolith.service;

import com.monolith.dto.OrderRequest;
import com.monolith.exception.OrderNotFoundException;
import com.monolith.repository.OrderEntity;
import com.monolith.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.monolith.OrderSide.BUY;
import static com.monolith.OrderSide.SELL;
import static com.monolith.OrderStatus.*;
import static com.monolith.utils.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create a BUY order successfully")
        void shouldCreateBuyOrderSuccessfully() {
            // Arrange
            OrderRequest request = createOrderRequest(PORTFOLIO_ID, ISIN_NVIDIA, BUY, DEFAULT_QUANTITY);
            BigDecimal price = PRICE_NVIDIA;

            OrderEntity expectedOrder = new OrderEntity(
                    PORTFOLIO_ID, ISIN_NVIDIA, CREATED, BUY, DEFAULT_QUANTITY, price);
            
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(expectedOrder);

            // Act
            OrderEntity result = orderService.createOrder(request, price);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
            assertThat(result.getIsin()).isEqualTo(ISIN_NVIDIA);
            assertThat(result.getStatus()).isEqualTo(CREATED);
            assertThat(result.getSide()).isEqualTo(BUY);
            assertThat(result.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
            assertThat(result.getPrice()).isEqualTo(price);

            verify(orderRepository).save(any(OrderEntity.class));
        }

        @Test
        @DisplayName("Should create a SELL order successfully")
        void shouldCreateSellOrderSuccessfully() {
            // Arrange
            OrderRequest request = createOrderRequest(PORTFOLIO_ID, ISIN_APPLE, SELL, DEFAULT_QUANTITY);
            BigDecimal price = PRICE_APPLE;

            OrderEntity expectedOrder = new OrderEntity(
                    PORTFOLIO_ID, ISIN_APPLE, CREATED, SELL, DEFAULT_QUANTITY, price);
            
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(expectedOrder);

            // Act
            OrderEntity result = orderService.createOrder(request, price);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
            assertThat(result.getIsin()).isEqualTo(ISIN_APPLE);
            assertThat(result.getStatus()).isEqualTo(CREATED);
            assertThat(result.getSide()).isEqualTo(SELL);
            assertThat(result.getQuantity()).isEqualTo(DEFAULT_QUANTITY);
            assertThat(result.getPrice()).isEqualTo(price);

            verify(orderRepository).save(any(OrderEntity.class));
        }
    }

    @Nested
    @DisplayName("Get Order Tests")
    class GetOrderTests {

        @Test
        @DisplayName("Should retrieve an order successfully")
        void shouldRetrieveOrderSuccessfully() {
            // Arrange
            OrderEntity expectedOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(expectedOrder));

            // Act
            OrderEntity result = orderService.getOrder(ORDER_ID);

            // Assert
            assertThat(result).isEqualTo(expectedOrder);
            
            // Verify
            verify(orderRepository).findById(ORDER_ID);
        }

        @Test
        @DisplayName("Should throw OrderNotFoundException when order doesn't exist")
        void shouldThrowOrderNotFoundExceptionWhenOrderDoesntExist() {
            // Arrange
            Long orderId = 999L;
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            // Act & Assert
            OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, 
                    () -> orderService.getOrder(orderId));
            
            assertThat(exception.getMessage()).isEqualTo("Order not found");
            
            // Verify
            verify(orderRepository).findById(orderId);
        }
    }

    @Nested
    @DisplayName("Get Order For Execution Tests")
    class GetOrderForExecutionTests {

        @Test
        @DisplayName("Should retrieve an order for execution successfully when in CREATED status")
        void shouldRetrieveOrderForExecutionSuccessfullyWhenCreated() {
            // Arrange
            OrderEntity expectedOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(expectedOrder));

            // Act
            OrderEntity result = orderService.getOrderForExecution(ORDER_ID);

            // Assert
            assertThat(result).isEqualTo(expectedOrder);
            
            // Verify
            verify(orderRepository).findById(ORDER_ID);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when order is in EXECUTED status")
        void shouldThrowIllegalArgumentExceptionWhenOrderIsExecuted() {
            // Arrange
            OrderEntity executedOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, EXECUTED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(executedOrder));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                    () -> orderService.getOrderForExecution(ORDER_ID));
            
            assertThat(exception.getMessage())
                    .isEqualTo("Order " + ORDER_ID + " cannot be executed because it's in " + EXECUTED + " status");
            
            // Verify
            verify(orderRepository).findById(ORDER_ID);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when order is in CANCELLED status")
        void shouldThrowIllegalArgumentExceptionWhenOrderIsCancelled() {
            // Arrange
            OrderEntity cancelledOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CANCELLED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(cancelledOrder));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                    () -> orderService.getOrderForExecution(ORDER_ID));
            
            assertThat(exception.getMessage()).isEqualTo("Order " + ORDER_ID + " cannot be executed because it's in " + CANCELLED + " status");
            
            // Verify
            verify(orderRepository).findById(ORDER_ID);
        }

        @Test
        @DisplayName("Should throw OrderNotFoundException when order doesn't exist")
        void shouldThrowOrderNotFoundExceptionWhenOrderDoesntExist() {
            // Arrange
            Long orderId = 999L;
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            // Act & Assert
            OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, 
                    () -> orderService.getOrderForExecution(orderId));
            
            assertThat(exception.getMessage()).isEqualTo("Order not found");
            
            // Verify
            verify(orderRepository).findById(orderId);
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel an order successfully when in CREATED status")
        void shouldCancelOrderSuccessfullyWhenCreated() {
            // Arrange
            OrderEntity createdOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            OrderEntity cancelledOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CANCELLED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(createdOrder));
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(cancelledOrder);

            // Act
            OrderEntity result = orderService.cancelOrder(ORDER_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(CANCELLED);
            
            // Verify
            verify(orderRepository).findById(ORDER_ID);

            ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
            verify(orderRepository).save(orderCaptor.capture());
            OrderEntity capturedOrder = orderCaptor.getValue();
            
            assertThat(capturedOrder.getStatus()).isEqualTo(CANCELLED);
            assertThat(capturedOrder).isSameAs(createdOrder); // Verify it's the same object reference that was modified
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when trying to cancel an EXECUTED order")
        void shouldThrowIllegalArgumentExceptionWhenOrderIsExecuted() {
            // Arrange
            OrderEntity executedOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, EXECUTED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(executedOrder));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                    () -> orderService.cancelOrder(ORDER_ID));
            
            assertThat(exception.getMessage()).isEqualTo("Order " + ORDER_ID + " cannot be cancelled because it's in " + EXECUTED + " status");
            
            // Verify
            verify(orderRepository).findById(ORDER_ID);
            verify(orderRepository, never()).save(any(OrderEntity.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when trying to cancel an already CANCELLED order")
        void shouldThrowIllegalArgumentExceptionWhenOrderIsCancelled() {
            // Arrange
            OrderEntity cancelledOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CANCELLED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(cancelledOrder));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                    () -> orderService.cancelOrder(ORDER_ID));
            
            assertThat(exception.getMessage()).isEqualTo("Order " + ORDER_ID + " cannot be cancelled because it's in " + CANCELLED + " status");
            
            // Verify
            verify(orderRepository).findById(ORDER_ID);
            verify(orderRepository, never()).save(any(OrderEntity.class));
        }

        @Test
        @DisplayName("Should throw OrderNotFoundException when trying to cancel a non-existent order")
        void shouldThrowOrderNotFoundExceptionWhenOrderDoesntExist() {
            // Arrange
            Long orderId = 999L;
            when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

            // Act & Assert
            OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, 
                    () -> orderService.cancelOrder(orderId));
            
            assertThat(exception.getMessage()).isEqualTo("Order not found");
            
            // Verify
            verify(orderRepository).findById(orderId);
            verify(orderRepository, never()).save(any(OrderEntity.class));
        }
    }

    @Nested
    @DisplayName("Finalize Order Execution Tests")
    class FinalizeOrderExecutionTests {

        @Test
        @DisplayName("Should finalize order execution successfully")
        void shouldFinalizeOrderExecutionSuccessfully() {
            // Arrange
            OrderEntity createdOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            OrderEntity executedOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, EXECUTED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(executedOrder);

            // Act
            OrderEntity result = orderService.finalizeOrderExecution(createdOrder);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(EXECUTED);

            ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
            verify(orderRepository).save(orderCaptor.capture());
            OrderEntity capturedOrder = orderCaptor.getValue();
            
            assertThat(capturedOrder.getStatus()).isEqualTo(EXECUTED);
            assertThat(capturedOrder).isSameAs(createdOrder); // Verify it's the same object reference that was modified
        }

        @Test
        @DisplayName("Should finalize order execution for both BUY and SELL orders")
        void shouldFinalizeOrderExecutionForBothBuyAndSellOrders() {
            // Arrange - BUY Order
            OrderEntity createdBuyOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            OrderEntity executedBuyOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, EXECUTED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(executedBuyOrder);

            // Act - BUY Order
            OrderEntity buyResult = orderService.finalizeOrderExecution(createdBuyOrder);

            // Assert - BUY Order
            assertThat(buyResult).isNotNull();
            assertThat(buyResult.getStatus()).isEqualTo(EXECUTED);
            
            // Reset mock for next test
            reset(orderRepository);
            
            // Arrange - SELL Order
            OrderEntity createdSellOrder = createMockOrderEntity(
                    2L, PORTFOLIO_ID, ISIN_APPLE, CREATED, SELL, DEFAULT_QUANTITY, PRICE_APPLE);
            
            OrderEntity executedSellOrder = createMockOrderEntity(
                    2L, PORTFOLIO_ID, ISIN_APPLE, EXECUTED, SELL, DEFAULT_QUANTITY, PRICE_APPLE);
            
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(executedSellOrder);

            // Act - SELL Order
            OrderEntity sellResult = orderService.finalizeOrderExecution(createdSellOrder);

            // Assert - SELL Order
            assertThat(sellResult).isNotNull();
            assertThat(sellResult.getStatus()).isEqualTo(EXECUTED);
        }

        @Test
        @DisplayName("Should handle repository errors when finalizing order execution")
        void shouldHandleRepositoryErrorsWhenFinalizingOrderExecution() {
            // Arrange
            OrderEntity createdOrder = createMockOrderEntity(
                    ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED, BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);
            
            when(orderRepository.save(any(OrderEntity.class))).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> orderService.finalizeOrderExecution(createdOrder));
            
            // Verify
            verify(orderRepository).save(any(OrderEntity.class));
        }
    }
}
