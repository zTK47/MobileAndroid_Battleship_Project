package ch.davide.pham.battleshipproject

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class BattleshipLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeMenuAndBackNavigationAreDisplayed() {
        composeRule.onNodeWithText("FLEET COMMAND").assertIsDisplayed()
        composeRule.onNodeWithText("BATTLESHIP").assertIsDisplayed()
        composeRule.onNodeWithText("ENTER BATTLE").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("MENU").assertIsDisplayed()
        composeRule.onNodeWithText("CONFIGURE HOST FLEET").assertIsDisplayed()
        composeRule.onNodeWithText("JOIN").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("CONFIGURE JOIN FLEET").assertIsDisplayed()
    }
}
