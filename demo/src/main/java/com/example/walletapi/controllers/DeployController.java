package com.example.walletapi.controllers;

import com.example.walletapi.dto.DeployRequestDTO;
import com.example.walletapi.models.AwardEntity;
import com.example.walletapi.models.KeyEntity;
import com.example.walletapi.repositories.AwardRepository;
import com.example.walletapi.repositories.KeyRepository;
import com.example.walletapi.utils.AESUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Tag(name = "Deploy Controller", description = "Realiza deploy das premiações NFT")
@RestController
@RequestMapping("/api/v1/deploy")
public class DeployController {

    @Autowired
    private AwardRepository awardRepository;

    @Autowired
    private KeyRepository keyRepository;

    private static final String ASSET_DIRECTORY = "/home/bigchaindb/";
    private static final String VELLUSCINUM_URL = "http://localhost:9984";

    @Operation(summary = "Realizar deploy de NFTs")
    @PostMapping
    public ResponseEntity<String> deployAwards(@RequestBody DeployRequestDTO deployRequestDTO) {
        String type = deployRequestDTO.getType();

        if (type == null || type.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Erro: Tipo de premiação não informado!");
        }

        String result = deployAwardsByType(type);
        return ResponseEntity.ok(result);
    }

    private String deployAwardsByType(String type) {
        List<AwardEntity> awards = awardRepository.findByType(type);
        if (awards.isEmpty()) {
            return "Nenhum prêmio do tipo " + type + " encontrado no banco de dados.";
        }
    
        int totalDeployed = 0;
    
        for (AwardEntity award : awards) {
            Long idPerson = award.getIdSubscription();
            String assetFilename = ASSET_DIRECTORY + idPerson + "_" + type.replace(" ", "_") + ".asset";
    
            if (!Files.exists(Path.of(assetFilename))) {
                System.err.println("Erro: Asset não encontrado: " + assetFilename);
                continue;
            }
    
            Optional<KeyEntity> userKeys = keyRepository.findByIdPerson(idPerson);
            if (userKeys.isEmpty()) {
                System.err.println("Erro: Chaves do usuário " + idPerson + " não encontradas.");
                continue;
            }
    
            String privateKey = decryptKey(userKeys.get().getPrivateKey());
            String publicKey = decryptKey(userKeys.get().getPublicKey());
    
            if (privateKey == null || publicKey == null) {
                System.err.println("Erro ao descriptografar chaves do usuário " + idPerson);
                continue;
            }
    
            try {
                Files.writeString(Path.of(ASSET_DIRECTORY + idPerson + ".privateKey"), privateKey);
                Files.writeString(Path.of(ASSET_DIRECTORY + idPerson + ".publicKey"), publicKey);
            } catch (Exception e) {
                System.err.println("Erro ao criar arquivos das chaves: " + e.getMessage());
                continue;
            }
    
            String deployCommand = String.format(
                "velluscinum deployNFT %s %s %s %s",
                VELLUSCINUM_URL,
                ASSET_DIRECTORY + idPerson + ".privateKey",
                ASSET_DIRECTORY + idPerson + ".publicKey",
                ASSET_DIRECTORY + idPerson + "_" + type.replace(" ", "_") + ".asset"
            );


    
            System.out.println("Executando comando de deploy: " + deployCommand);
            String deployResult = executeCommand(deployCommand);
    
            if (deployResult.contains("successfully") || deployResult.contains("Created")) {
                totalDeployed++;
                System.out.println("Deploy concluído para " + idPerson);
            } else {
                System.err.println("Erro no deploy para ID " + idPerson + ": " + deployResult);
            }
        }
    
        return "Deploy concluído para " + totalDeployed + " prêmios do tipo " + type + ".";
    }
    

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                output.append("ERRO: ").append(line).append("\n");
            }

            process.waitFor();
        } catch (Exception e) {
            return "Erro ao executar comando: " + e.getMessage();
        }
        return output.toString();
    }

    private String decryptKey(String encryptedKey) {
        try {
            return AESUtil.decrypt(encryptedKey);
        } catch (Exception e) {
            System.err.println("Erro ao descriptografar chave: " + e.getMessage());
            return null;
        }
    }
}



// @GetMapping("/walletBalance/{idPerson}")
// public ResponseEntity<String> walletBalance(@PathVariable Long idPerson) {
//     return ResponseEntity.ok(getWalletBalance(idPerson));
// }

// @GetMapping("/showToken/{tokenId}")
// public ResponseEntity<String> showToken(@PathVariable String tokenId) {
//     return ResponseEntity.ok(getTokenDetails(tokenId));
// }

// @GetMapping("/showToken1/{tokenId}")
// public ResponseEntity<byte[]> showTokenImage(@PathVariable String tokenId) {
//     return getTokenImage(tokenId);
// }
