/*
 * 破碎的地牢 (End fork) — 自定义装备的「原版写法」数值块
 *
 * 原版物品窗口 = 风味文案 + 数值/力量 + 机制说明。
 * 本 fork 的自定义装备把 info() 整个覆写成了"机制说明"，
 * 少了原版的数值/力量那一段。本类提供同一段文案，供各物品在
 * info() 末尾追加（`+ EndItemStats.block(this)`），即可与原版观感一致。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public final class EndItemStats {

	private EndItemStats() {}

	/** @return 要追加在 info() 末尾的数值块（含前导空行）；不支持的类型返回 ""。 */
	public static String block( Item it ){
		try {
			if (it instanceof MissileWeapon){
				MissileWeapon w = (MissileWeapon) it;
				return weaponBlock(w, MissileWeapon.class, w.tier,
						w.levelKnown ? w.augment.damageFactor(w.min()) : w.min(0),
						w.levelKnown ? w.augment.damageFactor(w.max()) : w.max(0),
						w.levelKnown ? w.STRReq() : w.STRReq(0), w.levelKnown);
			}
			if (it instanceof MeleeWeapon){
				MeleeWeapon w = (MeleeWeapon) it;
				return weaponBlock(w, MeleeWeapon.class, w.tier,
						w.levelKnown ? w.augment.damageFactor(w.min()) : w.min(0),
						w.levelKnown ? w.augment.damageFactor(w.max()) : w.max(0),
						w.levelKnown ? w.STRReq() : w.STRReq(0), w.levelKnown);
			}
			if (it instanceof Wand){
				return "\n\n" + ((Wand) it).statsDesc();
			}
			//==== END: 护甲 / 戒指也要有原版数值块 ====
			if (it instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor){
				com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor a =
					(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor) it;
				String as;
				if (a.levelKnown){
					as = "\n\n" + Messages.get(com.shatteredpixel.shatteredpixeldungeon
						.items.armor.Armor.class, "curr_absorb", a.tier, a.DRMin(), a.DRMax(), a.STRReq());
					if (Dungeon.hero != null && a.STRReq() > Dungeon.hero.STR())
						as += " " + Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.class, "too_heavy");
				} else {
					as = "\n\n" + Messages.get(com.shatteredpixel.shatteredpixeldungeon
						.items.armor.Armor.class, "avg_absorb", a.tier, a.DRMin(0), a.DRMax(0), a.STRReq(0));
					if (Dungeon.hero != null && a.STRReq(0) > Dungeon.hero.STR())
						as += " " + Messages.get(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.class, "probably_too_heavy");
				}
				return as;
			}
			
		} catch (Throwable ignored) { }
		return "";
	}

	private static String weaponBlock( Weapon w, Class<?> msgClass, int tier,
			int dmgMin, int dmgMax, int strReq, boolean known ){
		String s = "\n\n";
		if (known){
			s += Messages.get(msgClass, "stats_known", tier, dmgMin, dmgMax, strReq);
			if (Dungeon.hero != null){
				if (strReq > Dungeon.hero.STR()){
					s += " " + Messages.get(Weapon.class, "too_heavy");
				} else if (Dungeon.hero.STR() > strReq){
					s += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR() - strReq);
				}
			}
		} else {
			s += Messages.get(msgClass, "stats_unknown", tier, dmgMin, dmgMax, strReq);
			if (Dungeon.hero != null && strReq > Dungeon.hero.STR()){
				s += " " + Messages.get(Weapon.class, "probably_too_heavy");
			}
		}
		return s;
	}
}
