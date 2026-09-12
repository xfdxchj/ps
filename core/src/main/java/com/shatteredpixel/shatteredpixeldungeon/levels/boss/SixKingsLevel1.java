package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings.SpellKing;

/** 法术王之厅 */
public class SixKingsLevel1 extends SixKingsLevelBase {

    @Override
    protected Mob createBoss() {
        return new SpellKing();
    }

    @Override
    protected String levelName() {
        return "法术王之厅";
    }
}