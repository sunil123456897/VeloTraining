package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PresetWorkouts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ovicx Q200 Кокпит", appName)
    }

    @Test
    fun `preset workouts flatten repeatable series accurately`() {
        val presets = PresetWorkouts.allPresets
        assertEquals(5, presets.size)
        val machineGun = presets.first()
        val steps = machineGun.flattenSteps()
        assertTrue(steps.size > 16)
        assertEquals(28, machineGun.totalMinutesRounded)
    }
}
