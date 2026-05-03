package com.glycin.koita.gameplay.tutorial

class TutorialDirector(
    private val context: StepContext,
    private val state: TutorialState,
) {
    private val steps: List<TutorialStep> = listOf(
        MoveStep(),
        JumpStep(),
        DroneIntroStep(),
        EquipPickaxeStep(),
        MineBlockStep(),
        ShowResourcesStep(),
        EquipWeaponStep(),
        ShootWeaponStep(),
        EquipHammerStep(),
        PlaceBlockStep(),
        LavaJumpStep(),
        ShowHealthStep(),
        KillSlimeStep(),
        MineGoldStep(),
        HealStep(),
        PickupHeartStep(),
        PickupInfoStep(),
        ChargeShrineStep(),
        PickUpgradeStep(),
        UpgradeInfoStep(),
        WeaponSwitchInfoStep(),
        UpgradeCombinationInfoStep(),
        PortalGoalStep(),
        PlaceholderStep(),
    )

    private var activeStep: TutorialStep? = null

    init {
        check(steps.size == state.totalSteps) {
            "TutorialState.totalSteps (${state.totalSteps}) must match steps.size (${steps.size})"
        }
        loadCurrentStep()
    }

    fun update() {
        if (state.isCompleted) return
        val step = activeStep ?: return
        val clicked = step.awaitsContinue && state.continueRequested
        if (step.isComplete(context) || clicked) {
            state.continueRequested = false
            step.cleanup(context)
            advance()
        }
    }

    private fun advance() {
        state.currentStepIndex++
        if (state.currentStepIndex >= steps.size) {
            activeStep = null
            state.isCompleted = true
            state.awaitingContinue = false
            state.promptText = "Tutorial complete! Press ESC to return to the menu"
        } else {
            loadCurrentStep()
        }
    }

    private fun loadCurrentStep() {
        val step = steps[state.currentStepIndex]
        activeStep = step
        state.promptText = step.prompt
        state.awaitingContinue = step.awaitsContinue
        state.continueRequested = false
        step.setup(context)
    }

    companion object {
        const val STEP_COUNT = 24
    }
}
