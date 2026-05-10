package com.glycin.koita.gameplay.tutorial

object KeyboardTutorialFlow : TutorialFlow {
    override val tutorialSteps: List<TutorialStep> = listOf(
            MoveStep("Use WASD to move"),
            JumpStep("Press SPACE to jump"),
            DroneIntroStep(
                "The little drone floating beside you is your companion. It carries all of Kodee's tools and switches between them as you equip them.",
            ),
            EquipPickaxeStep("Press 1 to switch your drone to mining mode"),
            MineBlockStep("Hold left click on the block to mine it"),
            ShowResourcesStep("Look at the top-right. Your score and materials updated when you mined."),
            EquipWeaponStep("Press 2 to switch your drone to attack mode"),
            ShootWeaponStep("Left click to shoot your weapon"),
            EquipHammerStep("Press 3 to switch your drone to build mode"),
            PlaceBlockStep(
                "Left click to place a block when the indicator is green. This costs some of your mined materials.",
            ),
            LavaJumpStep("Jump in the lava until it damages you"),
            ShowHealthStep("You were hurt. On the top-left you can see your health."),
            KillSlimeStep("Defeat the slime"),
            MineGoldStep("Mine the gold ore"),
            HealStep("Press E to heal. This costs 100 ore, and is doubled every time you heal."),
            PickupHeartStep("Mine through the stone block to reach the heart pickup"),
            PickupInfoStep("That pickup increased your max health! Different pickups have different effects."),
            ChargeShrineStep("Stand on the shrine for 3 seconds to charge it"),
            PickUpgradeStep("Now you can see an upgrade. Walk into the orb to unlock it"),
            UpgradeInfoStep(
                "You unlocked your first upgrade! Red upgrades unlock new weapons, blue upgrades unlock movement options, and green upgrades unlock build options.",
            ),
            WeaponSwitchInfoStep(
                "Use the arrow buttons next to the Weapon panel on the right to cycle weapons. The Block panel works the same way once you unlock new blocks.",
            ),
            UpgradeCombinationInfoStep("Combining different kinds of upgrades can have unexpected and fun results!"),
            PortalGoalStep("Your goal is to help Kodee reach the top of the world and pass through the portal there."),
            DigUpStep("Dig up to reach the surface. That is your goal in the game"),
            CompleteTutorialStep("Press ESC to complete the tutorial"),
        )
}
