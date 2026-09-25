package com.example.data.repository

import com.example.data.local.AllocationLogDao
import com.example.data.local.AllocationLogEntity
import com.example.data.local.EmergencyContactDao
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.EmergencyResourceDao
import com.example.data.local.EmergencyResourceEntity
import com.example.data.local.IncidentDao
import com.example.data.local.IncidentEntity
import com.example.data.local.ResourceDao
import com.example.data.local.ResourceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository pattern implementation for Room offline data access.
 * Abstracts local database operations for emergency contacts, critical resources,
 * incident logs, and relief stockpile inventory.
 */
class OfflineEmergencyRepository(
    private val incidentDao: IncidentDao,
    private val resourceDao: ResourceDao,
    private val allocationLogDao: AllocationLogDao,
    private val emergencyContactDao: EmergencyContactDao,
    private val emergencyResourceDao: EmergencyResourceDao
) {
    // --- Emergency Contacts Flow & Operations ---
    val allContacts: Flow<List<EmergencyContactEntity>> = emergencyContactDao.getAllContacts()

    fun getContactsByCategory(category: String): Flow<List<EmergencyContactEntity>> =
        emergencyContactDao.getContactsByCategory(category)

    fun getContactsByDistrict(district: String): Flow<List<EmergencyContactEntity>> =
        emergencyContactDao.getContactsByDistrict(district)

    fun searchContacts(query: String): Flow<List<EmergencyContactEntity>> =
        emergencyContactDao.searchContacts(query)

    suspend fun insertContact(contact: EmergencyContactEntity): Long =
        emergencyContactDao.insertContact(contact)

    suspend fun updateContact(contact: EmergencyContactEntity) =
        emergencyContactDao.updateContact(contact)

    suspend fun deleteContactById(id: Long) =
        emergencyContactDao.deleteContactById(id)

    suspend fun getContactCount(): Int =
        emergencyContactDao.getContactCount()

    // --- Emergency Resources Flow & Operations ---
    val allResources: Flow<List<EmergencyResourceEntity>> = emergencyResourceDao.getAllResources()

    val criticalResources: Flow<List<EmergencyResourceEntity>> = emergencyResourceDao.getCriticalResources()

    fun getResourcesByCategory(category: String): Flow<List<EmergencyResourceEntity>> =
        emergencyResourceDao.getResourcesByCategory(category)

    fun getResourcesByDistrict(district: String): Flow<List<EmergencyResourceEntity>> =
        emergencyResourceDao.getResourcesByDistrict(district)

    fun searchResources(query: String): Flow<List<EmergencyResourceEntity>> =
        emergencyResourceDao.searchResources(query)

    suspend fun insertResource(resource: EmergencyResourceEntity): Long =
        emergencyResourceDao.insertResource(resource)

    suspend fun updateResource(resource: EmergencyResourceEntity) =
        emergencyResourceDao.updateResource(resource)

    suspend fun deleteResourceById(id: Long) =
        emergencyResourceDao.deleteResourceById(id)

    suspend fun getResourceCount(): Int =
        emergencyResourceDao.getResourceCount()

    // --- Depots & Stockpiles Operations ---
    val allDepotResources: Flow<List<ResourceEntity>> = resourceDao.getAllResources()

    suspend fun insertOrUpdateDepot(resource: ResourceEntity) =
        resourceDao.insertOrUpdate(resource)

    suspend fun insertAllDepots(resources: List<ResourceEntity>) =
        resourceDao.insertAll(resources)

    // --- Incidents Operations ---
    val allIncidents: Flow<List<IncidentEntity>> = incidentDao.getAllIncidents()

    suspend fun insertIncident(incident: IncidentEntity): Long =
        incidentDao.insertIncident(incident)

    // --- Allocation Logs Operations ---
    val allAllocationLogs: Flow<List<AllocationLogEntity>> = allocationLogDao.getAllAllocationLogs()

    suspend fun insertAllocationLog(log: AllocationLogEntity): Long =
        allocationLogDao.insertLog(log)

    /**
     * Seeds realistic, battle-tested offline emergency contacts and critical physical
     * resources if local Room database tables are empty.
     */
    suspend fun seedInitialOfflineDataIfEmpty() {
        if (emergencyContactDao.getContactCount() == 0) {
            val defaultContacts = listOf(
                EmergencyContactEntity(
                    name = "State Disaster Management Authority (SDMA) Control Room",
                    organization = "Odisha State Disaster Management Authority",
                    category = "DISASTER_MGMT",
                    primaryPhone = "1070",
                    alternatePhone = "0674-2395398",
                    email = "sdma.control@odisha.gov.in",
                    district = "Statewide",
                    address = "Rajiv Bhawan, Ground Floor, Bhubaneswar",
                    operationalHours = "24x7 Emergency Operations",
                    isTollFree = true,
                    priorityRank = 1,
                    notes = "Central nodal authority for all cyclone, flood, and meteorological alerts"
                ),
                EmergencyContactEntity(
                    name = "NDRF 3rd Battalion Disaster Response Cell",
                    organization = "National Disaster Response Force (Mundali Base)",
                    category = "MILITARY_NDRF",
                    primaryPhone = "0671-2879710",
                    alternatePhone = "+91 94379 64201",
                    email = "cmd-03ndrf@nic.in",
                    district = "Statewide",
                    address = "Mundali, Cuttack - 754006",
                    operationalHours = "24x7 Quick Reaction Force",
                    isTollFree = false,
                    priorityRank = 1,
                    notes = "Equipped with motorized flood rescue boats, deep-divers, and canine search units"
                ),
                EmergencyContactEntity(
                    name = "Emergency Response Support System (ERSS)",
                    organization = "State Police & First Responder Dispatch",
                    category = "POLICE",
                    primaryPhone = "112",
                    alternatePhone = "100",
                    email = "erss112@odisha.gov.in",
                    district = "Statewide",
                    address = "Police Commissionerate Headquarters",
                    operationalHours = "24x7 Unified Helpline",
                    isTollFree = true,
                    priorityRank = 1,
                    notes = "Unified emergency dispatch for police, rescue, fire, and medical emergency escalation"
                ),
                EmergencyContactEntity(
                    name = "State Emergency Medical Ambulance Service",
                    organization = "Directorate of Health Services (EMRI 108)",
                    category = "MEDICAL",
                    primaryPhone = "108",
                    alternatePhone = "102",
                    email = "ambulance108@odisha.gov.in",
                    district = "Statewide",
                    address = "State Health Directorate, Unit 3",
                    operationalHours = "24x7 Advanced Life Support",
                    isTollFree = true,
                    priorityRank = 1,
                    notes = "Critical patient evacuation, boat ambulances for coastal inundated sectors"
                ),
                EmergencyContactEntity(
                    name = "Odisha Fire & Emergency Disaster Service",
                    organization = "Directorate of Fire Services",
                    category = "FIRE_RESCUE",
                    primaryPhone = "101",
                    alternatePhone = "0671-2300311",
                    email = "firehq.od@nic.in",
                    district = "Statewide",
                    address = "Barabati Fort Area, Cuttack",
                    operationalHours = "24x7 Fire & Urban Search",
                    isTollFree = true,
                    priorityRank = 1,
                    notes = "Equipped with high-pressure dewatering pumps, tree cutters, and inflatable safety rafts"
                ),
                EmergencyContactEntity(
                    name = "District Emergency Operations Center (DEOC Cuttack)",
                    organization = "District Collectorate & Magistrate Office",
                    category = "DISTRICT_ADMIN",
                    primaryPhone = "1077",
                    alternatePhone = "0671-2508101",
                    email = "dm-cuttack@nic.in",
                    district = "Cuttack",
                    address = "Collectorate Campus, Chandni Chowk, Cuttack",
                    operationalHours = "24x7 Crisis Cell",
                    isTollFree = true,
                    priorityRank = 2,
                    notes = "Direct coordination cell for river basin embankments, relief camps, and logistics"
                ),
                EmergencyContactEntity(
                    name = "Coastal Disaster & Marine Police Wing",
                    organization = "Indian Coast Guard & Coastal Police",
                    category = "MILITARY_NDRF",
                    primaryPhone = "1554",
                    alternatePhone = "06722-220023",
                    email = "coastguard-paradip@nic.in",
                    district = "Puri",
                    address = "Coast Guard District HQ 7, Paradip/Puri Forward Station",
                    operationalHours = "24x7 Maritime Search & Rescue",
                    isTollFree = true,
                    priorityRank = 2,
                    notes = "Offshore and estuarine flood search, maritime helicopter surveillance support"
                ),
                EmergencyContactEntity(
                    name = "State Flood Control & Water Resources Hydrology",
                    organization = "Department of Water Resources",
                    category = "DISASTER_MGMT",
                    primaryPhone = "0674-2391060",
                    alternatePhone = "0674-2391300",
                    email = "floodcontrol.od@nic.in",
                    district = "Statewide",
                    address = "Secha Sadan, Unit 5, Bhubaneswar",
                    operationalHours = "24x7 River Gauge Monitoring",
                    isTollFree = false,
                    priorityRank = 2,
                    notes = "Mahanadi, Kathajodi, and Baitarani river discharge monitoring & barrage gate telemetry"
                )
            )
            emergencyContactDao.insertAllContacts(defaultContacts)
        }

        if (emergencyResourceDao.getResourceCount() == 0) {
            val defaultResources = listOf(
                EmergencyResourceEntity(
                    name = "High-Capacity Dewatering Pumps (50HP Diesel)",
                    category = "HEAVY_MACHINERY",
                    totalQuantity = 24,
                    availableQuantity = 18,
                    unit = "Units",
                    locationName = "Cuttack Municipal Central Warehouse",
                    district = "Cuttack",
                    lat = 20.4640,
                    lng = 85.8790,
                    custodianName = "Er. Manas Ranjan Jena (Executive Engineer)",
                    custodianPhone = "+91 94371 88204",
                    conditionStatus = "OPERATIONAL",
                    deploymentStatus = "READY_FOR_DISPATCH",
                    isCritical = true,
                    description = "Mobile diesel pump sets capable of 12,000 Liters/min for submerged causeways and hospitals."
                ),
                EmergencyResourceEntity(
                    name = "Zodiac Inflatable Rescue Boats (with 40HP OBM)",
                    category = "RESCUE_GEAR",
                    totalQuantity = 32,
                    availableQuantity = 22,
                    unit = "Boats",
                    locationName = "Puri Coastal Quick Reaction Depot",
                    district = "Puri",
                    lat = 19.8150,
                    lng = 85.8320,
                    custodianName = "Sub-Inspector T. K. Mohapatra (Marine Unit)",
                    custodianPhone = "+91 98610 44521",
                    conditionStatus = "OPERATIONAL",
                    deploymentStatus = "READY_FOR_DISPATCH",
                    isCritical = true,
                    description = "Heavy-duty inflatable puncture-resistant hulls with 8-person casualty capacity each."
                ),
                EmergencyResourceEntity(
                    name = "Mobile Reverse Osmosis Emergency Water Purifier",
                    category = "WATER_SANITATION",
                    totalQuantity = 10,
                    availableQuantity = 8,
                    unit = "Units",
                    locationName = "Khordha Public Health Engineering Hub",
                    district = "Khordha",
                    lat = 20.1800,
                    lng = 85.6200,
                    custodianName = "Dr. S. K. Patnaik (PHED Officer)",
                    custodianPhone = "+91 94370 12903",
                    conditionStatus = "OPERATIONAL",
                    deploymentStatus = "READY_FOR_DISPATCH",
                    isCritical = true,
                    description = "Solar-diesel hybrid mobile filtration plants providing 10,000 Liters/hour potable drinking water."
                ),
                EmergencyResourceEntity(
                    name = "Field Trauma First-Response Mobile ICU Tents",
                    category = "MEDICAL",
                    totalQuantity = 16,
                    availableQuantity = 12,
                    unit = "Kits",
                    locationName = "SCB Medical College Disaster Annex",
                    district = "Cuttack",
                    lat = 20.4720,
                    lng = 85.8900,
                    custodianName = "Dr. Anita Priyadarshini (Chief Triage Surgeon)",
                    custodianPhone = "+91 94372 90114",
                    conditionStatus = "OPERATIONAL",
                    deploymentStatus = "READY_FOR_DISPATCH",
                    isCritical = true,
                    description = "Inflatable hermetic surgical field hospitals with portable ventilators, defibrillators, and triage packs."
                ),
                EmergencyResourceEntity(
                    name = "Silent Mobile Diesel Generators (125 kVA)",
                    category = "POWER_COMMUNICATION",
                    totalQuantity = 20,
                    availableQuantity = 15,
                    unit = "Sets",
                    locationName = "Kendrapara Rural Electrification Depot",
                    district = "Kendrapara",
                    lat = 20.5010,
                    lng = 86.4220,
                    custodianName = "A. K. Biswal (Grid Reliability Lead)",
                    custodianPhone = "+91 98612 33490",
                    conditionStatus = "OPERATIONAL",
                    deploymentStatus = "READY_FOR_DISPATCH",
                    isCritical = true,
                    description = "Heavy 3-phase sound-attenuated gen-sets for emergency shelters and water treatment stations."
                ),
                EmergencyResourceEntity(
                    name = "Satellite Disaster Terminals & BGAN Handsets",
                    category = "POWER_COMMUNICATION",
                    totalQuantity = 28,
                    availableQuantity = 24,
                    unit = "Sets",
                    locationName = "State Disaster Telecom Annex",
                    district = "Statewide",
                    lat = 20.2961,
                    lng = 85.8245,
                    custodianName = "Capt. Rajesh Behera (Signals Officer)",
                    custodianPhone = "+91 94380 77112",
                    conditionStatus = "OPERATIONAL",
                    deploymentStatus = "READY_FOR_DISPATCH",
                    isCritical = true,
                    description = "Inmarsat / GSAT emergency satellite communication gear with Wi-Fi mesh failover hubs."
                ),
                EmergencyResourceEntity(
                    name = "Ready-to-Eat Emergency Food Packs (10-Day Family Pack)",
                    category = "LIFE_SAVING",
                    totalQuantity = 25000,
                    availableQuantity = 19500,
                    unit = "Packs",
                    locationName = "Central Food Corporation Logistics Depot",
                    district = "Cuttack",
                    lat = 20.4590,
                    lng = 85.8850,
                    custodianName = "P. C. Dash (Civil Supplies Officer)",
                    custodianPhone = "+91 94373 55198",
                    conditionStatus = "OPERATIONAL",
                    deploymentStatus = "READY_FOR_DISPATCH",
                    isCritical = true,
                    description = "Vacuum-sealed nutritious meals, high-energy biscuits, water purification tablets, and ORS."
                )
            )
            emergencyResourceDao.insertAllResources(defaultResources)
        }
    }
}
