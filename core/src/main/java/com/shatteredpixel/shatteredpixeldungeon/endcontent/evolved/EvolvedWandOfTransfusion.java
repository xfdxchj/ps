/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfTransfusion → 汲魂法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：注魂:获护盾时吸20%生命(TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfTransfusion extends WandOfTransfusion {

	@Override
	public String name() {
		return "汲魂法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 14715530, 1.5f );
	}

	// TODO(endcontent/M2): 注魂:获护盾时吸20%生命(TODO)
}
