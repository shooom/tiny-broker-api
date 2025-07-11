package com.monolith.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyOperationsTest {

    @Test
    @DisplayName("Should standardize decimal values correctly")
    void shouldStandardizeDecimalValuesCorrectly() {
        // Arrange
        BigDecimal input = new BigDecimal("100.123");
        BigDecimal expected = new BigDecimal("100.12");

        // Act
        BigDecimal result = MoneyOperations.standardize (input);

        // Assert
        assertThat(result).isEqualTo(expected);
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should round half up when standardizing values")
    void shouldRoundHalfUpWhenStandardizing() {
        // Arrange
        BigDecimal input = new BigDecimal("100.125");
        BigDecimal expected = new BigDecimal("100.13");

        // Act
        BigDecimal result = MoneyOperations.standardize(input);

        // Assert
        assertThat(result).isEqualTo(expected);
        assertThat(result.scale()).isEqualTo(2);
    }
}