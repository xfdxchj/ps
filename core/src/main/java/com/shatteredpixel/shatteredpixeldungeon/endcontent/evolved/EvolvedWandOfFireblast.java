/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 样板 2：爆炎法杖→灵炎法杖(火焰不再被水熄灭)。先“新物品+附魔光泽”模板，
 * 火焰不熄的特判由于涉及火焰地域/Buff 互动,在此作为 TODO 后续接入(模板先行)。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfFireblast extends WandOfFireblast {

	@Override
	public String name() {
		return "灵炎法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		// 独特的暖金/白焰光泽
		return new ItemSprite.Glowing( 0xFFD58A, 1.5f );
	}

	// TODO(endcontent/M2): “灵炎”让火焰不会被水熄灭 —— 需挂钩到火焰/Buff 逻辑后才真正生效。
}
