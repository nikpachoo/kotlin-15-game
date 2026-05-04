package com.glycin.koita.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.core.SpriteFrame
import com.glycin.koita.core.drawSpriteFrame
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
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
    val compact = isCompact()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuColors.PAUSE_OVERLAY),
        contentAlignment = Alignment.Center,
    ) {
        if (compact) {
            CompactPauseMenu(gameState, upgradeRepository)
        } else {
            NormalPauseMenu(gameState, upgradeRepository)
        }

        ReturnToMainMenuButton(
            onClick = { gameState.endRunAndGoTo(Screen.MAIN_MENU) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (compact) 12.dp else 24.dp),
        )
    }
}

@Composable
private fun ReturnToMainMenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val compact = isCompact()
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .width(if (compact) 160.dp else 200.dp)
            .height(if (compact) 36.dp else 44.dp),
        border = BorderStroke(2.dp, Color.White),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MenuColors.MAIN_BACKGROUND_LIGHT,
            contentColor = Color.White,
        ),
    ) {
        Text(
            text = "Main Menu",
            fontFamily = pixelFont(),
            fontSize = if (compact) 12.sp else 16.sp,
        )
    }
}

@Composable
private fun NormalPauseMenu(gameState: GameState, upgradeRepository: UpgradeRepository) {
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

        AudioAndDevControls(gameState)

        Spacer(modifier = Modifier.height(24.dp))

        UnlockedUpgradesPanel(unlocks = upgradeRepository.getUnlocked())

        Spacer(modifier = Modifier.height(12.dp))

        CollectedPickupsPanel(pickupCounts = gameState.pickupCounts)
    }
}

@Composable
private fun CompactPauseMenu(gameState: GameState, upgradeRepository: UpgradeRepository) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "PAUSED",
            fontFamily = pixelFont(),
            fontSize = 22.sp,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Press ESC to resume",
            fontFamily = pixelFont(),
            fontSize = 10.sp,
            color = Color.LightGray,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AudioAndDevControls(gameState)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                UnlockedUpgradesPanel(unlocks = upgradeRepository.getUnlocked())
                Spacer(modifier = Modifier.height(4.dp))
                CollectedPickupsPanel(pickupCounts = gameState.pickupCounts)
            }
        }
    }
}

@Composable
private fun AudioAndDevControls(gameState: GameState) {
    val rowGap = if (isCompact()) 4.dp else 12.dp

    VolumeSlider(
        label = "Music",
        value = gameState.musicVolume,
        onValueChange = { newVolume ->
            gameState.musicVolume = newVolume
            SoundManager.musicVolume = newVolume
        },
    )

    Spacer(modifier = Modifier.height(rowGap))

    VolumeSlider(
        label = "SFX",
        value = gameState.sfxVolume,
        onValueChange = { newVolume ->
            gameState.sfxVolume = newVolume
            SoundManager.sfxVolume = newVolume
        },
    )

    Spacer(modifier = Modifier.height(rowGap))

    DevModeToggle(
        value = gameState.devMode,
        onValueChange = { gameState.devMode = it },
    )
}

private const val UNLOCK_CAROUSEL_VISIBLE = 3

@Composable
private fun UnlockedUpgradesPanel(unlocks: List<Unlock>) {
    val compact = isCompact()
    Column(
        modifier = Modifier.padding(if (compact) 4.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 6.dp),
    ) {
        Text(
            text = "UPGRADES",
            fontFamily = pixelFont(),
            fontSize = if (compact) 11.sp else 16.sp,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))

        if (unlocks.isEmpty()) {
            Text(
                text = "None yet",
                fontFamily = pixelFont(),
                fontSize = if (compact) 9.sp else 12.sp,
                color = Color.LightGray,
            )
        } else {
            UnlockCarousel(unlocks)
        }
    }
}

@Composable
private fun UnlockCarousel(unlocks: List<Unlock>) {
    var startIndex by remember(unlocks.size) { mutableStateOf(0) }
    val size = unlocks.size
    val visibleCount = minOf(UNLOCK_CAROUSEL_VISIBLE, size)
    val showArrows = size > UNLOCK_CAROUSEL_VISIBLE
    val compact = isCompact()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
    ) {
        if (showArrows) {
            ArrowButton("<") { startIndex = (startIndex - 1 + size) % size }
        }
        for (i in 0 until visibleCount) {
            UnlockCard(unlocks[(startIndex + i) % size])
        }
        if (showArrows) {
            ArrowButton(">") { startIndex = (startIndex + 1) % size }
        }
    }
}

