package ch.davide.pham.battleshipproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ch.davide.pham.battleshipproject.ui.BattleshipApp
import ch.davide.pham.battleshipproject.ui.theme.MobileAndroidApplicationBattleshipProjectTheme

/*
 * Author: Davide Pham
 * This project was developed with assistance from OpenAI Codex.
 * Game rules and the REST protocol come from the supplied course specification.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileAndroidApplicationBattleshipProjectTheme {
                BattleshipApp()
            }
        }
    }
}
