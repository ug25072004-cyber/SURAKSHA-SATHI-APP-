package com.example.data.model

enum class PriorityLevel(val label: String, val weight: Int) {
    CRITICAL("CRITICAL", 4),
    HIGH("HIGH", 3),
    MEDIUM("MEDIUM", 2),
    LOW("LOW", 1)
}

enum class DisasterType(val displayName: String) {
    FLOOD("Flash Flood & Inundation"),
    CYCLONE("Severe Cyclonic Storm"),
    EARTHQUAKE("Seismic Activity"),
    LANDSLIDE("Hillside Landslide"),
    FIRE("Urban / Wildland Fire")
}

enum class UserRole(val title: String, val subtitle: String) {
    DISASTER_COMMANDER("Incident Commander", "State / NDRF Disaster Authority"),
    RESPONSE_TEAM("Field Response Leader", "Rapid Rescue & Deployment Unit"),
    HOSPITAL_LEAD("Medical Operations", "Emergency Triage & Bed Logistics"),
    CITIZEN_VOLUNTEER("Community Sathi", "Ground Reports & Safety Advisories")
}

data class ZoneTriage(
    val id: String,
    val name: String,
    val sector: String,
    val priority: PriorityLevel,
    val affectedPeople: Int,
    val demandWater: Int,
    val demandFood: Int,
    val demandMedicalTeams: Int,
    val demandRescueVehicles: Int,
    val allocatedWater: Int,
    val allocatedFood: Int,
    val allocatedMedicalTeams: Int,
    val allocatedRescueVehicles: Int,
    val accessStatus: String, // e.g. "Accessible via Highway 4", "Bridge Submerged"
    val urgencyRuler: Float // 0.0 to 1.0 urgency gauge
)

data class GISRouteWaypoint(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val isDepot: Boolean = false,
    val isDestination: Boolean = false,
    val isHazard: Boolean = false,
    val statusText: String
)

data class EmergencyConvoyRoute(
    val id: String,
    val routeName: String,
    val origin: String,
    val primaryTarget: String,
    val secondaryTarget: String,
    val totalDistanceKm: Float,
    val estimatedMinutes: Int,
    val safetyScorePercent: Int,
    val waypoints: List<GISRouteWaypoint>,
    val hazardAdvisory: String,
    val isRerouted: Boolean = false
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val thinkingTrace: String? = null,
    val isThinking: Boolean = false,
    val suggestedActions: List<String> = emptyList()
)

enum class DepotStatus(val label: String) {
    OPERATIONAL("Operational"),
    DEPLETION_ALERT("Depletion Alert"),
    CRITICAL_SHORTAGE("Critical Shortage"),
    RESTOCK_IN_TRANSIT("Restock In-Transit")
}

data class ReliefDepot(
    val id: String,
    val name: String,
    val code: String,
    val district: String,
    val locationDescription: String,
    val lat: Double,
    val lng: Double,
    val status: DepotStatus,
    val waterStock: Int,
    val waterCapacity: Int,
    val rationPacks: Int,
    val rationCapacity: Int,
    val medicalTraumaKits: Int,
    val medicalCapacity: Int,
    val rescueBoats: Int,
    val boatCapacity: Int,
    val emergencyFuelLiters: Int,
    val fuelCapacity: Int,
    val dewateringPumps: Int = 10,
    val shelterTents: Int = 100,
    val assignedVehiclesCount: Int = 8,
    val lastRestockTimestamp: Long = System.currentTimeMillis()
)

data class StockpileHealthSummary(
    val totalWaterStock: Int,
    val totalWaterCapacity: Int,
    val totalRationsStock: Int,
    val totalRationsCapacity: Int,
    val totalMedicalStock: Int,
    val totalMedicalCapacity: Int,
    val totalRescueBoatsStock: Int,
    val totalFuelLiters: Int,
    val burnRatePerHourWater: Int = 65,
    val hoursRemainingWater: Float = 14.2f,
    val isCriticalShortagePresent: Boolean = false
)

data class AllocationTransaction(
    val id: String,
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

enum class MapDisplayMode(val label: String, val subtitle: String) {
    TACTICAL_RADAR("Tactical Radar", "Vector telemetry & sensor overlays"),
    GOOGLE_MAPS_GIS("Google Maps Live", "Interactive live disaster positioning & GPS sync"),
    SATELLITE_HAZARD("Hazard Analysis", "Flood inundation & evacuation polygon")
}

data class DisasterLiveEpicenter(
    val id: String,
    val name: String,
    val type: DisasterType,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Float,
    val severityLevel: String,
    val waterLevelMeters: Float,
    val windSpeedKmh: Float,
    val affectedPopulationEstimate: Int,
    val activeHotspotsCount: Int,
    val evacuationCorridorOpen: Boolean,
    val lastSensorUpdate: Long = System.currentTimeMillis()
)

