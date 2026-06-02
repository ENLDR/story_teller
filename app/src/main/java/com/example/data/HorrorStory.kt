package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "horror_stories")
data class HorrorStory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val systemPromptUsed: String = ""
)
