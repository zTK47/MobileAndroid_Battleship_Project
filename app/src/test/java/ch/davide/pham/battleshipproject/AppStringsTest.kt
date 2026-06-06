package ch.davide.pham.battleshipproject

import ch.davide.pham.battleshipproject.ui.AppLanguage
import ch.davide.pham.battleshipproject.ui.strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStringsTest {
    @Test
    fun everyLanguageProvidesMainNavigationText() {
        AppLanguage.entries.forEach { language ->
            val text = language.strings
            assertTrue(text.battleship.isNotBlank())
            assertTrue(text.enterBattle.isNotBlank())
            assertTrue(text.settings.isNotBlank())
            assertTrue(text.howToPlay.isNotBlank())
            assertTrue(text.about.isNotBlank())
            assertTrue(text.backToMenu.isNotBlank())
            assertTrue(text.createGame.isNotBlank())
            assertTrue(text.joinGame.isNotBlank())
            assertTrue(text.onlineJoinHelp.isNotBlank())
            assertTrue(text.quitBattle.isNotBlank())
        }
    }

    @Test
    fun languageCodesAreUnique() {
        assertEquals(
            AppLanguage.entries.size,
            AppLanguage.entries.map { it.code }.toSet().size
        )
    }
}
