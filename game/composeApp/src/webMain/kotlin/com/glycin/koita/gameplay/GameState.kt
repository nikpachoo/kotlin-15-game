package com.glycin.koita.gameplay

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.core.PlayerSettings
import com.glycin.koita.gameplay.modes.AttackWeapon
import com.glycin.koita.gameplay.modes.BuildBlock
import com.glycin.koita.gameplay.pickups.PickupCatalog
import com.glycin.koita.rest.HighscoresResponse

enum class Screen {
    MAIN_MENU,
    GAME,
    TUTORIAL,
    OPTIONS,
    HIGHSCORES,
    ATLAS,
    MODIFIERS,
    GAME_OVER,
    GAME_WON,
}

@Stable
class GameState {
    var currentScreen by mutableStateOf(Screen.MAIN_MENU)
    var score by mutableStateOf(0)
    var collectedMinerals by mutableStateOf(0)
    var collectedSimple by mutableStateOf(0)
    var collectedRich by mutableStateOf(0)
    var elapsedTimeSeconds by mutableStateOf(0)
    var selectedHotkeyIndex by mutableStateOf(0)
    var selectedBlock by mutableStateOf(BuildBlock.STONE)
    var selectedWeapon by mutableStateOf(AttackWeapon.MISSILE)
    var isPaused by mutableStateOf(false)
    var musicVolume by mutableStateOf(0.25f)
    var sfxVolume by mutableStateOf(0.5f)
    var devMode by mutableStateOf(false)
    var pendingHighscoresResponse by mutableStateOf<HighscoresResponse?>(null)

    var canDoubleJump = false
    var canJetpack = false
    var canDash by mutableStateOf(false)
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
    var scoreMultiplier by mutableStateOf(1)

    fun addScore(base: Int) {
        score += (base * scoreMultiplier * ModifierConfiguration.scoreMultiplier).toInt()
    }

    var pickupNotification by mutableStateOf<String?>(null)
    val pickupCounts = mutableStateMapOf<String, Int>()

    val damageUpCount: Int get() = pickupCounts[PickupCatalog.DAMAGE_UP] ?: 0

    var nextHealCost by mutableStateOf(PlayerSettings.HEAL_COST)

    // Weapons
    var laserWeapon = false
    var superSoaker = false
    var sniperWeapon = false
    var rocketLauncher = false

    // Ultimates
    var ultimateAvailable by mutableStateOf<String?>(null)
    var ultimateActive by mutableStateOf(false)
    var ultimateTriggered = false
    var ultimateCooldownRemaining by mutableStateOf(0f)
    var ultimateBannerName by mutableStateOf<String?>(null)

    // Void
    var passedPortal by mutableStateOf(false)
    var bossSpawned by mutableStateOf(false)
    var bossDefeated = false
    var bossHealthPercent by mutableStateOf(0f)
    var bossName by mutableStateOf("The Final Void")
    var reachedSurfaceMusic = false

    fun endRunAndGoTo(screen: Screen) {
        resetForNewGame()
        if (screen == Screen.MAIN_MENU) {
            SoundManager.stopCurrentLoop()
        }
        currentScreen = screen
    }

    fun resetForNewGame() {
        score = 0
        collectedMinerals = 0
        collectedSimple = 0
        collectedRich = 0
        elapsedTimeSeconds = 0
        selectedHotkeyIndex = 0
        selectedBlock = BuildBlock.STONE
        selectedWeapon = AttackWeapon.MISSILE
        isPaused = false

        canDoubleJump = false
        canJetpack = false
        canDash = false
        canGroundPound = false
        canHover = false
        explosiveBlocks = false
        bouncyBlocks = false
        resourceShield = false
        canAnchor = false
        homingMissilesUnlocked = false
        turretUnlocked = false

        damageMultiplier = 1.0f
        miningRadiusMultiplier = 1.0f
        visionMultiplier = 1.0f
        visionFallOfMultiplier = 1.0f
        scoreMultiplier = 1

        pickupNotification = null
        pickupCounts.clear()
        nextHealCost = PlayerSettings.HEAL_COST

        laserWeapon = false
        superSoaker = false
        sniperWeapon = false
        rocketLauncher = false

        ultimateAvailable = null
        ultimateActive = false
        ultimateTriggered = false
        ultimateCooldownRemaining = 0f
        ultimateBannerName = null

        passedPortal = false
        bossSpawned = false
        bossDefeated = false
        bossHealthPercent = 0f
        bossName = "The Final Void"
        reachedSurfaceMusic = false
    }
}
