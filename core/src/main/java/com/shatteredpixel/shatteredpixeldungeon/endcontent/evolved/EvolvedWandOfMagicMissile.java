/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 样板 1：魔弹法杖·进化(魔弹×2)。先以“新物品+附魔光泽”落地模板，
 * 对应 by design 的 onZap 多发/分担机制作为 TODO 在此类后续接入（模板先行）。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfMagicMissile extends WandOfMagicMissile {

	// 用作不同化它的辨识名；模板期文案可后续迁到 messages/。
	@Override
	public String name() {
		return "灵陨·魔弹法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		// 独特“进阶附魔光泽” (冰蓝偏虹)
		return new ItemSprite.Glowing( 0x88FFFF, 2f );
	}

	// TODO(endcontent/M2): 设计为魔弹数量 ×2 —— 在此重写 onZap/fx 或引入 split 目标是后续增强内容。
}
