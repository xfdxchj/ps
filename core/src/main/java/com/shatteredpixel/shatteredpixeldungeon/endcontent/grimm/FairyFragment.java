/*
 * 破碎的地牢 (End fork) — 挑战 129「心爱的少女」的童话残片
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * END(挑战 129 心爱的少女): 童话残片。
 *
 * <h3>原表效果</h3>
 * "地牢中每层生成时有 2% 概率刷新'童话残片'。收集 9 个童话残片，
 *   可通过炼金合成（3 合 1 机制）获得 1 个唯一物品'心爱的少女'。"
 *
 * <h3>按文档所有者定稿的调整</h3>
 * <ul>
 *   <li><b>刷新频率</b>：不是"每层 2%"，而是**每 2 层必定刷新一个**
 *       （比原表慷慨得多，让 9 个残片在一局内可集齐）</li>
 *   <li><b>唯一</b>：同一层只刷一个</li>
 *   <li><b>9 种</b>：每种对应一个《BLACK SOULS》角色，
 *       各自有独立的名称与描述文本</li>
 * </ul>
 *
 * <h3>9 个角色</h3>
 * 贞德（圣女贞德）、多萝西（绿野仙踪）、艾露玛（卖火柴的小女孩）、
 * 古兹（黄金鹅）、卡塔丽娜（山德利亚的圣女）、伊丽莎白（女王伊丽莎白）、
 * 蕾克（小红帽）、爱丽丝（爱丽丝梦游仙境）、仙度瑞拉（灰姑娘）。
 *
 * <p>贴图暂时 9 种共用一个图标（羊皮纸残页），以后再换专属图。
 */
public class FairyFragment extends Item {

	/** 一共几种残片。 */
	public static final int KINDS = 9;

	/** 每隔几层刷新一个。 */
	public static final int SPAWN_EVERY_N_FLOORS = 2;

	/** 合成"心爱的少女"需要几个。 */
	public static final int NEEDED = 9;

	/**
	 * 本残片对应哪个角色（0..8）。
	 *
	 * <p>用字段而不是子类：9 个子类会让合成与掉落逻辑都要写 9 个分支，
	 * 而它们**行为完全相同**，只是文案不同 —— 用一个编号字段最省。
	 */
	public int kind = 0;

	{
		image = ItemSpriteSheet.GRIMM_FAIRY_FRAGMENT;
		stackable = true;
		bones = false;
	}

	@Override
	public String name(){
		//END(修订): 显示名改为「童话残片 1..9」而不是角色名 ——
		//文档所有者要求不要在名字里暴露角色身份，
		//角色名与设定保留在 desc() 里，翻开时才看到。
		return "童话残片 " + (kind + 1);
	}

	@Override
	public String desc(){
		return characterLore(kind);
	}

	@Override
	public String info(){
		return desc() + "\n\n集齐 " + NEEDED + " 枚不同的残片，可炼成《心爱的少女》。";
	}

	@Override
	public boolean isUpgradable(){ return false; }

	@Override
	public boolean isIdentified(){ return true; }

	@Override
	public int value(){ return 0; }

	//==================================================================
	//9 个角色的名称与设定
	//==================================================================

	/** END(129): 角色名。 */
	public static String characterName(int kind){
		switch (kind){
			case 0: return "贞德";
			case 1: return "多萝西";
			case 2: return "艾露玛";
			case 3: return "古兹";
			case 4: return "卡塔丽娜";
			case 5: return "伊丽莎白";
			case 6: return "蕾克";
			case 7: return "爱丽丝";
			case 8: return "仙度瑞拉";
			default: return "？";
		}
	}

	/** END(129): 角色对应的童话原型。 */
	public static String fairyTaleOf(int kind){
		switch (kind){
			case 0: return "圣女贞德";
			case 1: return "绿野仙踪";
			case 2: return "卖火柴的小女孩";
			case 3: return "黄金鹅";
			case 4: return "山德利亚的圣女";
			case 5: return "女王伊丽莎白";
			case 6: return "小红帽";
			case 7: return "爱丽丝梦游仙境";
			case 8: return "灰姑娘";
			default: return "—";
		}
	}

