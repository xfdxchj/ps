/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfRegrowth → 繁生法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：再生:取消次数限制、可长草(TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfRegrowth extends WandOfRegrowth {

	@Override
	public String name() {
		return "繁生法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 10020979, 1.8f );
	}

	// TODO(endcontent/M2): 再生:取消次数限制、可长草(TODO)
}
