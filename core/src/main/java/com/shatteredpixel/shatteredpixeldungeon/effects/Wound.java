/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;

public class Wound extends Image {

	private static final float TIME_TO_FADE = 1f;
	
	private float time;
	private float lifespan = TIME_TO_FADE;
	
	public Wound() {
		super( Effects.get( Effects.Type.WOUND ) );
		hardlight(1f, 0f, 0f );
		origin.set( width / 2, height / 2 );
	}
	
	public void reset( int p ) {
		reset( p, TIME_TO_FADE );
	}

	/** END(宿傩领域): 允许自定义淡出时长，让斩击消失更快。 */
	public void reset( int p, float lifespan ) {
		revive();

		x = (p % Dungeon.level.width()) * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - width) / 2;
		y = (p / Dungeon.level.width()) * DungeonTilemap.SIZE + (DungeonTilemap.SIZE - height) / 2;
		
		this.lifespan = Math.max(0.05f, lifespan);
		time = this.lifespan;
	}

	public void reset(Visual v) {
		reset( v, TIME_TO_FADE );
	}

	/** END(宿傩领域): 允许自定义淡出时长。 */
	public void reset(Visual v, float lifespan) {
		revive();

		point(v.center(this));

		this.lifespan = Math.max(0.05f, lifespan);
		time = this.lifespan;
	}
	
	@Override
	public void update() {
		super.update();
		
		if ((time -= Game.elapsed) <= 0) {
			kill();
		} else {
			float p = time / lifespan;
			alpha((float) Math.sqrt(p));
			scale.x = 1 + p;
		}
	}
	
	public static void hit( Char ch ) {
		hit( ch, 0 );
	}
	
	public static void hit( Char ch, float angle ) {
		if (ch.sprite.parent != null) {
			Wound w = (Wound) ch.sprite.parent.recycle(Wound.class);
			ch.sprite.parent.bringToFront(w);
			w.reset(ch.sprite);
			w.angle = angle;
		}
	}
	
	public static void hit( int pos ) {
		hit( pos, 0 );
	}
	
	public static void hit( int pos, float angle ) {
		hit( pos, angle, TIME_TO_FADE );
	}

	/** END(宿傩领域): 在指定格生成一道可自定义淡出时长的斩痕。 */
	public static void hit( int pos, float angle, float lifespan ) {
		if (Dungeon.level == null || Dungeon.hero == null || Dungeon.hero.sprite == null
				|| Dungeon.hero.sprite.parent == null) return;
		Group parent = Dungeon.hero.sprite.parent;
		Wound w = (Wound)parent.recycle( Wound.class );
		parent.bringToFront( w );
		w.reset( pos, lifespan );
		w.angle = angle;
	}
}
