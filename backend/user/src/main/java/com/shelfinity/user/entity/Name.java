/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.entity;

import java.util.Objects;

import com.shelfinity.user.dto.enums.Salutation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Name {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Salutation salutation;

    @Column(length = 50, nullable = false)
    private String firstName;

    @Column(length = 50)
    private String middleName;

    @Column(length = 50, nullable = false)
    private String lastName;

    public Name() {
    }

    public Name(Salutation salutation, String firstName, String middleName, String lastName) {
        this.salutation = salutation;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    // --- Getters and Setters ---
    public Salutation getSalutation() {
        return salutation;
    }

    public void setSalutation(Salutation salutation) {
        this.salutation = salutation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Name name)) {
            return false;
        }
        return Objects.equals(salutation, name.salutation)
                && Objects.equals(firstName, name.firstName)
                && Objects.equals(middleName, name.middleName)
                && Objects.equals(lastName, name.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salutation, firstName, middleName, lastName);
    }

    public static class Builder {

        private Salutation salutation;
        private String firstName;
        private String middleName;
        private String lastName;

        public Builder salutation(Salutation salutation) {
            this.salutation = salutation;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Name build() {
            return new Name(
                    salutation, firstName, middleName, lastName
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
