package com.glycin.koita.composables

import com.glycin.koita.core.SpriteSheet
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.boss_sheet
import koita.composeapp.generated.resources.confuser_sheet
import koita.composeapp.generated.resources.hydra_sheet
import koita.composeapp.generated.resources.null_phantom_sheet
import koita.composeapp.generated.resources.slime_sheet
import koita.composeapp.generated.resources.spider_sheet
import koita.composeapp.generated.resources.stone_golem_sheet
import koita.composeapp.generated.resources.wraith_sheet

object AtlasEnemyObjects {
    private val SLIME = SpriteSheet(Res.drawable.slime_sheet, 128, 128, 16)
    private val SPIDER = SpriteSheet(Res.drawable.spider_sheet, 128, 128, 9)
    private val STONE_GOLEM = SpriteSheet(Res.drawable.stone_golem_sheet, 128, 128, 10)
    private val HYDRA = SpriteSheet(Res.drawable.hydra_sheet, 128, 128, 16)
    private val WRAITH = SpriteSheet(Res.drawable.wraith_sheet, 128, 128, 14)
    private val NULL_PHANTOM = SpriteSheet(Res.drawable.null_phantom_sheet, 128, 128, 16)
    private val CONFUSER = SpriteSheet(Res.drawable.confuser_sheet, 128, 128, 14)
    private val BOSS = SpriteSheet(Res.drawable.boss_sheet, 64, 64, 16)

    val all: List<AtlasEntry> = listOf(
        AtlasEntry(SLIME, 0..2, 100, "Legacy Slime", "Bouncy ground enemy that leaves a sticky slime trail to immobilize Kodee."),
        AtlasEntry(SPIDER, 0..2, 100, "Dependency Spider", "Spins long web strands across terrain that blocks passage."),
        AtlasEntry(STONE_GOLEM, 30..32, 150, "Boilerplate Golem", "Heavy ground brawler that chases Kodee and digs through terrain."),
        AtlasEntry(HYDRA, 21..23, 200, "Callback Hydra", "Flying patroller that fires missiles towards Kodee."),
        AtlasEntry(WRAITH, 28..30, 100, "Race Condition Wraith", "Stationary spectre that fires missiles Kodee."),
        AtlasEntry(NULL_PHANTOM, 22..24, 100, "Null Phantom", "Hovers and dashes through terrain in a straight line towards Kodee."),
        AtlasEntry(CONFUSER, 31..40, 100, "Type Confuser", "Pulls tiles into orbit and unleashes them as a volley of lava projectiles."),
        AtlasEntry(BOSS, 0..9, 100, "The Final Void", "The void at the world's edge. Kodee's final test."),
    )
}
