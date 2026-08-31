package com.example.model

data class LockedNote(
    val id: String = "",
    val senderPartner: String = Partner.MALTA.id,
    val senderName: String = "Anish",
    val recipientPartner: String = Partner.NEPAL.id,
    val title: String = "A Surprise Note For You",
    val secretContent: String = "",
    val unlockTimestamp: Long = System.currentTimeMillis() + (24L * 60 * 60 * 1000), // default 24h
    val isRevealed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val hint: String = "Open only when the countdown reaches zero"
) {
    constructor() : this("", Partner.MALTA.id, "Anish", Partner.NEPAL.id, "A Surprise Note For You", "", System.currentTimeMillis() + (24L * 60 * 60 * 1000), false, System.currentTimeMillis(), "Open only when the countdown reaches zero")

    val isUnlocked: Boolean
        get() = isRevealed || System.currentTimeMillis() >= unlockTimestamp
}
