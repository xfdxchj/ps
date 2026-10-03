/* 空间斩的刀光：从宿傩飞向目标的弧形光刃（不是原地挥）。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;

public class JujutsuSlash extends Image {

	private static TextureFilm film;
	private PointF from, target;
	private Callback onArrive;
	private float elapsed;
	private float dur = 0.28f;

	public void reset(int fromCell, int toCell, Callback cb){
		texture(Assets.Effects.JUJUTSU_SLASH_BLADE);
		if (film == null) film = new TextureFilm(Assets.Effects.JUJUTSU_SLASH_BLADE, 128, 64);
		frame(film.get(0));
		origin.set(width/2f, height/2f);
		PointF a = DungeonTilemap.tileToWorld(fromCell);
		PointF b = DungeonTilemap.tileToWorld(toCell);
		a.x += DungeonTilemap.SIZE/2f; a.y += DungeonTilemap.SIZE/2f;
		b.x += DungeonTilemap.SIZE/2f; b.y += DungeonTilemap.SIZE/2f;
		from = a; target = b; onArrive = cb; elapsed = 0f;
		float travel = (float)(Math.atan2(b.y-a.y, b.x-a.x) * 180 / Math.PI);
		// 弧形刃口朝飞行方向
		this.angle = travel - 90f;
		//END(修复·空间斩贴图上下翻转): 素材本身上下颠倒，渲染时翻回来
		this.flipVertical = true;
		x = a.x - origin.x; y = a.y - origin.y;
		alpha(1f); scale.set(1f); revive();
	}

	@Override
	public void update(){
		super.update();
		elapsed += Game.elapsed;
		float t = Math.min(1f, elapsed/dur);
		x = from.x + (target.x-from.x)*t - origin.x;
		y = from.y + (target.y-from.y)*t - origin.y;
		frame(film.get(Math.min(5, (int)(t*6))));
		if (t >= 1f){
			if (onArrive != null) onArrive.call();
			killAndErase();
		}
	}
}
