package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.sixkings.SixKingsGuide;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

/**
 * 六王 · 第一层：引路人之厅
 *
 * 没有战斗。NPC 在此讲述六人的往事。
 */
public class SixKingsLevel0 extends SixKingsLevelBase {

    @Override
    protected Mob createBoss() {
        // 这一层放 NPC
        SixKingsGuide guide = new SixKingsGuide();
        guide.pos = (H / 2) * W + (W / 2);
        GameScene.add( guide );
        return null;    // 没有 Boss
    }

    @Override
    protected String levelName() {
        return "引路人之厅";
    }
}