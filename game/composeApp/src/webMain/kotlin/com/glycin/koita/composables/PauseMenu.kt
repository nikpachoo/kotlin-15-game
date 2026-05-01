package com.glycin.koita.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.pickups.PickupCatalog
import com.glycin.koita.gameplay.upgrades.Unlock
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.ui.pixelFont
import org.jetbrains.compose.resources.imageResource

internal val sliderColors
    @Composable get() = SliderDefaults.colors(
        thumbColor = Color.White,
        activeTrackColor = Color.White,
        inactiveTrackColor = Color.Gray,
    )

@Composable
fun PauseMenu(gameState: GameState, upgradeRepository: UpgradeRepository) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuColors.PAUSE_OVERLAY),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "PAUSED",
                fontFamily = pixelFont(),
                fontSize = 48.sp,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Press ESC to resume",
                fontFamily = pixelFont(),
                fontSize = 16.sp,
                color = Color.LightGray,
            )

            Spacer(modifier = Modifier.height(32.dp))

            VolumeSlider(
                label = "Music",
                value = gameState.musicVolume,
                onValueChange = { newVolume ->
                    gameState.musicVolume = newVolume
                    SoundManager.musicVolume = newVolume
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            VolumeSlider(
                label = "SFX",
                value = gameState.sfxVolume,
                onValueChange = { newVolume ->
                    gameState.sfxVolume = newVolume
                    SoundManager.sfxVolume = newVolume
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            DevModeToggle(
                value = gameState.devMode,
                onValueChange = { gameState.devMode = it },
            )

            Spacer(modifier = Modifier.height(24.dp))

            UnlockedUpgradesPanel(unlocks = upgradeRepository.getUnlocked())

            Spacer(modifier = Modifier.height(12.dp))

            CollectedPickupsPanel(pickupCounts = gameState.pickupCounts)
        }
    }
}

@Composable
private fun UnlockedUpgradesPanel(unlocks: List<Unlock>) {
    PausePanel(title = "UNLOCKED") {
        if (unlocks.isEmpty()) {
            Text(
                text = "None yet",
                fontFamily = pixelFont(),
                fontSize = 12.sp,
                color = Color.LightGray,
            )
        } else {
            unlocks.forEach { unlock ->
                Text(
                    text = unlock.name,
                    fontFamily = pixelFont(),
                    fontSize = 12.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun CollectedPickupsPanel(pickupCounts: Map<String, Int>) {
    Column(
        modifier = Modifier.width(260.dp).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "PICKUPS",
            fontFamily = pixelFont(),
            fontSize = 16.sp,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            PickupCatalog.all.forEach { entry ->
                PickupTile(entry, count = pickupCounts[entry.name] ?: 0)
            }
        }
    }
}

@Composable
private fun PickupTile(pickupEntry: PickupCatalog.PickupEntry, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val image = imageResource(pickupEntry.sprite)
        val painter = remember(image) {
            BitmapPainter(
                image = image,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(PickupCatalog.FRAME_SIZE, PickupCatalog.FRAME_SIZE),
                filterQuality = FilterQuality.None,
            )
        }
        Image(
            painter = painter,
            contentDescription = pickupEntry.name,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = "x$count",
            fontFamily = pixelFont(),
            fontSize = 16.sp,
            color = Color.White,
        )
    }
}

@Composable
private fun PausePanel(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .border(1.dp, Color.White)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            fontFamily = pixelFont(),
            fontSize = 16.sp,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
internal fun VolumeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.width(60.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.width(200.dp),
            colors = sliderColors,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${(value * 100).toInt()}%",
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.width(48.dp),
        )
    }
}

@Composable
internal fun DevModeToggle(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.width(320.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Dev Mode",
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.width(120.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = value,
            onCheckedChange = onValueChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF6B30F9),
                checkedBorderColor = Color.White,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFF3A3A4E),
                uncheckedBorderColor = Color.White,
            ),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = if (value) "ON" else "OFF",
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.width(48.dp),
        )
    }
}
