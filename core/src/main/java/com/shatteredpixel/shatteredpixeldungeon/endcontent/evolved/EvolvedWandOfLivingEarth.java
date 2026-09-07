/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 量产样板：WandOfLivingEarth → 灵壤大地法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：活体大地:灵壤守卫可装备(机制TODO)
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

public class EvolvedWandOfLivingEarth extends WandOfLivingEarth {

	@Override
	public String name() {
		return "灵壤大地法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 13152368, 1.7f );
	}

	//结束扩展 M2：伤害与"泥沙量"(armorToAdd)在原版 onZap 中同源(=damageRoll())，
	//统一提升 40%，一次覆写同时覆盖两者。
	@Override
	public int damageRoll() {
		return Math.round(super.damageRoll() * 1.4f);
	}
	// ---- 终焉·进化基础(统一13把)：真实等级+8、充能上限20(10起步,每级+1) ----
	@Override
	public void updateLevel() {
		maxCharges = Math.min(initialCharges() + level(), 20);
		curCharges = Math.min(curCharges, maxCharges);
	}
}
