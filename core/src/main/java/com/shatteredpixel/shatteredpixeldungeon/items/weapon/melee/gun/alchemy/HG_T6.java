package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.alchemy;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.HG.HG;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.HG.HG_T5;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(ReReARPD 枪械移植): T6 战术型。原 mod 里是炼金进阶武器；
 * 本 fork 没有 UpgradeDust/Evolution 那套，这里先作为普通 T6 枪，
 * 之后接入本 fork 的炼金/进化配方。
 */
public class HG_T6 extends HG {
	public HG_T6() {
		image = ItemSpriteSheet.HG_T6;
		tier = 6;
	}
}
