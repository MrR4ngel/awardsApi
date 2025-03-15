package com.example.walletapi.controllers;

import com.example.walletapi.dto.WalletGenerationRequestDTO;
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

import java.io.*;
import java.nio.file.Files;
import java.util.List;

@Tag(name = "Wallet Generation Controller", description = "Geração e armazenamento de carteiras digitais")
@RestController
@RequestMapping("/api/v1/wallet")
public class WalletControllerGeneration {

    @Autowired
    private AwardRepository awardRepository;

    @Autowired
    private KeyRepository keyRepository;

    private static final String KEYS_DIR = "/home/bigchaindb/";

    @Operation(summary = "Gerenciar carteiras digitais", description = "Realiza operações de geração ou armazenamento das carteiras.")
    @PostMapping("/generate")
    public ResponseEntity<String> manageWallets(@RequestBody WalletGenerationRequestDTO walletGenerationRequestDTO) {

        String operation = walletGenerationRequestDTO.getOperation();

        if (operation == null || operation.isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: Operação não informada! Utilize 'generate' ou 'store'.");
        }

        return switch (operation) {
            case "generate" -> ResponseEntity.ok(generateWallets());
            case "store" -> ResponseEntity.ok(storeWallets());
            default -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erro: Operação inválida. Use 'generate' para criar carteiras ou 'store' para armazená-las.");
        };
    }

    private String generateWallets() {
        int totalCreated = 0;
        List<AwardEntity> awards = awardRepository.findAll();

        for (AwardEntity award : awards) {
            String userId = String.valueOf(award.getIdSubscription());

            if (keyRepository.findByIdPerson(Long.parseLong(userId)).isPresent()) {
                continue;
            }

            String buildWalletCommand = String.format("velluscinum buildWallet %s", userId);
            String commandResult = executeCommand(buildWalletCommand);

            if (commandResult.contains("Error") || commandResult.isBlank()) {
                continue;
            }

            moveKeyFilesToCorrectDirectory(userId);
            totalCreated++;
        }

        return totalCreated + " carteiras criadas com sucesso.";
    }

    private String storeWallets() {
        int totalStored = 0;
        File keysDirectory = new File(KEYS_DIR);

        if (!keysDirectory.exists() || !keysDirectory.isDirectory()) {
            return "Erro: Diretório de chaves não encontrado.";
        }

        File[] files = keysDirectory.listFiles((dir, name) -> name.endsWith(".publicKey"));
        if (files == null || files.length == 0) {
            return "Nenhuma chave pública encontrada no diretório.";
        }

        for (File publicKeyFile : files) {
            String userId = publicKeyFile.getName().split("\\.")[0];
            File privateKeyFile = new File(KEYS_DIR + userId + ".privateKey");

            if (!privateKeyFile.exists()) {
                continue;
            }

            if (keyRepository.findByIdPerson(Long.parseLong(userId)).isPresent()) {
                continue;
            }

            try {
                String publicKey = Files.readString(publicKeyFile.toPath()).trim();
                String privateKey = Files.readString(privateKeyFile.toPath()).trim();
                String encryptedPublicKey = AESUtil.encrypt(publicKey);
                String encryptedPrivateKey = AESUtil.encrypt(privateKey);

                KeyEntity newWallet = new KeyEntity();
                newWallet.setIdPerson(Long.parseLong(userId));
                newWallet.setPublicKey(encryptedPublicKey);
                newWallet.setPrivateKey(encryptedPrivateKey);

                keyRepository.save(newWallet);
                totalStored++;

            } catch (Exception e) {
                System.err.println("Erro ao armazenar chaves do usuário " + userId + ": " + e.getMessage());
            }
        }

        return totalStored + " carteiras armazenadas com sucesso.";
    }

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "Erro ao executar comando. Código de saída: " + exitCode;
            }
        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
        return output.toString();
    }

    private void moveKeyFilesToCorrectDirectory(String userId) {
        File currentDirPublicKey = new File(userId + ".publicKey");
        File currentDirPrivateKey = new File(userId + ".privateKey");

        File correctDirPublicKey = new File(KEYS_DIR + userId + ".publicKey");
        File correctDirPrivateKey = new File(KEYS_DIR + userId + ".privateKey");

        if (currentDirPublicKey.exists()) {
            currentDirPublicKey.renameTo(correctDirPublicKey);
        }

        if (currentDirPrivateKey.exists()) {
            currentDirPrivateKey.renameTo(correctDirPrivateKey);
        }
    }
}
