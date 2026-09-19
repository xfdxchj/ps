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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HealthBar;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.ui.Component;

public class WndInfoMob extends WndTitledMessage {
	
	public WndInfoMob( Mob mob ) {

		super( new MobTitle( mob ), mob.info() );
		
	}
	
	private static class MobTitle extends Component {

		private static final int GAP	= 2;
		
		private CharSprite image;
		private RenderedTextBlock name;
		private HealthBar health;
		private BuffIndicator buffs;
		
		public MobTitle( Mob mob ) {
			
			name = PixelScene.renderTextBlock( Messages.titleCase( mob.name() ), 9 );
			name.hardlight( TITLE_COLOR );
			add( name );
			
			//==== END(修复·挑战 63/138): 窗口里的贴图要与场上一致 ====
			//原先这里是 {@code image = mob.sprite();} —— 直接新建一个实例。
			//
			//问题：{@code Mob.sprite()} 会走"随机换贴图"的流程并**挂 buff**，
			//而且新建的实例从未 link() 过（没有 place()、没有 updateSpriteState()），
			//表现出来就是：场上已经是小鼠了，点开窗口看到的却还是原怪物。
			//
			//修法（END 修订）：不用 {@code mob.sprite()}（有副作用），
			//改用**只读**的 {@code spriteClassFor()} 自己 new ——
			//既拿到与场上一致的贴图，又不会打断正在进行的渲染
			//（直接调 mob.sprite() 实测会导致 CharHealthIndicator NPE 闪退）。
			CharSprite imageSprite = null;
			Class<? extends CharSprite> want = com.shatteredpixel.shatteredpixeldungeon
					.endcontent.challenge.ChallengeEffects.spriteClassFor(mob);
			if (want != null) {
				imageSprite = com.watabou.utils.Reflection.newInstance(want);
			} else {
				imageSprite = mob.sprite();
			}
			image = imageSprite;
			add( image );

			health = new HealthBar();
			health.level(mob);
			add( health );

			buffs = new BuffIndicator( mob, false );
			add( buffs );
		}
		
		@Override
		protected void layout() {
			
			image.x = 0;
			image.y = Math.max( 0, name.height() + health.height() - image.height() );

			float w = width - image.width() - GAP;

			name.setPos(x + image.width() + GAP,
					image.height() > name.height() ? y +(image.height() - name.height()) / 2 : y);

			health.setRect(image.width() + GAP, name.bottom() + GAP, w, health.height());

			buffs.maxBuffs = 50; //infinite, effectively
			buffs.setRect(name.right(), name.bottom() - BuffIndicator.SIZE_SMALL-2, w - name.width(), 8);

			//If buff bar doesn't have enough room, move it below
			if (!buffs.allBuffsVisible()){
				buffs.setRect(0, health.bottom(), width, 8);
				height = Math.max(image.y + image.height(), buffs.bottom());
			} else {
				height = Math.max(image.y + image.height(), health.bottom());
			}
		}
	}
}
