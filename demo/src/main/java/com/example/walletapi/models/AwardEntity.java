package com.example.walletapi.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "award")
public class AwardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAward")
    private Long idAward;

    @Column(name = "idEvent", nullable = false)
    private Long idEvent;

    @Column(name = "idSubscription", nullable = false)
    private Long idSubscription;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "idAsset")  // adicionado corretamente aqui!
    private String idAsset;

    @Column(name = "awardDate", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date awardDate;

    // Getters e Setters

    public Long getIdAward() {
        return idAward;
    }

    public void setIdAward(Long idAward) {
        this.idAward = idAward;
    }

    public Long getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(Long idEvent) {
        this.idEvent = idEvent;
    }

    public Long getIdSubscription() {
        return idSubscription;
    }

    public void setIdSubscription(Long idSubscription) {
        this.idSubscription = idSubscription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdAsset() {
        return idAsset;
    }

    public void setIdAsset(String idAsset) {
        this.idAsset = idAsset;
    }

    public Date getAwardDate() {
        return awardDate;
    }

    public void setAwardDate(Date awardDate) {
        this.awardDate = awardDate;
    }
}
