package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.SecurityUtil
import com.example.ui.common.FormatUtil
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app_name from context matches ZAVA BANK REC`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ZAVA BANK REC", appName)
  }

  @Test
  fun `security util hashes and verifies credentials correctly`() {
    val salt = SecurityUtil.generateSalt()
    val hash = SecurityUtil.hashPassword("secret123", salt)
    assertTrue(SecurityUtil.verifyPassword("secret123", salt, hash))
    assertFalse(SecurityUtil.verifyPassword("wrongpass", salt, hash))
  }

  @Test
  fun `format util formats rupiah and wedding savings correctly`() {
    val formatted = FormatUtil.formatRupiah(1500000L, "RP_NOSPACE")
    assertEquals("Rp1.500.000", formatted)

    val privacy = FormatUtil.formatRupiah(1500000L, "RP_NOSPACE", isPrivacy = true)
    assertEquals("Rp ••••••••", privacy)

    // Test monthly savings calculation
    // Target 50 mil, current 15 mil, target in 365 days (~12 months) -> shortage 35 mil / 12 ~= 2.9 mil
    val oneYearAhead = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
    val monthly = FormatUtil.calculateMonthlySavings(50_000_000L, 15_000_000L, oneYearAhead)
    assertTrue(monthly in 2_800_000L..3_100_000L)
  }
}
