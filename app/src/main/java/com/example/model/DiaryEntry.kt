package com.example.model

data class DiaryEntry(
    val id: String = "",
    val authorPartner: String = Partner.MALTA.id,
    val authorName: String = "Anish",
    val caption: String = "",
    val imageUrl: String = "",
    val locationName: String = "Valletta, Malta",
    val timestamp: Long = System.currentTimeMillis(),
    val loveCount: Int = 0,
    val lovedBy: List<String> = emptyList()
) {
    constructor() : this("", Partner.MALTA.id, "Anish", "", "", "Valletta, Malta", System.currentTimeMillis(), 0, emptyList())
}
