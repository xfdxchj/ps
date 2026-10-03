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

	/** 当前场上正在飞行/停留的苍、赫光球（用于碰撞时让球消失）。 */
	private static final JujutsuBolt[] orbs = new JujutsuBolt[3];

	private int type = CANG;
	private PointF target;
	private Callback onArrive;
	private boolean persist = false;   //到达后停在原地（苍/赫），还是直接消失（茈）
	private boolean landed = false;    //已经停在落点
	private float speed = 0.55f; // 秒走完全程
	private float elapsed = 0f;
	private float life = 0f;           //停留计时（仅兜底，正常由 buff 结束时 dismiss）
	private float breath = 0f;

	public JujutsuBolt(){
		super();
	}

	/** 让指定类型的光球立刻消失（碰撞/被新球替换/场结束时）。 */
	public static void dismiss(int type){
		if (type < 0 || type >= orbs.length || orbs[type] == null) return;
		JujutsuBolt b = orbs[type];
		orbs[type] = null;
		b.onArrive = null;
		b.killAndErase();
	}

	private void unregister(){
		if (type >= 0 && type < orbs.length && orbs[type] == this) orbs[type] = null;
	}

	/** 取消某类型光球的“停留”标记：它会在本次到达后直接消失（用于两球碰撞）。 */
	public static void cancelPersist(int type){
		if (type >= 0 && type < orbs.length && orbs[type] != null){
			orbs[type].persist = false;
		}
	}

	public void reset(int fromCell, int toCell, int type, boolean persist, Callback cb){
		if (type != ZI){
			dismiss(type); //同类型只保留一个球
			orbs[type] = this;
		}
		this.type = type;
		this.persist = persist;
		this.landed = false;
		this.life = 0f;
		this.breath = 0f;
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

		//已停下的球：原地呼吸/转帧，等 buff 结束或碰撞时 dismiss
		if (landed){
			float dt = Game.elapsed;
			breath += dt;
			life -= dt;
			alpha(0.70f + 0.30f * (float)Math.sin(breath * 4));
			int f = (int)(breath * 10) % 6;
			frame(filmFor(type).get(f));
			if (life <= 0f){ unregister(); killAndErase(); }
			return;
		}

		if (onArrive == null) { unregister(); killAndErase(); return; }

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
			Callback cb = onArrive;
			onArrive = null;
			if (cb != null) cb.call();
			if (persist){
				//停在落点，成为持续的苍/赫场（视觉球就是它本身）
				landed = true;
				life = 300f; //兜底；正常由 Field.detach() 调 dismiss()
				alpha(0.95f);
				scale.set(1.1f);
			} else {
				unregister();
				killAndErase();
			}
		}
	}
}
