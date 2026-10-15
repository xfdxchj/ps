/*
 * 破碎的地牢 (End fork) — 破碎碎片：武器碎裂后的残骸。
 * 在炼金锅中加入液金可重铸回原武器。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class BladeShard extends Item {

	/** 碎裂前的武器（连同等级/附魔/后缀一起保存）。 */
	public Weapon original;

	{
		image = ItemSpriteSheet.SHARD;
		stackable = false;
		bones = false;
		unique = false;
	}

	@Override public String name(){ return "破碎碎片"; }
	@Override public String info(){
		return "一件武器碎裂后留下的碎片，边缘还带着原来的气息。\n\n"
				+ "在炼金锅中加入**液金**可以把它重铸回原武器，并获得**无限耐久**。\n"
				+ "可额外加入一种材料：\n"
				+ "- **升级卷轴**：重铸后 +1 级\n"
				+ "- **强化符石**：下限伤害 +20%\n"
				+ "- **诅咒菱晶**：+2 级并附带诅咒";
	}
	@Override public String desc(){ return info(); }
	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return original == null ? 0 : original.value(); }

	private static final String WEAPON = "weapon";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(WEAPON, original);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		original = (Weapon) bundle.get(WEAPON);
	}
}
