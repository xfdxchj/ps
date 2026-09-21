/*
 * 破碎的地牢 (End fork) — 无尽碎片体系的基础材料
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(顶级装备体系): 无尽碎片 / 无尽核心 / 无尽锭。
 *
 * <h3>流程（文档所有者定稿）</h3>
 * <pre>
 *   ① 15 级以上的装备  ──炼金──▶  无尽核心
 *   ② 任意装备         ──分解──▶  无尽碎片
 *   ③ 100 个无尽碎片   ──合成──▶  1 个无尽锭
 *   ④ 2 个无尽锭 + 对应核心  ──炼金──▶  对应装备
 * </pre>
 *
 * <p>三个材料都做成可堆叠、不可升级的纯材料 —— 与"符石/金属碎片"
 * 这些原版材料的定位一致。
 */
public final class InfinityMaterials {

	private InfinityMaterials() {}

	//==================================================================
	//无尽碎片：炼金分解装备得到，100 个合成 1 个锭
	//==================================================================

	/** END: 无尽碎片。 */
	public static class InfinityShard extends Item {

		{
			image = ItemSpriteSheet.GRIMM_INFINITY_CATALYST;   //复用催化剂图（同为碎片状）
			stackable = true;
			bones = false;
		}

		public static final int PER_INGOT = 100;

		@Override public String name(){ return "无尽碎片"; }

		@Override
		public String info(){
			return "装备被熔尽之后剩下的东西。边缘还在轻轻发烫。\n\n" +
					"-由**炼金分解装备**获得（装备等级越高，给得越多）\n" +
					"-**100 个**可以合成 1 个**无尽锭**";
		}

		@Override public String desc(){ return info(); }

		@Override public boolean isUpgradable(){ return false; }
		@Override public boolean isIdentified(){ return true; }
		@Override public int value(){ return 0; }
	}

	//==================================================================
	//无尽核心：15 级以上的装备炼金得到 —— **共 4 种，用颜色区分**
	//==================================================================

	/**
	 * END: 无尽核心（4 种）。
	 *
	 * <p>文档所有者定稿："4 种核心，用颜色来区别"。
	 *
	 * <pre>
	 *   核心·剑（红） → 寰宇支配之剑
	 *   核心·甲（紫） → 虚空不灭之甲
	 *   核心·戒（金） → 轮回噬灭之戒
	 *   核心·弓（青） → 天堂陨落长弓
	 * </pre>
	 *
	 * <h3>炼出哪一种</h3>
	 * 由**被炼化的那件装备的类型**决定：
	 * <pre>
	 *   炼掉一件近战武器 → 核心·剑
	 *   炼掉一件护甲     → 核心·甲
	 *   炼掉一件戒指     → 核心·戒
	 *   炼掉一件远程武器 → 核心·弓
	 * </pre>
	 * 这样"牺牲什么就得到什么方向的核心"，与成品一一对应。
	 */
	public static class InfinityCore extends Item {

		/** 核心的种类。 */
		public enum Kind {
			SWORD("剑", ItemSpriteSheet.GRIMM_CORE_SWORD),
			ARMOR("甲", ItemSpriteSheet.GRIMM_CORE_ARMOR),
			RING ("戒", ItemSpriteSheet.GRIMM_CORE_RING),
			BOW  ("弓", ItemSpriteSheet.GRIMM_CORE_BOW);

			public final String label;
			public final int image;
			Kind(String label, int image){ this.label = label; this.image = image; }
		}

		/** 只有这个等级以上的装备才能炼出核心。 */
		public static final int MIN_LEVEL = 15;

		private Kind kind = Kind.SWORD;

		public InfinityCore(){ this(Kind.SWORD); }

		public InfinityCore(Kind kind){
			this.kind = (kind == null) ? Kind.SWORD : kind;
			image = this.kind.image;
			stackable = true;
			bones = false;
		}

		public Kind kind(){ return kind; }

		@Override public String name(){ return "无尽核心·" + kind.label; }

		@Override
		public String info(){
			return "一件装备被强化到极致之后，内部凝结出的核。\n\n" +
					"-由 **" + MIN_LEVEL + " 级以上**的装备炼金转化而来\n" +
					"-种类由**被炼化的装备类型**决定（武器→剑 / 护甲→甲 / 戒指→戒 / 远程→弓）\n" +
					"-是合成对应顶级装备的必要材料";
		}

		@Override public String desc(){ return info(); }

		@Override public boolean isUpgradable(){ return false; }
		@Override public boolean isIdentified(){ return true; }
		@Override public int value(){ return 0; }

		/** END: 同种核心才能堆叠。 */
		@Override
		public boolean isSimilar(Item item){
			return item instanceof InfinityCore
					&& ((InfinityCore) item).kind == this.kind;
		}

		/** END: 存档用 —— 把种类写进去，否则读档会全变成"剑"。 */
		private static final String KIND = "inf_core_kind";

		@Override
		public void storeInBundle(com.watabou.utils.Bundle bundle){
			super.storeInBundle(bundle);
			bundle.put(KIND, kind.name());
		}

		@Override
		public void restoreFromBundle(com.watabou.utils.Bundle bundle){
			super.restoreFromBundle(bundle);
			try {
				kind = Kind.valueOf(bundle.getString(KIND));
			} catch (Exception e){
				kind = Kind.SWORD;
			}
			image = kind.image;
		}
	}

	//==================================================================
	//无尽锭：100 碎片合成
	//==================================================================

	/** END: 无尽锭。 */
	public static class InfinityIngot extends Item {

		{
			image = ItemSpriteSheet.GRIMM_INFINITY_INGOT;
			stackable = true;
			bones = false;
		}

		@Override public String name(){ return "无尽锭"; }

		@Override
		public String info(){
			return "一百份碎片压成的一块，沉得不像话。\n\n" +
					"-由 **100 个无尽碎片**合成\n" +
					"-**2 个无尽锭 + 对应核心** 可以炼金合成对应的顶级装备";
		}

		@Override public String desc(){ return info(); }

		@Override public boolean isUpgradable(){ return false; }
		@Override public boolean isIdentified(){ return true; }
		@Override public int value(){ return 0; }
	}

	//==================================================================
	//分解：装备 → 碎片数量
	//==================================================================

	/**
	 * END: 分解一件装备能得到多少碎片。
	 *
	 * <p>规律：**基础 10 + 每级 5**。
	 * <pre>
	 *   +0  →  10
	 *   +5  →  35
	 *   +10 →  60
	 *   +15 →  85
	 *   +20 → 110   （已能直接换 1 个锭）
	 * </pre>
	 *
	 * <p>这样"攒 100 个换一个锭"大约是 +18 件装备的量 ——
	 * 与"顶级装备需要 2 锭"的定位相称：需要认真攒，但不至于遥不可及。
	 */
	public static int shardsFrom(Item item){
		if (item == null) return 0;
		int lvl = Math.max(0, item.buffedLvl());
		return 10 + 5 * lvl;
	}
}
