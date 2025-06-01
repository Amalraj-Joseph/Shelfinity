/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.common.annotations.agerange;

import java.time.LocalDate;
import java.time.Period;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgeRangeValidator implements ConstraintValidator<AgeRange, LocalDate> {

    private int minAge;
    private int maxAge;

    @Override
    public void initialize(AgeRange constraintAnnotation) {
        this.minAge = constraintAnnotation.min();
        this.maxAge = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        if (dob == null) return true; // Leave @NotNull to handle this

        LocalDate today = LocalDate.now();
        Period age = Period.between(dob, today);
        int years = age.getYears();

        return years >= minAge && years <= maxAge;
    }
}

