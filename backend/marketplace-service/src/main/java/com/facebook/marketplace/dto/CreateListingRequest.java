package com.facebook.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateListingRequest {

    @NotBlank(message = "Tytuł jest wymagany")
    private String title;

    @NotNull(message = "Cena jest wymagana")
    @Positive(message = "Cena musi być większa od zera")
    private BigDecimal price;

    @NotBlank(message = "Kategoria jest wymagana")
    private String category;

    @NotBlank(message = "Stan przedmiotu jest wymagany")
    private String condition;

    private String description;

    // Współrzędne geograficzne dodawane podczas tworzenia ogłoszenia
    @NotNull(message = "Szerokość geograficzna (latitude) jest wymagana")
    private Double latitude;

    @NotNull(message = "Długość geograficzna (longitude) jest wymagana")
    private Double longitude;

    // Gettery i settery wygeneruje Lombok (@Data)
}