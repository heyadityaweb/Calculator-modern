package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("GraphCalc", appName)
  }

  @Test
  fun `test calculator view model initialization`() {
    val viewModel = CalculatorViewModel()
    val state = viewModel.uiState.value
    assertTrue("Equations list should not be empty", state.equations.isNotEmpty())
    assertNotNull("Active equation should be set", state.activeEquationId)
    assertTrue("Should compute critical points on startup", state.criticalPoints.isNotEmpty())
  }
}
