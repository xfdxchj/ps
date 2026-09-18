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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AmuletScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Game;

import java.io.IOException;
import java.util.ArrayList;

public class Amulet extends Item {
	
	private static final String AC_END = "END";
	
	{
		//==== END(挑战 132 黑暗之魂): 护符贴图 → 爱丽丝 ====
		//**在字段初始化时判**而不是运行时改 image：
		//这条规则是"全局外观替换"，玩家一旦勾选就不会中途取消，
		//所以开局定下来即可，没必要每帧判一次。
		image = com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
				.GrimmText.amuletImage(ItemSpriteSheet.AMULET);

		unique = true;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (hero.buff(AscensionChallenge.class) != null){
			actions.clear();
		} else {
			actions.add(AC_END);
		}
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_END)) {
			showAmuletScene( false );
		}
	}
	
	@Override
	public boolean doPickUp(Hero hero, int pos) {
		if (super.doPickUp( hero, pos )) {
			
			if (!Statistics.amuletObtained) {
				Statistics.amuletObtained = true;
				hero.spend(-hero.cooldown());

				//delay with an actor here so pickup behaviour can fully process.
				Actor.add(new Actor(){

					{
						actPriority = VFX_PRIO;
					}

					@Override
					protected boolean act() {
						Actor.remove(this);
						showAmuletScene( true );
						return false;
					}
				});
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	private void showAmuletScene( boolean showText ) {
		AmuletScene.noText = !showText;
		Game.switchScene( AmuletScene.class, new Game.SceneChangeCallback() {
			@Override
			public void beforeCreate() {

			}

			@Override
			public void afterCreate() {
				Badges.validateVictory();
				Badges.validateChampion(Challenges.activeChallenges());
				try {
					Dungeon.saveAll();
					Badges.saveGlobal();
				} catch (IOException e) {
					ShatteredPixelDungeon.reportException(e);
				}
			}
		});
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public String desc() {
		//==== END(挑战 132 黑暗之魂): 护符的说明文本替换 ====
		//勾选 132（且 125~131 全开）时，护符被描述成"那本书"。
		//未勾选时原样走原版逻辑。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
				.GrimmText.darkSoulEnabled()) {
			String d = com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.GrimmText.amulet("desc", Messages.get(this, "desc"));
			if (Dungeon.hero == null
					|| Dungeon.hero.buff(AscensionChallenge.class) == null){
				d += "\n\n" + com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
						.GrimmText.amulet("desc_origins", Messages.get(this, "desc_origins"));
			} else {
				d += "\n\n" + com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
						.GrimmText.amulet("desc_ascent", Messages.get(this, "desc_ascent"));
			}
			return d;
		}

		String desc = super.desc();

		if (Dungeon.hero == null || Dungeon.hero.buff(AscensionChallenge.class) == null){
			desc += "\n\n" + Messages.get(this, "desc_origins");
		} else {
			desc += "\n\n" + Messages.get(this, "desc_ascent");
		}

		return desc;
	}

	//==== END(挑战 132): 名字与贴图也一并替换 ====

	/**
	 * 覆写 {@code name()}：勾选 132 时护符叫「爱丽丝」。
	 *
	 * <p>与 131 的莉耶芙一样 —— 改这一处，日志/图鉴/物品栏全覆盖。
	 */
	@Override
	public String name() {
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
				.GrimmText.darkSoulEnabled()) {
			return "爱丽丝";
		}
		return super.name();
	}
}
