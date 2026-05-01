package com.glycin.koita.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.glycin.koita.ui.pixelFont
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Player
import com.glycin.koita.core.PlayerFacing
import com.glycin.koita.core.drawSpriteFrame
import com.glycin.koita.gameplay.FogOfWar
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.weapon.Laser
import com.glycin.koita.gameplay.weapon.MagicMissile
import com.glycin.koita.gameplay.weapon.Rocket
import com.glycin.koita.gameplay.weapon.Sniper
import com.glycin.koita.gameplay.weapon.SuperSoaker
import com.glycin.koita.gameplay.enemies.boss.Boss
import com.glycin.koita.gameplay.ultimates.UltimateManager
import com.glycin.koita.gameplay.enemies.Confuser
import com.glycin.koita.gameplay.enemies.EnemyFacing
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.pickups.PickupManager
import com.glycin.koita.gameplay.turrets.Turret
import com.glycin.koita.gameplay.turrets.TurretManager
import com.glycin.koita.gameplay.turrets.TurretMissile
import com.glycin.koita.gameplay.upgrades.ShrineManager
import com.glycin.koita.physics.CollectibleSystem
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.ParallaxBackground
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import org.jetbrains.compose.resources.imageResource

//TODO: Probably need to remove all the "devMode" checks in the rendering hot path
@Composable
fun WorldRenderer(
    gameState: GameState,
    world: World,
    camera: Camera,
    player: Player,
    particleSystem: ParticleSystem,
    collectibleSystem: CollectibleSystem,
    enemyManager: EnemyManager,
    shrineManager: ShrineManager,
    pickupManager: PickupManager,
    turretManager: TurretManager,
    mouse: Mouse,
    parallaxBackground: ParallaxBackground,
    fogOfWar: FogOfWar,
    ultimateManager: UltimateManager,
    frameCount: Long,
    boss: Boss?,
) {

    val playerSheet = imageResource(player.animator.spriteAnimator.sprite)
    val droneSheet = imageResource(player.droneAnimator.sprite)
    val enemySheets = enemyManager.getDistinctSprites().associateWith { imageResource(it) }
    val shrineSheets = shrineManager.getDistinctSprites().associateWith { imageResource(it) }
    val orbIconSheets = shrineManager.getDistinctOrbIconSprites().associateWith { imageResource(it) }
    val pickupSheets = pickupManager.getDistinctSprites().associateWith { imageResource(it) }
    val bossSheet = boss?.let { imageResource(it.spriteAnimator.sprite) }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current.density
    val titleFontSize = (14f / density).sp
    val descFontSize = (10f / density).sp
    val orbTitleStyle = TextStyle(fontFamily = pixelFont(), fontSize = titleFontSize, color = Color.White)
    val orbDescStyle = TextStyle(fontFamily = pixelFont(), fontSize = descFontSize, color = Color.LightGray, textAlign = TextAlign.Center)
    val outlineStyle = TextStyle(fontFamily = pixelFont(), color = Color.Black)

    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val _tick = frameCount // Hack to trigger recomposition, so it needs to remain here even if unused
        val canvasWidth = camera.canvasWidth
        val canvasHeight = camera.canvasHeight

        with(camera) { withVirtualViewport {

        if (!gameState.passedPortal) {
            parallaxBackground.render(this)
        }

        val visibleChunks = world.getVisibleChunks(camera, canvasWidth, canvasHeight)
        visibleChunks.forEach { chunk ->
            val chunkBitmap = chunk.generateBitmap()

            val chunkPixelSize = WorldConstants.CHUNK_SIZE * WorldConstants.TILE_SIZE
            val chunkWorldX = chunk.xGridPos * chunkPixelSize.toFloat()
            val chunkWorldY = chunk.yGridPos * chunkPixelSize.toFloat()

            val screenPos = camera.worldToScreen(chunkWorldX, chunkWorldY)

            drawImage(
                image = chunkBitmap,
                dstOffset = IntOffset(screenPos.x.toInt(), screenPos.y.toInt()),
                dstSize = IntSize(chunkPixelSize, chunkPixelSize),
                filterQuality = FilterQuality.None
            )

            if (gameState.devMode) {
                //Debug rects
                drawRect(
                    color = if(chunk.isDirty) Color.Red else Color.Yellow,
                    Offset(screenPos.x, screenPos.y),
                    size = Size(WorldConstants.CHUNK_PIXEL_WIDTH.toFloat(), WorldConstants.CHUNK_PIXEL_HEIGHT.toFloat()),
                    style = Stroke(),
                )
            }
        }

        val nearbyShrines = shrineManager.getShrinesInRange(player.center, 1000f)
        val shrineSheet = nearbyShrines.firstOrNull()?.spriteAnimator?.sprite?.let { shrineSheets[it] }
        if (shrineSheet != null) nearbyShrines.forEach { shrine ->
            val screenPos = camera.worldToScreen(shrine.position.x, shrine.position.y)
            val sa = shrine.spriteAnimator

            val drawOffsetX = (shrine.width - shrine.drawWidth) / 2f
            val drawOffsetY = shrine.height - shrine.drawHeight
            val drawX = screenPos.x + drawOffsetX
            val drawY = screenPos.y + drawOffsetY

            drawImage(
                image = shrineSheet,
                srcOffset = IntOffset(sa.srcX, sa.srcY),
                srcSize = IntSize(sa.frameWidth, sa.frameHeight),
                dstOffset = IntOffset(drawX.toInt(), drawY.toInt()),
                dstSize = IntSize(shrine.drawWidth.toInt(), shrine.drawHeight.toInt()),
                filterQuality = FilterQuality.None,
            )

            if (gameState.devMode) {
                drawRect(
                    color = Color.Cyan,
                    topLeft = screenPos,
                    size = Size(shrine.width, shrine.height),
                    style = Stroke(),
                )
            }
        }

        with(player) {
            val sa =  animator.spriteAnimator
            val srcOffsetX = sa.srcX
            val srcOffsetY = sa.srcY
            val scaledDrawWidth = (drawWidth * giantScale).toInt()
            val scaledDrawHeight = (drawHeight * giantScale).toInt()
            val offsetX = (drawWidth * giantScale - width) / 2f
            val offsetY = drawHeight * spriteFeetRatio - height + (drawHeight * spriteFeetRatio * (giantScale - 1f))
            val pScreenPos = camera.worldToScreen(position.x - offsetX, position.y - offsetY)
            val scaleX = if(facing == PlayerFacing.RIGHT) 1f else -1f

            scale(
                scaleX = scaleX,
                scaleY = 1f,
                pivot = if(facing == PlayerFacing.RIGHT) Offset(0f,0f) else Offset(pScreenPos.x + scaledDrawWidth / 2f, pScreenPos.y + scaledDrawHeight / 2f),
            ) {
                drawImage(
                    image = playerSheet,
                    srcOffset = IntOffset(srcOffsetX, srcOffsetY),
                    srcSize = IntSize(sa.frameWidth, sa.frameHeight),
                    dstOffset = IntOffset(pScreenPos.x.toInt(), pScreenPos.y.toInt()),
                    dstSize = IntSize(scaledDrawWidth, scaledDrawHeight),
                    filterQuality = FilterQuality.None,
                )
            }

            val droneScreenPos = camera.worldToScreen(dronePosition.x, dronePosition.y)
            val droneSize = 32
            val droneHalf = droneSize / 2f

            drawImage(
                image = droneSheet,
                srcOffset = IntOffset(droneAnimator.srcX, droneAnimator.srcY),
                srcSize = IntSize(droneAnimator.frameWidth, droneAnimator.frameHeight),
                dstOffset = IntOffset(
                    (droneScreenPos.x - droneHalf).toInt(),
                    (droneScreenPos.y - droneHalf).toInt(),
                ),
                dstSize = IntSize(droneSize, droneSize),
                filterQuality = FilterQuality.None,
            )

            if (gameState.devMode) {
                val playerHitboxScreenPos = camera.worldToScreen(position.x, position.y)
                drawRect(
                    color = Color.Green,
                    topLeft = playerHitboxScreenPos,
                    size = Size(width, height),
                    style = Stroke(),
                )
                drawRect(
                    color = Color.Magenta,
                    topLeft = droneScreenPos,
                    size = Size(droneSize.toFloat(), droneSize.toFloat()),
                    style = Stroke(),
                )
            }

        }

        val nearbyEnemies = enemyManager.getEnemiesInRange(player.center, 1000f)
        nearbyEnemies.forEach { e ->
            val screenPos = camera.worldToScreen(e.position.x + e.renderOffset.x, e.position.y + e.renderOffset.y)
            val sa = e.spriteAnimator
            val enemySheet = enemySheets[sa.sprite] ?: return@forEach

            val drawOffsetX = (e.width - e.drawWidth) / 2f
            val drawOffsetY = (e.height - e.drawHeight) / 2f + e.spriteOffsetY
            val drawX = screenPos.x + drawOffsetX
            val drawY = screenPos.y + drawOffsetY

            if(gameState.devMode) {
                drawRect(
                    color = Color.Red,
                    topLeft = screenPos,
                    size = Size(e.width, e.height),
                )
            }

            if (e.enemyFacing == EnemyFacing.RIGHT) {
                drawImage(
                    image = enemySheet,
                    srcOffset = IntOffset(sa.srcX, sa.srcY),
                    srcSize = IntSize(sa.frameWidth, sa.frameHeight),
                    dstOffset = IntOffset(drawX.toInt(), drawY.toInt()),
                    dstSize = IntSize(e.drawWidth.toInt(), e.drawHeight.toInt()),
                    filterQuality = FilterQuality.None,
                )
            } else {
                scale(
                    scaleX = -1f,
                    scaleY = 1f,
                    pivot = Offset(drawX + e.drawWidth / 2f, drawY + e.drawHeight / 2f),
                ) {
                    drawImage(
                        image = enemySheet,
                        srcOffset = IntOffset(sa.srcX, sa.srcY),
                        srcSize = IntSize(sa.frameWidth, sa.frameHeight),
                        dstOffset = IntOffset(drawX.toInt(), drawY.toInt()),
                        dstSize = IntSize(e.drawWidth.toInt(), e.drawHeight.toInt()),
                        filterQuality = FilterQuality.None,
                    )
                }
            }

            if (e is Confuser) {
                e.forEachPulledTile { x, y ->
                    val tileScreenPos = camera.worldToScreen(x, y)
                    drawRect(
                        color = WorldRendererColors.CONFUSER_TILE,
                        topLeft = tileScreenPos,
                        size = WorldConstants.STANDARD_SIZE,
                    )
                }
                e.forEachProjectile { x, y ->
                    val projScreenPos = camera.worldToScreen(x, y)
                    drawRect(
                        color = Tile.LAVA.color,
                        topLeft = projScreenPos,
                        size = Size(8f, 8f),
                    )
                }
            }

        }

        enemyManager.forEachMissile { missile ->
            val screenPos = camera.worldToScreen(missile.position.x, missile.position.y)
            drawRect(
                color = WorldRendererColors.ENEMY_MISSILE,
                topLeft = screenPos,
                size = Size(missile.size, missile.size),
            )
        }

        shrineManager.getOrbs().forEach { orb ->
            val screenPos = camera.worldToScreen(orb.position.x, orb.position.y)
            val orbCenterX = screenPos.x + orb.size / 2f

            val titleResult = textMeasurer.measure(orb.unlock.name, orbTitleStyle)
            val titleX = orbCenterX - titleResult.size.width / 2f
            val titleY = screenPos.y - titleResult.size.height - 6f

            val titleOutline = textMeasurer.measure(orb.unlock.name, outlineStyle.copy(fontSize = titleFontSize))
            for ((dx, dy) in listOf(-1f to 0f, 1f to 0f, 0f to -1f, 0f to 1f)) {
                drawText(titleOutline, topLeft = Offset(titleX + dx, titleY + dy))
            }
            drawText(titleResult, topLeft = Offset(titleX, titleY))

            drawOval(
                color = WorldRendererColors.ORB_OUTER,
                topLeft = Offset(screenPos.x - 4f, screenPos.y - 4f),
                size = Size(orb.size + 8f, orb.size + 8f),
            )
            drawOval(
                color = WorldRendererColors.ORB_INNER,
                topLeft = screenPos,
                size = Size(orb.size, orb.size),
            )

            val orbIcon = orb.unlock.icon
            val orbIconImage = orbIconSheets[orbIcon.sheet.sprite]
            if (orbIconImage != null) {
                drawSpriteFrame(
                    image = orbIconImage,
                    frame = orbIcon,
                    dstOffset = IntOffset(screenPos.x.toInt(), screenPos.y.toInt()),
                    dstSize = IntSize(orb.size.toInt(), orb.size.toInt()),
                )
            }

            val descConstraints = Constraints(maxWidth = (200f / density).toInt())
            val descResult = textMeasurer.measure(orb.unlock.description, orbDescStyle, constraints = descConstraints)
            val descX = orbCenterX - descResult.size.width / 2f
            val descY = screenPos.y + orb.size + 6f

            val descOutline = textMeasurer.measure(orb.unlock.description, outlineStyle.copy(fontSize = descFontSize, textAlign = TextAlign.Center), constraints = descConstraints)
            for ((dx, dy) in listOf(-1f to 0f, 1f to 0f, 0f to -1f, 0f to 1f)) {
                drawText(descOutline, topLeft = Offset(descX + dx, descY + dy))
            }
            drawText(descResult, topLeft = Offset(descX, descY))
        }

        pickupManager.forEachInRange(player.position.y, 1000) { pickup ->
            val sa = pickup.spriteAnimator
            val pickupSheet = pickupSheets[sa.sprite] ?: return@forEachInRange
            val screenPos = camera.worldToScreen(pickup.position.x, pickup.position.y)

            drawImage(
                image = pickupSheet,
                srcOffset = IntOffset(sa.srcX, sa.srcY),
                srcSize = IntSize(sa.frameWidth, sa.frameHeight),
                dstOffset = IntOffset(screenPos.x.toInt(), screenPos.y.toInt()),
                dstSize = IntSize(pickup.size, pickup.size),
                filterQuality = FilterQuality.None,
            )
        }

        particleSystem.forEachActive { x, y, tile ->
            val screenPos = camera.worldToScreen(x, y)
            drawRect(
                color = tile.color,
                topLeft = screenPos,
                size = WorldConstants.STANDARD_SIZE,
            )
        }

        collectibleSystem.forEachActive { x, y, tile ->
            val screenPos = camera.worldToScreen(x, y)
            drawRect(
                color = tile.color,
                topLeft = screenPos,
                size = WorldConstants.STANDARD_SIZE,
            )
        }

        turretManager.forEachTurret { turret ->
            val screenPos = camera.worldToScreen(turret.position.x, turret.position.y)
            drawOval(
                color = WorldRendererColors.TURRET,
                topLeft = screenPos,
                size = Size(Turret.SPHERE_SIZE, Turret.SPHERE_SIZE),
            )
        }

        turretManager.forEachMissile { missile ->
            val screenPos = camera.worldToScreen(missile.position.x, missile.position.y)
            drawRect(
                color = WorldRendererColors.TURRET_MISSILE,
                topLeft = screenPos,
                size = Size(TurretMissile.SIZE, TurretMissile.SIZE),
            )
        }

        fogOfWar.render(this)

        ultimateManager.activeUltimate?.let { ultimate ->
            with(ultimate) { render(camera, player, frameCount) }
        }

        // Render player attacks after fog of war so they're always visible
        player.weapons.forEach { attack ->
            when (attack) {
                is MagicMissile -> {
                    val aScreenPos = camera.worldToScreen(attack.position.x, attack.position.y)
                    drawRect(
                        color = Color.Red,
                        topLeft = aScreenPos,
                        size = Size(attack.baseSize, attack.baseSize),
                    )
                }
                is Rocket -> {
                    val rScreenPos = camera.worldToScreen(attack.position.x, attack.position.y)
                    drawRect(
                        color = WorldRendererColors.ROCKET,
                        topLeft = rScreenPos,
                        size = Size(Rocket.BASE_SIZE, Rocket.BASE_SIZE),
                    )
                }
                is Laser -> if (attack.isActive) {
                    val laserStart = camera.worldToScreen(attack.start.x, attack.start.y)
                    val laserEnd = camera.worldToScreen(attack.end.x, attack.end.y)
                    drawLaser(laserStart, laserEnd)
                }
                is SuperSoaker -> {
                    attack.droplets.forEach { droplet ->
                        if (droplet.alive) {
                            val dScreenPos = camera.worldToScreen(droplet.position.x, droplet.position.y)
                            drawRect(
                                color = Tile.WATER.color,
                                topLeft = dScreenPos,
                                size = Size(4f, 4f),
                            )
                        }
                    }
                }
                is Sniper -> {
                    if (attack.isCharging) {
                        val start = camera.worldToScreen(attack.guideStart.x, attack.guideStart.y)
                        val leftEnd = camera.worldToScreen(attack.guideLineLeft.x, attack.guideLineLeft.y)
                        val rightEnd = camera.worldToScreen(attack.guideLineRight.x, attack.guideLineRight.y)
                        drawLine(
                            color = WorldRendererColors.SNIPER_GUIDE,
                            start = start,
                            end = leftEnd,
                            strokeWidth = 1f,
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = WorldRendererColors.SNIPER_GUIDE,
                            start = start,
                            end = rightEnd,
                            strokeWidth = 1f,
                            cap = StrokeCap.Round,
                        )
                    }
                    if (attack.bulletActive) {
                        val bStart = camera.worldToScreen(attack.bulletStart.x, attack.bulletStart.y)
                        val bEnd = camera.worldToScreen(attack.bulletEnd.x, attack.bulletEnd.y)
                        drawGlowLine(
                            start = bStart,
                            end = bEnd,
                            outerColor = WorldRendererColors.SNIPER_BULLET_OUTER,
                            outerWidth = 8f,
                            middleColor = WorldRendererColors.SNIPER_BULLET_MIDDLE,
                            middleWidth = 3f,
                            coreColor = WorldRendererColors.SNIPER_BULLET_CORE,
                        )
                    }
                }
            }
        }

        // Render Boss after fog of war so it's always visible
        if (boss != null && boss.isAlive) {
            val bossScreenPos = camera.worldToScreen(
                boss.position.x + boss.renderOffset.x,
                boss.position.y + boss.renderOffset.y,
            )

            boss.forEachShieldTile { x, y ->
                val tileScreenPos = camera.worldToScreen(x, y)
                drawRect(
                    color = Tile.SHIELD.color,
                    topLeft = tileScreenPos,
                    size = WorldConstants.STANDARD_SIZE,
                )
            }

            val sa = boss.spriteAnimator
            val drawOffsetX = (boss.width - boss.drawWidth) / 2f
            val drawOffsetY = (boss.height - boss.drawHeight) / 2f
            val drawX = bossScreenPos.x + drawOffsetX
            val drawY = bossScreenPos.y + drawOffsetY

            if (bossSheet != null) {
                drawImage(
                    image = bossSheet,
                    srcOffset = IntOffset(sa.srcX, sa.srcY),
                    srcSize = IntSize(sa.frameWidth, sa.frameHeight),
                    dstOffset = IntOffset(drawX.toInt(), drawY.toInt()),
                    dstSize = IntSize(boss.drawWidth.toInt(), boss.drawHeight.toInt()),
                    filterQuality = FilterQuality.None,
                )
            }

            val eye = boss.eye
            val irisCX = bossScreenPos.x + boss.width / 2f + eye.irisOffset.x
            val irisCY = bossScreenPos.y + boss.height / 2f + eye.irisOffset.y
            val r = eye.irisRadius
            drawOval(
                color = WorldRendererColors.BOSS_EYE_PUPIL,
                topLeft = Offset(irisCX - r, irisCY - r),
                size = Size(r * 2f, r * 2f),
            )

            boss.forEachLaser { sx, sy, ex, ey ->
                val start = camera.worldToScreen(sx, sy)
                val end = camera.worldToScreen(ex, ey)
                drawBossLaser(start, end)
            }

            boss.forEachEyeBeam { sx, sy, ex, ey, charging ->
                val start = camera.worldToScreen(sx, sy)
                val end = camera.worldToScreen(ex, ey)
                if (charging) {
                    drawLine(
                        color = WorldRendererColors.BOSS_EYE_BEAM_CHARGE,
                        start = start,
                        end = end,
                        strokeWidth = 1.5f,
                        cap = StrokeCap.Round,
                    )
                } else {
                    drawGlowLine(
                        start = start,
                        end = end,
                        outerColor = WorldRendererColors.BOSS_LASER_OUTER,
                        outerWidth = 32f,
                        middleColor = WorldRendererColors.BOSS_LASER_MIDDLE,
                        middleWidth = 16f,
                        coreColor = WorldRendererColors.BOSS_LASER_CORE,
                        coreWidth = 5f,
                    )
                }
            }
        }

        if (gameState.devMode) {
            // Debug stuff:
            val cameraScreenPos = camera.screenPosition
            drawOval(
                color = Color.Magenta,
                topLeft = Offset(cameraScreenPos.x - 7.5f, cameraScreenPos.y - 7.5f),
                size = Size(15f, 15f),
            )

            drawOval(
                color = Color.Cyan,
                topLeft = Offset(mouse.position.x - 7.5f, mouse.position.y - 7.5f),
                size = Size(15f, 15f),
            )
        }

        } }
    }
}

