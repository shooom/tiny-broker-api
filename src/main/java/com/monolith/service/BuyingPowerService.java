package com.monolith.service;

import com.monolith.exception.InsufficientBuyingPowerException;
import com.monolith.repository.BuyingPowerEntity;
import com.monolith.repository.BuyingPowerRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.String.format;
import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Service
public class BuyingPowerService {

    private static final Logger log = LoggerFactory.getLogger(BuyingPowerService.class);

    private final BuyingPowerRepository buyingPowerRepository;
    
    @Value("${trading.initial-buying-power:5000.00}")
    private BigDecimal INITIAL_BUYING_POWER;

    private static final String INSUFFICIENT_BUY_POWER_EXC = "Insufficient buying power for portfolio %s: required %s, available %s";
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public BuyingPowerService(BuyingPowerRepository buyingPowerRepository) {
        this.buyingPowerRepository = buyingPowerRepository;
    }

    /**
     * @param portfolioId the ID of the portfolio
     * @return the current buying power entity
     */
    @Transactional(readOnly = true)
    public BuyingPowerEntity getBuyingPower(@NotBlank String portfolioId) {
        return buyingPowerRepository.findById(portfolioId)
                .orElseGet(() -> buyingPowerRepository.save(
                        new BuyingPowerEntity(portfolioId, standardize(INITIAL_BUYING_POWER))));
    }

    /**
     * @param portfolioId the ID of the portfolio
     * @param amount the amount to deduct
     * @throws IllegalArgumentException if the amount is negative
     * @throws InsufficientBuyingPowerException if there is insufficient buying power
     */
    @Transactional(isolation = SERIALIZABLE, propagation = REQUIRED)
    public void deductBuyingPower(@NotBlank String portfolioId, @NotNull BigDecimal amount) {
        log.info("Trying to deduct buying power from {} portfolio", portfolioId);
        validateInputAmount(amount, "Deduction amount cannot be negative");

        BuyingPowerEntity entity = getBuyingPower(portfolioId);
        BigDecimal currentAmount = entity.getAmount();
        
        if (currentAmount.compareTo(amount) < 0) {
            String exMessage = format(INSUFFICIENT_BUY_POWER_EXC, portfolioId, amount, currentAmount);
            log.warn(exMessage);
            throw new InsufficientBuyingPowerException(exMessage);
        }
        
        BigDecimal updatedAmount = standardize(currentAmount.subtract(amount));
        buyingPowerRepository.save(new BuyingPowerEntity(portfolioId, updatedAmount));
    }

    /**
     * @param portfolioId the ID of the portfolio
     * @param amount the amount to add
     * @throws IllegalArgumentException if the amount is negative
     */
    @Transactional(isolation = SERIALIZABLE, propagation = REQUIRED)
    public void addBuyingPower(@NotBlank String portfolioId, @NotNull BigDecimal amount) {
        log.info("Trying to add buying power to {} portfolio", portfolioId);
        validateInputAmount(amount, "Addition amount cannot be negative");

        BuyingPowerEntity entity = getBuyingPower(portfolioId);
        BigDecimal currentAmount = entity.getAmount();
        BigDecimal updatedAmount = standardize(currentAmount.add(amount));
        buyingPowerRepository.save(new BuyingPowerEntity(portfolioId, updatedAmount));
    }

    /**
     * @param portfolioId the ID of the portfolio
     * @param requiredAmount the amount to check against
     * @throws InsufficientBuyingPowerException if there is insufficient buying power
     */
    @Transactional(readOnly = true)
    public void verifySufficientBuyingPower(@NotBlank String portfolioId, @NotNull BigDecimal requiredAmount) {
        BigDecimal currentBuyingPower = getBuyingPower(portfolioId).getAmount();
        if (currentBuyingPower.compareTo(requiredAmount) < 0) {
            String exMessage = format(INSUFFICIENT_BUY_POWER_EXC, portfolioId, requiredAmount, currentBuyingPower);
            log.warn(exMessage);
            throw new InsufficientBuyingPowerException(exMessage);
        }
    }

    private static void validateInputAmount(BigDecimal amount, String message) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            log.warn(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Standardizes BigDecimal operations by applying consistent scale and rounding mode.
     *
     * @param amount the amount to standardize
     * @return the standardized amount
     */
    private BigDecimal standardize(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, ROUNDING_MODE);
    }
}