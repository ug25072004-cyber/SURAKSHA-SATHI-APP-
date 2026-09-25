package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.EmergencyContactEntity
import com.example.data.local.EmergencyResourceEntity
import com.example.data.repository.OfflineEmergencyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineRoomDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: OfflineEmergencyRepository

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = OfflineEmergencyRepository(
            incidentDao = database.incidentDao(),
            resourceDao = database.resourceDao(),
            allocationLogDao = database.allocationLogDao(),
            emergencyContactDao = database.emergencyContactDao(),
            emergencyResourceDao = database.emergencyResourceDao()
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `test emergency contact insertion, query, and search`() = runBlocking {
        val contactDao = database.emergencyContactDao()

        val contact1 = EmergencyContactEntity(
            name = "NDRF First Responder Unit 3",
            organization = "National Disaster Response Force",
            category = "MILITARY_NDRF",
            primaryPhone = "0671-2879710",
            alternatePhone = "+91 94379 64201",
            district = "Cuttack",
            priorityRank = 1,
            isTollFree = false
        )

        val contact2 = EmergencyContactEntity(
            name = "Central Emergency Helpline",
            organization = "State Police & Disaster Dispatch",
            category = "POLICE",
            primaryPhone = "112",
            district = "Statewide",
            priorityRank = 1,
            isTollFree = true
        )

        val id1 = contactDao.insertContact(contact1)
        val id2 = contactDao.insertContact(contact2)

        assertTrue(id1 > 0)
        assertTrue(id2 > 0)

        // Verify query all
        val allContacts = contactDao.getAllContacts().first()
        assertEquals(2, allContacts.size)

        // Verify query by category
        val ndrfContacts = contactDao.getContactsByCategory("MILITARY_NDRF").first()
        assertEquals(1, ndrfContacts.size)
        assertEquals("NDRF First Responder Unit 3", ndrfContacts[0].name)

        // Verify search by keyword
        val searchResults = contactDao.searchContacts("Helpline").first()
        assertEquals(1, searchResults.size)
        assertEquals("112", searchResults[0].primaryPhone)

        // Verify getById
        val fetchedContact = contactDao.getContactById(id1)
        assertNotNull(fetchedContact)
        assertEquals("Cuttack", fetchedContact?.district)

        // Verify update
        val updatedContact = fetchedContact!!.copy(notes = "Deployed at Mahanadi riverbank")
        contactDao.updateContact(updatedContact)
        val refreshed = contactDao.getContactById(id1)
        assertEquals("Deployed at Mahanadi riverbank", refreshed?.notes)

        // Verify delete
        contactDao.deleteContactById(id1)
        val remaining = contactDao.getAllContacts().first()
        assertEquals(1, remaining.size)
        assertEquals(id2, remaining[0].id)
    }

    @Test
    fun `test emergency resource inventory, critical filtering, and search`() = runBlocking {
        val resourceDao = database.emergencyResourceDao()

        val pumpResource = EmergencyResourceEntity(
            name = "50HP Submersible High-Capacity Pump",
            category = "HEAVY_MACHINERY",
            totalQuantity = 20,
            availableQuantity = 15,
            unit = "Pumps",
            locationName = "Cuttack Municipal Yard",
            district = "Cuttack",
            custodianName = "Chief Engineer Jena",
            custodianPhone = "+91 94371 88204",
            isCritical = true
        )

        val foodResource = EmergencyResourceEntity(
            name = "Emergency Ration Kits",
            category = "LIFE_SAVING",
            totalQuantity = 5000,
            availableQuantity = 4500,
            unit = "Kits",
            locationName = "Bhubaneswar Warehouse",
            district = "Khordha",
            custodianName = "Logistics Officer",
            custodianPhone = "1077",
            isCritical = false
        )

        val idPump = resourceDao.insertResource(pumpResource)
        val idFood = resourceDao.insertResource(foodResource)

        assertTrue(idPump > 0)
        assertTrue(idFood > 0)

        // Verify total resources
        val allResources = resourceDao.getAllResources().first()
        assertEquals(2, allResources.size)

        // Verify critical filter
        val criticalOnly = resourceDao.getCriticalResources().first()
        assertEquals(1, criticalOnly.size)
        assertEquals("50HP Submersible High-Capacity Pump", criticalOnly[0].name)
        assertTrue(criticalOnly[0].isCritical)

        // Verify search by location / name
        val searchPumps = resourceDao.searchResources("Submersible").first()
        assertEquals(1, searchPumps.size)
        assertEquals("Cuttack Municipal Yard", searchPumps[0].locationName)

        // Verify resource update
        val fetched = resourceDao.getResourceById(idPump)
        assertNotNull(fetched)
        val updatedPump = fetched!!.copy(availableQuantity = 10)
        resourceDao.updateResource(updatedPump)
        val refreshed = resourceDao.getResourceById(idPump)
        assertEquals(10, refreshed?.availableQuantity)

        // Verify deletion
        resourceDao.deleteResourceById(idFood)
        val count = resourceDao.getResourceCount()
        assertEquals(1, count)
    }

    @Test
    fun `test offline repository seeding populates default contacts and resources when empty`() = runBlocking {
        assertEquals(0, repository.getContactCount())
        assertEquals(0, repository.getResourceCount())

        // Run seed method
        repository.seedInitialOfflineDataIfEmpty()

        val contactCount = repository.getContactCount()
        val resourceCount = repository.getResourceCount()

        assertTrue("Initial contacts should be seeded", contactCount >= 8)
        assertTrue("Initial critical resources should be seeded", resourceCount >= 7)

        val contacts = repository.allContacts.first()
        val hasNdrf = contacts.any { it.name.contains("NDRF") || it.category == "MILITARY_NDRF" }
        val hasPolice = contacts.any { it.primaryPhone == "112" }
        assertTrue("Should have NDRF contact", hasNdrf)
        assertTrue("Should have 112 helpline", hasPolice)

        val resources = repository.allResources.first()
        val hasDewateringPumps = resources.any { it.name.contains("Dewatering Pumps") }
        val hasRescueBoats = resources.any { it.name.contains("Rescue Boats") }
        assertTrue("Should have Dewatering pumps", hasDewateringPumps)
        assertTrue("Should have Inflatable rescue boats", hasRescueBoats)

        // Verify second run does not duplicate data
        repository.seedInitialOfflineDataIfEmpty()
        assertEquals(contactCount, repository.getContactCount())
        assertEquals(resourceCount, repository.getResourceCount())
    }
}
