package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings.RangeKing;

/** 远程王之厅 */
public class SixKingsLevel4 extends SixKingsLevelBase {

    @Override
    protected Mob createBoss() {
        return new RangeKing();
    }

    @Override
    protected String levelName() {
        return "远程王之厅";
    }
}