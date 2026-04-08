package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.world.World

abstract class Enemy(
    var position: Vec2,
    val width: Float = 24f,
    val height: Float = 24f,
    val drawWidth: Float = width,
    val drawHeight: Float = height,
    var health: Float = 3f,
    val maxHealth: Float = 3f,
    protected val collisionDetector: CollisionDetector,
    protected val world: World,
) {
    var enemyFacing = EnemyFacing.RIGHT
    var enemyState = EnemyState.IDLE

    var isAlive = true
        protected set

    abstract val spriteAnimator: SpriteAnimator
    open val canAttack: Boolean = true
    open val contactDamage: Int = 0
    open val contactDamageCooldown: Float = 1.0f
    open val dropChance: Float = 0.02f
    open val spriteOffsetY: Float = 0f

    private var contactDamageTimer = 0f
    private var hurtTimer = 0f
    private var attackTimer = 0f

    private val hurtDuration = 0.4f
    private val attackDuration = 0.4f

    val center get() = Vec2(position.x + width / 2f, position.y + height / 2f)
    open var renderOffset: Vec2 = Vec2.zero
        protected set

    fun update(deltaTime: Float) {
        if (!isAlive) return

        if (enemyState == EnemyState.DEATH) {
            updateAnimation(deltaTime)
            return
        }

        if (hurtTimer > 0f) {
            hurtTimer -= deltaTime
            if (hurtTimer <= 0f) {
                enemyState = EnemyState.IDLE
            }
        }

        if (attackTimer > 0f) {
            attackTimer -= deltaTime
            if (attackTimer <= 0f) {
                enemyState = EnemyState.IDLE
            }
        }

        if (enemyState != EnemyState.HURT && enemyState != EnemyState.ATTACK) {
            updateBehavior(deltaTime)
        }

        if (contactDamageTimer > 0f) {
            contactDamageTimer -= deltaTime
        }

        updateAlways(deltaTime)
        updateAnimation(deltaTime)
    }

    fun tryContactDamage(): Boolean {
        if (contactDamage <= 0 || contactDamageTimer > 0f || enemyState == EnemyState.DEATH) return false
        contactDamageTimer = contactDamageCooldown
        return true
    }

    fun takeDamage(amount: Float) {
        if (enemyState == EnemyState.DEATH) return
        health -= amount
        if (health <= 0f) {
            health = 0f
            enemyState = EnemyState.DEATH
        } else {
            enemyState = EnemyState.HURT
            hurtTimer = hurtDuration
        }
    }

    fun startAttack() {
        if (enemyState == EnemyState.HURT || enemyState == EnemyState.DEATH) return
        enemyState = EnemyState.ATTACK
        attackTimer = attackDuration
    }

    protected abstract fun updateBehavior(deltaTime: Float)
    protected open fun updateAlways(deltaTime: Float) {}
    protected abstract fun updateAnimation(deltaTime: Float)
}

enum class EnemyState {
    FALLING,
    FLYING,  //TODO: Dont seperate FLYING and WALKING, make them one state MOVING
    ATTACK,
    HURT,
    DEATH,
    IDLE,
    WALKING,
}

enum class EnemyFacing {
    LEFT,
    RIGHT,
}
