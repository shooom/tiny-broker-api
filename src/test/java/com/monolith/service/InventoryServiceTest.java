package com.monolith.service;

import com.monolith.exception.InsufficientInventoryException;
import com.monolith.repository.InventoryEntity;
import com.monolith.repository.InventoryEntityId;
import com.monolith.repository.InventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static com.monolith.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Nested
    @DisplayName("Get Inventory Tests")
    class GetInventoryTests {

        @Test
        @DisplayName("Should return inventory entity when it exists")
        void shouldReturnInventoryWhenExists() {
            // Arrange
            InventoryEntity expected = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, BigDecimal.TEN, PRICE_NVIDIA);

            doReturn(Optional.of(expected)).when(inventoryRepository).findById(any(InventoryEntityId.class));

            // Act
            InventoryEntity result = inventoryService.getInventory(PORTFOLIO_ID, ISIN_NVIDIA);

            // Assert
            assertNotNull(result);
            assertEquals(PORTFOLIO_ID, result.getPortfolioId());
            assertEquals(ISIN_NVIDIA, result.getIsin());
            assertEquals(BigDecimal.TEN, result.getQuantity());
            assertEquals(PRICE_NVIDIA, result.getAveragePrice());
        }

        @Test
        @DisplayName("Should return null when inventory doesn't exist")
        void shouldReturnNullWhenNotExists() {
            // Arrange
            doReturn(Optional.empty()).when(inventoryRepository).findById(any(InventoryEntityId.class));

            // Act
            InventoryEntity result = inventoryService.getInventory(PORTFOLIO_ID, ISIN_NVIDIA);

            // Assert
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Add To Inventory Tests")
    class AddToInventoryTests {

        @Test
        @DisplayName("Should add quantity to existing inventory")
        void shouldAddToExistingInventory() {
            // Arrange
            BigDecimal initialQuantity = new BigDecimal("5");
            BigDecimal quantityToAdd = new BigDecimal("3");
            BigDecimal expectedQuantity = new BigDecimal("8");
            
            InventoryEntity initialEntity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, initialQuantity, PRICE_NVIDIA);
            InventoryEntity expectedEntity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, expectedQuantity, PRICE_NVIDIA);
            
            doReturn(Optional.of(initialEntity)).when(inventoryRepository).findById(any(InventoryEntityId.class));
            doReturn(expectedEntity).when(inventoryRepository).save(any(InventoryEntity.class));

            // Act
            InventoryEntity result = inventoryService.addToInventory(PORTFOLIO_ID, ISIN_NVIDIA, quantityToAdd, PRICE_NVIDIA);

            // Assert
            assertNotNull(result);
            assertEquals(PORTFOLIO_ID, result.getPortfolioId());
            assertEquals(ISIN_NVIDIA, result.getIsin());
            assertEquals(expectedQuantity, result.getQuantity());
            assertEquals(PRICE_NVIDIA, result.getAveragePrice());
        }

        @Test
        @DisplayName("Should create new inventory when it doesn't exist")
        void shouldCreateNewInventory() {
            // Arrange
            BigDecimal quantityToAdd = new BigDecimal("3");
            InventoryEntity expectedEntity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, quantityToAdd, PRICE_NVIDIA);
            
            doReturn(Optional.empty()).when(inventoryRepository).findById(any(InventoryEntityId.class));
            doReturn(expectedEntity).when(inventoryRepository).save(any(InventoryEntity.class));

            // Act
            InventoryEntity result = inventoryService.addToInventory(PORTFOLIO_ID, ISIN_NVIDIA, quantityToAdd, PRICE_NVIDIA);

            // Assert
            assertNotNull(result);
            assertEquals(PORTFOLIO_ID, result.getPortfolioId());
            assertEquals(ISIN_NVIDIA, result.getIsin());
            assertEquals(quantityToAdd, result.getQuantity());
            assertEquals(PRICE_NVIDIA, result.getAveragePrice());
        }
    }

    @Nested
    @DisplayName("Remove From Inventory Tests")
    class RemoveFromInventoryTests {

        @Test
        @DisplayName("Should remove quantity from existing inventory")
        void shouldRemoveFromInventory() {
            // Arrange
            BigDecimal initialQuantity = new BigDecimal("10");
            BigDecimal quantityToRemove = new BigDecimal("4");
            BigDecimal expectedQuantity = new BigDecimal("6");
            
            InventoryEntity initialEntity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, initialQuantity, PRICE_NVIDIA);
            InventoryEntity expectedEntity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, expectedQuantity, PRICE_NVIDIA);
            
            doReturn(Optional.of(initialEntity)).when(inventoryRepository).findById(any(InventoryEntityId.class));
            doReturn(expectedEntity).when(inventoryRepository).save(any(InventoryEntity.class));

            // Act
            InventoryEntity result = inventoryService.removeFromInventory(PORTFOLIO_ID, ISIN_NVIDIA, quantityToRemove);

            // Assert
            assertNotNull(result);
            assertEquals(PORTFOLIO_ID, result.getPortfolioId());
            assertEquals(ISIN_NVIDIA, result.getIsin());
            assertEquals(expectedQuantity, result.getQuantity());
            assertEquals(PRICE_NVIDIA, result.getAveragePrice());
        }

        @Test
        @DisplayName("Should set inventory quantity & averagePrice as ZERO")
        void shouldDeleteInventoryWhenQuantityBecomesZero() {
            // Arrange
            BigDecimal initialQuantity = new BigDecimal("5");
            BigDecimal quantityToRemove = new BigDecimal("5"); // Will result in zero
            
            InventoryEntity initialEntity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, initialQuantity, PRICE_NVIDIA);
            
            doReturn(Optional.of(initialEntity)).when(inventoryRepository).findById(any(InventoryEntityId.class));

            when(inventoryRepository.save(any(InventoryEntity.class)))
                    .thenAnswer(invocationOnMock -> invocationOnMock.<InventoryEntity>getArgument(0));

            // Act
            InventoryEntity result = inventoryService.removeFromInventory(PORTFOLIO_ID, ISIN_NVIDIA, quantityToRemove);

            // Assert
            assertEquals(BigDecimal.ZERO, result.getQuantity());
            assertEquals(BigDecimal.ZERO, result.getAveragePrice());
        }

        @Test
        @DisplayName("Should throw exception when inventory is insufficient")
        void shouldThrowExceptionWhenInsufficientInventory() {
            // Arrange
            BigDecimal initialQuantity = new BigDecimal("3");
            BigDecimal quantityToRemove = new BigDecimal("5"); // More than available
            
            InventoryEntity initialEntity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, initialQuantity, PRICE_NVIDIA);
            
            doReturn(Optional.of(initialEntity)).when(inventoryRepository).findById(any(InventoryEntityId.class));

            // Act & Assert
            InsufficientInventoryException exception = assertThrows(
                    InsufficientInventoryException.class,
                    () -> inventoryService.removeFromInventory(PORTFOLIO_ID, ISIN_NVIDIA, quantityToRemove)
            );
            
            assertTrue(exception.getMessage().contains("Insufficient inventory"));
            assertTrue(exception.getMessage().contains(PORTFOLIO_ID));
            assertTrue(exception.getMessage().contains(ISIN_NVIDIA));
            
            verify(inventoryRepository, never()).save(any(InventoryEntity.class));
            verify(inventoryRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should throw exception when inventory doesn't exist")
        void shouldThrowExceptionWhenInventoryDoesntExist() {
            // Arrange
            BigDecimal quantityToRemove = new BigDecimal("5");
            
            doReturn(Optional.empty()).when(inventoryRepository).findById(any(InventoryEntityId.class));

            // Act & Assert
            InsufficientInventoryException exception = assertThrows(
                    InsufficientInventoryException.class,
                    () -> inventoryService.removeFromInventory(PORTFOLIO_ID, ISIN_NVIDIA, quantityToRemove)
            );
            
            assertTrue(exception.getMessage().contains("Insufficient inventory"));
            assertTrue(exception.getMessage().contains(PORTFOLIO_ID));
            assertTrue(exception.getMessage().contains(ISIN_NVIDIA));
            
            verify(inventoryRepository, never()).save(any(InventoryEntity.class));
            verify(inventoryRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Verify Sufficient Inventory Tests")
    class GetAndVerifyInventoryTests {

        @Test
        @DisplayName("Should not throw exception when inventory is sufficient")
        void shouldNotThrowExceptionWhenSufficient() {
            // Arrange
            BigDecimal inventoryQuantity = new BigDecimal("10");
            BigDecimal requiredQuantity = new BigDecimal("7");
            
            InventoryEntity entity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, inventoryQuantity, PRICE_NVIDIA);
            
            doReturn(Optional.of(entity)).when(inventoryRepository).findById(any(InventoryEntityId.class));

            // Act & Assert
            assertDoesNotThrow(() -> 
                inventoryService.getAndVerifyInventory(PORTFOLIO_ID, ISIN_NVIDIA, requiredQuantity)
            );
        }

        @Test
        @DisplayName("Should not throw exception when inventory equals required quantity")
        void shouldNotThrowExceptionWhenEqual() {
            // Arrange
            BigDecimal quantity = new BigDecimal("5");
            
            InventoryEntity entity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, quantity, PRICE_NVIDIA);
            
            doReturn(Optional.of(entity)).when(inventoryRepository).findById(any(InventoryEntityId.class));

            // Act & Assert
            assertDoesNotThrow(() -> 
                inventoryService.getAndVerifyInventory(PORTFOLIO_ID, ISIN_NVIDIA, quantity)
            );
        }

        @Test
        @DisplayName("Should throw exception when inventory is insufficient")
        void shouldThrowExceptionWhenInsufficient() {
            // Arrange
            BigDecimal inventoryQuantity = new BigDecimal("3");
            BigDecimal requiredQuantity = new BigDecimal("5");
            
            InventoryEntity entity = new InventoryEntity(PORTFOLIO_ID, ISIN_NVIDIA, inventoryQuantity, PRICE_NVIDIA);
            
            doReturn(Optional.of(entity)).when(inventoryRepository).findById(any(InventoryEntityId.class));

            // Act & Assert
            InsufficientInventoryException exception = assertThrows(
                    InsufficientInventoryException.class,
                    () -> inventoryService.getAndVerifyInventory(PORTFOLIO_ID, ISIN_NVIDIA, requiredQuantity)
            );
            
            assertTrue(exception.getMessage().contains("Insufficient inventory"));
            assertTrue(exception.getMessage().contains(PORTFOLIO_ID));
            assertTrue(exception.getMessage().contains(ISIN_NVIDIA));
        }

        @Test
        @DisplayName("Should throw exception when inventory doesn't exist")
        void shouldThrowExceptionWhenInventoryDoesntExist() {
            // Arrange
            BigDecimal requiredQuantity = new BigDecimal("5");
            
            doReturn(Optional.empty()).when(inventoryRepository).findById(any(InventoryEntityId.class));

            // Act & Assert
            InsufficientInventoryException exception = assertThrows(
                    InsufficientInventoryException.class,
                    () -> inventoryService.getAndVerifyInventory(PORTFOLIO_ID, ISIN_NVIDIA, requiredQuantity)
            );
            
            assertTrue(exception.getMessage().contains("Insufficient inventory"));
            assertTrue(exception.getMessage().contains(PORTFOLIO_ID));
            assertTrue(exception.getMessage().contains(ISIN_NVIDIA));
        }
    }
}