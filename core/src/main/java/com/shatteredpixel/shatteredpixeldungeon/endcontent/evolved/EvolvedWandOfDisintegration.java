/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfDisintegration → 湮解法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：解离:命中得目标视野/未中仅耗半充(机制TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfDisintegration extends WandOfDisintegration {

	@Override
	public String name() {
		return "湮解法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 10477823, 1.1f );
	}

	// TODO(endcontent/M2): 解离:命中得目标视野/未中仅耗半充(机制TODO)
}
