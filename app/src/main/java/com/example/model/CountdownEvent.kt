package com.example.model

data class CountdownEvent(
    val id: String = "primary_countdown",
    val title: String = "Next Visit",
    val targetTimestamp: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // default ~30 days in future
    val category: String = "visit", // visit, call, anniversary, custom
    val note: String = "Counting down every second until we hug!",
    val updatedByPartner: String = Partner.MALTA.id,
    val updatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("primary_countdown", "Next Visit", System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), "visit", "Counting down every second until we hug!", Partner.MALTA.id, System.currentTimeMillis())

    companion object {
        val PRESET_CATEGORIES = listOf(
            CategoryOption("visit", "Next Visit", "Flight tickets & airport reunion"),
            CategoryOption("call", "Next Video Call", "Our special date night call"),
            CategoryOption("anniversary", "Anniversary", "Celebrating our love story"),
            CategoryOption("custom", "Special Date", "A special milestone for us")
        )
    }
}

data class CategoryOption(
    val key: String,
    val title: String,
    val description: String
)
