package com.glycin.koita.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.gameplay.pickups.PickupCatalog
import com.glycin.koita.gameplay.upgrades.Unlock
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.ui_composables.MenuColors
import com.glycin.koita.ui_composables.MenuHeader
import com.glycin.koita.ui_composables.SidebarMenuItem
import com.glycin.koita.ui_composables.SpriteFrameIcon
import com.glycin.koita.ui_composables.compactOr
import com.glycin.koita.ui_composables.pixelFont
import org.jetbrains.compose.resources.imageResource

private const val UNLOCK_CAROUSEL_VISIBLE = 3
private const val SHOW_DEV_TOGGLE = false

private val sliderColors
    @Composable get() = SliderDefaults.colors(
        thumbColor = Color.White,
        activeTrackColor = Color.White,
        inactiveTrackColor = MenuColors.SLIDER_TRACK,
    )

@Composable
fun PauseMenu(gameState: GameState, upgradeRepository: UpgradeRepository) {
    val unlocks = remember { upgradeRepository.getUnlocked() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuColors.PAUSE_OVERLAY),
        contentAlignment = Alignment.Center,
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Max)) {
            LeftPanel(gameState)
            RightPanel(unlocks, gameState.pickupCounts)
        }
    }
}

@Composable
private fun LeftPanel(gameState: GameState) {
    Column(
        modifier = Modifier
            .width(compactOr(260.dp, 420.dp))
            .background(MenuColors.SIDEBAR)
            .padding(bottom = compactOr(16.dp, 24.dp)),
    ) {
        MenuHeader(
            title = "Game",
            modifier = Modifier.fillMaxWidth(),
        )

        PauseMenuItem("Resume") { gameState.isPaused = false }
        PauseMenuItem("Quit") { gameState.endRunAndGoTo(Screen.MAIN_MENU) }

        Spacer(modifier = Modifier.height(compactOr(16.dp, 28.dp)))

        Column(
            modifier = Modifier.padding(horizontal = compactOr(24.dp, 48.dp)),
            verticalArrangement = Arrangement.spacedBy(compactOr(6.dp, 12.dp)),
        ) {
            VolumeSlider(
                label = "Music",
                value = gameState.musicVolume,
                onValueChange = { newVolume ->
                    gameState.musicVolume = newVolume
                    SoundManager.musicVolume = newVolume
                },
            )
            VolumeSlider(
                label = "SFX",
                value = gameState.sfxVolume,
                onValueChange = { newVolume ->
                    gameState.sfxVolume = newVolume
                    SoundManager.sfxVolume = newVolume
                },
            )
            if (SHOW_DEV_TOGGLE) {
                DevModeToggle(
                    value = gameState.devMode,
                    onValueChange = { gameState.devMode = it },
                )
            }
        }
    }
}

@Composable
private fun DevModeToggle(value: Boolean, onValueChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Dev",
            fontFamily = pixelFont(),
            fontSize = compactOr(10.sp, 14.sp),
            color = Color.White,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.width(compactOr(56.dp, 80.dp)),
        )
        Spacer(modifier = Modifier.width(compactOr(6.dp, 12.dp)))
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
    }
}

@Composable
private fun PauseMenuItem(text: String, onClick: () -> Unit) {
    SidebarMenuItem(
        text = text,
        fontSize = compactOr(18.sp, 28.sp),
        verticalPadding = compactOr(6.dp, 10.dp),
        onClick = onClick,
    )
}

