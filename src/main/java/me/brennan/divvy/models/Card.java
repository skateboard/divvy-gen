package me.brennan.divvy.models;

/**
 * @author Brennan
 * @since 6/28/21
 **/
public class Card {
    private String cardName, cardNumber, expData, cvv;

    public Card(String cardName, String cardNumber, String expData, String cvv) {
        this.cardName = cardName;
        this.cardNumber = cardNumber;
        this.expData = expData;
        this.cvv = cvv;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getExpData() {
        return expData;
    }

    public String getCvv() {
        return cvv;
    }
}
