/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfFrost → 凝霜法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：冰霜:冰冻变9格冰霜区域(机制TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfFrost extends WandOfFrost {

	@Override
	public String name() {
		return "凝霜法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 11923711, 1.5f );
	}

	// TODO(endcontent/M2): 冰霜:冰冻变9格冰霜区域(机制TODO)
}
