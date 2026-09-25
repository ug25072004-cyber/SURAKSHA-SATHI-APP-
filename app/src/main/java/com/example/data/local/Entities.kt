package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incident_reports")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val disasterType: String,
    val location: String,
    val severity: String,
    val affectedCount: Int,
    val description: String,
    val reportedBy: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynchronized: Boolean = true,
    val photoUrl: String? = null
)

@Entity(tableName = "depot_resources")
data class ResourceEntity(
    @PrimaryKey
    val depotId: String,
    val depotName: String,
    val district: String,
    val waterUnits: Int,
    val foodPackets: Int,
    val medicalKits: Int,
    val rescueBoats: Int,
    val emergencyFuelLiters: Int,
    val code: String = "DEPOT-01",
    val locationDescription: String = "Regional Staging Area",
    val lat: Double = 20.4625,
    val lng: Double = 85.8830,
    val status: String = "OPERATIONAL",
    val waterCapacity: Int = 1000,
    val foodCapacity: Int = 800,
    val medicalCapacity: Int = 120,
    val boatCapacity: Int = 25,
    val fuelCapacity: Int = 4000,
    val dewateringPumps: Int = 12,
    val shelterTents: Int = 250,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "allocation_logs")
data class AllocationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceDepotName: String,
    val targetZoneName: String,
    val waterUnits: Int,
    val foodPackets: Int,
    val medicalKits: Int,
    val rescueBoats: Int,
    val status: String,
    val etaMinutes: Int,
    val dispatchedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val organization: String,
    val category: String, // "DISASTER_MGMT", "MEDICAL", "POLICE", "FIRE_RESCUE", "MILITARY_NDRF", "DISTRICT_ADMIN", "HELPLINE"
    val primaryPhone: String,
    val alternatePhone: String? = null,
    val email: String? = null,
    val district: String, // "Statewide", "Cuttack", "Puri", "Khordha", "Kendrapara"
    val address: String? = null,
    val operationalHours: String = "24x7 Emergency",
    val isTollFree: Boolean = false,
    val priorityRank: Int = 1, // 1 = Critical Life-Safety First, 2 = Support Services, 3 = Local Administration
    val notes: String? = null,
    val lastVerifiedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emergency_resources")
data class EmergencyResourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "LIFE_SAVING", "MEDICAL", "WATER_SANITATION", "RESCUE_GEAR", "POWER_COMMUNICATION", "HEAVY_MACHINERY", "SHELTER"
    val totalQuantity: Int,
    val availableQuantity: Int,
    val unit: String, // "Units", "Kits", "Boats", "Sets", "Liters", "Tons"
    val locationName: String,
    val district: String,
    val lat: Double = 20.2961,
    val lng: Double = 85.8245,
    val custodianName: String,
    val custodianPhone: String,
    val conditionStatus: String = "OPERATIONAL", // "OPERATIONAL", "DEPLOYED", "STANDBY", "MAINTENANCE"
    val deploymentStatus: String = "READY_FOR_DISPATCH", // "READY_FOR_DISPATCH", "DISPATCHED", "EN_ROUTE", "ON_SITE"
    val isCritical: Boolean = true,
    val description: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

