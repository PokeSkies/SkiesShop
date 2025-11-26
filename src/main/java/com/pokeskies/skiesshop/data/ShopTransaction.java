package com.pokeskies.skiesshop.data;

import java.util.UUID;

public class ShopTransaction {
    public UUID player; // The UUID of the player who made the transaction
    public Long timestamp; // The time the transaction occurred
    public String shopId; // The ID of the shop bought from
    public String entryId; // The ID of the specific shop entry
    public TransactionType type; // The type of transaction (BUY or SELL)
    public Double price; // The total price of the transaction
    public Integer amount; // The amount of items bought/sold
    public String entry; // The shop entry as a JSON string

    public ShopTransaction() {
    }

    public ShopTransaction(UUID player, Long timestamp, String shopId, String entryId, TransactionType type, Double price, Integer amount, String entry) {
        this.player = player;
        this.timestamp = timestamp;
        this.shopId = shopId;
        this.entryId = entryId;
        this.type = type;
        this.price = price;
        this.amount = amount;
        this.entry = entry;
    }

    @Override
    public String toString() {
        return "ShopTransaction{" +
                "player=" + player +
                ", timestamp=" + timestamp +
                ", shopId='" + shopId + '\'' +
                ", entryId='" + entryId + '\'' +
                ", type=" + type +
                ", price=" + price +
                ", amount=" + amount +
                ", entry='" + entry + '\'' +
                '}';
    }
}
