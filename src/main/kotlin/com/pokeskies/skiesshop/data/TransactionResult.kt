package com.pokeskies.skiesshop.data

class TransactionResult(
    val success: Boolean, // Indicates if the transaction was successful
    val response: String = "", // Response message if failed
    val amount: Int // Amount Processed
)