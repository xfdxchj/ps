/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfLivingEarth → 灵壤大地法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：活体大地:灵壤守卫可装备(机制TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfLivingEarth extends WandOfLivingEarth {

	@Override
	public String name() {
		return "灵壤大地法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 13152368, 1.7f );
	}

	// TODO(endcontent/M2): 活体大地:灵壤守卫可装备(机制TODO)
}
