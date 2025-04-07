package com.monolith.utils;

import com.monolith.OrderSide;
import com.monolith.OrderStatus;
import com.monolith.dto.OrderRequest;
import com.monolith.repository.OrderEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class TestUtils {
    // Constants for test data
    public static final String PORTFOLIO_ID = "portfolio-id-1";
    public static final String ISIN_NVIDIA = "US67066G1040";
    public static final String ISIN_APPLE = "US0378331005";
    public static final String ISIN_MICROSOFT = "US5949181045";
    public static final BigDecimal PRICE_NVIDIA = new BigDecimal("100.00");
    public static final BigDecimal PRICE_APPLE = new BigDecimal("200.00");
    public static final BigDecimal PRICE_MICROSOFT = new BigDecimal("35.50");
    public static final BigDecimal DEFAULT_QUANTITY = new BigDecimal("10.00");
    public static final Long ORDER_ID = 1L;

    /**
     * Creates an OrderRequest object for testing.
     *
     * @param portfolioId The portfolio ID
     * @param isin The ISIN of the security
     * @param side The order side (BUY or SELL)
     * @param quantity The quantity
     * @return A new OrderRequest
     */
    public static OrderRequest createOrderRequest(String portfolioId, String isin, OrderSide side, BigDecimal quantity) {
        OrderRequest request = new OrderRequest();
        request.setPortfolioId(portfolioId);
        request.setIsin(isin);
        request.setSide(side);
        request.setQuantity(quantity);
        return request;
    }

    /**
     * Creates a mock OrderEntity for testing.
     *
     * @param id The order ID
     * @param portfolioId The portfolio ID
     * @param isin The ISIN of the security
     * @param status The order status
     * @param side The order side
     * @param quantity The quantity
     * @param price The price
     * @return A new OrderEntity with the ID set via reflection
     */
    public static OrderEntity createMockOrderEntity(Long id, String portfolioId, String isin, OrderStatus status,
                                           OrderSide side, BigDecimal quantity, BigDecimal price) {
        OrderEntity order = new OrderEntity(portfolioId, isin, status, side, quantity, price);
        // Using reflection to set the ID since it's normally generated
        try {
            java.lang.reflect.Field idField = OrderEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, id);
        } catch (Exception e) {
            // Ignore in test
        }
        return order;
    }

    /**
     * Gets the stock price for a given ISIN.
     *
     * @param isin The ISIN of the security
     * @return The price of the security
     */
    public static BigDecimal getStockPrice(String isin) {
        return switch (isin) {
            case ISIN_NVIDIA -> PRICE_NVIDIA;
            case ISIN_APPLE -> PRICE_APPLE;
            case ISIN_MICROSOFT -> PRICE_MICROSOFT;
            default -> new BigDecimal("100.00");
        };
    }

    /**
     * Creates an order via the API.
     *
     * @param mvc The MockMvc instance
     * @param orderRequest The order request JSON
     * @return The result actions
     * @throws Exception If an error occurs
     */
    public static ResultActions createOrder(MockMvc mvc, String orderRequest) throws Exception {
        MockHttpServletRequestBuilder content = post("/orders")
                .content(orderRequest)
                .contentType("application/json");

        return mvc.perform(content);
    }

    /**
     * Cancels an order via the API.
     *
     * @param mvc The MockMvc instance
     * @param orderId The order ID
     * @return The result actions
     * @throws Exception If an error occurs
     */
    public static ResultActions cancelOrder(MockMvc mvc, long orderId) throws Exception {
        MockHttpServletRequestBuilder content = delete("/orders/" + orderId)
                .contentType("application/json");

        return mvc.perform(content);
    }

    /**
     * Executes an order via the API.
     *
     * @param mvc The MockMvc instance
     * @param orderId The order ID
     * @return The result actions
     * @throws Exception If an error occurs
     */
    public static ResultActions executeOrder(MockMvc mvc, long orderId) throws Exception {
        MockHttpServletRequestBuilder content = put("/orders/" + orderId + "/execute")
                .contentType("application/json");

        return mvc.perform(content);
    }

    /**
     * Gets an order via the API.
     *
     * @param mvc The MockMvc instance
     * @param orderId The order ID
     * @return The result actions
     * @throws Exception If an error occurs
     */
    public static ResultActions getOrder(MockMvc mvc, long orderId) throws Exception {
        MockHttpServletRequestBuilder content = get("/orders/" + orderId)
                .contentType("application/json");

        return mvc.perform(content);
    }
}
