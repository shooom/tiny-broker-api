package com.monolith.api;

import com.monolith.MarketDataService;
import com.monolith.OrderStatus;
import com.monolith.dto.OrderRequest;
import com.monolith.exception.InsufficientBuyingPowerException;
import com.monolith.exception.InsufficientInventoryException;
import com.monolith.exception.OrderNotFoundException;
import com.monolith.repository.OrderEntity;
import com.monolith.service.BuyingPowerService;
import com.monolith.service.InventoryService;
import com.monolith.service.TradingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static com.monolith.OrderSide.BUY;
import static com.monolith.OrderSide.SELL;
import static com.monolith.OrderStatus.CREATED;
import static com.monolith.utils.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TradingController.class)
@AutoConfigureMockMvc
public class TradingControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TradingService tradingService;

    @MockitoBean
    private BuyingPowerService buyingPowerService;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private MarketDataService marketDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should successfully create a BUY order")
        public void whenCreateBuyOrder_thenReturnSuccessResponse() throws Exception {
            // Prepare test data
            OrderRequest request = createOrderRequest(PORTFOLIO_ID, ISIN_NVIDIA, BUY, DEFAULT_QUANTITY);
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED,
                    BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);

            // Configure mock
            when(tradingService.createOrder(any())).thenReturn(mockOrder);

            // Execute and verify
            createOrder(mvc, objectMapper.writeValueAsString(request))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.isin").value(ISIN_NVIDIA))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.side").exists())
                    .andExpect(jsonPath("$.quantity").exists());
        }

        @Test
        @DisplayName("Should successfully create a SELL order")
        public void whenCreateSellOrder_thenReturnSuccessResponse() throws Exception {
            // Prepare test data
            OrderRequest request = createOrderRequest(PORTFOLIO_ID, ISIN_NVIDIA, SELL, DEFAULT_QUANTITY);
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED,
                    SELL, DEFAULT_QUANTITY, PRICE_NVIDIA);

            // Configure mock
            when(tradingService.createOrder(any(OrderRequest.class))).thenReturn(mockOrder);

            // Execute and verify
            createOrder(mvc, objectMapper.writeValueAsString(request))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.isin").value(ISIN_NVIDIA))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.side").exists())
                    .andExpect(jsonPath("$.quantity").exists())
                    .andExpect(jsonPath("$.price").exists());
        }

        @Test
        @DisplayName("Should return error when insufficient buying power")
        public void whenInsufficientBuyingPower_thenReturnError() throws Exception {
            // Prepare test data
            OrderRequest request = createOrderRequest(PORTFOLIO_ID, ISIN_NVIDIA, BUY, new BigDecimal("100.00"));

            // Configure mock
            when(tradingService.createOrder(any(OrderRequest.class)))
                    .thenThrow(new InsufficientBuyingPowerException("Insufficient buying power"));

            // Execute and verify
            createOrder(mvc, objectMapper.writeValueAsString(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Insufficient buying power"));
        }

        @Test
        @DisplayName("Should return error when insufficient inventory")
        public void whenInsufficientInventory_thenReturnError() throws Exception {
            // Prepare test data
            OrderRequest request = createOrderRequest(PORTFOLIO_ID, ISIN_NVIDIA, SELL, DEFAULT_QUANTITY);

            // Configure mock
            when(tradingService.createOrder(any(OrderRequest.class)))
                    .thenThrow(new InsufficientInventoryException("Insufficient inventory"));

            // Execute and verify
            createOrder(mvc, objectMapper.writeValueAsString(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Insufficient inventory"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"US67066G1040", "US0378331005", "US5949181045"})
        @DisplayName("Should handle different ISINs for BUY orders")
        public void whenCreateBuyOrderWithDifferentIsins_thenReturnSuccessResponse(String isin) throws Exception {
            // Prepare test data
            OrderRequest request = createOrderRequest(PORTFOLIO_ID, isin, BUY, DEFAULT_QUANTITY);
            BigDecimal price = getStockPrice(isin);
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, isin, CREATED,
                    BUY, DEFAULT_QUANTITY, price);

            // Configure mock
            when(tradingService.createOrder(any(OrderRequest.class))).thenReturn(mockOrder);

            // Execute and verify
            createOrder(mvc, objectMapper.writeValueAsString(request))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isin").value(isin));
        }
    }

    @Nested
    @DisplayName("Get Order Tests")
    class GetOrderTests {

        @Test
        @DisplayName("Should successfully get a BUY order")
        public void whenGetBuyOrder_thenReturnOrder() throws Exception {
            // Prepare mock order
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED,
                    BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);

            // Configure mock
            when(tradingService.getOrder(ORDER_ID)).thenReturn(mockOrder);

            // Execute and verify
            getOrder(mvc, ORDER_ID)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.isin").value(ISIN_NVIDIA))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.side").exists())
                    .andExpect(jsonPath("$.quantity").exists());
        }

        @Test
        @DisplayName("Should successfully get a SELL order")
        public void whenGetSellOrder_thenReturnOrder() throws Exception {
            // Prepare mock order
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, CREATED,
                    SELL, DEFAULT_QUANTITY, PRICE_NVIDIA);

            // Configure mock
            when(tradingService.getOrder(ORDER_ID)).thenReturn(mockOrder);

            // Execute and verify
            getOrder(mvc, ORDER_ID)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.isin").value(ISIN_NVIDIA))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.side").exists())
                    .andExpect(jsonPath("$.quantity").exists())
                    .andExpect(jsonPath("$.price").exists());
        }

        @Test
        @DisplayName("Should return error when order not found")
        public void whenOrderNotFound_thenReturnError() throws Exception {
            // Configure mock
            when(tradingService.getOrder(anyLong())).thenThrow(new OrderNotFoundException("Order not found"));

            // Execute and verify
            getOrder(mvc, 999L)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Order not found"));
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should successfully cancel a BUY order")
        public void whenCancelBuyOrder_thenReturnCancelledOrder() throws Exception {
            // Prepare mock order
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, OrderStatus.CANCELLED,
                    BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);

            // Configure mock
            when(tradingService.cancelOrder(ORDER_ID)).thenReturn(mockOrder);

            // Execute and verify
            cancelOrder(mvc, ORDER_ID)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.isin").value(ISIN_NVIDIA))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.side").exists())
                    .andExpect(jsonPath("$.quantity").exists());
        }

        @Test
        @DisplayName("Should successfully cancel a SELL order")
        public void whenCancelSellOrder_thenReturnCancelledOrder() throws Exception {
            // Prepare mock order
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, OrderStatus.CANCELLED,
                    SELL, DEFAULT_QUANTITY, PRICE_NVIDIA);

            // Configure mock
            when(tradingService.cancelOrder(ORDER_ID)).thenReturn(mockOrder);

            // Execute and verify
            cancelOrder(mvc, ORDER_ID)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.isin").value(ISIN_NVIDIA))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.side").exists())
                    .andExpect(jsonPath("$.quantity").exists())
                    .andExpect(jsonPath("$.price").exists());
        }

        @Test
        @DisplayName("Should return error when trying to cancel non-existent order")
        public void whenCancelNonExistentOrder_thenReturnError() throws Exception {
            // Configure mock
            when(tradingService.cancelOrder(anyLong())).thenThrow(new OrderNotFoundException("Order not found"));

            // Execute and verify
            cancelOrder(mvc, 999L)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Order not found"));
        }

        @Test
        @DisplayName("Should return error when trying to cancel already cancelled order")
        public void whenCancelAlreadyCancelledOrder_thenReturnError() throws Exception {
            // Configure mock
            when(tradingService.cancelOrder(anyLong())).thenThrow(new IllegalArgumentException("Order cannot be cancelled"));

            // Execute and verify
            cancelOrder(mvc, ORDER_ID)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Order cannot be cancelled"));
        }
    }

    @Nested
    @DisplayName("Execute Order Tests")
    class ExecuteOrderTests {

        @Test
        @DisplayName("Should successfully execute a BUY order")
        public void whenExecuteBuyOrder_thenReturnExecutedOrder() throws Exception {
            // Prepare mock order
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, OrderStatus.EXECUTED,
                    BUY, DEFAULT_QUANTITY, PRICE_NVIDIA);

            // Configure mock
            when(tradingService.executeOrder(ORDER_ID)).thenReturn(mockOrder);

            // Execute and verify
            executeOrder(mvc, ORDER_ID)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ORDER_ID))
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.isin").value(ISIN_NVIDIA))
                    .andExpect(jsonPath("$.status").value("EXECUTED"))
                    .andExpect(jsonPath("$.side").value("BUY"))
                    .andExpect(jsonPath("$.quantity").exists());
        }

        @Test
        @DisplayName("Should successfully execute a SELL order")
        public void whenExecuteSellOrder_thenReturnExecutedOrder() throws Exception {
            // Prepare mock order
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, ISIN_NVIDIA, OrderStatus.EXECUTED,
                    SELL, DEFAULT_QUANTITY, PRICE_NVIDIA);

            // Configure mock
            when(tradingService.executeOrder(ORDER_ID)).thenReturn(mockOrder);

            // Execute and verify
            executeOrder(mvc, ORDER_ID)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ORDER_ID))
                    .andExpect(jsonPath("$.portfolioId").value(PORTFOLIO_ID))
                    .andExpect(jsonPath("$.isin").value(ISIN_NVIDIA))
                    .andExpect(jsonPath("$.status").value("EXECUTED"))
                    .andExpect(jsonPath("$.side").value("SELL"))
                    .andExpect(jsonPath("$.quantity").exists())
                    .andExpect(jsonPath("$.price").exists());
        }
        
        @Test
        @DisplayName("Should return error when order not found")
        public void whenExecuteNonExistentOrder_thenReturnError() throws Exception {
            // Configure mock
            when(tradingService.executeOrder(anyLong())).thenThrow(new OrderNotFoundException("Order not found"));

            // Execute and verify
            executeOrder(mvc, 999L)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Order not found"));
        }
        
        @Test
        @DisplayName("Should return error when order is already executed")
        public void whenExecuteAlreadyExecutedOrder_thenReturnError() throws Exception {
            // Configure mock
            when(tradingService.executeOrder(ORDER_ID))
                    .thenThrow(new IllegalArgumentException("Order cannot be executed because it's in EXECUTED status"));

            // Execute and verify
            executeOrder(mvc, ORDER_ID)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Order cannot be executed because it's in EXECUTED status"));
        }
        
        @Test
        @DisplayName("Should return error when order is already cancelled")
        public void whenExecuteCancelledOrder_thenReturnError() throws Exception {
            // Configure mock
            when(tradingService.executeOrder(ORDER_ID))
                    .thenThrow(new IllegalArgumentException("Order cannot be executed because it's in CANCELLED status"));

            // Execute and verify
            executeOrder(mvc, ORDER_ID)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Order cannot be executed because it's in CANCELLED status"));
        }
        
        @Test
        @DisplayName("Should return error when insufficient buying power for BUY order execution")
        public void whenExecuteBuyOrderWithInsufficientBuyingPower_thenReturnError() throws Exception {
            // Configure mock
            when(tradingService.executeOrder(ORDER_ID))
                    .thenThrow(new InsufficientBuyingPowerException("Insufficient buying power to execute order"));

            // Execute and verify
            executeOrder(mvc, ORDER_ID)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Insufficient buying power to execute order"));
        }
        
        @Test
        @DisplayName("Should return error when insufficient inventory for SELL order execution")
        public void whenExecuteSellOrderWithInsufficientInventory_thenReturnError() throws Exception {
            // Configure mock
            when(tradingService.executeOrder(ORDER_ID))
                    .thenThrow(new InsufficientInventoryException("Insufficient inventory to execute order"));

            // Execute and verify
            executeOrder(mvc, ORDER_ID)
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Insufficient inventory to execute order"));
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"US67066G1040", "US0378331005", "US5949181045"})
        @DisplayName("Should handle execution of orders with different ISINs")
        public void whenExecuteOrderWithDifferentIsins_thenReturnSuccessResponse(String isin) throws Exception {
            // Prepare mock order
            BigDecimal price = getStockPrice(isin);
            OrderEntity mockOrder = createMockOrderEntity(ORDER_ID, PORTFOLIO_ID, isin, OrderStatus.EXECUTED,
                    BUY, DEFAULT_QUANTITY, price);

            // Configure mock
            when(tradingService.executeOrder(ORDER_ID)).thenReturn(mockOrder);

            // Execute and verify
            executeOrder(mvc, ORDER_ID)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isin").value(isin))
                    .andExpect(jsonPath("$.status").value("EXECUTED"));
        }
    }
}