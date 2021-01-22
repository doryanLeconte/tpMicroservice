package org.imt.frontBancaire.Models;

public class Transaction {


    private String description;
    private Long montant;
    private Account origine;
    private Account destination;
    private Long transactionId;


    public Transaction() {
        super();
    }

    public Transaction(Long id) {
        super();

        this.transactionId = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getMontant() {
        return montant;
    }

    public void setMontant(Long montant) {
        this.montant = montant;
    }

    public Account getOrigine() {
        return origine;
    }

    public void setOrigine(Account origine) {
        this.origine = origine;
    }

    public Account getDestination() {
        return destination;
    }

    public void setDestination(Account destination) {
        this.destination = destination;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getId() {
        return transactionId;
    }

    public void setId(Long id) {
        this.transactionId = id;
    }


}