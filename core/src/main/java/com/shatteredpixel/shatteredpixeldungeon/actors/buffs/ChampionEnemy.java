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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bat;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public abstract class ChampionEnemy extends Buff {

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	protected int color;
	protected int rays;

	@Override
	public int icon() {
		return BuffIndicator.CORRUPT;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(color);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.aura( color, rays );
		else target.sprite.clearAura();
	}

	public void onAttackProc(Char enemy ){

	}

	public boolean canAttackWithExtraReach( Char enemy ){
		return false;
	}

	public float meleeDamageFactor(){
		return 1f;
	}

	public float damageTakenFactor(){
		return 1f;
	}

	public float evasionAndAccuracyFactor(){
		return 1f;
	}

	{
		immunities.add(AllyBuff.class);
	}

	public static void rollForChampion(Mob m){
		Dungeon.mobsToChampion--;

		//we roll for a champion enemy even if we aren't spawning one to ensure that
		//mobsToChampion does not affect levelgen RNG (number of calls to Random.Int() is constant)
		Class<?extends ChampionEnemy> buffCls;
		switch (Random.Int(6)){
			case 0: default:    buffCls = Blazing.class;      break;
			case 1:             buffCls = Projecting.class;   break;
			case 2:             buffCls = AntiMagic.class;    break;
			case 3:             buffCls = Giant.class;        break;
			case 4:             buffCls = Blessed.class;      break;
			case 5:             buffCls = Growing.class;      break;
		}

		//==== END(精英体系): 精英由「精英类规则」开启 ====
		//原版条件是 `mobsToChampion <= 0 && isChallenged(CHAMPION_ENEMIES)`。
	//本 fork 让精英类规则（14/34/64/75/4 任意一条）自行开启精英，
		//这样"勾了 14 却没勾 116 就完全无效"的死组合不再出现。
		//注意 116 精英强敌**保持原版行为**（由它自身触发精英）。
		if (Dungeon.mobsToChampion <= 0
				&& (Dungeon.isChallenged(Challenges.CHAMPION_ENEMIES)
					|| com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.shouldRollChampion())) {

			//we block certain standout enemies on floor <10 from becoming champions
			//
			//==== END(挑战 4 精英迁徙): 取消"楼层限制" ====
			//原表："精英怪可出现在原本不属于自己的区域"。
			//
			//本 fork 里"精英怪只属于自己区域"正是靠下面这四行实现的：
			//螃蟹/盗贼/守卫/蝙蝠这些**区域代表怪**在过早的楼层被禁止精英化。
			//（例如蝙蝠是 3 区怪，在 1-9 层出现时不给精英，
			//  以免玩家在前期就撞上"精英蝙蝠"这种不属于该区域的组合。）
			//
			//勾选 4 后取消这层限制 —— 任何怪在任何楼层都可能成为精英，
			//即"精英迁徙"。
			boolean migration = com.shatteredpixel.shatteredpixeldungeon.endcontent
					.challenge.ChallengeEffects.eliteMigrationEnabled();
			if (!migration) {
				if (m instanceof Crab  && Dungeon.scalingDepth() <= 3) return;
				if (m instanceof Thief && Dungeon.scalingDepth() <= 4) return;
				if (m instanceof Guard && Dungeon.scalingDepth() <= 7) return;
				if (m instanceof Bat   && Dungeon.scalingDepth() <= 9) return;
			}

			//END(挑战 14): 加血前先确认"本次确实是新精英化"。
			//Buff.affect 对已有该 buff 的目标会**直接返回旧的、不做任何事**，
			//若不判断就会在重复调用时给同一只怪叠加多次血量。
			boolean newlyChampion = (m.buff(buffCls) == null);

			Buff.affect(m, buffCls);

			//==== END(挑战 14 精英强化): 精英怪生命上限 ×1.2 ====
			//在这里加血是因为这是"怪物刚变成精英"的**唯一时刻** ——
			//rollForChampion 只在生成/召唤时调用，之后不会再走。
			//当前血量同步提升，否则精英会以"残血"状态登场。
			if (newlyChampion) {
				float eliteHp = com.shatteredpixel.shatteredpixeldungeon.endcontent
						.challenge.ChallengeEffects.eliteStatMultiplier();
				if (eliteHp != 1f) {
					int newHT = Math.max(1, Math.round(m.HT * eliteHp));
					int gained = newHT - m.HT;
					m.HT = newHT;
					m.HP = Math.min(newHT, m.HP + Math.max(0, gained));
				}
			}

			//numbers of mobs until a champion scales from 1/8 to 1/6 as depths increases
			//==== END(移植·15/17): 精英间隔改由 ChallengeEffects 统一给 ====
			float interval = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.championInterval(Dungeon.scalingDepth());

			//==== END(精英体系): 未勾「精英强敌」时，精英概率减半 ====
			//减半出现率 = 把"距下一只精英的间隔"翻倍。
			//为什么这样设计：116 精英强敌是"精英很多"的那一档；
			//只勾 14/34/64 这类规则时精英仍会出现，但要稀疏得多，
			//让 116 保持"专门刷精英"的定位。
			if (!Dungeon.isChallenged(Challenges.CHAMPION_ENEMIES)) {
				interval *= 2f;
			}

			//==== END(移植·15/17): 高阶精英 —— 额外再挂词条 ====
			int extraWords = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.championExtraWords(m);
			for (int i = 0; i < extraWords; i++){
				try {
					Buff.affect(m, com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.randomHighTierChampionClass());
				} catch (Throwable ignored) { }
			}

			Dungeon.mobsToChampion += interval;
			if (m.state != m.PASSIVE) {
				m.state = m.WANDERING;
			}
		}
	}

	public static class Blazing extends ChampionEnemy {

		{
			color = 0xFF8800;
			rays = 4;
		}

		@Override
		public void onAttackProc(Char enemy) {
			if (!Dungeon.level.water[enemy.pos]) {
				Buff.affect(enemy, Burning.class).reignite(enemy);
			}
		}

		@Override
		public void detach() {
			//don't trigger when killed by being knocked into a pit
			if (target.flying || !Dungeon.level.pit[target.pos]) {
				for (int i : PathFinder.NEIGHBOURS9) {
					if (!Dungeon.level.solid[target.pos + i] && !Dungeon.level.water[target.pos + i]) {
						GameScene.add(Blob.seed(target.pos + i, 2, Fire.class));
					}
				}
			}
			super.detach();
		}

		@Override
		public float meleeDamageFactor() {
			return 1.25f;
		}

		{
			immunities.add(Burning.class);
		}
	}

	public static class Projecting extends ChampionEnemy {

		{
			color = 0x8800FF;
			rays = 4;
		}

		@Override
		public float meleeDamageFactor() {
			return 1.25f;
		}

		@Override
		public boolean canAttackWithExtraReach(Char enemy) {
			if (Dungeon.level.distance( target.pos, enemy.pos ) > 4){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Char ch : Actor.chars()) {
					//our own tile is always passable
					passable[ch.pos] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.pos, passable, 4);

				return PathFinder.distance[target.pos] <= 4;
			}
		}
	}

	public static class AntiMagic extends ChampionEnemy {

		{
			color = 0x00FF00;
			rays = 5;
		}

		@Override
		public float damageTakenFactor() {
			return 0.5f;
		}

		{
			immunities.addAll(com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic.RESISTS);
		}

	}

	//Also makes target large, see Char.properties()
	public static class Giant extends ChampionEnemy {

		{
			color = 0x0088FF;
			rays = 5;
		}

		@Override
		public float damageTakenFactor() {
			return 0.2f;
		}

		@Override
		public boolean canAttackWithExtraReach(Char enemy) {
			if (Dungeon.level.distance( target.pos, enemy.pos ) > 2){
				return false;
			} else {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				for (Char ch : Actor.chars()) {
					//our own tile is always passable
					passable[ch.pos] = ch == target;
				}

				PathFinder.buildDistanceMap(enemy.pos, passable, 2);

				return PathFinder.distance[target.pos] <= 2;
			}
		}
	}

	public static class Blessed extends ChampionEnemy {

		{
			color = 0xFFFF00;
			rays = 6;
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return 4f;
		}
	}

	public static class Growing extends ChampionEnemy {

		{
			color = 0xFF2222; //a little white helps it stick out from background
			rays = 6;
		}

		private float multiplier = 1.19f;

		@Override
		public boolean act() {
			multiplier += 0.01f;
			spend(4*TICK);
			return true;
		}

		@Override
		public float meleeDamageFactor() {
			return multiplier;
		}

		@Override
		public float damageTakenFactor() {
			return 1f/multiplier;
		}

		@Override
		public float evasionAndAccuracyFactor() {
			return multiplier;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", (int)(100*(multiplier-1)), (int)(100*(1 - 1f/multiplier)));
		}

		private static final String MULTIPLIER = "multiplier";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(MULTIPLIER, multiplier);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			multiplier = bundle.getFloat(MULTIPLIER);
		}
	}

}
