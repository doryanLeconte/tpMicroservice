package org.imt.frontBancaire.Models;

import java.util.Set;
import java.util.UUID;

public class Account {

    private UUID id;

    private String nom;

    private String IBAN;

    private Set<Transaction> transactions_debit;

    private Set<Transaction> transactions_credit;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Set<Transaction> getTransactions_debit() {
        return transactions_debit;
    }

    public void setTransactions_debit(Set<Transaction> transactions_debit) {
        this.transactions_debit = transactions_debit;
    }

    public Set<Transaction> getTransactions_credit() {
        return transactions_credit;
    }

    public void setTransactions_credit(Set<Transaction> transactions_credit) {
        this.transactions_credit = transactions_credit;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }


}
