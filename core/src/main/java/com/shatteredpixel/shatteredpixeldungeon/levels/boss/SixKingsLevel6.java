package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings.OmniKing;

/** 全能王之厅（关底） */
public class SixKingsLevel6 extends SixKingsLevelBase {

    @Override
    protected Mob createBoss() {
        return new OmniKing();
    }

    @Override
    protected String levelName() {
        return "全能王之厅";
    }

	//END(六天王): 本层音乐
	@Override
	public void playLevelMusic() {
		com.watabou.noosa.audio.Music.INSTANCE.play(
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.SIXKINGS_FINAL, true );
	}
}