package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM horror_stories ORDER BY timestamp DESC")
    fun getAllStories(): Flow<List<HorrorStory>>

    @Query("SELECT * FROM horror_stories WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteStories(): Flow<List<HorrorStory>>

    @Query("SELECT * FROM horror_stories WHERE id = :id")
    suspend fun getStoryById(id: Long): HorrorStory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: HorrorStory): Long

    @Update
    suspend fun updateStory(story: HorrorStory)

    @Query("DELETE FROM horror_stories WHERE id = :id")
    suspend fun deleteStoryById(id: Long)

    @Query("DELETE FROM horror_stories")
    suspend fun deleteAllStories()
}
