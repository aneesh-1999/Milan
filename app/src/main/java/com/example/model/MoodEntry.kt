package com.example.model

data class MoodEntry(
    val id: String = "",
    val partnerId: String = Partner.MALTA.id,
    val moodKey: String = "loved",
    val moodLabel: String = "Loved",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", Partner.MALTA.id, "loved", "Loved", "", System.currentTimeMillis())

    companion object {
        val PRESET_MOODS = listOf(
            MoodOption("loved", "Loved", "Feeling so in love with you"),
            MoodOption("missing_you", "Missing You", "Wishing you were here"),
            MoodOption("happy", "Happy", "Having a wonderful day"),
            MoodOption("busy", "Busy", "Working hard, but thinking of you"),
            MoodOption("sleepy", "Sleepy", "Cozy & ready for sweet dreams"),
            MoodOption("down", "Need a Hug", "Need your comforting presence"),
            MoodOption("excited", "Excited", "Looking forward to our next talk")
        )
    }
}

data class MoodOption(
    val moodKey: String,
    val label: String,
    val subtitle: String
)
