package com.monolith.repository;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * WARNING: Do not modify the interface.
 * The file does not need to be submitted, it is only for your reference.
 */
public interface InventoryRepository extends CrudRepository<InventoryEntity, InventoryEntityId> {

    List<InventoryEntity> findAllByPortfolioId(String portFolioId);

}
