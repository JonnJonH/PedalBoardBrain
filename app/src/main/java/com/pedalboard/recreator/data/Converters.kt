package com.pedalboard.recreator.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromChainStage(value: ChainStage) = value.name
    @TypeConverter
    fun toChainStage(value: String) = ChainStage.valueOf(value)

    @TypeConverter
    fun fromChannelType(value: ChannelType) = value.name
    @TypeConverter
    fun toChannelType(value: String) = ChannelType.valueOf(value)
    
    @TypeConverter
    fun fromSignalState(value: SignalState) = value.name
    @TypeConverter
    fun toSignalState(value: String) = SignalState.valueOf(value)
}
