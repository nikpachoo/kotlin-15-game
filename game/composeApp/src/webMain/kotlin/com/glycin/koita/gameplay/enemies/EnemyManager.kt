package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameSettings
import com.glycin.koita.gameplay.enemies.boss.Boss
import com.glycin.koita.gameplay.pickups.PickupManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.World
import kotlin.math.abs

class EnemyManager(
    private val collisionDetector: CollisionDetector,
    private val world: World,
    private val particleSystem: ParticleSystem,
    private val pickupManager: PickupManager,
) {
    private val enemies = mutableListOf<Enemy>()
    private var boss: Boss? = null
    private val activationRange = (GameSettings.BASE_LIGHT_RADIUS + GameSettings.FALL_OFF_DISTANCE) * 2

    var onEnemyKilled: () -> Unit = {}
    private val killHandler: () -> Unit = { onEnemyKilled() }

    fun add(enemy: Enemy) {
        enemy.onKill = killHandler
        enemies.add(enemy)
    }

    fun setBoss(boss: Boss?) {
        this.boss = boss
    }

    private fun isInRange(enemy: Enemy, playerY: Float): Boolean =
        abs(enemy.position.y - playerY) <= activationRange

    fun update(deltaTime: Float, player: Player) {
        val playerPos = player.position
        val playerW = player.width
        val playerH = player.height

        for (i in 0..<enemies.size) {
            val enemy = enemies[i]
            val shooter = enemy as? ShootingEnemy
            shooter?.tickMissiles(deltaTime)

            var skipMissiles = false
            if (isInRange(enemy, playerPos.y)) {
                enemy.update(deltaTime)
                if (!enemy.isAlive) {
                    skipMissiles = true
                } else {
                    if (enemy.contactDamage > 0 &&
                        collisionDetector.checkAABBOverlap(enemy.position, enemy.width, enemy.height, playerPos, playerW, playerH) &&
                        enemy.tryContactDamage()
                    ) {
                        player.takeDamage(enemy.contactDamage)
                    }
                    shooter?.tickAttack(deltaTime, playerPos)
                }
            }

            if (shooter != null && !skipMissiles) {
                val missiles = shooter.activeMissiles
                for (j in 0..<missiles.size) {
                    val m = missiles[j]
                    if (m.isAlive && m.checkPlayerCollision(playerPos, playerW, playerH)) {
                        m.isAlive = false
                        player.takeDamage(1)
                    }
                }
            }
        }

        for (i in enemies.size - 1 downTo 0) {
            val enemy = enemies[i]
            if (!enemy.isAlive) {
                pickupManager.randomChanceSpawn(enemy.center, enemy.dropChance)
                enemies.removeAt(i)
            }
        }
    }

    fun forEachMissile(action: (EnemyMissile) -> Unit) {
        for (enemy in enemies) {
            if (enemy is ShootingEnemy) {
                val missiles = enemy.activeMissiles
                for (i in 0..<missiles.size) {
                    val m = missiles[i]
                    if (m.isAlive) action(m)
                }
            }
        }
    }

    fun clearAll() {
        enemies.clear()
        boss = null
    }

    //TODO: Called per-frame by WorldRenderer. Cache a Set<DrawableResource> updated only on add/remove instead of allocating list+set+list every call.
    fun getDistinctSprites() = enemies.map { it.spriteAnimator.sprite }.distinct()

    //TODO: Optimization, dont use .filter
    fun getEnemiesCollidingWith(pos: Vec2, width: Float, height: Float): List<Enemy> {
        return enemies.filter { e ->
            e.isAlive &&
            pos.x < e.position.x + e.width &&
            pos.x + width > e.position.x &&
            pos.y < e.position.y + e.height &&
            pos.y + height > e.position.y
        }
    }

    //TODO: Optimization, instead of filtering each time we call this, on update mark enemies in range or not and filter based on that boolean
    fun getEnemiesInRange(pos: Vec2, range: Float): List<Enemy> {
        val rangeSq = range * range
        return enemies.filter { e ->
            Vec2.fastDistance(e.center, pos) <= rangeSq
        }
    }

    fun findFirstEnemyCollidingWith(px: Float, py: Float, w: Float, h: Float): Enemy? {
        for (i in 0..<enemies.size) {
            val e = enemies[i]
            if (e.isAlive &&
                px < e.position.x + e.width &&
                px + w > e.position.x &&
                py < e.position.y + e.height &&
                py + h > e.position.y
            ) return e
        }
        return null
    }

    fun findNearestAliveEnemy(pos: Vec2, range: Float): Enemy? =
        findNearestAliveEnemy(pos.x, pos.y, range)

    fun findNearestAliveEnemy(x: Float, y: Float, range: Float): Enemy? {
        var nearest: Enemy? = null
        var nearestDistSq = range * range
        for (i in 0..<enemies.size) {
            val e = enemies[i]
            if (!e.isAlive) continue
            val distSq = Vec2.fastDistance(x, y, e.center.x, e.center.y)
            if (distSq <= nearestDistSq) {
                nearestDistSq = distSq
                nearest = e
            }
        }
        return nearest
    }

    fun damageInRange(pos: Vec2, range: Float, damage: Float) {
        val rangeSq = range * range
        for (i in 0..<enemies.size) {
            val e = enemies[i]
            if (!e.isAlive) continue
            if (Vec2.fastDistance(e.center, pos) <= rangeSq) {
                e.takeDamage(damage)
            }
        }
        val b = boss
        if (b != null && b.isAlive && Vec2.fastDistance(b.center, pos) <= rangeSq) {
            b.takeDamage(damage)
        }
    }

    fun damageFirstColliding(px: Float, py: Float, w: Float, h: Float, damage: Float): Boolean {
        for (i in 0..<enemies.size) {
            val e = enemies[i]
            if (e.isAlive &&
                px < e.position.x + e.width &&
                px + w > e.position.x &&
                py < e.position.y + e.height &&
                py + h > e.position.y
            ) {
                e.takeDamage(damage)
                return true
            }
        }
        val b = boss
        if (b != null && b.isAlive &&
            px < b.position.x + b.width &&
            px + w > b.position.x &&
            py < b.position.y + b.height &&
            py + h > b.position.y
        ) {
            b.takeDamage(damage)
            return true
        }
        return false
    }

    fun anyHostileColliding(px: Float, py: Float, w: Float, h: Float): Boolean {
        for (i in 0..<enemies.size) {
            val e = enemies[i]
            if (e.isAlive &&
                px < e.position.x + e.width &&
                px + w > e.position.x &&
                py < e.position.y + e.height &&
                py + h > e.position.y
            ) return true
        }
        val b = boss
        return b != null && b.isAlive &&
                px < b.position.x + b.width &&
                px + w > b.position.x &&
                py < b.position.y + b.height &&
                py + h > b.position.y
    }

    fun findNearestTargetCenter(pos: Vec2, range: Float): Vec2? =
        findNearestTargetCenter(pos.x, pos.y, range)

    fun findNearestTargetCenter(x: Float, y: Float, range: Float): Vec2? {
        var nearestCenter: Vec2? = null
        var nearestDistSq = range * range
        for (i in 0..<enemies.size) {
            val e = enemies[i]
            if (!e.isAlive) continue
            val ec = e.center
            val distSq = Vec2.fastDistance(x, y, ec.x, ec.y)
            if (distSq <= nearestDistSq) {
                nearestDistSq = distSq
                nearestCenter = ec
            }
        }
        val b = boss
        if (b != null && b.isAlive) {
            val bc = b.center
            if (Vec2.fastDistance(x, y, bc.x, bc.y) <= nearestDistSq) {
                nearestCenter = bc
            }
        }
        return nearestCenter
    }

    fun destroyShieldsInRadius(pos: Vec2, radius: Float) {
        boss?.destroyShieldsInRadius(pos, radius)
    }

    fun damageInBeam(origin: Vec2, direction: Vec2, length: Float, width: Float, damage: Float) {
        val ox = origin.x
        val oy = origin.y
        val dirX = direction.x
        val dirY = direction.y
        for (i in 0..<enemies.size) {
            val e = enemies[i]
            if (!e.isAlive) continue
            val ec = e.center
            val toX = ec.x - ox
            val toY = ec.y - oy
            val proj = toX * dirX + toY * dirY
            if (proj <= 0f || proj >= length) continue
            val perpDist = abs(toX * dirY - toY * dirX)
            if (perpDist < width) {
                e.takeDamage(damage)
            }
        }
        val b = boss
        if (b != null && b.isAlive) {
            val bc = b.center
            val toX = bc.x - ox
            val toY = bc.y - oy
            val proj = toX * dirX + toY * dirY
            if (proj > 0f && proj < length) {
                val perpDist = abs(toX * dirY - toY * dirX)
                if (perpDist < width) {
                    b.takeDamage(damage)
                }
            }
        }
    }
}
