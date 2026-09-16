package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END 灵能弓改造 · 中间产物：灵能核心
 *
 * 炼金炉：1x 强化符石(StoneOfAugmentation) + 1x 驱邪卷轴(ScrollOfRemoveCurse) -> 灵能核心
 * 灵能核心再 +（进化前的）灵能弓 + 相应特殊材料 -> 锻造为四选一的成品（见 EvolveSpiritBowRecipe）。
 *
 * END(修复·用户要求):
 *   · 图标原来用的是 ItemSpriteSheet.SOMETHING（占位图标），现在改为专用图标
 *   · 名字/描述原来是硬编码中文，现在走 Messages（与其它物品一致）
 */
public class SpiritBowCore extends com.shatteredpixel.shatteredpixeldungeon.items.Item {

	{
		image = ItemSpriteSheet.SPIRIT_BOW_CORE;
		stackable = true;
		defaultAction = null;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 40 * quantity();
	}
}
