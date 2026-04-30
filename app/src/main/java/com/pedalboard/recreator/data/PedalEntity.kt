package com.pedalboard.recreator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedals")
data class PedalEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val name: String,
    val imagePath: String?,
    val croppedImagePath: String?,
    val chainStage: ChainStage,
    val position: Int,
    val channel: ChannelType
)
