package com.bugbounty.academy

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BugBountyUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testDashboardInitialRender() {
        // Verify Dashboard title or rank is displayed
        composeTestRule.onNodeWithText("Bug Bounty Curriculum").assertIsDisplayed()
        composeTestRule.onNodeWithText("Practice Labs (32 Isolated Labs)").assertExists()
    }
}