	/**
	 * END(129): 角色的设定文本。
	 *
	 * <p>内容来自文档所有者提供的《BLACK SOULS》人物设定。
	 * **蕾克那一条刻意保留了原文的"血迹涂改"效果** ——
	 * 原文是"沾满鲜血的xxxx红帽xxxx创造xxxxxx狼xxxxxxxxxxx
	 * 梦梦幻xxxxx少女是第几个呢xxxxxxxxx"，
	 * 那些 x 是设定的一部分（表示文本被涂改/损坏），不是占位符。
	 */
	public static String characterLore(int kind){
		switch (kind){
			case 0:
				return "为何会出现战争呢。为何人类会如此丑恶呢。\n" +
						"我再也不会去信奉什么神明了。\n" +
						"在业火之中，魔女如此想到。";
			case 1:
				return "她带着三个下仆杀掉了无数的魔女。只差一点了。\n" +
						"总有一天老子会成为支配全世界的魔法师吧。";
			case 2:
				return "母亲这么说了。这个火柴呢……\n" +
						"隆冬的夜晚，可怜的卖火柴的少女被拥挤的人群践踏了。\n" +
						"她诅咒着不讲理的世界，在民房纵火温暖自己的身体和心灵。";
			case 3:
				return "这只鸟可以给予凡夫莫大的财富。\n" +
						"但同时这只鸟也是反复无常的，厌倦之后就会飞走到不知哪儿去。";
			case 4:
				return "信仰越深，就越会对神的存在感到怀疑。\n" +
						"世界都已经变成了这副样子，神又跑到哪里去了？";
			case 5:
				return "暴虐的女王的兴趣是残酷地拷问下仆以及贵族的女儿。\n" +
						"她尤其喜欢用「铁处女」杀害她们，然后品尝灵魂的滋味。";
			case 6:
				return "沾满鲜血的xxxx红帽xxxx创造xxxxxx狼xxxxxxxxxxx\n" +
						"梦梦幻xxxxx少女是第几个呢xxxxxxxxx";
			case 7:
				return "爱丽丝终于发觉了。这个不可思议的世界是伪造出来的。\n" +
						"一切都不不过是被谁所创造出来的妄想罢了……\n" +
						"自己也是如此。";
			case 8:
				return "披灰之姬出现在憧憬恋爱与地位的女性面前，把她们带往舞踏会。\n" +
						"但是她们并不知道，实现梦想的代价是被夺走灵魂……\n" +
						"这是童话残片的内容。";
			default:
				return "字迹已经无法辨认了。";
		}
	}

	//==================================================================
	//掉落
	//==================================================================

	/**
	 * END(129): 本层是否应该刷新一枚残片。
	 *
	 * <p>文档所有者定稿："每 2 层刷新，唯一"。
	 * 即 {@code depth % 2 == 0} 的楼层刷一枚，每层最多一枚。
	 *
	 * @param depth 实际楼层
	 * @return true 表示本层应刷
	 */
	public static boolean shouldSpawnOnFloor(int depth){
		if (depth <= 0) return false;
		return depth % SPAWN_EVERY_N_FLOORS == 0;
	}

	/**
	 * END(129): 抽一枚**当前还没收集过**的残片。
	 *
	 * <p>优先给缺的那种 —— 否则玩家会拿到一堆重复的，
	 * 而合成要求"9 枚不同的残片"。
	 *
	 * @return 新的残片实例；若已集齐则返回 null
	 */
	public static FairyFragment rollMissingKind(){
		boolean[] owned = new boolean[KINDS];

		//统计玩家已有的种类
		if (Dungeon.hero != null && Dungeon.hero.belongings != null) {
			ArrayList<Item> all = Dungeon.hero.belongings.getAllItems(Item.class);
			if (all != null) {
				for (Item it : all) {
					if (it instanceof FairyFragment) {
						int k = ((FairyFragment) it).kind;
						if (k >= 0 && k < KINDS) owned[k] = true;
					}
				}
			}
		}

		ArrayList<Integer> missing = new ArrayList<>();
		for (int i = 0; i < KINDS; i++) if (!owned[i]) missing.add(i);
		if (missing.isEmpty()) return null;          //已集齐

		FairyFragment f = new FairyFragment();
		f.kind = missing.get(Random.Int(missing.size()));
		return f;
	}

	/**
	 * END(129): 在本层找一块可以放置残片的地面。
	 *
	 * <p>条件：可通行、不是水/草/高草（那些格子放东西会被踩掉）、
	 * 当前没有其它物品或角色。最多试 200 次。
	 *
	 * @param level 目标楼层
	 * @return 格子编号；找不到返回 -1
	 */
	public static int pickDropCell(com.shatteredpixel.shatteredpixeldungeon.levels.Level level){
		if (level == null) return -1;

		for (int tries = 0; tries < 200; tries++){
			int cell = Random.Int(level.length());
			int t = level.map[cell];

			//只放在普通地面上 —— 水/草/门/楼梯都不合适
			if (t != com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY
					&& t != com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY_SP
					&& t != com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY_DECO){
				continue;
			}
			if (!level.passable[cell]) continue;
			if (level.heaps.get(cell) != null) continue;
			if (com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(cell) != null) continue;

			return cell;
		}
		return -1;
	}

	//==================================================================
	//存档
	//==================================================================

	private static final String KIND = "kind";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(KIND, kind);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		kind = bundle.getInt(KIND);
	}
}
