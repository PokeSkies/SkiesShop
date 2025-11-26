package com.pokeskies.skiesshop.config

import com.google.gson.annotations.SerializedName
import com.pokeskies.skiesshop.logging.LoggerType

class LoggingOptions(
    var enabled: Boolean = false,
    val type: LoggerType = LoggerType.CONSOLE,
    val host: String = "",
    val port: Int = 3306,
    val database: String = "skiesshop",
    val username: String = "root",
    val password: String = "",
    @SerializedName("table_prefix")
    val tablePrefix: String = "skiesshop_",
    val properties: Map<String, String> = mapOf("useUnicode" to "true", "characterEncoding" to "utf8"),
    @SerializedName("pool_settings")
    val poolSettings: PoolSettings = PoolSettings(),
    @SerializedName("url_override")
    val urlOverride: String = ""
) {
    override fun toString(): String {
        return "LoggingOptions(enabled=$enabled, type=$type, host='$host', port=$port, database='$database', username='$username', password='$password', tablePrefix='$tablePrefix', properties=$properties, poolSettings=$poolSettings, urlOverride='$urlOverride')"
    }
}