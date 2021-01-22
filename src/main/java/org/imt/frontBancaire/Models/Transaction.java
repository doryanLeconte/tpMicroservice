package org.imt.frontBancaire.Models;

public class Transaction {


    private Long id;

    public Transaction() {
        super();
    }

    public Transaction(Long id) {
        super();

        this.id = id;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}