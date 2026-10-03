/* 苍/赫/茈的飞行光球（专属贴图，到达位置后触发释放回调）。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class JujutsuBolt extends Image {

	public static final int CANG = 0;
	public static final int HE   = 1;
	public static final int ZI   = 2;

	private static TextureFilm[] films = new TextureFilm[3];

	private int type = CANG;
	private PointF target;
	private Callback onArrive;
	private float speed = 0.55f; // 秒走完全程
	private float elapsed = 0f;

	public JujutsuBolt(){
		super();
	}

	public void reset(int fromCell, int toCell, int type, Callback cb){
		this.type = type;
		texture(texFor(type));
		TextureFilm film = filmFor(type);
		frame(film.get(0));
		origin.set(width/2f, height/2f);

		PointF a = DungeonTilemap.tileToWorld(fromCell);
		PointF b = DungeonTilemap.tileToWorld(toCell);
		a.x += DungeonTilemap.SIZE/2f; a.y += DungeonTilemap.SIZE/2f;
		b.x += DungeonTilemap.SIZE/2f; b.y += DungeonTilemap.SIZE/2f;
		x = a.x - origin.x; y = a.y - origin.y;
		target = b;
		onArrive = cb;
		elapsed = 0f;
		alpha(1f);
		scale.set(1f);
		revive();
	}

	private static String texFor(int type){
		return type == CANG ? Assets.Effects.JUJUTSU_CANG
				: type == HE ? Assets.Effects.JUJUTSU_HE : Assets.Effects.JUJUTSU_ZI;
	}

	private static TextureFilm filmFor(int type){
		if (films[type] == null){
			films[type] = new TextureFilm(texFor(type), 52, 52);
		}
		return films[type];
	}

	@Override
	public void update(){
		super.update();
		if (onArrive == null) { killAndErase(); return; }

		float dt = Game.elapsed;
		elapsed += dt;
		float t = Math.min(1f, elapsed / speed);

		// 起点在 reset 时没存，用当前位置朝目标插值
		PointF cur = new PointF(x + origin.x, y + origin.y);
		float d = Math.max(0.001f, PointF.distance(cur, target));
		float step = d * Math.min(1f, dt / Math.max(0.01f, (speed * (1f - t) + 0.05f)));
		x += (target.x - cur.x) / d * step;
		y += (target.y - cur.y) / d * step;

		// 帧动画
		int f = Math.min(5, (int)(t * 6));
		frame(filmFor(type).get(f));
		scale.set(0.8f + t * 0.6f);

		if (PointF.distance(new PointF(x + origin.x, y + origin.y), target) < 6f || t >= 1f){
			if (onArrive != null) onArrive.call();
			onArrive = null;
			killAndErase();
		}
	}
}
