package com.pokeskies.skiesshop.config

import com.google.gson.annotations.SerializedName

class PoolSettings(
    @SerializedName("maximum_pool_size")
    val maximumPoolSize: Int = 10,
    @SerializedName("minimum_idle")
    val minimumIdle: Int = 10,
    @SerializedName("keepalive_time")
    val keepaliveTime: Long = 0,
    @SerializedName("connection_timeout")
    val connectionTimeout: Long = 30000,
    @SerializedName("idle_timeout")
    val idleTimeout: Long = 600000,
    @SerializedName("max_lifetime")
    val maxLifetime: Long = 1800000
) {
    override fun toString(): String {
        return "StoragePoolSettings(maximumPoolSize=$maximumPoolSize, minimumIdle=$minimumIdle," +
                " keepaliveTime=$keepaliveTime, connectionTimeout=$connectionTimeout," +
                " idleTimeout=$idleTimeout, maxLifetime=$maxLifetime)"
    }
}