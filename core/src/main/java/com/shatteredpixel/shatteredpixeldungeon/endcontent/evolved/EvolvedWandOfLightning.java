/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 样板 3：闪电法杖·进化(受自身电伤时获等量护盾)。先“新物品+附魔光泽”模板，
 * 护盾化电的具体 in/out 环节在 TODO 入口后续接入(模板先行)。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfLightning extends WandOfLightning {

	@Override
	public String name() {
		return "雷髓法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		// 独特的电蓝/白弧光泽
		return new ItemSprite.Glowing( 0x9FF5FF, 1.2f );
	}

	// TODO(endcontent/M2): 受自身闪电伤害时转化为等量护盾 —— 需挂到伤害来源是本条闪电的判断。
}
