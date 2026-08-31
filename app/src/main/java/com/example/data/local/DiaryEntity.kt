package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.DiaryEntry

@Entity(tableName = "diary_entries")
data class DiaryEntity(
    @PrimaryKey val id: String,
    val authorPartner: String,
    val authorName: String,
    val caption: String,
    val imageUrl: String,
    val locationName: String,
    val timestamp: Long,
    val loveCount: Int
) {
    fun toDiaryEntry(): DiaryEntry = DiaryEntry(
        id = id,
        authorPartner = authorPartner,
        authorName = authorName,
        caption = caption,
        imageUrl = imageUrl,
        locationName = locationName,
        timestamp = timestamp,
        loveCount = loveCount,
        lovedBy = emptyList()
    )

    companion object {
        fun fromDiaryEntry(entry: DiaryEntry): DiaryEntity = DiaryEntity(
            id = entry.id,
            authorPartner = entry.authorPartner,
            authorName = entry.authorName,
            caption = entry.caption,
            imageUrl = entry.imageUrl,
            locationName = entry.locationName,
            timestamp = entry.timestamp,
            loveCount = entry.loveCount
        )
    }
}
