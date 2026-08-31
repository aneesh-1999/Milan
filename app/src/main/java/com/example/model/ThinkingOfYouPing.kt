package com.example.model

data class ThinkingOfYouPing(
    val id: String = "",
    val senderPartner: String = Partner.MALTA.id,
    val senderDisplayName: String = "Anish",
    val message: String = "Thinking of you right now",
    val timestamp: Long = System.currentTimeMillis()
) {
    // No-arg constructor required for Firestore
    constructor() : this("", Partner.MALTA.id, "Anish", "Thinking of you right now", System.currentTimeMillis())
}
