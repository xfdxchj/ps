/* 苍/赫/茈命中后的释放环（专属贴图，放大+淡出）。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

public class JujutsuRing extends Image {

	private static TextureFilm[] films = new TextureFilm[3];
	private int type;
	private float life = 0f;
	private float maxLife = 0.45f;

	public void reset(int cell, int type){
		this.type = type;
		TextureFilm film = filmFor(type);
		frame(film.get(0));
		origin.set(width/2f, height/2f);

		com.watabou.utils.PointF p = DungeonTilemap.tileToWorld(cell);
		x = p.x + DungeonTilemap.SIZE/2f - origin.x;
		y = p.y + DungeonTilemap.SIZE/2f - origin.y;
		life = 0f;
		scale.set(0.6f);
		alpha(1f);
		revive();
	}

	private static TextureFilm filmFor(int type){
		if (films[type] == null){
			String tex = type == JujutsuBolt.CANG ? Assets.Effects.JUJUTSU_RING_BLUE
					: type == JujutsuBolt.HE ? Assets.Effects.JUJUTSU_RING_RED
					: Assets.Effects.JUJUTSU_RING_PURPLE;
			films[type] = new TextureFilm(tex, 56, 56);
		}
		return films[type];
	}

	@Override
	public void update(){
		super.update();
		life += Game.elapsed;
		float p = life / maxLife;
		if (p >= 1f){ killAndErase(); return; }
		scale.set(0.6f + p * 1.8f);
		alpha(1f - p);
		int f = Math.min(5, (int)(p * 6));
		frame(filmFor(type).get(f));
	}
}
