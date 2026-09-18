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

		texture( Assets.Sprites.YOG );
		
		//==== END(古神贴图替换): 帧尺寸 20x19 -> 50x42 ====
		//文档所有者提供了新的古神贴图（音乐/莉耶夫.png），
		//源图 150x168，3 列 x 4 行，每帧 50x42。
		//
		//**不缩放**，直接把游戏帧尺寸改成 50x42 —— 缩放会让瞳孔等细节全丢。
		//代价：古神在屏幕上会变大 2.5 倍（原来 20x19）。
		//
		//贴图只取**第一行的 3 帧**（列 0/1/2），排成 150x42 的单行图。
		//行 1-3 未使用。
		//
		//帧映射由文档所有者指定：0,0,0,1,1,1,2,2,2
		//（每帧重复 3 次，形成缓慢的呼吸/凝视节奏）
		TextureFilm frames = new TextureFilm( texture, 50, 42 );
		
		idle = new Animation( 10, true );
		idle.frames( frames, 0, 0, 0, 1, 1, 1, 2, 2, 2 );
		
		run = new Animation( 12, true );
		run.frames( frames, 0 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 0 );
		
		//==== END: 死亡没有专属贴图 ====
		//文档所有者说明"死亡没有贴图"，所以复用已有的 3 帧做收尾，
		//而不是指向不存在的帧号（那会显示 nofound 或越界取到别的花纹）。
		die = new Animation( 10, false );
		die.frames( frames, 2, 1, 0 );
		
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
