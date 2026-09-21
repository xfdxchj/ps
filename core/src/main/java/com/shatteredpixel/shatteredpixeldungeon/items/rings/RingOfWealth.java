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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDivineInspiration;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ExoticCrystals;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.Visual;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class RingOfWealth extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_WEALTH;
		buffClass = Wealth.class;
	}

	private float triesToDrop = Float.MIN_VALUE;
	private int dropsToRare = Integer.MIN_VALUE;
	
	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					Messages.decimalFormat("#.##", 100f * (Math.pow(1.20f, soloBuffedBonus()) - 1f)));
			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						Messages.decimalFormat("#.##", 100f * (Math.pow(1.20f, combinedBuffedBonus(Dungeon.hero)) - 1f)));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", Messages.decimalFormat("#.##", 20f));
		}
	}

	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return Messages.decimalFormat("#.##", 100f * (Math.pow(1.2f, level+1)-1f)) + "%";
	}

	private static final String TRIES_TO_DROP = "tries_to_drop";
	private static final String DROPS_TO_RARE = "drops_to_rare";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TRIES_TO_DROP, triesToDrop);
		bundle.put(DROPS_TO_RARE, dropsToRare);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		triesToDrop = bundle.getFloat(TRIES_TO_DROP);
		dropsToRare = bundle.getInt(DROPS_TO_RARE);
	}

	@Override
	protected RingBuff buff( ) {
		return new Wealth();
	}
	
	public static float dropChanceMultiplier( Char target ){
		float base = (float)Math.pow(1.20, getBuffedBonus(target, Wealth.class));

		//==== END(挑战 198 幸运药水): 财富戒效果 +50% ====
		//文档所有者定稿："提升财富戒/幸运附魔 50% 效果，50 回合。"
		//
		//注意乘的是**额外部分**（base - 1），不是整个 base：
		//否则没有财富戒时（base = 1）会凭空变成 1.5 倍掉落 —— 那就不是
		//"提升财富戒效果"而是"白送幸运"了。
		if (target != null && target.buff(com.shatteredpixel.shatteredpixeldungeon
				.actors.buffs.LuckyPotionBuff.class) != null) {
			base = 1f + (base - 1f) * com.shatteredpixel.shatteredpixeldungeon.actors
					.buffs.LuckyPotionBuff.MULT;
		}
		return base;
	}
	
	public static ArrayList<Item> tryForBonusDrop(Char target, int tries ){
		int bonus = getBuffedBonus(target, Wealth.class);

		if (bonus <= 0) return null;

		CounterBuff triesToDrop = target.buff(TriesToDropTracker.class);
		if (triesToDrop == null){
			triesToDrop = Buff.affect(target, TriesToDropTracker.class);
			triesToDrop.countUp( Random.NormalIntRange(0, 20) );
		}

		CounterBuff dropsToEquip = target.buff(DropsToEquipTracker.class);
		if (dropsToEquip == null){
			dropsToEquip = Buff.affect(target, DropsToEquipTracker.class);
			dropsToEquip.countUp( Random.NormalIntRange(5, 10) );
		}

		//now handle reward logic
		ArrayList<Item> drops = new ArrayList<>();

		triesToDrop.countDown(tries);
		while ( triesToDrop.count() <= 0 ){
			if (dropsToEquip.count() <= 0){
				int equipBonus = 0;

				//A second ring of wealth can be at most +1 when calculating wealth bonus for equips
				//This is to prevent using an upgraded wealth to farm another upgraded wealth and
				//using the two to get substantially more upgrade value than intended
				for (Wealth w : target.buffs(Wealth.class)){
					if (w.buffedLvl() > equipBonus){
						equipBonus = w.buffedLvl() + Math.min(equipBonus, 2);
					} else {
						equipBonus += Math.min(w.buffedLvl(), 2);
					}
				}

				Item i;
				do {
					i = genEquipmentDrop(equipBonus - 1);
				} while (Challenges.isItemBlocked(i));
				drops.add(i);
				dropsToEquip.countUp(Random.NormalIntRange(5, 10));
			} else {
				Item i;
				do {
					i = genConsumableDrop(bonus - 1);
				} while (Challenges.isItemBlocked(i));
				drops.add(i);
				dropsToEquip.countDown(1);
			}
			//==== END(新增·幸运度溢出转额外掉落) ====
			//文档所有者问："财富戒指的幸运度到达 1000 多以后会提升什么？"
			//
			//原实现下答案是"什么都不提升" ——
			//幸运度提升的是 `dropChanceMultiplier`（怪物掉率），
			//而那个值超过约 1000% 之后掉率已接近必掉，再加就是浪费。
			//（财富戒 +13 级的幸运度就是 1.20^13 - 1 ≈ 1000%。）
			//
			//现在把**溢出的部分**转成"这一次多掉一件"：
			//  幸运度 ≤ 1000%  → 不额外掉（原版行为）
			//  每超 500%       → 多掉 1 件
			//
			//为什么用"每 500% 一件"而不是线性：
			//幸运度是按 1.20^等级 指数增长的，+13→1000%、+18→2500%、+23→6200%，
			//若按 100% 一件，后期一次击杀会掉几十件 —— 背包瞬间爆满。
			//500% 一档意味着 +13 时 0 件、+18 时 3 件、+23 时 10 件，节奏合理。
			//
			//实现方式：额外多补几次 triesToDrop —— 补满之后立刻又能触发下一件。
			int extra = luckyOverflowDrops(target);
			for (int e = 0; e < extra; e++) {
				triesToDrop.countUp( Random.NormalIntRange(0, 20) );
			}
		}
		
		return drops;
	}

	/** END(新增): 幸运度超过 1000% 后，每 500% 折算一次额外掉落。 */
	public static final float LUCKY_BASE_PCT = 1000f;
	public static final float LUCKY_PER_EXTRA = 500f;

	/**
	 * END(新增·幸运度溢出): 本次应额外掉几件。
	 *
	 * <p>幸运度 = {@code 1.20^财富等级 - 1}（百分比）。
	 * 超过 {@link #LUCKY_BASE_PCT} 的部分，每 {@link #LUCKY_PER_EXTRA} 换一件。
	 *
	 * @param target 戴着财富戒的角色
	 * @return 额外件数；幸运度不足 1000% 时返回 0
	 */
	public static int luckyOverflowDrops(Char target) {
		if (target == null) return 0;

		float mult = dropChanceMultiplier(target);      // = 1.20^等级
		float pct = 100f * (mult - 1f);                 // 幸运度（百分比）

		if (pct <= LUCKY_BASE_PCT) return 0;

		int extra = (int) ((pct - LUCKY_BASE_PCT) / LUCKY_PER_EXTRA);

		//上限 10 件 —— 再多会让一次击杀刷出几十件，背包与性能都受不了
		return Math.min(10, extra);
	}

	//used for visuals
	// 1/2/3 used for low/mid/high tier consumables
	// 3 used for +0-1 equips, 4 used for +2 or higher equips
	private static int latestDropTier = 0;

	public static void showFlareForBonusDrop( Visual vis ){
		if (vis == null || vis.parent == null) return;
		switch (latestDropTier){
			default:
				break; //do nothing
			case 1:
				new Flare(6, 20).color(0x00FF00, true).show(vis, 3f);
				break;
			case 2:
				new Flare(6, 24).color(0x00AAFF, true).show(vis, 3.33f);
				break;
			case 3:
				new Flare(6, 28).color(0xAA00FF, true).show(vis, 3.67f);
				break;
			case 4:
				new Flare(6, 32).color(0xFFAA00, true).show(vis, 4f);
				break;
		}
		latestDropTier = 0;
	}
	
	public static Item genConsumableDrop(int level) {
		float roll = Random.Float();
		//60% chance - 4% per level. Starting from +15: 0%
		if (roll < (0.6f - 0.04f * level)) {
			latestDropTier = 1;
			return genLowValueConsumable();
		//30% chance + 2% per level. Starting from +15: 60%-2%*(lvl-15)
		} else if (roll < (0.9f - 0.02f * level)) {
			latestDropTier = 2;
			return genMidValueConsumable();
		//10% chance + 2% per level. Starting from +15: 40%+2%*(lvl-15)
		} else {
			latestDropTier = 3;

			//==== END(挑战 198 幸运药水): 幸运药水的获取途径 ====
			//文档所有者定稿："获得方式财富/幸运掉落。"
			//
			//接在**高价值档**（10% 那一档）—— 而不是低/中档：
			//药水的效果不弱，放在常见档会泛滥。
			//
			//而且只有勾选 198 时才可能掉（未勾选时概率为 0）。
			if (Random.Float() < com.shatteredpixel.shatteredpixeldungeon.endcontent
					.challenge.ChallengeEffects.luckyPotionDropChance()) {
				return new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
						.LuckyPotion();
			}

			return genHighValueConsumable();
		}
	}

	private static Item genLowValueConsumable(){

		//==== END(修复 81 搏杀赌徒): 升级卷轴判定提到 switch 之外 ====
		//原先判定只写在 case 3 里面，要连过三关才能出升级卷轴：
		//    掉落分级 48%（+3 戒指）× 子类型 25% × 概率 50%  ≈ 6%
		//而"财富戒指触发掉落"本身还有一层判定 —— 玩家实测打十几只都不出。
		//
		//现在把判定**提到最前面**：只要走了低价值掉落这一档，
		//就有一次独立的机会出升级卷轴（概率以常量表示，当前 50%）。
		//这样它与"具体摸到哪个子类型"无关，触发率提升到"掉落分级 × 50%"。
		//
		//未勾选 81 时进不来（概率为 0），等价于原版行为。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.gamblerUpgradeScrollChance() > 0
				&& Random.Float() < com.shatteredpixel.shatteredpixeldungeon
						.endcontent.challenge.ChallengeEffects
						.gamblerUpgradeScrollChance()) {
			return new com.shatteredpixel.shatteredpixeldungeon.items.scrolls
					.ScrollOfUpgrade();
		}

		switch (Random.Int(4)){
			case 0: default:
				Item i = new Gold().random();
				return i.quantity(i.quantity()/2);
			case 1:
				return Generator.randomUsingDefaults(Generator.Category.STONE);
			case 2:
				return Generator.randomUsingDefaults(Generator.Category.POTION);
			case 3:
				return Generator.randomUsingDefaults(Generator.Category.SCROLL);
		}
	}

	private static Item genMidValueConsumable(){
		switch (Random.Int(6)){
			case 0: default:
				Item i = genLowValueConsumable();
				return i.quantity(i.quantity()*2);
			case 1:
				i = Generator.randomUsingDefaults(Generator.Category.POTION);
				if (!(i instanceof ExoticPotion)) {
					return Reflection.newInstance(ExoticPotion.regToExo.get(i.getClass()));
				} else {
					return Reflection.newInstance(i.getClass());
				}
			case 2:
				i = Generator.randomUsingDefaults(Generator.Category.SCROLL);
				if (!(i instanceof ExoticScroll)){
					return Reflection.newInstance(ExoticScroll.regToExo.get(i.getClass()));
				} else {
					return Reflection.newInstance(i.getClass());
				}
			case 3:
				return Random.Int(2) == 0 ? new UnstableBrew() : new UnstableSpell();
			case 4:
				return new Bomb();
			case 5:
				return new Honeypot();
		}
	}

	private static Item genHighValueConsumable(){
		switch (Random.Int(4)){
			case 0: default:
				Item i = genMidValueConsumable();
				if (i instanceof Bomb){
					return new Bomb.DoubleBomb();
				} else {
					return i.quantity(i.quantity()*2);
				}
			case 1:
				return new StoneOfEnchantment();
			case 2:
				return Random.Float() < ExoticCrystals.consumableExoticChance() ? new PotionOfDivineInspiration() : new PotionOfExperience();
			case 3:
				return Random.Float() < ExoticCrystals.consumableExoticChance() ? new ScrollOfMetamorphosis() : new ScrollOfTransmutation();
		}
	}

	private static Item genEquipmentDrop( int level ){
		Item result;
		//each upgrade increases depth used for calculating drops by 1
		int floorset = (Dungeon.depth + level)/5;
		switch (Random.Int(5)){
			default: case 0: case 1:
				Weapon w = Generator.randomWeapon(floorset, true);
				if (!w.hasGoodEnchant() && Random.Int(10) < level)      w.enchant();
				else if (w.hasCurseEnchant())                           w.enchant(null);
				result = w;
				break;
			case 2:
				Armor a = Generator.randomArmor(floorset);
				if (!a.hasGoodGlyph() && Random.Int(10) < level)        a.inscribe();
				else if (a.hasCurseGlyph())                             a.inscribe(null);
				result = a;
				break;
			case 3:
				result = Generator.randomUsingDefaults(Generator.Category.RING);
				break;
			case 4:
				result = Generator.random(Generator.Category.ARTIFACT);
				break;
		}
		//minimum level is 1/2/3/4/5/6 when ring level is 1/3/5/7/9/11
		if (result.isUpgradable()){
			int minLevel = (level+1)/2;
			if (result.level() < minLevel){
				result.level(minLevel);
			}
		}
		result.cursed = false;
		result.cursedKnown = true;
		if (result.level() >= 2) {
			latestDropTier = 4;
		} else {
			latestDropTier = 3;
		}
		return result;
	}

	public class Wealth extends RingBuff {
	}

	public static class TriesToDropTracker extends CounterBuff {
		{
			revivePersists = true;
		}
	}

	public static class DropsToEquipTracker extends CounterBuff {
		{
			revivePersists = true;
		}
	}
}
