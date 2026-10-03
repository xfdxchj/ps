/* 空间斩的黑色裂缝。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

public class JujutsuRift extends Image {

	private static TextureFilm film;
	private float life = 0f;
	private static final float MAX = 0.6f;

	public void reset(int cell){
		texture(Assets.Effects.JUJUTSU_RIFT);
		if (film == null) film = new TextureFilm(Assets.Effects.JUJUTSU_RIFT, 64, 64);
		frame(film.get(0));
		origin.set(width/2f, height/2f);
		com.watabou.utils.PointF p = DungeonTilemap.tileToWorld(cell);
		x = p.x + DungeonTilemap.SIZE/2f - origin.x;
		y = p.y + DungeonTilemap.SIZE/2f - origin.y;
		life = 0f; scale.set(0.6f); alpha(1f); revive();
	}

	@Override
	public void update(){
		super.update();
		life += Game.elapsed;
		float p = life / MAX;
		if (p >= 1f){ killAndErase(); return; }
		scale.set(0.6f + p * 0.8f);
		alpha(1f - p * p);
		frame(film.get(Math.min(3, (int)(p * 4))));
	}
}
