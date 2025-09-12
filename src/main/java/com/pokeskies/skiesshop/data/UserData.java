package com.pokeskies.skiesshop.data;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.UUID;

public class UserData {
    @BsonProperty("_id")
    public UUID uuid;

    public UserData(UUID uuid) {
        this.uuid = uuid;
    }

    public UserData() {}

    @Override
    public String toString() {
        return "UserData{" +
                "uuid=" + uuid +
                '}';
    }
}
