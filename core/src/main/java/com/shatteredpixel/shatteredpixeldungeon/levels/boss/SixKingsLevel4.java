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

	//END(六天王): 本层音乐
	@Override
	public void playLevelMusic() {
		com.watabou.noosa.audio.Music.INSTANCE.play(
				com.shatteredpixel.shatteredpixeldungeon.Assets.Music.SIXKINGS_4, true );
	}
}