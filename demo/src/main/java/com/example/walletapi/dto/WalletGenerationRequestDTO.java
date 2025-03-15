package com.example.walletapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class WalletGenerationRequestDTO {

    @Schema(description = "Operação desejada ('generate' ou 'store')", example = "generate")
    private String operation;

    // Getter e Setter
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
}
