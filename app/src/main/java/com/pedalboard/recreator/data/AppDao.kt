package com.pedalboard.recreator.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT * FROM pedals WHERE sessionId = :sessionId")
    fun getPedalsForSession(sessionId: String): Flow<List<PedalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPedal(pedal: PedalEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPedals(pedals: List<PedalEntity>)

    @Query("SELECT * FROM connections WHERE sessionId = :sessionId")
    fun getConnectionsForSession(sessionId: String): Flow<List<ConnectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: ConnectionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnections(connections: List<ConnectionEntity>)
    
    @Query("DELETE FROM pedals WHERE sessionId = :sessionId")
    suspend fun deletePedalsForSession(sessionId: String)

    @Query("DELETE FROM connections WHERE sessionId = :sessionId")
    suspend fun deleteConnectionsForSession(sessionId: String)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
