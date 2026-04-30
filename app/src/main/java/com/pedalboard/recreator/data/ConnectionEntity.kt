package com.pedalboard.recreator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val fromPedalId: String,
    val toPedalId: String,
    val channel: ChannelType
)
