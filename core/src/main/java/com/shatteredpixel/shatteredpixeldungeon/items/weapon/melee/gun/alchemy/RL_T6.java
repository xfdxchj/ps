package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Evolution;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UpgradeDust;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.RL.RL;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.RL.RL_T5;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * END(ReReARPD gun port): T6 战术型，炼金进阶产物。
 * 配方（按 ReReARPD）：T5 枪 + 升级之尘 + 进化法术 -> 枪械蓝图 -> 对 T5 枪使用。
 */
public class RL_T6 extends RL implements AlchemyWeapon {
	public RL_T6() {
		image = ItemSpriteSheet.RL_T6;
		tier = 6;
	}

	@Override
	public ArrayList<Class<? extends Item>> weaponRecipe() {
		return new ArrayList<>(Arrays.asList(RL_T5.class, UpgradeDust.class, Evolution.class));
	}

	public String discoverHint() {
		return AlchemyWeapon.hintString(weaponRecipe());
	}

	@Override
	public String desc() {
		return super.desc() + "\n\n" + AlchemyWeapon.hintString(weaponRecipe());
	}
}
