package com.example.walletapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requisição para consultas na carteira digital.")
public class WalletQueryRequestDTO {

    @Schema(description = "Tipo da consulta", example = "walletBalance", allowableValues = {"walletBalance", "showToken", "showToken1"})
    private String type;

    @Schema(description = "ID do usuário para consulta", example = "1001")
    private Long idPerson;

    @Schema(description = "ID do token para consulta (necessário somente para 'showToken' e 'showToken1')", example = "e04719ec942d628f2543d3ed3ff445a5935f6937b4ba3a26608287c1feca6f6c")
    private String tokenId;

    // Getters e setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getIdPerson() { return idPerson; }
    public void setIdPerson(Long idPerson) { this.idPerson = idPerson; }

    public String getTokenId() { return tokenId; }
    public void setTokenId(String tokenId) { this.tokenId = tokenId; }
}
