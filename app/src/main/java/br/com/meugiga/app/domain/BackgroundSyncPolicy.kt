package br.com.meugiga.app.domain

/** An interval of zero cancels periodic monitoring without affecting foreground readings. */
object BackgroundSyncPolicy {
    const val DISABLED = 0
    val supportedIntervals: Set<Int> = setOf(DISABLED, 15, 30, 60)

    fun shouldSchedule(onboardingComplete: Boolean, hasUsageAccess: Boolean, intervalMinutes: Int): Boolean =
        onboardingComplete && hasUsageAccess && intervalMinutes in supportedIntervals &&
            intervalMinutes != DISABLED
}
