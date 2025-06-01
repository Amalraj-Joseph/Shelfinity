/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {

    @Column(length = 100, nullable = false)    
    private String street;

    @Column(length = 50, nullable = false)
    private String city;

    @Column(length = 50, nullable = false)
    private String state;

    @Column(length = 10, nullable = false)
    private String postalCode;

    @Column(length = 50, nullable = false)
    private String country;

    public Address(){ }

    public Address(String street, String city, String state, String postalCode, String country){
        this.street     = street;
        this.city       = city;
        this.state      = state;
        this.postalCode = postalCode;
        this.country    = country;
    }

    // --- Getters and Setters ---

    public String getStreet(){
        return street;
    }

    public void setStreet(String street){
        this.street = street;
    }

    public String getCity(){
        return city;
    }

    public void setCity(String city){
        this.city = city;
    }

    public String getState(){
        return state;
    }

    public void setState(String state){
        this.state = state;
    }

    public String getPostalCode(){
        return postalCode;
    }

    public void setPostalCode(String postalCode){
        this.postalCode = postalCode;
    }

    public void setCountry(String country){
        this.country = country;
    }

    public String getCountry(){
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(street, address.street) &&
               Objects.equals(city, address.city) &&
               Objects.equals(state, address.state) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, postalCode, country);
    }

    public static class Builder{
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        public Builder street(String street){
            this.street = street;
            return this;
        }

        public Builder city(String city){
            this.city = city;
            return this;
        }

        public Builder state(String state){
            this.state = state;
            return this;
        }

        public Builder postalCode(String postalCode){
            this.postalCode = postalCode;
            return this;
        }

        public Builder country(String country){
            this.country = country;
            return this;
        }
        public Address build() {
            return new Address(
                street, city, state, postalCode, country
            );
        }
    }
    public static Builder builder() {
        return new Builder();
    }
}
