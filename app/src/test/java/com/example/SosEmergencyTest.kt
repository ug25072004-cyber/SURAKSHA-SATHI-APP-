package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.EmergencyViewModel
import kotlinx.coroutines.test.runTest
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
class SosEmergencyTest {

    @Test
    fun `test emergency SOS protocol execution and dismissal`() = runTest {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = EmergencyViewModel(app)

        // Verify initial state
        assertFalse(viewModel.uiState.value.isSosProtocolActive)
        assertEquals("STANDBY", viewModel.uiState.value.sosTransmissionStatus)

        // Execute SOS protocol
        val protocolName = "Level 1: Distress SITREP & GPS Beacon"
        viewModel.executeSosProtocol(protocolName, "Urgent life safety evacuation beacon")

        // Verify active state
        val activeState = viewModel.uiState.value
        assertTrue(activeState.isSosProtocolActive)
        assertEquals(protocolName, activeState.activeSosProtocolName)
        assertNotNull(activeState.sosTelemetryCoords)

        // Dismiss SOS protocol
        viewModel.dismissSosProtocol()
        val dismissedState = viewModel.uiState.value
        assertFalse(dismissedState.isSosProtocolActive)
        assertEquals("STANDBY", dismissedState.sosTransmissionStatus)
    }
}
