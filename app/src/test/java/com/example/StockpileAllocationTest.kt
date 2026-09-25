package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AllocationTransaction
import com.example.data.model.DepotStatus
import com.example.data.model.ReliefDepot
import com.example.viewmodel.EmergencyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StockpileAllocationTest {

    @Test
    fun `test initial depot loading and stockpile summary computation`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = EmergencyViewModel(app)

        val state = viewModel.uiState.value
        assertEquals(4, state.depots.size)
        assertTrue(state.stockpileSummary.totalWaterStock > 0)
        assertTrue(state.stockpileSummary.totalRationsStock > 0)
        assertTrue(state.stockpileSummary.totalMedicalStock > 0)
        assertTrue(state.stockpileSummary.totalRescueBoatsStock > 0)
        assertTrue(state.stockpileSummary.hoursRemainingWater > 0f)

        // Verify critical shortage flag is active initially because Delta Outpost is critically low
        val deltaDepot = state.depots.find { it.id == "DEPOT-KND-04" }
        assertNotNull(deltaDepot)
        assertEquals(DepotStatus.CRITICAL_SHORTAGE, deltaDepot?.status)
        assertTrue(state.stockpileSummary.isCriticalShortagePresent)
    }

    @Test
    fun `test computeStockpileSummary algorithm and critical shortage detection`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = EmergencyViewModel(app)

        val operationalDepots = listOf(
            ReliefDepot(
                id = "DEPOT-TEST-01",
                name = "Test Hub Alpha",
                code = "DEPOT-01",
                district = "Cuttack",
                locationDescription = "Logistics Hub",
                lat = 20.46,
                lng = 85.88,
                status = DepotStatus.OPERATIONAL,
                waterStock = 2000,
                waterCapacity = 2500,
                rationPacks = 1000,
                rationCapacity = 1500,
                medicalTraumaKits = 50,
                medicalCapacity = 60,
                rescueBoats = 10,
                boatCapacity = 12,
                emergencyFuelLiters = 2000,
                fuelCapacity = 2500
            ),
            ReliefDepot(
                id = "DEPOT-TEST-02",
                name = "Test Hub Beta",
                code = "DEPOT-02",
                district = "Puri",
                locationDescription = "Coastal Hub",
                lat = 19.81,
                lng = 85.83,
                status = DepotStatus.OPERATIONAL,
                waterStock = 1400,
                waterCapacity = 2000,
                rationPacks = 800,
                rationCapacity = 1000,
                medicalTraumaKits = 30,
                medicalCapacity = 40,
                rescueBoats = 6,
                boatCapacity = 8,
                emergencyFuelLiters = 1400,
                fuelCapacity = 1500
            )
        )

        val summaryOperational = viewModel.computeStockpileSummary(operationalDepots)
        assertEquals(3400, summaryOperational.totalWaterStock)
        assertEquals(4500, summaryOperational.totalWaterCapacity)
        assertEquals(1800, summaryOperational.totalRationsStock)
        assertEquals(80, summaryOperational.totalMedicalStock)
        assertEquals(16, summaryOperational.totalRescueBoatsStock)
        assertEquals(3400, summaryOperational.totalFuelLiters)
        assertFalse(summaryOperational.isCriticalShortagePresent)
        assertEquals(3400f / 85f, summaryOperational.hoursRemainingWater, 0.01f)

        // Inject a depot with CRITICAL_SHORTAGE
        val depletedDepots = operationalDepots + ReliefDepot(
            id = "DEPOT-TEST-03",
            name = "Depleted Outpost",
            code = "DEPOT-03",
            district = "Delta",
            locationDescription = "Rivermouth",
            lat = 20.10,
            lng = 86.20,
            status = DepotStatus.CRITICAL_SHORTAGE,
            waterStock = 50,
            waterCapacity = 1500,
            rationPacks = 20,
            rationCapacity = 800,
            medicalTraumaKits = 2,
            medicalCapacity = 30,
            rescueBoats = 1,
            boatCapacity = 6,
            emergencyFuelLiters = 100,
            fuelCapacity = 1000
        )

        val summaryDepleted = viewModel.computeStockpileSummary(depletedDepots)
        assertTrue(summaryDepleted.isCriticalShortagePresent)
        assertEquals(3450, summaryDepleted.totalWaterStock)
    }

    @Test
    fun `test allocation transaction model and manifest integrity`() {
        val tx = AllocationTransaction(
            id = "TX-9988",
            sourceDepotName = "Central Supply Hub - Cuttack",
            targetZoneName = "Zone 1 - Riverbank Delta",
            waterUnits = 350,
            foodPackets = 150,
            medicalKits = 4,
            rescueBoats = 2,
            status = "DISPATCHED",
            etaMinutes = 25,
            dispatchedBy = "Incident Commander"
        )

        assertEquals("TX-9988", tx.id)
        assertEquals("Central Supply Hub - Cuttack", tx.sourceDepotName)
        assertEquals("Zone 1 - Riverbank Delta", tx.targetZoneName)
        assertEquals(350, tx.waterUnits)
        assertEquals(150, tx.foodPackets)
        assertEquals(4, tx.medicalKits)
        assertEquals(2, tx.rescueBoats)
        assertEquals("DISPATCHED", tx.status)
        assertEquals(25, tx.etaMinutes)
        assertEquals("Incident Commander", tx.dispatchedBy)
    }
}
