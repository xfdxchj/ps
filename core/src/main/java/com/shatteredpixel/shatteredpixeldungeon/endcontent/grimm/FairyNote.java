/*
 * 破碎的地牢 (End fork) — 挑战 129「心爱的少女」的中间产物
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(挑战 129): 童话残页。
 *
 * <p>3 枚**不同**的童话残片合成的中间产物，3 张残页再合成
 * {@link BelovedGirl 心爱的少女}。所以整条链共需 9 枚残片 ——
 * 与原表"收集 9 个童话残片"和"3 合 1 机制"两个数字都对上。
 *
 * <p>它不是终点，所以不做任何"使用"效果，只是一个可堆叠的合成材料。
 */
public class FairyNote extends Item {

	{
		image = ItemSpriteSheet.GRIMM_FAIRY_FRAGMENT;   //暂时共用图标
		stackable = true;
		bones = false;
	}

	@Override public String name(){ return "童话残页"; }

	@Override public String info(){
		return "三枚残片拼合而成的一页。\n\n" +
				"纸面上的字迹会随光线变化 —— 有时是一个少女站在雪地里，\n" +
				"有时是一双握着火柴的手。\n\n" +
				"再凑齐 " + FairyFragmentRecipe.NOTES_PER_GIRL + " 张，就能读到完整的故事。";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }
}
