package com.glycin.koita.ui_composables.main_menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.ModifierConfiguration
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.ui_composables.MenuColors
import com.glycin.koita.ui_composables.compactOr
import com.glycin.koita.ui_composables.input.BackTab
import com.glycin.koita.ui_composables.menuPanelLayout
import com.glycin.koita.ui_composables.pixelFont
import com.glycin.koita.ui_composables.rememberMenuBackgroundBrush
import com.glycin.koita.util.formatTwoDecimals

@Composable
fun ModifierScreen(gameState: GameState) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(rememberMenuBackgroundBrush()),
    ) {
        val layout = menuPanelLayout()
        val panelMaxHeight = maxHeight * compactOr(0.9f, 0.85f)
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(layout.panelGap),
        ) {
            BackTab(
                boxSize = layout.backTabSize,
                onClick = { gameState.currentScreen = Screen.MAIN_MENU },
            )
            ModifiersPanel(width = layout.panelWidth, maxHeight = panelMaxHeight)
        }
    }
}

@Composable
private fun ModifiersPanel(width: Dp, maxHeight: Dp) {
    Column(
        modifier = Modifier
            .width(width)
            .heightIn(max = maxHeight)
            .background(MenuColors.SIDEBAR)
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = compactOr(24.dp, 48.dp),
                vertical = compactOr(20.dp, 36.dp),
            ),
        verticalArrangement = Arrangement.spacedBy(compactOr(16.dp, 24.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            KotlinLogo(boxSize = compactOr(32.dp, 64.dp))
            Text(
                text = "MODIFIERS",
                fontFamily = pixelFont(),
                fontSize = compactOr(20.sp, 36.sp),
                color = Color.White,
            )
        }

        ModifierRow(
            label = "Git-less",
            description = "Kodee can't heal. Healing does nothing. Good luck.",
            weight = ModifierConfiguration.WEIGHT_NO_HEAL,
            checked = ModifierConfiguration.noHeal,
            onToggle = { ModifierConfiguration.noHeal = !ModifierConfiguration.noHeal },
        )
        ModifierRow(
            label = "VIM developer",
            description = "No starting orbs, no shrines spawn. Just a missile. Hardcore.",
            weight = ModifierConfiguration.WEIGHT_NO_SHRINES,
            checked = ModifierConfiguration.noShrines,
            onToggle = { ModifierConfiguration.noShrines = !ModifierConfiguration.noShrines },
        )
        ModifierRow(
            label = "Caffeinated",
            description = "No damage, health, or vision pickups in the world. Not for the faint-hearted.",
            weight = ModifierConfiguration.WEIGHT_NO_PICKUPS,
            checked = ModifierConfiguration.noPickups,
            onToggle = { ModifierConfiguration.noPickups = !ModifierConfiguration.noPickups },
        )
        ModifierRow(
            label = "Good ol' waterfall",
            description = "Digging upward no longer pulls Kodee up. It just felt weird, didn't it?",
            weight = ModifierConfiguration.WEIGHT_NO_MINING_BOOST,
            checked = ModifierConfiguration.noMiningBoost,
            onToggle = { ModifierConfiguration.noMiningBoost = !ModifierConfiguration.noMiningBoost },
        )
        ModifierRow(
            label = "10X developer",
            description = "Double the number of enemies spawning. The more, the merrier!",
            weight = ModifierConfiguration.WEIGHT_DOUBLE_ENEMIES,
            checked = ModifierConfiguration.doubleEnemies,
            onToggle = { ModifierConfiguration.doubleEnemies = !ModifierConfiguration.doubleEnemies },
        )

        MultiplierReadout(multiplier = ModifierConfiguration.scoreMultiplier)
    }
}

@Composable
private fun ModifierRow(
    label: String,
    description: String,
    weight: Float,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = compactOr(4.dp, 6.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(compactOr(10.dp, 16.dp)),
    ) {
        CheckBox(checked = checked)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = pixelFont(),
                fontSize = compactOr(14.sp, 20.sp),
                color = Color.White,
            )
            Text(
                text = description,
                fontFamily = pixelFont(),
                fontSize = compactOr(10.sp, 13.sp),
                color = Color.White,
            )
        }
        Text(
            text = "+${weight.formatTwoDecimals()}",
            fontFamily = pixelFont(),
            fontSize = compactOr(14.sp, 20.sp),
            color = Color.White,
        )
    }
}

@Composable
private fun CheckBox(checked: Boolean) {
    val size = compactOr(20.dp, 28.dp)
    Box(
        modifier = Modifier
            .size(size)
            .border(2.dp, Color.White)
            .background(if (checked) MenuColors.MAIN_BACKGROUND_DARK else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Text(
                text = "X",
                fontFamily = pixelFont(),
                fontSize = compactOr(14.sp, 20.sp),
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MultiplierReadout(multiplier: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = compactOr(8.dp, 12.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Score Multiplier",
            fontFamily = pixelFont(),
            fontSize = compactOr(14.sp, 20.sp),
            color = Color.White,
        )
        Text(
            text = "×${multiplier.formatTwoDecimals()}",
            fontFamily = pixelFont(),
            fontSize = compactOr(20.sp, 32.sp),
            color = Color.White,
        )
    }
}
