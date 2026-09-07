/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfCorrosion → 蚀骨法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：腐蚀:命中附加1回合缠绕(机制TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfCorrosion extends WandOfCorrosion {

	@Override
	public String name() {
		return "蚀骨法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 10215546, 1.6f );
	}

	// TODO(endcontent/M2): 腐蚀:命中附加1回合缠绕(机制TODO)
}
