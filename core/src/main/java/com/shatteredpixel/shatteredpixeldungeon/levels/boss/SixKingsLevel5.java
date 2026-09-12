package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings.GuidingKing;

/** 召唤王之厅 */
public class SixKingsLevel5 extends SixKingsLevelBase {

    @Override
    protected Mob createBoss() {
        return new GuidingKing();
    }

    @Override
    protected String levelName() {
        return "召唤王之厅";
    }
}