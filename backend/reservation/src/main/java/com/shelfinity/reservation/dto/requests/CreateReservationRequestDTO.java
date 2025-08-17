package com.shelfinity.reservation.dto.requests;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name="CreateReservationRequest")
public class CreateReservationRequestDTO {
    @NotNull public java.util.UUID bookId;
    @NotNull public java.util.UUID userId;
    @NotNull public String reservedFrom; // ISO-8601
    @NotNull public String reservedTo;   // ISO-8601
}
