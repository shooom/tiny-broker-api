package com.codility.repository;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for managing buying power.
 * The entity is a simple BigDecimal.
 * The key is a String, associated to a unique portfolio ID.
 *
 * WARNING: Do not modify the interface.
 * The file does not need to be submitted, it is only for your reference.
 */
public interface BuyingPowerRepository extends CrudRepository<BuyingPowerEntity, String> {

}

