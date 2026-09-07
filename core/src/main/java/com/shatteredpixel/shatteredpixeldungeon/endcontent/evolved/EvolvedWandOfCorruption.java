/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfCorruption → 腐灵法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：腐化:触发概率+30%(机制TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfCorruption extends WandOfCorruption {

	@Override
	public String name() {
		return "腐灵法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 13269728, 1.4f );
	}

	// TODO(endcontent/M2): 腐化:触发概率+30%(机制TODO)
}
