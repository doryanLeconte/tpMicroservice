package org.imt.frontBancaire.Models;

import java.util.UUID;

public class Order {
    private String orderId;
    private String name;
    private String description;
    private UUID originId;
    private UUID destinationId;
    private Long montant;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getOriginId() {
        return originId;
    }

    public void setOriginId(UUID originId) {
        this.originId = originId;
    }

    public UUID getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(UUID destinationId) {
        this.destinationId = destinationId;
    }

    public Long getMontant() {
        return montant;
    }

    public void setMontant(Long montant) {
        this.montant = montant;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
