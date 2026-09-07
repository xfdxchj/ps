/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfBlastWave → 震岳法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：冲击波:撞墙眩晕翻倍/伤害+50%/可调冲击距离(机制TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfBlastWave extends WandOfBlastWave {

	@Override
	public String name() {
		return "震岳法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 13148415, 1.3f );
	}

	// TODO(endcontent/M2): 冲击波:撞墙眩晕翻倍/伤害+50%/可调冲击距离(机制TODO)
}
