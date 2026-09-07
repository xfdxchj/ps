/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * 便利“召器”（不消耗）：便利挑战开局给一个。用它可选择一个大类，
 * 随即在你手中生出一件该类物品（不消耗、可反复用）。
 * 当前为大类随机版（可日后扩为精确点选）。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

import java.util.ArrayList;

public class EndSummoner extends Item {

	public static final String AC_SUMMON = "SUMMON";

	{
		image = ItemSpriteSheet.SCROLL;            //占位图标（正式画后续）
		defaultAction = AC_SUMMON;
		stackable = false;                          //不叠加，且不因使用消耗
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SUMMON);
		return actions;
	}
	@Override
	public String defaultAction(){ return AC_SUMMON; }
	@Override
	public String actionName(String action, Hero hero){
		if (action.equals(AC_SUMMON)) return "获取物品";
		return super.actionName(action, hero);
	}
	@Override
	public String name(){ return "物质召器"; }
	@Override
	public String info(){ return "选择一类，我会把其中一样物品召到面前。使用不消耗。"; }

	//大类的显示名 → Generator.Category（覆盖“能拿到几乎所有”的常用分类）
	private static final String[] CAT_NAMES = {
			"近战武器", "投掷武器", "护甲", "法杖", "戒指", "神器",
			"药水", "卷轴", "种子", "符石", "食物", "炸弹"
	};
	private static final Generator.Category[] CATS = {
			Generator.Category.WEAPON, Generator.Category.MISSILE, Generator.Category.ARMOR,
			Generator.Category.WAND,  Generator.Category.RING,    Generator.Category.ARTIFACT,
			Generator.Category.POTION,Generator.Category.SCROLL,  Generator.Category.SEED,
			Generator.Category.STONE, Generator.Category.FOOD,    Generator.Category.BOMB
	};

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_SUMMON)){
			String[] labels = new String[CAT_NAMES.length];
			System.arraycopy(CAT_NAMES, 0, labels, 0, CAT_NAMES.length);
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
					new WndOptions(new ItemSprite(this),
							Messages.titleCase(name()),
							"拿取哪一类？",
							labels) {
						@Override
						protected void onSelect(int index) {
							super.onSelect(index);
							if (index >= 0 && index < CATS.length){
								handOut(CATS[index]);
							}
						}
					});
		}
	}

	private void handOut(Generator.Category category){
		Item item = Generator.randomUsingDefaults(category);
		if (item == null) return;
		item.identify();

		if (!item.collect()) {
			if (Dungeon.hero != null && Dungeon.level != null){
				Dungeon.level.drop(item, Dungeon.hero.pos);
			} else {
				GLog.w("背包已满，无法再拿取。");
			}
		}
		//不消耗本召器
	}
}
