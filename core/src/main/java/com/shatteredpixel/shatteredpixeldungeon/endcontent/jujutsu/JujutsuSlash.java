/* 空间斩的刀光（白色弧光掠过）。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

public class JujutsuSlash extends Image {

	private static TextureFilm film;
	private float life;
	private static final float MAX = 0.45f;

	public void reset(int cell, float angleDeg){
		if (film == null) film = new TextureFilm(Assets.Effects.JUJUTSU_SLASH_BLADE, 128, 64);
		frame(film.get(0));
		origin.set(width/2f, height/2f);
		com.watabou.utils.PointF p = DungeonTilemap.tileToWorld(cell);
		x = p.x + DungeonTilemap.SIZE/2f - origin.x;
		y = p.y + DungeonTilemap.SIZE/2f - origin.y;
		this.angle = angleDeg;
		life = 0f; alpha(1f); scale.set(0.7f); revive();
	}

	@Override
	public void update(){
		super.update();
		life += Game.elapsed;
		float p = life/MAX;
		if (p >= 1f){ killAndErase(); return; }
		scale.set(0.7f + p*0.7f);
		alpha(1f - p*p);
		frame(film.get(Math.min(5, (int)(p*6))));
	}
}
