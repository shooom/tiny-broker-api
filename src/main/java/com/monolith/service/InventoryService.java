package com.monolith.service;

import com.monolith.exception.InsufficientInventoryException;
import com.monolith.exception.ValidationException;
import com.monolith.repository.InventoryEntity;
import com.monolith.repository.InventoryEntityId;
import com.monolith.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.transaction.annotation.Isolation.REPEATABLE_READ;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * @param portfolioId the ID of the portfolio
     * @param isin the ISIN of the security
     * @param quantity the quantity to add
     * @return the updated inventory entity
     * @throws ValidationException if validation fails
     */
    @Transactional(isolation = REPEATABLE_READ)
    public InventoryEntity addToInventory(String portfolioId, String isin, BigDecimal quantity) {
        log.info("Trying to add inventory to {} portfolio", portfolioId);

        validateParametersWithQuantity(portfolioId, isin, quantity);

        InventoryEntity inventory = getInventory(portfolioId, isin);
        BigDecimal currentQuantity = inventory != null ? inventory.getQuantity() : BigDecimal.ZERO;
        BigDecimal updatedQuantity = currentQuantity.add(quantity);

        return inventoryRepository.save(new InventoryEntity(portfolioId, isin, updatedQuantity));
    }

    /**
     * @param portfolioId the ID of the portfolio
     * @param isin the ISIN of the security
     * @param quantity the quantity to remove
     * @return the updated inventory entity or null if inventory was deleted (quantity became zero)
     * @throws InsufficientInventoryException if there is insufficient inventory
     * @throws ValidationException if validation fails
     */
    @Transactional(isolation = REPEATABLE_READ)
    public InventoryEntity removeFromInventory(String portfolioId, String isin, BigDecimal quantity)
            throws InsufficientInventoryException {
        log.info("Trying to remove inventory from {} portfolio", portfolioId);

        InventoryEntity inventory = getAndVerifyInventory(portfolioId, isin, quantity);
        BigDecimal updatedQuantity = inventory.getQuantity().subtract(quantity);

        if (updatedQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // If the updated quantity is zero, delete the inventory entry
            inventoryRepository.deleteById(new InventoryEntityId(portfolioId, isin));
            return null;
        } else {
            return inventoryRepository.save(new InventoryEntity(portfolioId, isin, updatedQuantity));
        }
    }

    /**
     * @param portfolioId the ID of the portfolio
     * @param isin the ISIN of the security
     * @param quantity the quantity to check
     * @throws InsufficientInventoryException if there is insufficient inventory
     * @throws ValidationException if validation fails
     */
    public InventoryEntity getAndVerifyInventory(String portfolioId, String isin, BigDecimal quantity)
            throws InsufficientInventoryException {
        validateParametersWithQuantity(portfolioId, isin, quantity);

        InventoryEntity inventory = getInventory(portfolioId, isin);
        BigDecimal currentQuantity = inventory != null ? inventory.getQuantity() : BigDecimal.ZERO;

        if (currentQuantity.compareTo(quantity) < 0) {
            String exMessage  = format("Insufficient inventory for portfolio %s, ISIN %s: required %s, available %s",
                    portfolioId, isin, quantity, currentQuantity);
            log.warn(exMessage);
            throw new InsufficientInventoryException(exMessage);
        }

        return inventory;
    }

    /**
     * @param portfolioId the ID of the portfolio
     * @param isin the ISIN of the security
     * @return the inventory entity or null if no inventory exists
     * @throws ValidationException if validation fails
     */
    public InventoryEntity getInventory(String portfolioId, String isin) {
        validateBaseParameters(portfolioId, isin);

        InventoryEntityId inventoryId = new InventoryEntityId(portfolioId, isin);
        Optional<InventoryEntity> inventoryOpt = inventoryRepository.findById(inventoryId);

        return inventoryOpt.orElse(null);
    }

    public List<InventoryEntity> getInventoriesByPortfolioId(String portfolioId) {
        return inventoryRepository.findAllByPortfolioId(portfolioId);
    }

    private void validateParametersWithQuantity(String portfolioId, String isin, BigDecimal quantity) {
        validateBaseParameters(portfolioId, isin);
        if (quantity == null) {
            throw new ValidationException("Quantity cannot be null");
        }
    }

    private void validateBaseParameters(String portfolioId, String isin) {
        if (portfolioId == null) {
            throw new ValidationException("Portfolio ID cannot be null");
        }
        if (isin == null) {
            throw new ValidationException("ISIN cannot be null");
        }
    }
}