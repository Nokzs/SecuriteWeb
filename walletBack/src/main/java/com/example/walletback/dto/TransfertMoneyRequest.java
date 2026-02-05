package com.example.walletback.dto;

public class TransfertMoneyRequest {
    private String recipientEmail;
    private String label;
    private String amount;

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
