/*
 * 破碎的地牢 (End fork) — 挑战 41「钱是万能」的入口道具
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMoneyIsPower;

import java.util.ArrayList;

/**
 * END(挑战 41 钱是万能): 万能钱袋。
 *
 * <h3>作用</h3>
 * 打开「钱是万能」采购窗口的入口。
 *
 * <p>为什么不直接挂在商店 NPC 上：那个 NPC 每层不一定有，
 * 而"钱是万能"是**随时可用**的规则 —— 给一件常驻道具更合适。
 */
public class AlmightyPurse extends Item {

	{
		image = ItemSpriteSheet.GOLD;
		stackable = false;
		unique = true;
		bones = false;
	}

	@Override public String name(){ return "万能钱袋"; }

	@Override
	public String info(){
		return "一个能装下任何东西的钱袋 —— 只要你付得起。\n\n" +
				"- 使用后打开**采购窗口**\n" +
				"- 可以用金币直接买到药水、卷轴、装备、神器……\n" +
				"- 价格固定，不随层数变化\n\n" +
				"钱能解决的问题，都不是问题。";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }

	public static final String AC_SHOP = "SHOP";

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_SHOP);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!action.equals(AC_SHOP) || hero == null) return;

		GameScene.show(new WndMoneyIsPower());
	}
}
