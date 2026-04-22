package com.glycin.koita.gameplay

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.glycin.koita.gameplay.modes.AttackWeapon
import com.glycin.koita.gameplay.modes.BuildBlock

enum class Screen {
    MAIN_MENU,
    GAME,
    HOW_TO_PLAY,
    OPTIONS,
    HIGHSCORES,
}

@Stable
class GameState {
    var currentScreen by mutableStateOf(Screen.MAIN_MENU)
    var score by mutableStateOf(0)
    var collectedStones by mutableStateOf(0)
    var elapsedTimeSeconds by mutableStateOf(0)
    var selectedHotkeyIndex by mutableStateOf(0)
    var selectedBlock by mutableStateOf(BuildBlock.STONE)
    var selectedWeapon by mutableStateOf(AttackWeapon.MISSILE)
    var isPaused by mutableStateOf(false)
    var musicVolume by mutableStateOf(0.25f)
    var sfxVolume by mutableStateOf(1f)

    var canDoubleJump = false
    var canJetpack = false
    var canDash = false
    var canGroundPound = false
    var canHover = false
    var explosiveBlocks = false
    var bouncyBlocks = false
    var resourceShield = false
    var canAnchor = false
    var homingMissilesUnlocked = false
    var turretUnlocked = false

    var damageMultiplier = 1.0f
    var miningRadiusMultiplier = 1.0f
    var visionMultiplier = 1.0f
    var visionFallOfMultiplier = 1.0f
    
    var pickupNotification by mutableStateOf<String?>(null)

    // Weapons
    var laserWeapon = false
    var superSoaker = false
    var sniperWeapon = false
    var rocketLauncher = false

    // Ultimates
    var ultimateAvailable by mutableStateOf<String?>(null)
    var ultimateActive by mutableStateOf(false)
    var ultimateTriggered = false

    // Void
    var passedPortal by mutableStateOf(false)
    var bossSpawned = false
    var bossHealthPercent by mutableStateOf(0f)
}
