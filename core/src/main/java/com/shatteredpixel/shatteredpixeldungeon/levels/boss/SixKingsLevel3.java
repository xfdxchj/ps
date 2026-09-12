package com.shatteredpixel.shatteredpixeldungeon.levels.boss;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings.DebuffKing;

/** 疫病之厅 */
public class SixKingsLevel3 extends SixKingsLevelBase {

    @Override
    protected Mob createBoss() {
        return new DebuffKing();
    }

    @Override
    protected String levelName() {
        return "疫病之厅";
    }

	//END(六天王): 本层音乐
	@Override
	public void playLevelMusic() {
		com.watabou.noosa.audio.Music.INSTANCE.play(
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.SIXKINGS_3, true );
	}
}