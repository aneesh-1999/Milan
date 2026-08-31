package com.example.model

enum class Partner(
    val id: String,
    val displayName: String,
    val countryCode: String,
    val timeZoneId: String,
    val cityName: String,
    val countryName: String,
    val defaultEmail: String
) {
    MALTA(
        id = "malta_partner",
        displayName = "Anish",
        countryCode = "MLT",
        timeZoneId = "Europe/Malta",
        cityName = "Valletta",
        countryName = "Malta",
        defaultEmail = "anish@milan.app"
    ),
    NEPAL(
        id = "nepal_partner",
        displayName = "Puri",
        countryCode = "NPL",
        timeZoneId = "Asia/Kathmandu",
        cityName = "Kathmandu",
        countryName = "Nepal",
        defaultEmail = "puri@milan.app"
    );

    val otherPartner: Partner
        get() = if (this == MALTA) NEPAL else MALTA

    companion object {
        fun fromId(id: String?): Partner {
            return entries.find { it.id == id || it.name.equals(id, ignoreCase = true) } ?: MALTA
        }
    }
}
