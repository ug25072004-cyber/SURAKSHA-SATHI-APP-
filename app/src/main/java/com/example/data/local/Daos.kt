package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incident_reports ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Query("DELETE FROM incident_reports WHERE id = :id")
    suspend fun deleteIncident(id: Long)
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM depot_resources ORDER BY depotName ASC")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM depot_resources WHERE depotId = :depotId LIMIT 1")
    suspend fun getDepotById(depotId: String): ResourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(resource: ResourceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<ResourceEntity>)

    @Update
    suspend fun updateResource(resource: ResourceEntity)
}

@Dao
interface AllocationLogDao {
    @Query("SELECT * FROM allocation_logs ORDER BY timestamp DESC")
    fun getAllAllocationLogs(): Flow<List<AllocationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AllocationLogEntity): Long
}

@Dao
interface EmergencyContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY priorityRank ASC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE category = :category ORDER BY priorityRank ASC, name ASC")
    fun getContactsByCategory(category: String): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE district = :district OR district = 'Statewide' ORDER BY priorityRank ASC, name ASC")
    fun getContactsByDistrict(district: String): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE name LIKE '%' || :query || '%' OR organization LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR district LIKE '%' || :query || '%' ORDER BY priorityRank ASC, name ASC")
    fun searchContacts(query: String): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Long): EmergencyContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContacts(contacts: List<EmergencyContactEntity>)

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun getContactCount(): Int
}

@Dao
interface EmergencyResourceDao {
    @Query("SELECT * FROM emergency_resources ORDER BY isCritical DESC, name ASC")
    fun getAllResources(): Flow<List<EmergencyResourceEntity>>

    @Query("SELECT * FROM emergency_resources WHERE category = :category ORDER BY name ASC")
    fun getResourcesByCategory(category: String): Flow<List<EmergencyResourceEntity>>

    @Query("SELECT * FROM emergency_resources WHERE district = :district ORDER BY name ASC")
    fun getResourcesByDistrict(district: String): Flow<List<EmergencyResourceEntity>>

    @Query("SELECT * FROM emergency_resources WHERE isCritical = 1 ORDER BY name ASC")
    fun getCriticalResources(): Flow<List<EmergencyResourceEntity>>

    @Query("SELECT * FROM emergency_resources WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR locationName LIKE '%' || :query || '%' OR district LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchResources(query: String): Flow<List<EmergencyResourceEntity>>

    @Query("SELECT * FROM emergency_resources WHERE id = :id LIMIT 1")
    suspend fun getResourceById(id: Long): EmergencyResourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: EmergencyResourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllResources(resources: List<EmergencyResourceEntity>)

    @Update
    suspend fun updateResource(resource: EmergencyResourceEntity)

    @Delete
    suspend fun deleteResource(resource: EmergencyResourceEntity)

    @Query("DELETE FROM emergency_resources WHERE id = :id")
    suspend fun deleteResourceById(id: Long)

    @Query("SELECT COUNT(*) FROM emergency_resources")
    suspend fun getResourceCount(): Int
}

