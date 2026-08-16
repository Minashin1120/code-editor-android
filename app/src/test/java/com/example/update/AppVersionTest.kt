package com.example.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {
    @Test
    fun compareNames_ordersSemanticVersions() {
        assertTrue(AppVersion.compareNames("1.2", "1.1") > 0)
        assertTrue(AppVersion.compareNames("1.10", "1.2") > 0)
        assertEquals(0, AppVersion.compareNames("v1.1", "1.1.0"))
        assertTrue(AppVersion.compareNames("1.1", "1.1.1") < 0)
    }

    @Test
    fun isRemoteNewer_prefersVersionCode() {
        assertTrue(AppVersion.isRemoteNewer("1.0", 3, "1.9", 2))
        assertFalse(AppVersion.isRemoteNewer("2.0", 2, "1.0", 2))
    }

    @Test
    fun isRemoteNewer_fallsBackToName() {
        assertTrue(AppVersion.isRemoteNewer("1.2", null, "1.1", 2))
        assertFalse(AppVersion.isRemoteNewer("1.1", 0, "1.1", 2))
    }
}