@Composable
private fun RightPanel(unlocks: List<Unlock>, pickupCounts: Map<String, Int>) {
    Column(
        modifier = Modifier
            .width(compactOr(440.dp, 680.dp))
            .fillMaxHeight()
            .background(MenuColors.PAUSE_RIGHT_BG)
            .padding(compactOr(16.dp, 24.dp)),
        verticalArrangement = Arrangement.spacedBy(compactOr(12.dp, 20.dp)),
    ) {
        SectionHeader(label = "Upgrades", count = unlocks.size)
        Box(
            modifier = Modifier.height(compactOr(96.dp, 156.dp)),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (unlocks.isEmpty()) {
                Text(
                    text = "None yet",
                    fontFamily = pixelFont(),
                    fontSize = compactOr(10.sp, 14.sp),
                    color = MenuColors.PAUSE_RIGHT_MUTED,
                )
            } else {
                UnlockCarousel(unlocks)
            }
        }

        SectionHeader(label = "Pickups")
        Row(
            horizontalArrangement = Arrangement.spacedBy(compactOr(12.dp, 20.dp)),
            verticalAlignment = Alignment.Top,
        ) {
            PickupCatalog.all.forEach { entry ->
                PickupTile(entry, count = pickupCounts[entry.name] ?: 0)
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, count: Int? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = compactOr(14.sp, 20.sp),
            color = MenuColors.PAUSE_RIGHT_TEXT,
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(compactOr(6.dp, 10.dp)))
            Text(
                text = count.toString(),
                fontFamily = pixelFont(),
                fontSize = compactOr(14.sp, 20.sp),
                color = MenuColors.PAUSE_ACCENT,
            )
        }
    }
}

@Composable
private fun UnlockCarousel(unlocks: List<Unlock>) {
    var startIndex by remember(unlocks.size) { mutableStateOf(0) }
    val size = unlocks.size
    val visibleCount = minOf(UNLOCK_CAROUSEL_VISIBLE, size)
    val showArrows = size > UNLOCK_CAROUSEL_VISIBLE

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(compactOr(4.dp, 8.dp)),
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
    Column(
        modifier = Modifier
            .width(compactOr(112.dp, 176.dp))
            .height(compactOr(96.dp, 156.dp))
            .border(2.dp, MenuColors.PAUSE_ACCENT)
            .padding(compactOr(4.dp, 8.dp)),
        verticalArrangement = Arrangement.spacedBy(compactOr(2.dp, 4.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpriteFrameIcon(unlock.icon, size = compactOr(24.dp, 36.dp))
        Text(
            text = unlock.name,
            fontFamily = pixelFont(),
            fontSize = compactOr(10.sp, 14.sp),
            color = MenuColors.PAUSE_RIGHT_TEXT,
        )
        Text(
            text = unlock.description,
            fontFamily = pixelFont(),
            fontSize = compactOr(9.sp, 12.sp),
            color = MenuColors.PAUSE_RIGHT_TEXT,
        )
    }
}

@Composable
private fun ArrowButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(compactOr(24.dp, 36.dp))
            .border(1.dp, MenuColors.PAUSE_RIGHT_TEXT)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = compactOr(14.sp, 20.sp),
            color = MenuColors.PAUSE_RIGHT_TEXT,
        )
    }
}

@Composable
private fun PickupTile(pickupEntry: PickupCatalog.PickupEntry, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(compactOr(2.dp, 4.dp)),
    ) {
        val image = imageResource(pickupEntry.sprite)
        Canvas(modifier = Modifier.size(compactOr(32.dp, 56.dp))) {
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
            fontSize = compactOr(11.sp, 16.sp),
            color = MenuColors.PAUSE_RIGHT_TEXT,
        )
    }
}

@Composable
private fun VolumeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    val labelWidth = compactOr(56.dp, 80.dp)
    val sliderWidth = compactOr(100.dp, 160.dp)
    val percentWidth = compactOr(36.dp, 48.dp)
    val gap = compactOr(6.dp, 12.dp)
    val fontSize = compactOr(10.sp, 14.sp)

    Row(verticalAlignment = Alignment.CenterVertically) {
        VolumeText(label, fontSize, labelWidth)
        Spacer(modifier = Modifier.width(gap))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.width(sliderWidth),
            colors = sliderColors,
        )
        Spacer(modifier = Modifier.width(gap))
        VolumeText("${(value * 100).toInt()}%", fontSize, percentWidth)
    }
}

@Composable
private fun VolumeText(text: String, fontSize: TextUnit, width: Dp) {
    Text(
        text = text,
        fontFamily = pixelFont(),
        fontSize = fontSize,
        color = Color.White,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier.width(width),
    )
}
