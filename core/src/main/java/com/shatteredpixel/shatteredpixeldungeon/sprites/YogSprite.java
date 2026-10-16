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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.watabou.noosa.TextureFilm;

public class YogSprite extends MobSprite {
	
	public YogSprite() {
		super();

		perspectiveRaise = 5 / 16f;

		//==== END(修复·没开 131 古神也变成莉耶芙) ====
		//原来直接把 assets/sprites/yog.png 替换成了莉耶芙贴图，
		//YogSprite 又无条件加载它 —— 于是没勾选挑战 131 也显示莉耶芙。
		//改为两张图分开：原版 yog.png + grimm_yog.png，按挑战 131 切换。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
				.GrimmText.grimmEnemyEnabled()) {

			//---- 莉耶芙（150x42，单行 3 帧，每帧 50x42）----
			texture( Assets.Sprites.GRIMM_YOG );

			TextureFilm frames = new TextureFilm( texture, 50, 42 );

			idle = new Animation( 10, true );
			idle.frames( frames, 0, 0, 0, 1, 1, 1, 2, 2, 2 );

			run = new Animation( 12, true );
			run.frames( frames, 0 );

			attack = new Animation( 12, false );
			attack.frames( frames, 0 );

			die = new Animation( 10, false );
			die.frames( frames, 2, 1, 0 );

		} else {

			//---- 原版古神 ----
			texture( Assets.Sprites.YOG );

			TextureFilm frames = new TextureFilm( texture, 20, 19 );

			idle = new Animation( 10, true );
			idle.frames( frames, 0, 1, 2, 2, 1, 0, 3, 4, 4, 3, 0, 5, 6, 6, 5 );

			run = new Animation( 12, true );
			run.frames( frames, 0 );

			attack = new Animation( 12, false );
			attack.frames( frames, 0 );

			die = new Animation( 10, false );
			die.frames( frames, 0, 7, 8, 9 );

		}

		play( idle );
	}

	@Override
	public void link(Char ch) {
		super.link(ch);
		renderShadow = false;
	}

	@Override
	public void die() {
		super.die();
		
		Splash.at( center(), blood(), 12 );
	}
}
