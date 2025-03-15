package com.example.walletapi.models;

import jakarta.persistence.*;

@Entity
@Table(name = "wallet")
public class KeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idWallet")
    private Long idWallet;

    @Column(name = "idPerson", nullable = false)
    private Long idPerson;

    @Column(name = "pubKey", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "prvKey", nullable = false, columnDefinition = "TEXT")
    private String privateKey;

    // Getters e setters:
    public Long getIdWallet() { return idWallet; }
    public void setIdWallet(Long idWallet) { this.idWallet = idWallet; }

    public Long getIdPerson() { return idPerson; }
    public void setIdPerson(Long idPerson) { this.idPerson = idPerson; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getPrivateKey() { return privateKey; }
    public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }
}
