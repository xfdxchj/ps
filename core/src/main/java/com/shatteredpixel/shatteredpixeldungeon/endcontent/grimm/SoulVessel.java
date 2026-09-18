/*
 * 破碎的地牢 (End fork) — 挑战 126「格林之心」的魂之容器
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

/**
 * END(挑战 126 格林之心): 黑之魂的容器。
 *
 * <h3>为什么需要一个道具</h3>
 * 玩家攒了魂之后得有个地方**花掉**它。原表只说"通过杀怪获得魂来增加属性"，
 * 没说怎么操作。
 *
 * <p>做成道具而不是菜单项的理由：
 * <ul>
 *   <li>不占屏幕空间，玩家想用的时候自己点</li>
 *   <li>可以顺带显示当前魂数（物品栏里就能看到）</li>
 *   <li>和"格林系列"其它道具（镇魂歌、蜂蜜酒）风格一致</li>
 * </ul>
 *
 * <p>它**不能丢、不会消耗** —— 是玩家的常驻界面入口。
 */
public class SoulVessel extends Item {

	{
		//暂时复用镇魂歌的图标（同样是"魂"主题）
		image = ItemSpriteSheet.GRIMM_REQUIEM;
		stackable = false;
		bones = false;
		unique = true;
	}

	@Override public String name(){ return "魂之容器"; }

	@Override
	public String info(){
		int have = BlackSoul.souls(
				com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero);
		return "一个装着黑之魂的玻璃瓶。\n\n" +
				"当前 **" + have + "** 点。\n\n" +
				"- 击杀怪物获得魂\n" +
				"- 每次死亡额外获得 ? 点\n" +
				"- 使用它可以**献祭** " + BlackSoul.COST_PER_OFFERING + " 点，换取一项属性";
	}

	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	/** 不能丢弃 —— 它是界面入口。 */
	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		//移除 DROP / THROW，只留"使用"
		actions.remove(AC_DROP);
		actions.remove(AC_THROW);
		actions.add(AC_USE);
		return actions;
	}

	public static final String AC_USE = "USE";

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (!action.equals(AC_USE) || hero == null) return;

		BlackSoul.openOfferingWindow(hero);
	}
}
