package com.example.walletapi.controllers;

import com.example.walletapi.dto.GenerateAwardRequestDTO;
import com.example.walletapi.models.AwardEntity;
import com.example.walletapi.repositories.AwardRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Tag(name = "Award Controller", description = "Gera arquivos de premiação")
@RestController
@RequestMapping("/api/v1")
public class AwardController {

    @Autowired
    private AwardRepository awardRepository;

    private static final String ASSET_DIRECTORY = "/home/bigchaindb/";
    private static final String IMAGE_DIRECTORY = "src/main/resources/images/";

    @Operation(summary = "Gerar prêmios por tipo", description = "Gera arquivos de premiação baseado no tipo informado.")
    @PostMapping("/generate")
    public ResponseEntity<String> generateAwards(@RequestBody GenerateAwardRequestDTO generateAwardRequestDTO) {
        String type = generateAwardRequestDTO.getType();

        if (type == null || type.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Erro: Tipo de premiação não informado!");
        }

        String result = generateAwardsByType(type);
        return ResponseEntity.ok(result);
    }

    private String generateAwardsByType(String type) {
        List<AwardEntity> awards = awardRepository.findByType(type);

        if (awards.isEmpty()) {
            return "Nenhum prêmio do tipo " + type + " encontrado no banco de dados.";
        }

        String base64Image = loadBase64Image(type);

        for (AwardEntity award : awards) {
            Long idPerson = award.getIdSubscription();
            String awardDate = award.getAwardDate().toString();
            String assetFilename = ASSET_DIRECTORY + idPerson + type.replace(" ", "_") + ".asset";

            String assetContent = String.format(
                "{\"metadata\":[{\"owner\":\"%s\",\"value_eur\":\"null\"}],\"asset\":[{\"year\":\"%s\",\"type\":\"%s\",\"image\":\"%s\"}]}",
                idPerson, awardDate, type, base64Image
            );

            try {
                Files.writeString(Path.of(assetFilename), assetContent);
                System.out.println("Asset salvo em: " + assetFilename);
            } catch (IOException e) {
                System.err.println("Erro ao salvar o arquivo de asset: " + e.getMessage());
            }
        }
        return "Geração de prêmios do tipo " + type + " concluída!";
    }

    private String loadBase64Image(String type) {
        String filePath = IMAGE_DIRECTORY;

        switch (type.toUpperCase()) {
            case "GOLDEN":
                filePath += "gold.txt";
                break;
            case "SILVER":
                filePath += "silver.txt";
                break;
            case "BRONZE":
                filePath += "bronze.txt";
                break;
            case "HONORABLE MENTION":
            case "HONOR":
                filePath += "honor.txt";
                break;
            default:
                System.err.println("Tipo de premiação inválido: " + type);
                return "";
        }

        try {
            return Files.readString(Path.of(filePath)).trim();
        } catch (IOException e) {
            System.err.println("Erro ao carregar a imagem Base64 para " + type + ": " + e.getMessage());
            return "";
        }
    }
}
