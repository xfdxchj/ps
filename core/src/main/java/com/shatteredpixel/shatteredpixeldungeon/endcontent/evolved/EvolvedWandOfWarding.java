/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfWarding → 灵哨法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：哨戒:可对自己使用、携带哨位(TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfWarding extends WandOfWarding {

	@Override
	public String name() {
		return "灵哨法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 9090280, 1.3f );
	}

	// TODO(endcontent/M2): 哨戒:可对自己使用、携带哨位(TODO)
}
