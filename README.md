# API Awards

A API Awards é uma aplicação desenvolvida em **Spring Boot** para gerenciar premiações digitais em blockchain, permitindo a geração, armazenamento e consulta de NFTs.

## Tecnologias Utilizadas

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **MariaDB**
- **BigChainDB**
- **Velluscinum (Middleware Blockchain)**
- **AES (Advanced Encryption Standard)**
- **Swagger/OpenAPI**

## Endpoints da API

### Criar ou Armazenar Carteiras

#### `POST /api/v1/wallet/generate`
Cria ou armazena carteiras para usuários premiados.

**Request Body:**
```json
{
  "operation": "generate" // ou "store"
}
```

**Respostas:**
- `200 OK`: Operação realizada com sucesso.
- `400 Bad Request`: Parâmetro inválido.

---

### Consultar Saldo ou Tokens

#### `POST /api/v1/wallet/query`
Realiza consultas sobre as carteiras digitais.

**Request Body:**
```json
{
  "type": "walletBalance", // ou "showToken"
  "idPerson": 1001, // Apenas para consulta de saldo
  "tokenId": "abc123" // Apenas para exibição de um token
}
```

**Respostas:**
- `200 OK`: Retorna os dados solicitados.
- `400 Bad Request`: Requisição inválida.

---

### Exibir Imagem de um NFT

#### `GET /api/v1/showToken1/{tokenId}`
Retorna a imagem de um NFT específico em formato Base64.

**Respostas:**
- `200 OK`: Retorna a imagem do NFT.
- `404 Not Found`: Token não encontrado.

---

### Deploy de NFTs

#### `POST /api/v1/deploy`
Realiza o deploy de premiações NFT na blockchain.

**Request Body:**
```json
{
  "type": "GOLDEN" // Ou "SILVER", "BRONZE", "HONORABLE MENTION"
}
```

**Respostas:**
- `200 OK`: Deploy realizado com sucesso.
- `400 Bad Request`: Tipo de premiação inválido.

---

### Geração de Arquivos NFT

#### `POST /api/v1/generate`
Gera arquivos de premiação (NFTs) baseados no tipo da premiação.

**Request Body:**
```json
{
  "type": "GOLDEN" // Ou "SILVER", "BRONZE", "HONORABLE MENTION"
}
```

**Respostas:**
- `200 OK`: Arquivo gerado com sucesso.
- `400 Bad Request`: Tipo inválido.

---

## Segurança
A API utiliza **criptografia AES-128** para armazenar chaves privadas dos usuários de forma segura no banco de dados. A descriptografia ocorre apenas no momento das transações, garantindo que as credenciais dos usuários não sejam expostas.

## Instalação e Execução

1. Clone este repositório:
   ```sh
   git clone https://github.com/seuusuario/api-awards.git
   ```
2. Acesse a pasta do projeto:
   ```sh
   cd api-awards
   ```
3. Compile e execute a aplicação:
   ```sh
   ./gradlew bootRun
   ```
4. Acesse a documentação Swagger:
   - **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
   - **Especificação OpenAPI**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

## Contribuição
Contribuições são bem-vindas! Para contribuir:

1. Fork o repositório
2. Crie uma branch (`git checkout -b feature-nova`)
3. Commit suas alterações (`git commit -m 'Adiciona nova funcionalidade'`)
4. Faça push para a branch (`git push origin feature-nova`)
5. Abra um Pull Request
