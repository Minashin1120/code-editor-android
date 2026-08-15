package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.ui.LaunchSplashScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LaunchSplashScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun splash_exposes_brand_content_description() {
    composeTestRule.mainClock.autoAdvance = false
    composeTestRule.setContent { LaunchSplashScreen(onFinished = {}) }

    composeTestRule.onNodeWithContentDescription("HTMLエディター").assertExists()
  }
}
