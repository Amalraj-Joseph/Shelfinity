/*
 * Copyright (c) 2025 Shadow-Codex
 *
 * This source code is licensed under the MIT License.
 * See the LICENSE file in the root directory for more information.
 */
package com.shelfinity.user.dto.requests;

import java.time.LocalDate;
import java.util.Objects;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.shelfinity.common.annotations.agerange.AgeRange;
import com.shelfinity.user.dto.enums.Gender;
import com.shelfinity.user.dto.enums.Salutation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO representing the request payload for updating an existing user's profile.
 * Password is intentionally excluded; it should be updated via a separate flow.
 */
@Schema(description = "DTO representing the request payload for updating a user's profile (excluding static fields)")
public class UpdateUserProfileRequestDTO {

    @Schema(description = "User's salutation (e.g., Mr, Mrs, Ms, Dr)", example = "MR", required = false)
    private Salutation salutation;

    @Schema(description = "User's first name", example = "John", required = false)
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Schema(description = "User's middle name", example = "A.", required = false)
    @Size(max = 50, message = "Middle name must not exceed 50 characters")
    private String middleName;

    @Schema(description = "User's last name", example = "Doe", required = false)
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Schema(description = "User's date of birth", example = "1995-08-20", required = false)
    @NotNull(message = "Date of birth is required")
    @AgeRange(min = 12, max = 130, message = "User must be between 12 and 130 years old")
    private LocalDate dateOfBirth;

    @Schema(description = "User's gender", example = "MALE", required = false)
    private Gender gender;

    @Schema(description = "Street address", example = "123 Main St", required = false)
    @Size(max = 100, message = "Street must not exceed 100 characters")
    private String street;

    @Schema(description = "City name", example = "New York", required = false)
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Schema(description = "State name", example = "New York", required = false)
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @Schema(description = "Postal or ZIP code", example = "10001", required = false)
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String postalCode;

    @Schema(description = "Country name", example = "USA", required = false)
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    /**
     * Default constructor for UpdateUserProfileRequestDTO.
     * Initializes a new instance of UpdateUserProfileRequestDTO with default values.
     */
    public UpdateUserProfileRequestDTO() {
        // Default constructor
    }

    /**
     * Constructs a UpdateUserProfileRequestDTO with the given parameters.
     *
     * @param salutation   The user's salutation (e.g., Mr, Ms, Dr).
     * @param firstName    The user's first name.
     * @param middleName   The user's middle name (optional).
     * @param lastName     The user's last name.
     * @param dateOfBirth  The user's date of birth.
     * @param gender       The user's gender (optional).
     */
    public UpdateUserProfileRequestDTO(Salutation salutation, String firstName, String middleName, String lastName, 
                                       LocalDate dateOfBirth, Gender gender, String street, String city, String state, 
                                       String postalCode, String country) {
        this.salutation = salutation;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    /**
     * Provides a string representation of the UpdateUserProfileRequestDTO object.
     *
     * @return A string containing the UpdateUserProfileRequestDTO information.
     */
    @Override
    public String toString() {
        return "UserDTO{" +
                "salutation='" + salutation + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    /**
     * Compares this UpdateUserProfileRequestDTO object to another object for equality.
     *
     * @param o The object to compare.
     * @return true if the objects are equal, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateUserProfileRequestDTO updateUserRequestDTO = (UpdateUserProfileRequestDTO) o;
        return  Objects.equals(salutation, updateUserRequestDTO.salutation) &&
                Objects.equals(firstName, updateUserRequestDTO.firstName) &&
                Objects.equals(middleName, updateUserRequestDTO.middleName) &&
                Objects.equals(lastName, updateUserRequestDTO.lastName);
    }

    /**
     * Generates a hash code for the UpdateUserProfileRequestDTO object.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(salutation, firstName, middleName, lastName);
    }

    // Getters and Setters

    public Salutation getSalutation(){
        return salutation;
    }

    public void setSalutation(Salutation salutation){
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

    public LocalDate getDateOfBirth(){
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth){
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender(){
        return gender;
    }

    public void setGender(Gender gender){
        this.gender = gender;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
