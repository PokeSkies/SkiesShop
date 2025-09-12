package com.pokeskies.skiesshop.placeholders

interface ServerPlaceholder {
    fun handle(args: List<String>): GenericResult
    fun id(): String
}
