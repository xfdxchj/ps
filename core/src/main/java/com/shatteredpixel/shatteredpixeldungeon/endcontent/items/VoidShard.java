package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 终焉·便利料「邪能碎片」：高级 EX 专属物(破印分支/刺杀匕首)的炼金升级共用材料。
 * 由炼金/掉落给到玩家；通过多条 Recipe 消耗。
 */
public class VoidShard extends Item {

	{
		image = ItemSpriteSheet.SOMETHING;
		stackable = true;
		defaultAction = null;
	}

	@Override public String name(){ return "邪能碎片"; }

	@Override public boolean isIdentified(){ return true; }

	@Override public String info(){
		return "一块凝聚着邪能的漆黑碎片。可用于在炼金炉里给专属物做「炼金升级」——"
				+ "给战士破印选分支、或把刺杀匕首炼成 三叉戟/传送/处决 的强化型。";
	}

	@Override public int value(){ return 25 * quantity(); }
}
