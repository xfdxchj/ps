package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings.ImmortalKing;

/** 不灭之厅 */
public class SixKingsLevel2 extends SixKingsLevelBase {

    @Override
    protected Mob createBoss() {
        return new ImmortalKing();
    }

    @Override
    protected String levelName() {
        return "不灭之厅";
    }
}