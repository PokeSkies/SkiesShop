package com.pokeskies.skiesshop;

import java.util.UUID;

public class ShopTransaction {
    public UUID player;
    public Long timestamp;
    public String shopId;
    public String entryId;
    public Double entryPrice;
    public String currency;
    public Integer amount;
    public Action action;
    public String itemId;

    public ShopTransaction() {
    }

    public ShopTransaction(UUID player, Long timestamp, String shopId, String entryId, Double entryPrice, String currency, Integer amount, Action action, String itemId) {
        this.player = player;
        this.timestamp = timestamp;
        this.shopId = shopId;
        this.entryId = entryId;
        this.entryPrice = entryPrice;
        this.currency = currency;
        this.amount = amount;
        this.action = action;
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return "ShopTransaction{" +
                "player=" + player +
                ", timestamp=" + timestamp +
                ", shopId='" + shopId + '\'' +
                ", entryId='" + entryId + '\'' +
                ", entryPrice=" + entryPrice +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                ", action=" + action +
                ", itemId='" + itemId + '\'' +
                '}';
    }

    public enum Action {
        BUY,
        SELL
    }
}
