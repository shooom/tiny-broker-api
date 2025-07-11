package com.monolith.service;

import com.monolith.exception.InsufficientBuyingPowerException;
import com.monolith.repository.BuyingPowerEntity;
import com.monolith.repository.BuyingPowerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.monolith.utils.TestUtils.PORTFOLIO_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BuyingPowerServiceTest {

    private static final BigDecimal INITIAL_BUYING_POWER = new BigDecimal("5000.00");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("1000.00");
    private static final BigDecimal NEGATIVE_AMOUNT = new BigDecimal("-100.00");
    private static final BigDecimal EXCESSIVE_AMOUNT = new BigDecimal("10000.00");

    @Mock
    private BuyingPowerRepository buyingPowerRepository;

    @InjectMocks
    private BuyingPowerService buyingPowerService;

    @Nested
    @DisplayName("Get Buying Power Tests")
    class GetBuyingPowerTests {

        @Test
        @DisplayName("Should return existing buying power when portfolio exists")
        void shouldReturnExistingBuyingPower() {
            // Arrange
            BuyingPowerEntity entity = new BuyingPowerEntity(PORTFOLIO_ID, INITIAL_BUYING_POWER);
            when(buyingPowerRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(entity));

            // Act
            BuyingPowerEntity result = buyingPowerService.getBuyingPower(PORTFOLIO_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
            assertThat(result.getAmount()).isEqualTo(INITIAL_BUYING_POWER);
        }

        @Test
        @DisplayName("Should initialize buying power when portfolio doesn't exist")
        void shouldInitializeBuyingPowerWhenPortfolioDoesntExist() {
            // Arrange
            ReflectionTestUtils.setField(buyingPowerService, "INITIAL_BUYING_POWER", INITIAL_BUYING_POWER);
            
            when(buyingPowerRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.empty());
            when(buyingPowerRepository.save(any(BuyingPowerEntity.class)))
                    .thenReturn(new BuyingPowerEntity(PORTFOLIO_ID, INITIAL_BUYING_POWER));

            // Act
            BuyingPowerEntity result = buyingPowerService.getBuyingPower(PORTFOLIO_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
            assertThat(result.getAmount()).isEqualTo(INITIAL_BUYING_POWER);
        }
    }

    @Nested
    @DisplayName("Deduct Buying Power Tests")
    class DeductBuyingPowerTests {

        @Test
        @DisplayName("Should successfully deduct buying power when sufficient funds exist")
        void shouldSuccessfullyDeductBuyingPower() {
            // Arrange
            BigDecimal initialAmount = new BigDecimal("2000.00");
            BigDecimal deductAmount = new BigDecimal("500.00");
            BigDecimal expectedAmount = new BigDecimal("1500.00");
            
            BuyingPowerEntity entity = new BuyingPowerEntity(PORTFOLIO_ID, initialAmount);
            when(buyingPowerRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(entity));
            when(buyingPowerRepository.save(any(BuyingPowerEntity.class)))
                    .thenReturn(new BuyingPowerEntity(PORTFOLIO_ID, expectedAmount));

            // Act
            buyingPowerService.deductBuyingPower(PORTFOLIO_ID, deductAmount);

            // Assert & Verify
            ArgumentCaptor<BuyingPowerEntity> entityCaptor = ArgumentCaptor.forClass(BuyingPowerEntity.class);
            verify(buyingPowerRepository).save(entityCaptor.capture());
            
            BuyingPowerEntity capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
            assertThat(capturedEntity.getAmount()).isEqualTo(expectedAmount);
        }

        @Test
        @DisplayName("Should throw InsufficientBuyingPowerException when funds are insufficient")
        void shouldThrowExceptionWhenFundsInsufficient() {
            // Arrange
            BigDecimal initialAmount = new BigDecimal("300.00");
            BigDecimal deductAmount = new BigDecimal("500.00");
            
            BuyingPowerEntity entity = new BuyingPowerEntity(PORTFOLIO_ID, initialAmount);
            when(buyingPowerRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(entity));

            // Act & Assert
            InsufficientBuyingPowerException exception = assertThrows(
                    InsufficientBuyingPowerException.class,
                    () -> buyingPowerService.deductBuyingPower(PORTFOLIO_ID, deductAmount)
            );
            
            assertThat(exception.getMessage()).contains("Insufficient buying power");
            assertThat(exception.getMessage()).contains(PORTFOLIO_ID);
            assertThat(exception.getMessage()).contains(deductAmount.toString());
            assertThat(exception.getMessage()).contains(initialAmount.toString());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when trying to deduct negative amount")
        void shouldThrowExceptionWhenAmountNegative() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> buyingPowerService.deductBuyingPower(PORTFOLIO_ID, NEGATIVE_AMOUNT)
            );
            
            assertThat(exception.getMessage()).isEqualTo("Deduction amount cannot be negative");
        }

        @Test
        @DisplayName("Should initialize new portfolio with deduction if it doesn't exist")
        void shouldInitializePortfolioWithDeduction() {
            // Arrange
            ReflectionTestUtils.setField(buyingPowerService, "INITIAL_BUYING_POWER", INITIAL_BUYING_POWER);
            
            BigDecimal deductAmount = new BigDecimal("1000.00");
            BigDecimal expectedAmount = new BigDecimal("4000.00");
            
            BuyingPowerEntity entity = new BuyingPowerEntity(PORTFOLIO_ID, INITIAL_BUYING_POWER);
            when(buyingPowerRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(entity));
            when(buyingPowerRepository.save(any(BuyingPowerEntity.class)))
                    .thenReturn(new BuyingPowerEntity(PORTFOLIO_ID, expectedAmount));

            // Act
            buyingPowerService.deductBuyingPower(PORTFOLIO_ID, deductAmount);

            // Assert & Verify
            ArgumentCaptor<BuyingPowerEntity> entityCaptor = ArgumentCaptor.forClass(BuyingPowerEntity.class);
            verify(buyingPowerRepository).save(entityCaptor.capture());
            
            BuyingPowerEntity capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
            assertThat(capturedEntity.getAmount()).isEqualTo(expectedAmount);
        }
    }

    @Nested
    @DisplayName("Add Buying Power Tests")
    class AddBuyingPowerTests {

        @Test
        @DisplayName("Should successfully add buying power to existing portfolio")
        void shouldSuccessfullyAddBuyingPower() {
            // Arrange
            BigDecimal initialAmount = new BigDecimal("2000.00");
            BigDecimal addAmount = new BigDecimal("500.00");
            BigDecimal expectedAmount = new BigDecimal("2500.00");
            
            BuyingPowerEntity entity = new BuyingPowerEntity(PORTFOLIO_ID, initialAmount);
            when(buyingPowerRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(entity));
            when(buyingPowerRepository.save(any(BuyingPowerEntity.class)))
                    .thenReturn(new BuyingPowerEntity(PORTFOLIO_ID, expectedAmount));

            // Act
            buyingPowerService.addBuyingPower(PORTFOLIO_ID, addAmount);

            // Assert & Verify
            ArgumentCaptor<BuyingPowerEntity> entityCaptor = ArgumentCaptor.forClass(BuyingPowerEntity.class);
            verify(buyingPowerRepository).save(entityCaptor.capture());
            
            BuyingPowerEntity capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
            assertThat(capturedEntity.getAmount()).isEqualTo(expectedAmount);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when trying to add negative amount")
        void shouldThrowExceptionWhenAmountNegative() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> buyingPowerService.addBuyingPower(PORTFOLIO_ID, NEGATIVE_AMOUNT)
            );
            
            assertThat(exception.getMessage()).isEqualTo("Addition amount cannot be negative");
        }

        @Test
        @DisplayName("Should initialize new portfolio with addition if it doesn't exist")
        void shouldInitializePortfolioWithAddition() {
            // Arrange
            ReflectionTestUtils.setField(buyingPowerService, "INITIAL_BUYING_POWER", INITIAL_BUYING_POWER);
            
            BigDecimal addAmount = new BigDecimal("1000.00");
            BigDecimal expectedAmount = new BigDecimal("6000.00");
            
            BuyingPowerEntity entity = new BuyingPowerEntity(PORTFOLIO_ID, INITIAL_BUYING_POWER);
            when(buyingPowerRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(entity));
            when(buyingPowerRepository.save(any(BuyingPowerEntity.class)))
                    .thenReturn(new BuyingPowerEntity(PORTFOLIO_ID, expectedAmount));

            // Act
            buyingPowerService.addBuyingPower(PORTFOLIO_ID, addAmount);

            // Assert & Verify
            ArgumentCaptor<BuyingPowerEntity> entityCaptor = ArgumentCaptor.forClass(BuyingPowerEntity.class);
            verify(buyingPowerRepository).save(entityCaptor.capture());
            
            BuyingPowerEntity capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getPortfolioId()).isEqualTo(PORTFOLIO_ID);
            assertThat(capturedEntity.getAmount()).isEqualTo(expectedAmount);
        }
    }

    @Nested
    @DisplayName("Verify Sufficient Buying Power Tests")
    class VerifySufficientBuyingPowerTests {

        @Test
        @DisplayName("Should not throw exception when portfolio has sufficient buying power")
        void shouldNotThrowExceptionWhenSufficient() {
            // Arrange
            BigDecimal availableAmount = new BigDecimal("2000.00");
            BigDecimal requiredAmount = new BigDecimal("1500.00");
            
            BuyingPowerEntity entity = new BuyingPowerEntity(PORTFOLIO_ID, availableAmount);
            when(buyingPowerRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(entity));

            // Act & Assert - No exception should be thrown
            buyingPowerService.verifySufficientBuyingPower(PORTFOLIO_ID, requiredAmount);
        }

        @Test
        @DisplayName("Should throw InsufficientBuyingPowerException when portfolio has insufficient buying power")
        void shouldThrowExceptionWhenInsufficient() {
            // Arrange
            BigDecimal availableAmount = new BigDecimal("1000.00");
            BigDecimal requiredAmount = new BigDecimal("1500.00");
            
            BuyingPowerEntity entity = new BuyingPowerEntity(PORTFOLIO_ID, availableAmount);
            when(buyingPowerRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(entity));

            // Act & Assert
            InsufficientBuyingPowerException exception = assertThrows(
                    InsufficientBuyingPowerException.class,
                    () -> buyingPowerService.verifySufficientBuyingPower(PORTFOLIO_ID, requiredAmount)
            );
            
            assertThat(exception.getMessage()).contains("Insufficient buying power");
            assertThat(exception.getMessage()).contains(PORTFOLIO_ID);
            assertThat(exception.getMessage()).contains(requiredAmount.toString());
            assertThat(exception.getMessage()).contains(availableAmount.toString());
        }
    }
}