@Composable
private fun UnlockCard(unlock: Unlock) {
    val compact = isCompact()
    val card = if (compact) 84.dp else 140.dp
    Column(
        modifier = Modifier
            .width(card)
            .height(card)
            .background(MenuColors.BACKGROUND)
            .border(2.dp, MenuColors.cardBorder(unlock.group))
            .padding(if (compact) 4.dp else 8.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UpgradeIcon(unlock.icon)
        Text(
            text = unlock.name,
            fontFamily = pixelFont(),
            fontSize = if (compact) 8.sp else 12.sp,
            color = Color.White,
        )
        Text(
            text = unlock.description,
            fontFamily = pixelFont(),
            fontSize = if (compact) 7.sp else 10.sp,
            color = Color.LightGray,
        )
    }
}

@Composable
private fun UpgradeIcon(icon: SpriteFrame) {
    val image = imageResource(icon.sheet.sprite)
    Canvas(modifier = Modifier.size(if (isCompact()) 24.dp else 36.dp)) {
        drawSpriteFrame(
            image = image,
            frame = icon,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
        )
    }
}

@Composable
private fun ArrowButton(label: String, onClick: () -> Unit) {
    val compact = isCompact()
    Box(
        modifier = Modifier
            .size(if (compact) 24.dp else 36.dp)
            .border(1.dp, Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = if (compact) 14.sp else 20.sp,
            color = Color.White,
        )
    }
}

@Composable
private fun CollectedPickupsPanel(pickupCounts: Map<String, Int>) {
    val compact = isCompact()
    Column(
        modifier = Modifier
            .width(if (compact) 200.dp else 260.dp)
            .padding(if (compact) 4.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 6.dp),
    ) {
        Text(
            text = "PICKUPS",
            fontFamily = pixelFont(),
            fontSize = if (compact) 11.sp else 16.sp,
            color = Color.White,
        )
        Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 16.dp),
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
    val compact = isCompact()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp),
    ) {
        val image = imageResource(pickupEntry.sprite)
        Canvas(modifier = Modifier.size(if (compact) 32.dp else 56.dp)) {
            if (image.width <= 0 || image.height <= 0) return@Canvas
            drawImage(
                image = image,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(PickupCatalog.FRAME_SIZE, PickupCatalog.FRAME_SIZE),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                filterQuality = FilterQuality.None,
            )
        }
        Text(
            text = "x$count",
            fontFamily = pixelFont(),
            fontSize = if (compact) 11.sp else 16.sp,
            color = Color.White,
        )
    }
}

@Composable
internal fun VolumeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val compact = isCompact()
    val labelWidth = if (compact) 40.dp else 60.dp
    val sliderWidth = if (compact) 130.dp else 200.dp
    val percentWidth = if (compact) 36.dp else 48.dp
    val gap = if (compact) 6.dp else 12.dp
    val fontSize = if (compact) 10.sp else 14.sp

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.width(labelWidth),
        )

        Spacer(modifier = Modifier.width(gap))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.width(sliderWidth),
            colors = sliderColors,
        )

        Spacer(modifier = Modifier.width(gap))

        Text(
            text = "${(value * 100).toInt()}%",
            fontFamily = pixelFont(),
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.width(percentWidth),
        )
    }
}

@Composable
internal fun DevModeToggle(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {
    val compact = isCompact()
    val rowWidth = if (compact) 220.dp else 320.dp
    val labelWidth = if (compact) 80.dp else 120.dp
    val statusWidth = if (compact) 36.dp else 48.dp
    val gap = if (compact) 6.dp else 12.dp
    val fontSize = if (compact) 10.sp else 14.sp

    Row(
        modifier = Modifier.width(rowWidth),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Dev Mode",
            fontFamily = pixelFont(),
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.width(labelWidth),
        )

        Spacer(modifier = Modifier.width(gap))

        Switch(
            checked = value,
            onCheckedChange = onValueChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MenuColors.MAIN_BACKGROUND_LIGHT,
                checkedBorderColor = Color.White,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MenuColors.SECTION_TITLE,
                uncheckedBorderColor = Color.White,
            ),
        )

        Spacer(modifier = Modifier.width(gap))

        Text(
            text = if (value) "ON" else "OFF",
            fontFamily = pixelFont(),
            fontSize = fontSize,
            color = Color.White,
            modifier = Modifier.width(statusWidth),
        )
    }
}
