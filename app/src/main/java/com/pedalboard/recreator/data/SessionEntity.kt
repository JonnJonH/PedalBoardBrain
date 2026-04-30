package com.pedalboard.recreator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val songTitle: String,
    val section: String,
    val part: String,
    val date: Long,
    val notes: String,
    val fullBoardImagePath: String?
)
