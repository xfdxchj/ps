/* 茈命中的白紫大爆炸。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

public class JujutsuBurst extends Image {

	private static TextureFilm film;
	private float life = 0f;
	private static final float MAX = 0.55f;

	public void reset(int cell){
		if (film == null) film = new TextureFilm(Assets.Effects.JUJUTSU_BURST, 96, 96);
		frame(film.get(0));
		origin.set(width/2f, height/2f);
		com.watabou.utils.PointF p = DungeonTilemap.tileToWorld(cell);
		x = p.x + DungeonTilemap.SIZE/2f - origin.x;
		y = p.y + DungeonTilemap.SIZE/2f - origin.y;
		life = 0f; scale.set(0.3f); alpha(1f); revive();
	}

	@Override
	public void update(){
		super.update();
		life += Game.elapsed;
		float p = life / MAX;
		if (p >= 1f){ killAndErase(); return; }
		scale.set(0.3f + p * 1.6f);
		alpha(1f - p * p);
		frame(film.get(Math.min(5, (int)(p * 6))));
	}
}