private fun DrawScope.drawGlowLine(
    start: Offset,
    end: Offset,
    outerColor: Color,
    outerWidth: Float,
    middleColor: Color,
    middleWidth: Float,
    coreColor: Color,
    coreWidth: Float = 2f,
) {
    drawLine(color = outerColor, start = start, end = end, strokeWidth = outerWidth, cap = StrokeCap.Round)
    drawLine(color = middleColor, start = start, end = end, strokeWidth = middleWidth, cap = StrokeCap.Round)
    drawLine(color = coreColor, start = start, end = end, strokeWidth = coreWidth, cap = StrokeCap.Round)
}

private fun DrawScope.drawLaser(laserStart: Offset, laserEnd: Offset) {
    drawGlowLine(
        start = laserStart,
        end = laserEnd,
        outerColor = WorldRendererColors.LASER_OUTER,
        outerWidth = 12f,
        middleColor = WorldRendererColors.LASER_MIDDLE,
        middleWidth = 5f,
        coreColor = WorldRendererColors.LASER_CORE,
    )
    drawOval(
        color = WorldRendererColors.LASER_IMPACT,
        topLeft = Offset(laserEnd.x - 6f, laserEnd.y - 6f),
        size = Size(12f, 12f),
    )
}

private fun DrawScope.drawBossLaser(start: Offset, end: Offset) {
    drawGlowLine(
        start = start,
        end = end,
        outerColor = WorldRendererColors.BOSS_LASER_OUTER,
        outerWidth = 14f,
        middleColor = WorldRendererColors.BOSS_LASER_MIDDLE,
        middleWidth = 6f,
        coreColor = WorldRendererColors.BOSS_LASER_CORE,
    )
}