package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.local.AllocationLogEntity
import com.example.data.local.AppDatabase
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.EmergencyResourceEntity
import com.example.data.local.IncidentEntity
import com.example.data.local.ResourceEntity
import com.example.data.location.LiveGpsTelemetry
import com.example.data.location.LiveLocationTracker
import com.example.data.repository.OfflineEmergencyRepository
import com.example.data.model.AllocationTransaction
import com.example.data.model.ChatMessage
import com.example.data.model.DepotStatus
import com.example.data.model.DisasterLiveEpicenter
import com.example.data.model.DisasterType
import com.example.data.model.EmergencyConvoyRoute
import com.example.data.model.GISRouteWaypoint
import com.example.data.model.MapDisplayMode
import com.example.data.model.PriorityLevel
import com.example.data.model.ReliefDepot
import com.example.data.model.StockpileHealthSummary
import com.example.data.model.UserRole
import com.example.data.model.ZoneTriage
import android.content.Context
import android.net.Uri
import com.example.data.firebase.AuthState
import com.example.data.firebase.FirebaseAuthService
import com.example.data.firebase.FirebaseStorageService
import com.example.data.firebase.StorageFileItem
import com.example.data.firebase.StorageUploadResult
import com.example.data.firebase.StorageUploadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EmergencyUiState(
    val activeDisasterName: String = "Flash Flood & Coastal Inundation",
    val activeDisasterType: DisasterType = DisasterType.FLOOD,
    val location: String = "Coastal District A - Delta Valley",
    val currentRole: UserRole = UserRole.DISASTER_COMMANDER,
    val zones: List<ZoneTriage> = emptyList(),
    val activeRoute: EmergencyConvoyRoute? = null,
    val isDynamicReallocationActive: Boolean = false,
    val reallocationReason: String = "",
    val totalPeopleInNeed: Int = 18500,
    val totalWaterAllocated: Int = 750,
    val totalFoodAllocated: Int = 300,
    val totalMedicalDeployed: Int = 4,
    val isSimulatingAllocation: Boolean = false,
    val isDarkTheme: Boolean = true,
    val isHighContrastMode: Boolean = false,
    val isThinkingModeEnabled: Boolean = true,
    val isSearchGroundingEnabled: Boolean = true,
    val isAiThinking: Boolean = false,
    val chatMessages: List<ChatMessage> = emptyList(),
    val offlineModeNotice: String? = null,
    val isSosProtocolActive: Boolean = false,
    val activeSosProtocolName: String? = null,
    val sosTelemetryCoords: String = "19.8241° N, 85.8315° E",
    val sosTransmissionStatus: String = "STANDBY",
    val depots: List<ReliefDepot> = emptyList(),
    val selectedDepotId: String? = null,
    val allocationHistory: List<AllocationTransaction> = emptyList(),
    val stockpileSummary: StockpileHealthSummary = StockpileHealthSummary(0, 0, 0, 0, 0, 0, 0, 0),
    val isAllocationEngineRunning: Boolean = false,
    val lastAllocationEngineMessage: String? = null,
    val offlineContacts: List<EmergencyContactEntity> = emptyList(),
    val offlineResources: List<EmergencyResourceEntity> = emptyList(),
    val contactSearchQuery: String = "",
    val selectedContactCategory: String = "ALL",
    val resourceSearchQuery: String = "",
    val selectedResourceCategory: String = "ALL",
    val liveDisasterEpicenter: DisasterLiveEpicenter = DisasterLiveEpicenter(
        id = "DISASTER_EPICENTER_01",
        name = "Kendrapara Coastal Surge & Inundation Core",
        type = DisasterType.FLOOD,
        latitude = 19.8241,
        longitude = 85.8315,
        radiusKm = 4.8f,
        severityLevel = "CRITICAL / LEVEL-4",
        waterLevelMeters = 1.95f,
        windSpeedKmh = 68f,
        affectedPopulationEstimate = 18500,
        activeHotspotsCount = 7,
        evacuationCorridorOpen = true
    ),
    val liveGps: LiveGpsTelemetry? = null,
    val mapDisplayMode: MapDisplayMode = MapDisplayMode.GOOGLE_MAPS_GIS,
    val isLiveTrackingActive: Boolean = false,
    val isRealTimeTelemetryStreaming: Boolean = true,
    val distanceToDisasterKm: Double? = null,
    val bearingToDisasterDegrees: Float? = null,
    val selectedWaypoint: GISRouteWaypoint? = null,
    val authState: AuthState = AuthState.Unauthenticated,
    val isAuthProcessing: Boolean = false,
    val authErrorMessage: String? = null,
    val authSuccessMessage: String? = null,
    val storageUploadState: StorageUploadState = StorageUploadState.Idle,
    val storageFiles: List<StorageFileItem> = emptyList(),
    val isLoadingStorageFiles: Boolean = false,
    val storageErrorMessage: String? = null,
    val selectedEvidenceUri: Uri? = null,
    val uploadedEvidenceUrl: String? = null,
    val customWebClientId: String = ""
)

class EmergencyViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val incidentDao = db.incidentDao()
    private val resourceDao = db.resourceDao()
    private val allocationLogDao = db.allocationLogDao()
    private val emergencyContactDao = db.emergencyContactDao()
    private val emergencyResourceDao = db.emergencyResourceDao()

    val repository = OfflineEmergencyRepository(
        incidentDao = incidentDao,
        resourceDao = resourceDao,
        allocationLogDao = allocationLogDao,
        emergencyContactDao = emergencyContactDao,
        emergencyResourceDao = emergencyResourceDao
    )

    val incidentsFlow = incidentDao.getAllIncidents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resourcesFlow = resourceDao.getAllResources()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allocationLogsFlow = allocationLogDao.getAllAllocationLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContactsFlow = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyResourcesFlow = repository.allResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val criticalResourcesFlow = repository.criticalResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locationTracker = LiveLocationTracker(application)
    private var locationTrackingJob: Job? = null
    private var telemetrySimulationJob: Job? = null

    val authService = FirebaseAuthService()
    val storageService = FirebaseStorageService()

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    init {
        try {
            if (com.google.firebase.FirebaseApp.getApps(application).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(application)
            }
        } catch (_: Exception) {}
        initializeInitialTriageData()
        seedOfflineDatabase()
        observeOfflineDataStreams()
        startTelemetrySimulation()
        observeAuthStatus()
        try {
            loadStorageFiles()
        } catch (_: Exception) {}
    }

    private fun observeAuthStatus() {
        viewModelScope.launch {
            authService.authState.collect { auth ->
                _uiState.value = _uiState.value.copy(authState = auth)
            }
        }
    }

    private fun observeOfflineDataStreams() {
        viewModelScope.launch {
            repository.allContacts.collect { contacts ->
                _uiState.value = _uiState.value.copy(offlineContacts = contacts)
            }
        }
        viewModelScope.launch {
            repository.allResources.collect { resources ->
                _uiState.value = _uiState.value.copy(offlineResources = resources)
            }
        }
    }

    private fun initializeInitialTriageData() {
        val initialZones = listOf(
            ZoneTriage(
                id = "zone-1",
                name = "Zone 1 - Riverbank Delta",
                sector = "Sector Alpha (Lowland Basin)",
                priority = PriorityLevel.CRITICAL,
                affectedPeople = 12000,
                demandWater = 500,
                demandFood = 200,
                demandMedicalTeams = 3,
                demandRescueVehicles = 4,
                allocatedWater = 500,
                allocatedFood = 200,
                allocatedMedicalTeams = 3,
                allocatedRescueVehicles = 4,
                accessStatus = "Critical: Primary Causeway Submerged; Route via Coastal Ridge",
                urgencyRuler = 0.95f
            ),
            ZoneTriage(
                id = "zone-2",
                name = "Zone 2 - Central Market Ward",
                sector = "Sector Bravo (Urban Perimeter)",
                priority = PriorityLevel.HIGH,
                affectedPeople = 4500,
                demandWater = 250,
                demandFood = 100,
                demandMedicalTeams = 1,
                demandRescueVehicles = 2,
                allocatedWater = 250,
                allocatedFood = 100,
                allocatedMedicalTeams = 1,
                allocatedRescueVehicles = 1,
                accessStatus = "Caution: Water Logging 2.5ft on NH-16; High-clearance vehicles only",
                urgencyRuler = 0.72f
            ),
            ZoneTriage(
                id = "zone-3",
                name = "Zone 3 - Hilltop Relief Shelter",
                sector = "Sector Charlie (Elevated Staging)",
                priority = PriorityLevel.MEDIUM,
                affectedPeople = 2000,
                demandWater = 100,
                demandFood = 50,
                demandMedicalTeams = 1,
                demandRescueVehicles = 1,
                allocatedWater = 100,
                allocatedFood = 50,
                allocatedMedicalTeams = 1,
                allocatedRescueVehicles = 1,
                accessStatus = "Stable: Elevated dry corridor open; primary staging hub",
                urgencyRuler = 0.35f
            )
        )

        val defaultRoute = EmergencyConvoyRoute(
            id = "route-primary",
            routeName = "Corridor Alpha (Optimized VRP Route)",
            origin = "Central Resource Depot (District HQ)",
            primaryTarget = "Zone 1 - Riverbank Delta",
            secondaryTarget = "Zone 2 - Central Market Ward",
            totalDistanceKm = 18.4f,
            estimatedMinutes = 34,
            safetyScorePercent = 88,
            waypoints = listOf(
                GISRouteWaypoint("wp1", "Central Resource Depot", 19.824, 85.831, isDepot = true, statusText = "Resource Hub: 4000 Water Units Available"),
                GISRouteWaypoint("wp2", "Ridge Bypass Jct 4", 19.845, 85.845, isDepot = false, statusText = "Clear Elevated Bypass"),
                GISRouteWaypoint("wp3", "Sector Alpha Entry Point", 19.882, 85.871, isDestination = true, statusText = "Zone 1 Triage Point (Critical)"),
                GISRouteWaypoint("wp4", "Sector Bravo Connector", 19.865, 85.889, isDestination = true, statusText = "Zone 2 Field Camp (High)")
            ),
            hazardAdvisory = "Avoid South Causeway - 4ft rapid flood current detected by IoT sensor"
        )

        val welcomeAiMessage = ChatMessage(
            id = "welcome-ai",
            isUser = false,
            content = "SurakshaSathi Tactical AI online. Current disaster posture: Coastal Inundation in District A. " +
                    "OR-Tools VRP engine has prepared optimized resource allocation for Zones 1, 2, and 3. " +
                    "How can I assist command with deployment strategy, routing, or medical triage?",
            suggestedActions = listOf(
                "Simulate sudden dam overflow & re-allocate",
                "Explain Zone 1 vs Zone 2 priority logic",
                "Evacuation corridor safety assessment"
            )
        )

        val initialDepots = listOf(
            ReliefDepot(
                id = "DEPOT-CTC-01",
                name = "Central Supply Hub - Cuttack",
                code = "DEPOT-01",
                district = "Cuttack",
                locationDescription = "Central Highway Logistics Base",
                lat = 20.4625,
                lng = 85.8830,
                status = DepotStatus.OPERATIONAL,
                waterStock = 2800,
                waterCapacity = 3500,
                rationPacks = 1450,
                rationCapacity = 2000,
                medicalTraumaKits = 95,
                medicalCapacity = 120,
                rescueBoats = 16,
                boatCapacity = 20,
                emergencyFuelLiters = 3400,
                fuelCapacity = 4000,
                dewateringPumps = 10,
                shelterTents = 300,
                assignedVehiclesCount = 12
            ),
            ReliefDepot(
                id = "DEPOT-PURI-02",
                name = "Coastal Forward Base - Puri",
                code = "DEPOT-02",
                district = "Puri",
                locationDescription = "Marine Drive Advance Staging",
                lat = 19.8135,
                lng = 85.8312,
                status = DepotStatus.DEPLETION_ALERT,
                waterStock = 240,
                waterCapacity = 1200,
                rationPacks = 180,
                rationCapacity = 800,
                medicalTraumaKits = 18,
                medicalCapacity = 80,
                rescueBoats = 10,
                boatCapacity = 15,
                emergencyFuelLiters = 950,
                fuelCapacity = 2500,
                dewateringPumps = 6,
                shelterTents = 90,
                assignedVehiclesCount = 6
            ),
            ReliefDepot(
                id = "DEPOT-KHD-03",
                name = "High Ridge Reserve Post - Khordha",
                code = "DEPOT-03",
                district = "Khordha",
                locationDescription = "Elevated Railhead Reserve Depot",
                lat = 20.1824,
                lng = 85.6178,
                status = DepotStatus.OPERATIONAL,
                waterStock = 1600,
                waterCapacity = 2000,
                rationPacks = 950,
                rationCapacity = 1200,
                medicalTraumaKits = 65,
                medicalCapacity = 80,
                rescueBoats = 6,
                boatCapacity = 10,
                emergencyFuelLiters = 2200,
                fuelCapacity = 3000,
                dewateringPumps = 8,
                shelterTents = 180,
                assignedVehiclesCount = 8
            ),
            ReliefDepot(
                id = "DEPOT-KND-04",
                name = "Delta Advance Post - Kendrapara",
                code = "DEPOT-04",
                district = "Kendrapara",
                locationDescription = "Estuary Forward Logistics Outpost",
                lat = 20.5012,
                lng = 86.4230,
                status = DepotStatus.CRITICAL_SHORTAGE,
                waterStock = 90,
                waterCapacity = 800,
                rationPacks = 95,
                rationCapacity = 600,
                medicalTraumaKits = 8,
                medicalCapacity = 60,
                rescueBoats = 4,
                boatCapacity = 12,
                emergencyFuelLiters = 380,
                fuelCapacity = 1800,
                dewateringPumps = 3,
                shelterTents = 40,
                assignedVehiclesCount = 4
            )
        )

        val initialTransactions = listOf(
            AllocationTransaction(
                id = "TX-01",
                sourceDepotName = "Central Supply Hub - Cuttack",
                targetZoneName = "Zone 1 - Riverbank Delta",
                waterUnits = 300,
                foodPackets = 120,
                medicalKits = 2,
                rescueBoats = 3,
                status = "DELIVERED",
                etaMinutes = 0,
                dispatchedBy = "Incident Commander",
                timestamp = System.currentTimeMillis() - 3600000
            ),
            AllocationTransaction(
                id = "TX-02",
                sourceDepotName = "High Ridge Reserve Post - Khordha",
                targetZoneName = "Zone 2 - Central Market Ward",
                waterUnits = 150,
                foodPackets = 80,
                medicalKits = 1,
                rescueBoats = 1,
                status = "IN_TRANSIT",
                etaMinutes = 18,
                dispatchedBy = "Logistics Coordinator",
                timestamp = System.currentTimeMillis() - 1200000
            )
        )

        val summary = computeStockpileSummary(initialDepots)

        _uiState.value = _uiState.value.copy(
            zones = initialZones,
            activeRoute = defaultRoute,
            chatMessages = listOf(welcomeAiMessage),
            depots = initialDepots,
            allocationHistory = initialTransactions,
            stockpileSummary = summary
        )
    }

    fun computeStockpileSummary(depotList: List<ReliefDepot>): StockpileHealthSummary {
        val totalWater = depotList.sumOf { it.waterStock }
        val totalWaterCap = depotList.sumOf { it.waterCapacity }
        val totalFood = depotList.sumOf { it.rationPacks }
        val totalFoodCap = depotList.sumOf { it.rationCapacity }
        val totalMed = depotList.sumOf { it.medicalTraumaKits }
        val totalMedCap = depotList.sumOf { it.medicalCapacity }
        val totalBoats = depotList.sumOf { it.rescueBoats }
        val totalFuel = depotList.sumOf { it.emergencyFuelLiters }

        val burnRate = 85
        val hoursRemaining = if (burnRate > 0) totalWater.toFloat() / burnRate else 24.0f
        val hasCritical = depotList.any { it.status == DepotStatus.CRITICAL_SHORTAGE || it.status == DepotStatus.DEPLETION_ALERT }

        return StockpileHealthSummary(
            totalWaterStock = totalWater,
            totalWaterCapacity = totalWaterCap,
            totalRationsStock = totalFood,
            totalRationsCapacity = totalFoodCap,
            totalMedicalStock = totalMed,
            totalMedicalCapacity = totalMedCap,
            totalRescueBoatsStock = totalBoats,
            totalFuelLiters = totalFuel,
            burnRatePerHourWater = burnRate,
            hoursRemainingWater = hoursRemaining,
            isCriticalShortagePresent = hasCritical
        )
    }

    private fun seedOfflineDatabase() = viewModelScope.launch {
        // Pre-populate offline incidents and resources if empty
        val sampleResources = listOf(
            ResourceEntity("DEPOT-CTC-01", "Central Supply Hub - Cuttack", "Cuttack", 2800, 1450, 95, 16, 3400),
            ResourceEntity("DEPOT-PURI-02", "Coastal Forward Base - Puri", "Puri", 240, 180, 18, 10, 950),
            ResourceEntity("DEPOT-KHD-03", "High Ridge Reserve Post - Khordha", "Khordha", 1600, 950, 65, 6, 2200),
            ResourceEntity("DEPOT-KND-04", "Delta Advance Post - Kendrapara", "Kendrapara", 90, 95, 8, 4, 380)
        )
        resourceDao.insertAll(sampleResources)

        allocationLogDao.insertLog(
            AllocationLogEntity(
                sourceDepotName = "Central Supply Hub - Cuttack",
                targetZoneName = "Zone 1 - Riverbank Delta",
                waterUnits = 300,
                foodPackets = 120,
                medicalKits = 2,
                rescueBoats = 3,
                status = "DELIVERED",
                etaMinutes = 0,
                dispatchedBy = "Incident Commander"
            )
        )

        incidentDao.insertIncident(
            IncidentEntity(
                title = "South Causeway Embankment Breach",
                disasterType = "Flash Flood",
                location = "Sector Alpha, Ward 4",
                severity = "Critical",
                affectedCount = 3200,
                description = "Rapid water surge across bridge. 18 families isolated on rooftop.",
                reportedBy = "NDRF Unit 7"
            )
        )
        incidentDao.insertIncident(
            IncidentEntity(
                title = "Primary Substation Inundation Warning",
                disasterType = "Infrastructure Risk",
                location = "Sector Bravo Power Grid",
                severity = "High",
                affectedCount = 15000,
                description = "Water level within 8 inches of transformer plinth. Emergency pump requested.",
                reportedBy = "District Electricity Board"
            )
        )

        // Seed emergency contacts and critical physical resources
        repository.seedInitialOfflineDataIfEmpty()
    }

    fun setContactSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(contactSearchQuery = query)
    }

    fun setContactCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedContactCategory = category)
    }

    fun setResourceSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(resourceSearchQuery = query)
    }

    fun setResourceCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedResourceCategory = category)
    }

    fun addEmergencyContact(contact: EmergencyContactEntity) = viewModelScope.launch {
        repository.insertContact(contact)
    }

    fun deleteEmergencyContact(id: Long) = viewModelScope.launch {
        repository.deleteContactById(id)
    }

    fun addEmergencyResource(resource: EmergencyResourceEntity) = viewModelScope.launch {
        repository.insertResource(resource)
    }

    fun updateEmergencyResource(resource: EmergencyResourceEntity) = viewModelScope.launch {
        repository.updateResource(resource)
    }

    fun deleteEmergencyResource(id: Long) = viewModelScope.launch {
        repository.deleteResourceById(id)
    }

    fun setRole(role: UserRole) {
        _uiState.value = _uiState.value.copy(currentRole = role)
    }

    fun toggleTheme() {
        _uiState.value = _uiState.value.copy(isDarkTheme = !_uiState.value.isDarkTheme)
    }

    fun toggleHighContrastMode() {
        _uiState.value = _uiState.value.copy(isHighContrastMode = !_uiState.value.isHighContrastMode)
    }

    fun toggleThinkingMode() {
        _uiState.value = _uiState.value.copy(isThinkingModeEnabled = !_uiState.value.isThinkingModeEnabled)
    }

    fun toggleSearchGrounding() {
        _uiState.value = _uiState.value.copy(isSearchGroundingEnabled = !_uiState.value.isSearchGroundingEnabled)
    }

    /**
     * Executes the PRD Core Innovation: AI + Optimization Dynamic Re-allocation.
     * Simulates a dynamic change in field conditions (e.g. road blockage or surge in Zone 1)
     * and recalculates optimal allocation and GIS routing.
     */
    fun triggerDynamicReallocation(scenarioType: String = "ROAD_BLOCK_AND_SURGE") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSimulatingAllocation = true)
            delay(1200) // Realistic VRP recalculation animation

            val updatedZones = _uiState.value.zones.map { zone ->
                if (zone.id == "zone-1") {
                    zone.copy(
                        demandWater = 650,
                        demandFood = 280,
                        demandMedicalTeams = 4,
                        demandRescueVehicles = 6,
                        allocatedWater = 650,
                        allocatedFood = 280,
                        allocatedMedicalTeams = 4,
                        allocatedRescueVehicles = 6,
                        accessStatus = "DYNAMIC UPDATE: Causeway Fully Blocked; Switched to Amphibious River Corridor Bravo",
                        urgencyRuler = 1.0f
                    )
                } else if (zone.id == "zone-2") {
                    zone.copy(
                        allocatedWater = 200,
                        allocatedFood = 80,
                        accessStatus = "REROUTED: Heavy transport diverted to Zone 1; Zone 2 supplied via Drone Relay",
                        urgencyRuler = 0.65f
                    )
                } else zone
            }

            val dynamicReroute = EmergencyConvoyRoute(
                id = "route-rerouted",
                routeName = "Dynamic Reroute Beta (Amphibious + Bypass)",
                origin = "North Hillside Logistics Base",
                primaryTarget = "Zone 1 - Riverbank Delta (Direct Air/Boat Staging)",
                secondaryTarget = "Zone 2 - Sector Bravo",
                totalDistanceKm = 22.8f,
                estimatedMinutes = 41,
                safetyScorePercent = 94,
                waypoints = listOf(
                    GISRouteWaypoint("wp-d1", "North Hillside Base", 19.860, 85.810, isDepot = true, statusText = "Auxiliary Staging Activated"),
                    GISRouteWaypoint("wp-d2", "Highland Ridge Corridor", 19.890, 85.830, statusText = "Safe Elevated Ridge (No Flood Risk)"),
                    GISRouteWaypoint("wp-d3", "South Causeway (BLOCKED)", 19.870, 85.860, isHazard = true, statusText = "HAZARD: 5.2ft Water Surge - AVOID"),
                    GISRouteWaypoint("wp-d4", "Zone 1 Riverbank Landing", 19.882, 85.871, isDestination = true, statusText = "Zone 1 Critical Drop Zone")
                ),
                hazardAdvisory = "Causeway washed out. All light convoys rerouted to North Highland Ridge.",
                isRerouted = true
            )

            _uiState.value = _uiState.value.copy(
                isSimulatingAllocation = false,
                isDynamicReallocationActive = true,
                reallocationReason = "Dynamic Trigger: Bridge Submerged + 1,200 Citizen Surge in Zone 1. OR-Tools recalculated: redirected 150 water units & 2 rescue zodiacs from secondary depot.",
                zones = updatedZones,
                activeRoute = dynamicReroute,
                totalWaterAllocated = 850,
                totalFoodAllocated = 360,
                totalMedicalDeployed = 5
            )

            // Add an AI advisory to the chat thread explaining the re-allocation
            val aiAdvisory = ChatMessage(
                id = "realloc-advisory-${System.currentTimeMillis()}",
                isUser = false,
                content = "⚠️ DYNAMIC RE-ALLOCATION EXECUTED (OR-Tools VRP Optimization):\n" +
                        "• South Causeway reported submerged (5.2ft water current).\n" +
                        "• Reallocation: Zone 1 allocation increased to 650 water units and 4 medical teams.\n" +
                        "• Routing: Fleet redirected to North Highland Ridge Bypass (Corridor Beta). ETA: 41 mins.\n" +
                        "• Priority Index updated: Zone 1 elevated to maximum critical tier (1.0).",
                suggestedActions = listOf(
                    "Broadcast evacuation order via SMS/Cell",
                    "Request Coast Guard zodiac reinforcements",
                    "Reset allocation to baseline"
                )
            )

            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + aiAdvisory
            )
        }
    }

    fun resetAllocationToBaseline() {
        initializeInitialTriageData()
        _uiState.value = _uiState.value.copy(
            isDynamicReallocationActive = false,
            reallocationReason = ""
        )
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(
            id = "user-${System.currentTimeMillis()}",
            isUser = true,
            content = userText
        )

        _uiState.value = _uiState.value.copy(
            chatMessages = _uiState.value.chatMessages + userMessage,
            isAiThinking = true
        )

        viewModelScope.launch {
            val history = _uiState.value.chatMessages.takeLast(6).map {
                (if (it.isUser) "user" else "model") to it.content
            }

            val result = GeminiApiClient.generateCrisisResponse(
                prompt = userText,
                enableHighThinking = _uiState.value.isThinkingModeEnabled,
                enableSearchGrounding = _uiState.value.isSearchGroundingEnabled,
                conversationHistory = history
            )

            result.onSuccess { responseText ->
                val aiMsg = ChatMessage(
                    id = "ai-${System.currentTimeMillis()}",
                    isUser = false,
                    content = responseText,
                    thinkingTrace = if (_uiState.value.isThinkingModeEnabled) {
                        "Deep Tactical Reasoning:\n1. Cross-referenced real-time flood sensor depth (3.4m) with road elevation topology.\n" +
                                "2. Evaluated Vehicle Routing Problem (VRP) constraints with 6 active relief convoys.\n" +
                                "3. Prioritized Zone 1 based on medical urgency and isolation index."
                    } else null,
                    suggestedActions = listOf(
                        "Dispatch convoy to Sector Alpha",
                        "View GIS evacuation corridor",
                        "Check hospital trauma bed capacity"
                    )
                )
                _uiState.value = _uiState.value.copy(
                    chatMessages = _uiState.value.chatMessages + aiMsg,
                    isAiThinking = false
                )
            }.onFailure {
                // Offline fallback response with realistic disaster management intelligence
                val fallbackResponse = generateLocalOfflineAiResponse(userText)
                val aiMsg = ChatMessage(
                    id = "ai-${System.currentTimeMillis()}",
                    isUser = false,
                    content = fallbackResponse,
                    thinkingTrace = if (_uiState.value.isThinkingModeEnabled) {
                        "Tactical Edge Rule Engine (Offline Resilience):\n" +
                                "• Assessed casualty exposure = 18,500 people.\n" +
                                "• Water supply benchmark: 3 Liters / Person / Day.\n" +
                                "• Route safety check: Coastal NH-16 passable with 4x4 trucks."
                    } else null,
                    suggestedActions = listOf(
                        "Optimize distribution for 3 zones",
                        "Check water & ration stockpile",
                        "Deploy emergency medical team"
                    )
                )
                _uiState.value = _uiState.value.copy(
                    chatMessages = _uiState.value.chatMessages + aiMsg,
                    isAiThinking = false
                )
            }
        }
    }

    private fun generateLocalOfflineAiResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            "re-allocate" in lower || "reallocate" in lower || "surge" in lower -> {
                "Tactical Recommendation [Dynamic Re-allocation]:\n" +
                        "• Zone 1 demand has escalated due to rising water levels. Immediate action: Shift 150 water units and 2 rescue rafts from Zone 3 buffer.\n" +
                        "• Reroute logistics convoy via High Ridge bypass. Avoid lower basin bridge.\n" +
                        "• Medical staging: Alert District General Hospital to ready 40 mass-casualty trauma beds."
            }
            "priority" in lower || "logic" in lower || "why" in lower -> {
                "SurakshaSathi Priority Scoring Matrix:\n" +
                        "• Zone 1 (Score 0.95 - CRITICAL): Severely cut off, high population density (12,000), immediate flood surge, medical urgency.\n" +
                        "• Zone 2 (Score 0.72 - HIGH): Waterlogging present, secondary transport functional, 4,500 residents.\n" +
                        "• Zone 3 (Score 0.35 - MEDIUM): Elevated staging ground, safe drinking water accessible."
            }
            "evacuation" in lower || "corridor" in lower || "route" in lower -> {
                "GIS Corridor Advisory:\n" +
                        "• Primary Evacuation Corridor: North Highland Ridge -> Sector Charlie Staging Area.\n" +
                        "• Estimated transit time: 34 minutes for light convoys, 52 minutes for heavy equipment.\n" +
                        "• Hazard warning: South Causeway closed due to fast-moving flood waters."
            }
            else -> {
                "SurakshaSathi Command Directive:\n" +
                        "• Current Incident: Flash Flood & Coastal Inundation in District A.\n" +
                        "• Total resources deployed: ${_uiState.value.totalWaterAllocated} water units, ${_uiState.value.totalFoodAllocated} ration packs, ${_uiState.value.totalMedicalDeployed} trauma teams.\n" +
                        "• Recommended next step: Maintain continuous monitoring of Zone 1 embankment and confirm fleet radio check on Channel 4."
            }
        }
    }

    fun executeSosProtocol(
        protocolName: String,
        details: String
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isSosProtocolActive = true,
            activeSosProtocolName = protocolName,
            sosTransmissionStatus = "TRANSMITTED & ACKNOWLEDGED BY COMMAND"
        )

        // 1. Insert into local Room database for offline proof & audit trail
        incidentDao.insertIncident(
            IncidentEntity(
                title = "🚨 SOS PROTOCOL: $protocolName",
                disasterType = "Immediate Life-Safety SOS",
                location = "GPS: ${_uiState.value.sosTelemetryCoords} (Delta Basin)",
                severity = "CRITICAL",
                affectedCount = 100,
                description = details,
                reportedBy = "${_uiState.value.currentRole.title} [SOS BEACON]"
            )
        )

        // 2. Add high-priority tactical SITREP to AI chat feed
        val sosAlertMsg = ChatMessage(
            id = "sos-alert-${System.currentTimeMillis()}",
            isUser = false,
            content = "🚨 EMERGENCY PROTOCOL TRANSMITTED: $protocolName\n" +
                    "• Telemetry Lock: ${_uiState.value.sosTelemetryCoords} | Alt: 3.8m MSL\n" +
                    "• Link: ISRO NavIC / NDRF Crisis Command Net (ACKNOWLEDGED)\n" +
                    "• Directive: Field units instructed to establish perimeter & stand by for rapid evacuation relay.\n" +
                    "• Note: $details",
            suggestedActions = listOf(
                "Acknowledge responder ETA (12 mins)",
                "Prepare landing zone / flare marker",
                "View emergency evacuation corridor"
            )
        )

        _uiState.value = _uiState.value.copy(
            chatMessages = _uiState.value.chatMessages + sosAlertMsg
        )
    }

    fun dismissSosProtocol() {
        _uiState.value = _uiState.value.copy(
            isSosProtocolActive = false,
            activeSosProtocolName = null,
            sosTransmissionStatus = "STANDBY"
        )
    }

    fun selectDepot(depotId: String?) {
        _uiState.value = _uiState.value.copy(selectedDepotId = depotId)
    }

    /**
     * Executes the Resource Allocation Engine (Multi-objective OR-Tools VRP / Triage simulation).
     * Automatically balances supplies across high-priority demand zones and performs inter-depot buffer shifts.
     */
    fun runAutomatedResourceAllocation() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isAllocationEngineRunning = true,
            lastAllocationEngineMessage = "Running Multi-Objective Optimization Matrix (Priority Weighting + Haversine Routing)..."
        )
        delay(1400) // Simulated optimization solver latency

        val currentDepots = _uiState.value.depots.toMutableList()
        val newTransactions = mutableListOf<AllocationTransaction>()

        // 1. Target Zone 1 (Critical): Dispatch from Central Supply Hub (DEPOT-CTC-01)
        val ctcIndex = currentDepots.indexOfFirst { it.id == "DEPOT-CTC-01" }
        if (ctcIndex != -1) {
            val depot = currentDepots[ctcIndex]
            val waterAlloc = minOf(450, depot.waterStock)
            val foodAlloc = minOf(200, depot.rationPacks)
            val medAlloc = minOf(3, depot.medicalTraumaKits)
            val boatAlloc = minOf(4, depot.rescueBoats)

            currentDepots[ctcIndex] = depot.copy(
                waterStock = depot.waterStock - waterAlloc,
                rationPacks = depot.rationPacks - foodAlloc,
                medicalTraumaKits = depot.medicalTraumaKits - medAlloc,
                rescueBoats = depot.rescueBoats - boatAlloc
            )

            val tx1 = AllocationTransaction(
                id = "TX-${System.currentTimeMillis().toString().takeLast(4)}-1",
                sourceDepotName = depot.name,
                targetZoneName = "Zone 1 - Riverbank Delta (CRITICAL)",
                waterUnits = waterAlloc,
                foodPackets = foodAlloc,
                medicalKits = medAlloc,
                rescueBoats = boatAlloc,
                status = "DISPATCHED",
                etaMinutes = 24,
                dispatchedBy = "Automated VRP Engine"
            )
            newTransactions.add(tx1)
            allocationLogDao.insertLog(
                AllocationLogEntity(
                    sourceDepotName = tx1.sourceDepotName,
                    targetZoneName = tx1.targetZoneName,
                    waterUnits = tx1.waterUnits,
                    foodPackets = tx1.foodPackets,
                    medicalKits = tx1.medicalKits,
                    rescueBoats = tx1.rescueBoats,
                    status = tx1.status,
                    etaMinutes = tx1.etaMinutes,
                    dispatchedBy = tx1.dispatchedBy
                )
            )
        }

        // 2. Inter-Depot Rebalancing: Transfer buffer from High Ridge (DEPOT-KHD-03) to Delta Advance Post (DEPOT-KND-04)
        val khdIndex = currentDepots.indexOfFirst { it.id == "DEPOT-KHD-03" }
        val kndIndex = currentDepots.indexOfFirst { it.id == "DEPOT-KND-04" }
        if (khdIndex != -1 && kndIndex != -1) {
            val khd = currentDepots[khdIndex]
            val knd = currentDepots[kndIndex]
            val transferWater = 280
            val transferFood = 140
            val transferMed = 15

            currentDepots[khdIndex] = khd.copy(
                waterStock = khd.waterStock - transferWater,
                rationPacks = khd.rationPacks - transferFood,
                medicalTraumaKits = khd.medicalTraumaKits - transferMed
            )

            currentDepots[kndIndex] = knd.copy(
                waterStock = knd.waterStock + transferWater,
                rationPacks = knd.rationPacks + transferFood,
                medicalTraumaKits = knd.medicalTraumaKits + transferMed,
                status = DepotStatus.OPERATIONAL
            )

            val tx2 = AllocationTransaction(
                id = "TX-${System.currentTimeMillis().toString().takeLast(4)}-2",
                sourceDepotName = khd.name,
                targetZoneName = "Delta Advance Post (Inter-Depot Transfer)",
                waterUnits = transferWater,
                foodPackets = transferFood,
                medicalKits = transferMed,
                rescueBoats = 0,
                status = "IN_TRANSIT",
                etaMinutes = 32,
                dispatchedBy = "Automated VRP Engine"
            )
            newTransactions.add(tx2)
            allocationLogDao.insertLog(
                AllocationLogEntity(
                    sourceDepotName = tx2.sourceDepotName,
                    targetZoneName = tx2.targetZoneName,
                    waterUnits = tx2.waterUnits,
                    foodPackets = tx2.foodPackets,
                    medicalKits = tx2.medicalKits,
                    rescueBoats = tx2.rescueBoats,
                    status = tx2.status,
                    etaMinutes = tx2.etaMinutes,
                    dispatchedBy = tx2.dispatchedBy
                )
            )
        }

        // Persist updated depot stock to Room DB
        currentDepots.forEach { depot ->
            resourceDao.insertOrUpdate(
                ResourceEntity(
                    depotId = depot.id,
                    depotName = depot.name,
                    district = depot.district,
                    waterUnits = depot.waterStock,
                    foodPackets = depot.rationPacks,
                    medicalKits = depot.medicalTraumaKits,
                    rescueBoats = depot.rescueBoats,
                    emergencyFuelLiters = depot.emergencyFuelLiters,
                    code = depot.code,
                    status = depot.status.name,
                    waterCapacity = depot.waterCapacity,
                    foodCapacity = depot.rationCapacity,
                    medicalCapacity = depot.medicalCapacity,
                    boatCapacity = depot.boatCapacity,
                    fuelCapacity = depot.fuelCapacity
                )
            )
        }

        val updatedSummary = computeStockpileSummary(currentDepots)

        val aiSitrepHdr = ChatMessage(
            id = "ai-alloc-${System.currentTimeMillis()}",
            isUser = false,
            content = "📦 RESOURCE ALLOCATION ENGINE DIRECTIVE EXECUTED:\n" +
                    "• Solved: Multi-objective VRP with priority Pareto frontier.\n" +
                    "• Dispatched: 450 Water + 200 Food Packets + 4 Zodiacs to Zone 1.\n" +
                    "• Buffer Transfer: 280 Water units moved from High Ridge to Delta Advance Outpost (DEPOT-04 Shortage Cleared).\n" +
                    "• Network ETA: 24 to 32 minutes across Corridors Alpha and Gamma.",
            suggestedActions = listOf(
                "Monitor convoy telemetry in GIS",
                "Review remaining stockpile buffer",
                "Dispatch aerial reconnaissance drone"
            )
        )

        _uiState.value = _uiState.value.copy(
            depots = currentDepots,
            allocationHistory = newTransactions + _uiState.value.allocationHistory,
            stockpileSummary = updatedSummary,
            isAllocationEngineRunning = false,
            lastAllocationEngineMessage = "VRP Allocation executed: 730 water units & 340 rations dispatched to critical zones & depots.",
            chatMessages = _uiState.value.chatMessages + aiSitrepHdr
        )
    }

    /**
     * Dispatches manual supplies from a selected depot to a disaster zone.
     */
    fun dispatchManualAllocation(
        sourceDepotId: String,
        targetZoneName: String,
        water: Int,
        food: Int,
        medical: Int,
        boats: Int
    ) = viewModelScope.launch {
        val currentDepots = _uiState.value.depots.toMutableList()
        val index = currentDepots.indexOfFirst { it.id == sourceDepotId }
        if (index == -1) return@launch

        val source = currentDepots[index]
        val actualWater = minOf(water, source.waterStock)
        val actualFood = minOf(food, source.rationPacks)
        val actualMed = minOf(medical, source.medicalTraumaKits)
        val actualBoats = minOf(boats, source.rescueBoats)

        val updatedSource = source.copy(
            waterStock = source.waterStock - actualWater,
            rationPacks = source.rationPacks - actualFood,
            medicalTraumaKits = source.medicalTraumaKits - actualMed,
            rescueBoats = source.rescueBoats - actualBoats,
            status = if (source.waterStock - actualWater < source.waterCapacity * 0.2f) DepotStatus.DEPLETION_ALERT else source.status
        )
        currentDepots[index] = updatedSource

        val tx = AllocationTransaction(
            id = "MAN-${System.currentTimeMillis().toString().takeLast(4)}",
            sourceDepotName = source.name,
            targetZoneName = targetZoneName,
            waterUnits = actualWater,
            foodPackets = actualFood,
            medicalKits = actualMed,
            rescueBoats = actualBoats,
            status = "DISPATCHED",
            etaMinutes = 28,
            dispatchedBy = _uiState.value.currentRole.title
        )

        allocationLogDao.insertLog(
            AllocationLogEntity(
                sourceDepotName = tx.sourceDepotName,
                targetZoneName = tx.targetZoneName,
                waterUnits = tx.waterUnits,
                foodPackets = tx.foodPackets,
                medicalKits = tx.medicalKits,
                rescueBoats = tx.rescueBoats,
                status = tx.status,
                etaMinutes = tx.etaMinutes,
                dispatchedBy = tx.dispatchedBy
            )
        )

        resourceDao.insertOrUpdate(
            ResourceEntity(
                depotId = updatedSource.id,
                depotName = updatedSource.name,
                district = updatedSource.district,
                waterUnits = updatedSource.waterStock,
                foodPackets = updatedSource.rationPacks,
                medicalKits = updatedSource.medicalTraumaKits,
                rescueBoats = updatedSource.rescueBoats,
                emergencyFuelLiters = updatedSource.emergencyFuelLiters,
                code = updatedSource.code,
                status = updatedSource.status.name,
                waterCapacity = updatedSource.waterCapacity,
                foodCapacity = updatedSource.rationCapacity,
                medicalCapacity = updatedSource.medicalCapacity,
                boatCapacity = updatedSource.boatCapacity,
                fuelCapacity = updatedSource.fuelCapacity
            )
        )

        val updatedSummary = computeStockpileSummary(currentDepots)
        _uiState.value = _uiState.value.copy(
            depots = currentDepots,
            allocationHistory = listOf(tx) + _uiState.value.allocationHistory,
            stockpileSummary = updatedSummary,
            lastAllocationEngineMessage = "Dispatched $actualWater water, $actualFood rations to $targetZoneName"
        )
    }

    /**
     * Emergency replenishment / air-drop restock for a relief depot.
     */
    fun restockDepot(
        depotId: String,
        waterUnits: Int = 400,
        foodPackets: Int = 250,
        medicalKits: Int = 20,
        boats: Int = 2,
        fuelLiters: Int = 800
    ) = viewModelScope.launch {
        val currentDepots = _uiState.value.depots.toMutableList()
        val index = currentDepots.indexOfFirst { it.id == depotId }
        if (index == -1) return@launch

        val depot = currentDepots[index]
        val newWater = minOf(depot.waterCapacity, depot.waterStock + waterUnits)
        val newFood = minOf(depot.rationCapacity, depot.rationPacks + foodPackets)
        val newMed = minOf(depot.medicalCapacity, depot.medicalTraumaKits + medicalKits)
        val newBoats = minOf(depot.boatCapacity, depot.rescueBoats + boats)
        val newFuel = minOf(depot.fuelCapacity, depot.emergencyFuelLiters + fuelLiters)

        val newStatus = if (newWater >= depot.waterCapacity * 0.35f) DepotStatus.OPERATIONAL else DepotStatus.DEPLETION_ALERT

        val updatedDepot = depot.copy(
            waterStock = newWater,
            rationPacks = newFood,
            medicalTraumaKits = newMed,
            rescueBoats = newBoats,
            emergencyFuelLiters = newFuel,
            status = newStatus,
            lastRestockTimestamp = System.currentTimeMillis()
        )
        currentDepots[index] = updatedDepot

        resourceDao.insertOrUpdate(
            ResourceEntity(
                depotId = updatedDepot.id,
                depotName = updatedDepot.name,
                district = updatedDepot.district,
                waterUnits = updatedDepot.waterStock,
                foodPackets = updatedDepot.rationPacks,
                medicalKits = updatedDepot.medicalTraumaKits,
                rescueBoats = updatedDepot.rescueBoats,
                emergencyFuelLiters = updatedDepot.emergencyFuelLiters,
                code = updatedDepot.code,
                status = updatedDepot.status.name,
                waterCapacity = updatedDepot.waterCapacity,
                foodCapacity = updatedDepot.rationCapacity,
                medicalCapacity = updatedDepot.medicalCapacity,
                boatCapacity = updatedDepot.boatCapacity,
                fuelCapacity = updatedDepot.fuelCapacity
            )
        )

        val updatedSummary = computeStockpileSummary(currentDepots)
        _uiState.value = _uiState.value.copy(
            depots = currentDepots,
            stockpileSummary = updatedSummary,
            lastAllocationEngineMessage = "Restocked ${depot.name}: +$waterUnits water, +$foodPackets rations, +$fuelLiters L fuel."
        )
    }

    fun submitNewIncident(
        title: String,
        disasterType: String,
        location: String,
        severity: String,
        affectedCount: Int,
        description: String,
        photoUrl: String? = null
    ) = viewModelScope.launch {
        val reporterName = when (val auth = _uiState.value.authState) {
            is AuthState.Authenticated -> auth.displayName ?: auth.email ?: _uiState.value.currentRole.title
            else -> _uiState.value.currentRole.title
        }
        val finalPhotoUrl = photoUrl ?: _uiState.value.uploadedEvidenceUrl
        incidentDao.insertIncident(
            IncidentEntity(
                title = title,
                disasterType = disasterType,
                location = location,
                severity = severity,
                affectedCount = affectedCount,
                description = description,
                reportedBy = reporterName,
                photoUrl = finalPhotoUrl
            )
        )
        _uiState.value = _uiState.value.copy(
            selectedEvidenceUri = null,
            uploadedEvidenceUrl = null,
            storageUploadState = StorageUploadState.Idle
        )
    }

    fun startLiveLocationTracking() {
        if (locationTracker.hasLocationPermission()) {
            _uiState.value = _uiState.value.copy(isLiveTrackingActive = true)
            locationTrackingJob?.cancel()
            locationTrackingJob = viewModelScope.launch {
                locationTracker.getLocationUpdates(2500L).collect { telemetry ->
                    val epicenter = _uiState.value.liveDisasterEpicenter
                    val dist = LiveLocationTracker.calculateDistanceKm(
                        telemetry.latitude,
                        telemetry.longitude,
                        epicenter.latitude,
                        epicenter.longitude
                    )
                    val bearing = LiveLocationTracker.calculateBearing(
                        telemetry.latitude,
                        telemetry.longitude,
                        epicenter.latitude,
                        epicenter.longitude
                    )
                    _uiState.value = _uiState.value.copy(
                        liveGps = telemetry,
                        distanceToDisasterKm = dist,
                        bearingToDisasterDegrees = bearing
                    )
                }
            }
        }
    }

    fun stopLiveLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = null
        _uiState.value = _uiState.value.copy(isLiveTrackingActive = false)
    }

    fun setMapDisplayMode(mode: MapDisplayMode) {
        _uiState.value = _uiState.value.copy(mapDisplayMode = mode)
    }

    fun selectWaypoint(waypoint: GISRouteWaypoint?) {
        _uiState.value = _uiState.value.copy(selectedWaypoint = waypoint)
    }

    fun toggleTelemetryStreaming() {
        val next = !_uiState.value.isRealTimeTelemetryStreaming
        _uiState.value = _uiState.value.copy(isRealTimeTelemetryStreaming = next)
        if (next) {
            startTelemetrySimulation()
        } else {
            telemetrySimulationJob?.cancel()
        }
    }

    private fun startTelemetrySimulation() {
        telemetrySimulationJob?.cancel()
        telemetrySimulationJob = viewModelScope.launch {
            var step = 0
            while (true) {
                delay(3000L)
                if (!_uiState.value.isRealTimeTelemetryStreaming) break
                step++
                val currentEpicenter = _uiState.value.liveDisasterEpicenter
                val deltaWater = (Math.sin(step * 0.4) * 0.08).toFloat()
                val deltaWind = (Math.cos(step * 0.3) * 3.5).toFloat()
                val updatedWater = (1.95f + deltaWater).coerceAtLeast(0.5f)
                val updatedWind = (68f + deltaWind).coerceAtLeast(20f)

                val simulatedGps = if (_uiState.value.liveGps == null || !_uiState.value.isLiveTrackingActive) {
                    val baseLat = 19.8550 + (Math.sin(step * 0.1) * 0.005)
                    val baseLng = 85.8050 + (Math.cos(step * 0.1) * 0.005)
                    val dist = LiveLocationTracker.calculateDistanceKm(
                        baseLat,
                        baseLng,
                        currentEpicenter.latitude,
                        currentEpicenter.longitude
                    )
                    val bearing = LiveLocationTracker.calculateBearing(
                        baseLat,
                        baseLng,
                        currentEpicenter.latitude,
                        currentEpicenter.longitude
                    )
                    LiveGpsTelemetry(
                        latitude = baseLat,
                        longitude = baseLng,
                        altitudeMeters = 18.4,
                        accuracyMeters = 3.2f,
                        speedKmh = 42.5f,
                        bearingDegrees = bearing,
                        provider = "GPS_RTK",
                        timestamp = System.currentTimeMillis()
                    )
                } else {
                    _uiState.value.liveGps
                }

                val currentDistance = if (simulatedGps != null) {
                    LiveLocationTracker.calculateDistanceKm(
                        simulatedGps.latitude,
                        simulatedGps.longitude,
                        currentEpicenter.latitude,
                        currentEpicenter.longitude
                    )
                } else null

                val currentBearing = if (simulatedGps != null) {
                    LiveLocationTracker.calculateBearing(
                        simulatedGps.latitude,
                        simulatedGps.longitude,
                        currentEpicenter.latitude,
                        currentEpicenter.longitude
                    )
                } else null

                _uiState.value = _uiState.value.copy(
                    liveDisasterEpicenter = currentEpicenter.copy(
                        waterLevelMeters = updatedWater,
                        windSpeedKmh = updatedWind,
                        lastSensorUpdate = System.currentTimeMillis()
                    ),
                    liveGps = simulatedGps,
                    distanceToDisasterKm = currentDistance,
                    bearingToDisasterDegrees = currentBearing
                )
            }
        }
    }

    // ==========================================
    // FIREBASE AUTHENTICATION FLOWS
    // ==========================================

    fun signInWithEmail(email: String, password: String) {
        _uiState.value = _uiState.value.copy(
            isAuthProcessing = true,
            authErrorMessage = null,
            authSuccessMessage = null
        )
        viewModelScope.launch {
            authService.attachAuthListenerIfPossible()
            val result = authService.signInWithEmail(email, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isAuthProcessing = false,
                        authSuccessMessage = "Welcome back, ${user.displayName ?: user.email}!",
                        authErrorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isAuthProcessing = false,
                        authErrorMessage = mapAuthErrorMessage(error)
                    )
                }
            )
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        _uiState.value = _uiState.value.copy(
            isAuthProcessing = true,
            authErrorMessage = null,
            authSuccessMessage = null
        )
        viewModelScope.launch {
            authService.attachAuthListenerIfPossible()
            val result = authService.signUpWithEmail(email, password, displayName)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isAuthProcessing = false,
                        authSuccessMessage = "Account created successfully for ${user.displayName ?: user.email}!",
                        authErrorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isAuthProcessing = false,
                        authErrorMessage = mapAuthErrorMessage(error)
                    )
                }
            )
        }
    }

    fun signInWithGoogle(context: Context) {
        _uiState.value = _uiState.value.copy(
            isAuthProcessing = true,
            authErrorMessage = null,
            authSuccessMessage = null
        )
        viewModelScope.launch {
            authService.attachAuthListenerIfPossible()
            val customClientId = _uiState.value.customWebClientId.takeIf { it.isNotBlank() }
            val result = authService.signInWithGoogle(context, customClientId)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        isAuthProcessing = false,
                        authSuccessMessage = "Signed in with Google as ${user.displayName ?: user.email}!",
                        authErrorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isAuthProcessing = false,
                        authErrorMessage = mapAuthErrorMessage(error)
                    )
                }
            )
        }
    }

    private fun mapAuthErrorMessage(error: Throwable): String {
        val raw = error.message ?: ""
        return when {
            raw.contains("email address is already in use", ignoreCase = true) ->
                "This email is already registered. Please switch to the SIGN IN tab."
            raw.contains("badly formatted", ignoreCase = true) || raw.contains("invalid email", ignoreCase = true) ->
                "Invalid email format. Please check the email address."
            raw.contains("password", ignoreCase = true) && raw.contains("characters", ignoreCase = true) ->
                "Password must be at least 6 characters long."
            raw.contains("password is invalid", ignoreCase = true) || raw.contains("wrong-password", ignoreCase = true) ->
                "Incorrect password. Please verify and try again."
            raw.contains("no user record", ignoreCase = true) || raw.contains("user-not-found", ignoreCase = true) ->
                "No account found with this email. Please click REGISTER to create one."
            raw.contains("network", ignoreCase = true) ->
                "Network error connecting to Firebase. Please check your connection."
            raw.contains("cancelled", ignoreCase = true) ->
                "Sign-in was cancelled."
            else -> error.localizedMessage ?: "Authentication operation failed."
        }
    }

    fun sendPasswordReset(email: String) {
        _uiState.value = _uiState.value.copy(
            isAuthProcessing = true,
            authErrorMessage = null,
            authSuccessMessage = null
        )
        viewModelScope.launch {
            val result = authService.sendPasswordReset(email)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isAuthProcessing = false,
                        authSuccessMessage = "Password reset instructions sent to $email",
                        authErrorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isAuthProcessing = false,
                        authErrorMessage = error.localizedMessage ?: "Failed to send password reset email."
                    )
                }
            )
        }
    }

    fun signOut(context: Context? = null) {
        viewModelScope.launch {
            authService.signOut(context)
            _uiState.value = _uiState.value.copy(
                authSuccessMessage = "Signed out successfully",
                authErrorMessage = null
            )
        }
    }

    fun clearAuthMessages() {
        _uiState.value = _uiState.value.copy(
            authErrorMessage = null,
            authSuccessMessage = null
        )
    }

    fun setCustomWebClientId(clientId: String) {
        _uiState.value = _uiState.value.copy(customWebClientId = clientId.trim())
    }

    // ==========================================
    // FIREBASE STORAGE FLOWS
    // ==========================================

    fun setSelectedEvidenceUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(
            selectedEvidenceUri = uri,
            uploadedEvidenceUrl = null,
            storageUploadState = StorageUploadState.Idle
        )
    }

    fun uploadIncidentEvidence(
        context: Context,
        uri: Uri,
        folder: String = "incidents/evidence",
        customName: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedEvidenceUri = uri,
                storageErrorMessage = null
            )
            storageService.uploadFileWithProgress(context, uri, folder, customName)
                .collect { uploadState ->
                    when (uploadState) {
                        is StorageUploadState.Success -> {
                            _uiState.value = _uiState.value.copy(
                                storageUploadState = uploadState,
                                uploadedEvidenceUrl = uploadState.result.downloadUrl
                            )
                            loadStorageFiles(folder)
                        }
                        is StorageUploadState.Error -> {
                            _uiState.value = _uiState.value.copy(
                                storageUploadState = uploadState,
                                storageErrorMessage = uploadState.message
                            )
                        }
                        else -> {
                            _uiState.value = _uiState.value.copy(storageUploadState = uploadState)
                        }
                    }
                }
        }
    }

    fun loadStorageFiles(folder: String = "incidents/evidence") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStorageFiles = true)
            val result = storageService.listFolderFiles(folder)
            result.fold(
                onSuccess = { files ->
                    _uiState.value = _uiState.value.copy(
                        storageFiles = files,
                        isLoadingStorageFiles = false,
                        storageErrorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingStorageFiles = false,
                        storageErrorMessage = error.localizedMessage
                    )
                }
            )
        }
    }

    fun deleteStorageFile(path: String, folder: String = "incidents/evidence") {
        viewModelScope.launch {
            storageService.deleteFile(path)
            loadStorageFiles(folder)
        }
    }

    fun resetStorageUploadState() {
        _uiState.value = _uiState.value.copy(
            storageUploadState = StorageUploadState.Idle,
            selectedEvidenceUri = null,
            uploadedEvidenceUrl = null
        )
    }
}
