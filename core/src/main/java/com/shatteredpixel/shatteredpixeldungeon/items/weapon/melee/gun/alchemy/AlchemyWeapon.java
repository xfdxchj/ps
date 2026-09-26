/*
 * END(ReReARPD gun port): 炼金武器接口，移植自 ReReARPD。
 * T6 战术型实现它，BluePrint 的炼金配方据此匹配 T5 + 升级之尘 + 进化法术。
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UpgradeDust;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public interface AlchemyWeapon {

	ArrayList<Class<? extends Item>> weaponRecipe();

	static String hintString(ArrayList<Class<? extends Item>> recipe) {
		if (recipe.get(1).isAssignableFrom(UpgradeDust.class)) {
			return Messages.get(Item.class, "discover_hint_alchemy_one",
					Reflection.newInstance(recipe.get(0)).name());
		}
		return Messages.get(Item.class, "discover_hint_alchemy_two",
				Reflection.newInstance(recipe.get(0)).name(),
				Reflection.newInstance(recipe.get(1)).name());
	}
}
