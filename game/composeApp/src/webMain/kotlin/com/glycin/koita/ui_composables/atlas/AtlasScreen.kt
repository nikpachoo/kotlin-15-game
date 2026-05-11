package com.glycin.koita.ui_composables.atlas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.ui_composables.input.BackTab
import com.glycin.koita.ui_composables.main_menu.KotlinLogo
import com.glycin.koita.ui_composables.MenuColors
import com.glycin.koita.ui_composables.SpriteAnimationIcon
import com.glycin.koita.ui_composables.compactOr
import com.glycin.koita.ui_composables.menuPanelLayout
import com.glycin.koita.ui_composables.rememberMenuBackgroundBrush
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.ui_composables.pixelFont

private enum class AtlasTab(val label: String) {
    UPGRADES("Upgrades"),
    ENEMIES("Enemies"),
}

private fun rowsFor(tab: AtlasTab): List<AtlasEntry> = when (tab) {
    AtlasTab.UPGRADES -> AtlasUpgradeObjects.all
    AtlasTab.ENEMIES -> AtlasEnemyObjects.all
}

@Composable
private fun iconSizeFor(tab: AtlasTab): Dp = when (tab) {
    AtlasTab.UPGRADES -> compactOr(40.dp, 56.dp)
    AtlasTab.ENEMIES -> compactOr(80.dp, 112.dp)
}

@Composable
fun AtlasScreen(gameState: GameState) {
    AtlasScreen(onBack = { gameState.currentScreen = Screen.MAIN_MENU })
}

@Composable
private fun AtlasScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(AtlasTab.UPGRADES) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(rememberMenuBackgroundBrush()),
    ) {
        val layout = menuPanelLayout()
        val panelHeight = maxHeight * compactOr(0.9f, 0.85f)

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(layout.panelGap),
        ) {
            BackTab(boxSize = layout.backTabSize, onClick = onBack)
            AtlasPanel(
                width = layout.panelWidth,
                height = panelHeight,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        }
    }
}

@Composable
private fun AtlasPanel(
    width: Dp,
    height: Dp,
    selectedTab: AtlasTab,
    onTabSelected: (AtlasTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(MenuColors.SIDEBAR),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = compactOr(24.dp, 48.dp),
                    vertical = compactOr(20.dp, 36.dp),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            KotlinLogo(boxSize = compactOr(32.dp, 64.dp))
            Text(
                text = "ATLAS",
                fontFamily = pixelFont(),
                fontSize = compactOr(20.sp, 36.sp),
                color = Color.White,
            )
        }

        AtlasTabBar(selected = selectedTab, onSelected = onTabSelected)
        AtlasList(rows = rowsFor(selectedTab), iconSize = iconSizeFor(selectedTab))
    }
}

@Composable
private fun AtlasTabBar(selected: AtlasTab, onSelected: (AtlasTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = compactOr(16.dp, 32.dp)),
        horizontalArrangement = Arrangement.spacedBy(compactOr(16.dp, 24.dp)),
    ) {
        AtlasTab.entries.forEach { tab ->
            AtlasTabButton(
                tab = tab,
                active = tab == selected,
                onClick = { onSelected(tab) },
            )
        }
    }
}

@Composable
private fun AtlasTabButton(tab: AtlasTab, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = compactOr(6.dp, 10.dp)),
        verticalArrangement = Arrangement.spacedBy(compactOr(4.dp, 6.dp)),
    ) {
        Text(
            text = tab.label,
            fontFamily = pixelFont(),
            fontSize = compactOr(14.sp, 20.sp),
            color = if (active) Color.White else MenuColors.ATLAS_TAB_INACTIVE,
        )
        Box(
            modifier = Modifier
                .width(compactOr(48.dp, 72.dp))
                .height(2.dp)
                .background(if (active) Color.White else Color.Transparent),
        )
    }
}

@Composable
private fun AtlasList(rows: List<AtlasEntry>, iconSize: Dp) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = compactOr(16.dp, 32.dp),
            vertical = compactOr(8.dp, 12.dp),
        ),
        verticalArrangement = Arrangement.spacedBy(compactOr(6.dp, 10.dp)),
    ) {
        items(rows) { row -> AtlasRow(row, iconSize) }
    }
}

@Composable
private fun AtlasRow(row: AtlasEntry, iconSize: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.White)
            .padding(compactOr(8.dp, 12.dp)),
        horizontalArrangement = Arrangement.spacedBy(compactOr(8.dp, 14.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpriteAnimationIcon(
            sheet = row.sheet,
            frames = row.frames,
            frameDurationMs = row.frameDurationMs,
            size = iconSize,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(compactOr(2.dp, 4.dp)),
        ) {
            Text(
                text = row.name,
                fontFamily = pixelFont(),
                fontSize = compactOr(12.sp, 16.sp),
                color = Color.White,
            )
            Text(
                text = row.description,
                fontFamily = pixelFont(),
                fontSize = compactOr(9.sp, 12.sp),
                color = Color.White,
            )
        }
    }
}
