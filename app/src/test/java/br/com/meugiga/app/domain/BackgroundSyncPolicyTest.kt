package br.com.meugiga.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundSyncPolicyTest {
    @Test
    fun `no sync until onboarding complete and access granted`() {
        assertFalse(BackgroundSyncPolicy.shouldSchedule(false, true, 15))
        assertFalse(BackgroundSyncPolicy.shouldSchedule(true, false, 30))
        assertTrue(BackgroundSyncPolicy.shouldSchedule(true, true, 15))
        assertTrue(BackgroundSyncPolicy.shouldSchedule(true, true, 30))
        assertTrue(BackgroundSyncPolicy.shouldSchedule(true, true, 60))
    }

    @Test
    fun `user can disable background sync`() {
        assertFalse(BackgroundSyncPolicy.shouldSchedule(true, true, 0))
        assertFalse(BackgroundSyncPolicy.shouldSchedule(true, true, 1))
        assertFalse(BackgroundSyncPolicy.shouldSchedule(true, true, 120))
        assertTrue(0 in BackgroundSyncPolicy.supportedIntervals)
    }
}
