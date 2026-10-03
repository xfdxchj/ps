/* 灶开的火焰箭（蓝白箭身，飞向目标后爆开）。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class JujutsuArrow extends Image {

	private static TextureFilm film;
	private PointF from, target;
	private Callback onArrive;
	private float elapsed;
	private float dur = 0.35f;

	public void reset(int fromCell, int toCell, Callback cb){
		if (film == null) film = new TextureFilm(Assets.Effects.JUJUTSU_FUGA_ARROW, 64, 24);
		frame(film.get(0));
		origin.set(width/2f, height/2f);
		PointF a = DungeonTilemap.tileToWorld(fromCell);
		PointF b = DungeonTilemap.tileToWorld(toCell);
		a.x += DungeonTilemap.SIZE/2f; a.y += DungeonTilemap.SIZE/2f;
		b.x += DungeonTilemap.SIZE/2f; b.y += DungeonTilemap.SIZE/2f;
		from = a; target = b; onArrive = cb; elapsed = 0f;
		x = a.x - origin.x; y = a.y - origin.y;
		angle = (float)(Math.atan2(b.y-a.y, b.x-a.x) * 180 / Math.PI);
		alpha(1f); scale.set(1f); revive();
	}

	@Override
	public void update(){
		super.update();
		elapsed += Game.elapsed;
		float t = Math.min(1f, elapsed/dur);
		x = from.x + (target.x-from.x)*t - origin.x;
		y = from.y + (target.y-from.y)*t - origin.y;
		frame(film.get(Math.min(3, (int)(t*4))));
		if (t >= 1f){
			if (onArrive != null) onArrive.call();
			killAndErase();
		}
	}
}
