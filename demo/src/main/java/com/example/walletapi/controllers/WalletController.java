package com.example.walletapi.controllers;

import com.example.walletapi.dto.WalletQueryRequestDTO;
import com.example.walletapi.models.KeyEntity;
import com.example.walletapi.repositories.KeyRepository;
import com.example.walletapi.utils.AESUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;

@Tag(name = "Wallet Controller", description = "Gerencia consultas da carteira digital")
@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    @Autowired
    private KeyRepository keyRepository;

    @Operation(summary = "Consulta informações da carteira", description = "Realiza consultas como saldo da carteira ou detalhes do token.")
    @PostMapping("/query")
    public ResponseEntity<?> queryWallet(@RequestBody WalletQueryRequestDTO walletQueryRequestDTO) {
        String type = walletQueryRequestDTO.getType();
        Long idPerson = walletQueryRequestDTO.getIdPerson();
        String tokenId = walletQueryRequestDTO.getTokenId();

        if (type == null || type.isEmpty()) {
            return ResponseEntity.badRequest().body("Erro: Tipo de consulta não informado.");
        }
        
        if (type.equals("walletBalance") && idPerson == null) {
            return ResponseEntity.badRequest().body("Erro: ID do usuário não informado para consulta de saldo.");
        }

        return switch (type) {
            case "walletBalance" -> ResponseEntity.ok(getWalletBalance(idPerson));
            case "showToken" -> ResponseEntity.ok(getTokenDetails(tokenId));
            default -> ResponseEntity.badRequest().body("Erro: Tipo de consulta inválido.");
        };
    }
    
    @Operation(summary = "Retorna a imagem do token", description = "Busca a imagem associada a um ativo NFT e a retorna em formato PNG.")
    @GetMapping("/showToken1/{tokenId}")
    public ResponseEntity<byte[]> getTokenImage(@PathVariable String tokenId) {
        String jsonResponse = executeCommand("velluscinum showToken http://localhost:9984 " + tokenId);
        
        // Debug no servidor para checar o JSON recebido
        System.out.println("JSON recebido: " + jsonResponse);

        String base64Image = extractBase64Image(jsonResponse);

        if (base64Image == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);

            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao decodificar a imagem: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }




    private String getWalletBalance(Long idPerson) {
        Optional<KeyEntity> userKeys = keyRepository.findByIdPerson(idPerson);
        if (userKeys.isEmpty()) {
            return "Erro: Chaves do usuário não encontradas!";
        }

        try {
            String privateKey = AESUtil.decrypt(userKeys.get().getPrivateKey());
            String publicKey = AESUtil.decrypt(userKeys.get().getPublicKey());

            return executeCommandWithFiles(privateKey, publicKey, idPerson.toString(), "walletBalance");
        } catch (Exception e) {
            return "Erro ao descriptografar chaves: " + e.getMessage();
        }
    }

    private String getTokenDetails(String tokenId) {
        String commandOutput = executeCommand("velluscinum showToken http://localhost:9984 " + tokenId);
    
        // Verificar se há dados válidos no retorno
        if (commandOutput == null || commandOutput.isEmpty()) {
            return "{\"error\": \"Nenhum dado retornado pelo comando.\"}";
        }
    
        // Remover caracteres indesejados
        int jsonStart = commandOutput.indexOf('[');
        int jsonEnd = commandOutput.lastIndexOf(']') + 1;
    
        if (jsonStart == -1 || jsonEnd == -1) {
            return "{\"error\": \"Formato inesperado da resposta.\"}";
        }
    
        String jsonArray = commandOutput.substring(jsonStart, jsonEnd).trim();

    
        // Converter JSON
        String jsonResponse = convertArrayToJson(jsonArray);
    
        // Registrar JSON final formatado
        System.out.println("Final JSON Response: " + jsonResponse);
    
        return jsonResponse;
    }
    
    
    private String convertArrayToJson(String jsonArray) {
        StringBuilder jsonObject = new StringBuilder();
        jsonObject.append("{");
    
        // Substituir colchetes e aspas duplas
        jsonArray = jsonArray.replace("[[", "").replace("]]", "").replace("\"", "");
    
        // Dividir pares de valores
        String[] pairs = jsonArray.split("\\],\\[");
    
        for (String pair : pairs) {
            String[] keyValue = pair.split(",");
    
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
    
                // Verificar se o valor é "null" e tratá-lo corretamente
                if ("null".equalsIgnoreCase(value)) {
                    jsonObject.append("\"").append(key).append("\": null,");
                } else {
                    jsonObject.append("\"").append(key).append("\": \"").append(value).append("\",");
                }
            }
        }
    
        // Remover a última vírgula e fechar JSON
        if (jsonObject.charAt(jsonObject.length() - 1) == ',') {
            jsonObject.deleteCharAt(jsonObject.length() - 1);
        }
        jsonObject.append("}");
    
        return jsonObject.toString();
    }
    
    private String executeCommandWithFiles(String privateKey, String publicKey, String userId, String commandType) {
        StringBuilder output = new StringBuilder();
        File privateKeyFile = new File(userId + ".privateKey");
        File publicKeyFile = new File(userId + ".publicKey");

        try {
            Files.writeString(privateKeyFile.toPath(), privateKey);
            Files.writeString(publicKeyFile.toPath(), publicKey);

            String command = switch (commandType) {
                case "walletBalance" -> "velluscinum walletBalance http://localhost:9984 " + userId + ".privateKey " + userId + ".publicKey";
                default -> throw new IllegalArgumentException("Comando inválido: " + commandType);
            };

            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            privateKeyFile.delete();
            publicKeyFile.delete();

            if (exitCode != 0) {
                return "Erro ao executar comando. Código de saída: " + exitCode;
            }

        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }

        return output.toString();
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

    private String extractBase64Image(String jsonResponse) {
        try {
            // Remove os colchetes iniciais e finais do JSON e divide por ","
            jsonResponse = jsonResponse.replace("[", "").replace("]", "").replace("\"", "");
            String[] parts = jsonResponse.split(",");
    
            // Itera pelo JSON para encontrar a chave "image"
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].contains("image")) {
                    return parts[i + 1].trim();  // Retorna o valor da imagem
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao extrair imagem Base64: " + e.getMessage());
        }
        return null;  // Retorna null se não encontrar
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
