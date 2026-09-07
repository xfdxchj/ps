/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * END 灵能弓改造· 中间产物：灵能核心
 *   由 炼金炉：2× 升级卷轴 + 50× 液金(LiquidMetal) → 灵能核心。
 *   灵能核心再 +（进化前的）灵能弓 + 相应 特殊材料 → 锻造为 四选一的成品改弓(见 EvolveSpiritBowRecipe)。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class SpiritBowCore extends com.shatteredpixel.shatteredpixeldungeon.items.Item {

	{
		image = ItemSpriteSheet.SOMETHING;
		stackable = true;
		defaultAction = null;
	}

	@Override
	public String name() {
		return "灵能核心";
	}

	@Override
	public String info() {
		return "凝聚着升级卷轴与灵金之力的核心，用于把" + name() + "锻造为终焉形态。";
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
