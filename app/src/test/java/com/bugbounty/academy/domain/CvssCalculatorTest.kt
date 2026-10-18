package com.bugbounty.academy.domain

import com.bugbounty.academy.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class CvssCalculatorTest {

    @Test
    fun testCriticalCvssVector() {
        val vector = CvssVector(
            attackVector = AttackVector.NETWORK,
            attackComplexity = AttackComplexity.LOW,
            privilegesRequired = PrivilegesRequired.NONE,
            userInteraction = UserInteraction.NONE,
            scope = Scope.UNCHANGED,
            confidentiality = ImpactMetric.HIGH,
            integrity = ImpactMetric.HIGH,
            availability = ImpactMetric.HIGH
        )

        val score = vector.calculateScore()
        assertEquals(9.8, score, 0.1)
        assertEquals(SeverityLevel.CRITICAL, SeverityLevel.fromScore(score))
        assertEquals("CVSS:3.1/AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H", vector.toVectorString())
    }

    @Test
    fun testMediumCvssVector() {
        val vector = CvssVector(
            attackVector = AttackVector.NETWORK,
            attackComplexity = AttackComplexity.LOW,
            privilegesRequired = PrivilegesRequired.LOW,
            userInteraction = UserInteraction.NONE,
            scope = Scope.UNCHANGED,
            confidentiality = ImpactMetric.HIGH,
            integrity = ImpactMetric.NONE,
            availability = ImpactMetric.NONE
        )

        val score = vector.calculateScore()
        assertTrue(score in 6.0..7.0)
        assertEquals(SeverityLevel.MEDIUM, SeverityLevel.fromScore(score))
    }

    @Test
    fun testZeroImpactScore() {
        val vector = CvssVector(
            confidentiality = ImpactMetric.NONE,
            integrity = ImpactMetric.NONE,
            availability = ImpactMetric.NONE
        )

        assertEquals(0.0, vector.calculateScore(), 0.0)
        assertEquals(SeverityLevel.INFO, SeverityLevel.fromScore(0.0))
    }
}
