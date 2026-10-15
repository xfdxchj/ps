/*
 * 破碎的地牢 (End fork) — 破碎之刃：所有武器都有耐久度。
 *
 * 每次命中 -1；耐久到 0 武器碎裂，从装备槽卸下、变成 1 个「破碎碎片」。
 * 碎片用液金(LiquidMetal) 在炼金锅重铸回原武器，并可加料：
 *   升级卷轴 -> +1 级；强化符石 -> 下限伤害 +20%；诅咒菱晶 -> +2 级并附带诅咒。
 * 重铸一次后该武器获得「无限耐久」。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.items.BladeShard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public final class BrittleBlade {

	private BrittleBlade(){}

	public static final int MAX_DURABILITY = 100;

	/** 每次命中调用。 */
	public static void wear(Weapon w, Hero hero){
		if (w == null || hero == null || w.infiniteDurability) return;
		if (w.durability > 0) w.durability--;
		if (w.durability <= 0){
			breakWeapon(w, hero);
		}
	}

	/** 碎裂：卸下装备、变成碎片。 */
	public static void breakWeapon(Weapon w, Hero hero){
		if (w == null || hero == null) return;
		if (hero.belongings.weapon == w) hero.belongings.weapon = null;
		if (hero.belongings.secondWep == w) hero.belongings.secondWep = null;
		if (hero.belongings.thrownWeapon == w) hero.belongings.thrownWeapon = null;
		if (hero.belongings.abilityWeapon == w) hero.belongings.abilityWeapon = null;

		BladeShard shard = new BladeShard();
		shard.original = w;
		w.durability = MAX_DURABILITY;
		if (!shard.collect(hero.belongings.backpack)){
			Dungeon.level.drop(shard, hero.pos);
		}
		GLog.w("武器碎裂了！它变成了一块碎片。");
		Item_updateQuickslot();
	}

	private static void Item_updateQuickslot(){
		try { com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot(); }
		catch (Throwable ignored) {}
	}
}
