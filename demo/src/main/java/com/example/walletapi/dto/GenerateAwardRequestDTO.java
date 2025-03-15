package com.example.walletapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class GenerateAwardRequestDTO {

    @Schema(description = "Tipo da premiação (GOLDEN, SILVER, BRONZE ou HONOR)", example = "GOLDEN")
    private String type;

    // Getter e Setter
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
