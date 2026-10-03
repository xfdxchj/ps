/* 苍/赫 落点的持续可视化场（5 回合）。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;

public class JujutsuFieldFx extends Image {

	private static JujutsuFieldFx cangFx, heFx;

	public static void show(int cell, int type){
		hide(type);
		JujutsuFieldFx fx = new JujutsuFieldFx();
		fx.setup(cell, type);
		GameScene.effect(fx);
		if (type == 0) cangFx = fx; else heFx = fx;
	}

	public static void hide(int type){
		if (type == 0 && cangFx != null){ cangFx.killAndErase(); cangFx = null; }
		if (type == 1 && heFx != null){ heFx.killAndErase(); heFx = null; }
	}

	private int type;
	private float time = 0f;
	private static TextureFilm cangFilm, heFilm;

	private void setup(int cell, int type2){
		this.type = type2;
		String tex = type2 == 0 ? Assets.Effects.JUJUTSU_CANG : Assets.Effects.JUJUTSU_HE;
		TextureFilm film = type2 == 0 ? cangFilm : heFilm;
		if (film == null){
			film = new TextureFilm(tex, 52, 52);
			if (type2 == 0) cangFilm = film; else heFilm = film;
		}
		texture(tex);
		frame(film.get(0));
		origin.set(width/2f, height/2f);
		com.watabou.utils.PointF p = DungeonTilemap.tileToWorld(cell);
		x = p.x + DungeonTilemap.SIZE/2f - origin.x;
		y = p.y + DungeonTilemap.SIZE/2f - origin.y;
		scale.set(1.1f);
	}

	@Override
	public void update(){
		super.update();
		time += Game.elapsed;
		alpha(0.75f + 0.25f * (float)Math.sin(time * 4));
		int f = (int)(time * 10) % 6;
		TextureFilm film = type == 0 ? cangFilm : heFilm;
		if (film != null) frame(film.get(f));
	}
}
