/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfPrismaticLight → 棱辉法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：棱光:伤害+30%(TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfPrismaticLight extends WandOfPrismaticLight {

	@Override
	public String name() {
		return "棱辉法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 16762598, 1.2f );
	}

	//END M2 真机制：棱光伤害 +30%（覆写父 min/max 即影响真实伤害roll）
	@Override
	public int min(int lvl){
		return Math.round( super.min(lvl) * 1.30f );
	}
	@Override
	public int max(int lvl){
		return Math.round( super.max(lvl) * 1.30f );
	}
}

