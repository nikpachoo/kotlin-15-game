package com.glycin.koita.gameplay.tutorial

object TouchTutorialFlow : TutorialFlow {
    override val tutorialSteps: List<TutorialStep> = listOf(
            MoveStep("Push the left joystick to move"),
            JumpStep("Push the left joystick up to jump"),
            DroneIntroStep(
                "The little drone floating beside you is your companion. It carries all of Kodee's tools and switches between them as you equip them.",
            ),
            EquipPickaxeStep("Tap the Mine chip on the right to switch your drone to mining mode"),
            MineBlockStep("Push the right joystick toward the block to mine it"),
            ShowResourcesStep("Look at the top-right. Your score and materials updated when you mined."),
            EquipWeaponStep("Tap the Attack chip on the right to switch your drone to attack mode"),
            ShootWeaponStep("Push the right joystick to aim and shoot your weapon"),
            EquipHammerStep("Tap the Build chip on the right to switch your drone to build mode"),
            PlaceBlockStep(
                "Push the right joystick to place a block when the indicator is green. This costs some of your mined materials.",
            ),
            LavaJumpStep("Jump in the lava until it damages you"),
            ShowHealthStep("You were hurt. On the top-left you can see your health."),
            KillSlimeStep("Defeat the slime"),
            MineGoldStep("Mine the gold ore"),
            HealStep("Tap the Heal button on the left to heal. This costs 100 ore, and is doubled every time you heal."),
            PickupHeartStep("Mine through the stone block to reach the heart pickup"),
            PickupInfoStep("That pickup increased your max health! Different pickups have different effects."),
            ChargeShrineStep("Stand on the shrine for 3 seconds to charge it"),
            PickUpgradeStep("Now you can see an upgrade. Walk into the orb to unlock it"),
            UpgradeInfoStep(
                "You unlocked your first upgrade! Red upgrades unlock new weapons, blue upgrades unlock movement options, and green upgrades unlock build options.",
            ),
            WeaponSwitchInfoStep(
                "You can change your weapon by tapping the WEAPON button at the bottom-left. The same works for new BLOCKS once you unlock them.",
            ),
            UpgradeCombinationInfoStep("Combining different kinds of upgrades can have unexpected and fun results!"),
            PortalGoalStep("Your goal is to help Kodee reach the top of the world and pass through the portal there."),
            DigUpStep("Dig up to reach the surface. That is your goal in the game"),
            CompleteTutorialStep("Tap ESC at the top-left to complete the tutorial"),
        )
}
