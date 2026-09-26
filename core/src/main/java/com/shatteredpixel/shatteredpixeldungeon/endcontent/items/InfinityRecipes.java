/*
 * 破碎的地牢 (End fork) — 无尽碎片体系的炼金配方
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.endcontent.ItemConsume;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.HeavenFallBow;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.UniverseSword;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.VoidArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

/**
 * END(顶级装备体系): 无尽碎片体系的三条配方。
 *
 * <h3>流程（文档所有者定稿）</h3>
 * <pre>
 *   ① 15 级以上的装备      ──炼金──▶  无尽核心
 *   ② 任意装备             ──分解──▶  无尽碎片
 *   ③ 100 个无尽碎片       ──合成──▶  1 个无尽锭
 *   ④ 2 个无尽锭 + 对应核心 ──炼金──▶  对应装备
 * </pre>
 *
 * <p>三条配方写在一个文件里（都是同一套体系），用内部类区分。
 */
public final class InfinityRecipes {

	private InfinityRecipes() {}

	/** 参与炼金的材料在配方里的占位数量。 */
	private static final int CORE_INGREDIENTS = 1;
	private static final int SHARDS_PER_INGOT = InfinityMaterials.InfinityShard.PER_INGOT;

	//==================================================================
	//① 分解装备 → 无尽碎片
	//==================================================================

	/**
	 * END(配方·分解): 任意可炼金的装备 → 无尽碎片。
	 *
	 * <p>**注意**：原版炼金炉本来就支持"把装备分解成炼金能量"，
	 * 这条配方是在那之上追加的 —— 会同时产出碎片。
	 *
	 * <p>判据：武器或护甲，且已鉴定、未被诅咒（与其它配方一致）。
	 */
	public static class Decompose extends Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			//END(214 无尽碎片): 勾了这条挑战才有这套配方
			if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.infinityShardEnabled()) return false;
			if (ingredients.size() != 1) return false;
			Item it = ingredients.get(0);
			if (it == null) return false;
			if (it.cursed || !it.isIdentified()) return false;
			return (it instanceof Weapon) || (it instanceof Armor);
		}

		@Override public int cost(ArrayList<Item> ingredients){ return 0; }

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients){
			int n = InfinityMaterials.shardsFrom(ingredients.get(0));
			InfinityMaterials.InfinityShard s =
					new InfinityMaterials.InfinityShard();
			s.quantity(n);
			return s;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients){
			if (!testIngredients(ingredients)) return null;

			Item src = ingredients.get(0);
			int n = InfinityMaterials.shardsFrom(src);

			InfinityMaterials.InfinityShard out =
					new InfinityMaterials.InfinityShard();
			out.quantity(n);

			//真正消耗掉那件装备
			ItemConsume.remove(src);

			GLog.i("装备熔尽，得到 " + n + " 个无尽碎片。");
			return out;
		}
	}

	//==================================================================
	//② 15 级以上装备 → 无尽核心
	//==================================================================

	/**
	 * END(配方·核心): 15 级以上的装备 → **对应种类的**无尽核心。
	 *
	 * <p>文档所有者定稿："15 级以上装备也通过炼金获得无尽核心"，
	 * 且核心**共 4 种、用颜色区分**。
	 *
	 * <h3>种类由装备类型决定</h3>
	 * <pre>
	 *   近战武器 → 核心·剑（红）
	 *   护甲     → 核心·甲（紫）
	 *   戒指     → 核心·戒（金）
	 *   远程武器 → 核心·弓（青）
	 * </pre>
	 * 这样"牺牲什么就得到什么方向的核心"，与四件成品一一对应。
	 *
	 * <p>代价是**消耗掉那件装备本身**（+15 的武器很贵，这是个真实的取舍）。
	 */
	public static class ToCore extends Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			//END(214 无尽碎片)
			if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.infinityShardEnabled()) return false;
			if (ingredients.size() != 1) return false;
			Item it = ingredients.get(0);
			if (it == null) return false;
			if (it.cursed || !it.isIdentified()) return false;
			if (kindOf(it) == null) return false;
			return it.buffedLvl() >= InfinityMaterials.InfinityCore.MIN_LEVEL;
		}

		/** END: 这件装备能炼出哪种核心；不支持的类型返回 null。 */
		public static InfinityMaterials.InfinityCore.Kind kindOf(Item it){
			if (it == null) return null;

			//==== END(修订·弓核心要"投掷物") ====
			//文档所有者定稿："弓的是分解投掷物。"
			//
			//原版 MissileWeapon 覆盖了两类东西：
			//  · **投掷物**（飞刀/标枪/战斧…）—— 特征是**可堆叠**
			//  · 灵能弓（SpiritBow）—— 唯一装备，不可堆叠
			//所以"投掷物"的判据用 stackable，把灵能弓之类排除在外。
			//
			//放在最前面：投掷物也属于 Weapon，若不先判它会被下面的分支截走。
			if (it instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon
					.missiles.MissileWeapon
					&& it.stackable){
				return InfinityMaterials.InfinityCore.Kind.BOW;
			}
			//戒指 → 戒
			if (it instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring){
				return InfinityMaterials.InfinityCore.Kind.RING;
			}
			//护甲 → 甲
			if (it instanceof Armor){
				return InfinityMaterials.InfinityCore.Kind.ARMOR;
			}
			//近战武器 → 剑
			if (it instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon
					.melee.MeleeWeapon){
				return InfinityMaterials.InfinityCore.Kind.SWORD;
			}
			return null;
		}

		@Override public int cost(ArrayList<Item> ingredients){ return 0; }

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients){
			InfinityMaterials.InfinityCore.Kind k = kindOf(ingredients.get(0));
			return (k == null) ? null : new InfinityMaterials.InfinityCore(k);
		}

		@Override
		public Item brew(ArrayList<Item> ingredients){
			if (!testIngredients(ingredients)) return null;

			Item src = ingredients.get(0);
			InfinityMaterials.InfinityCore.Kind k = kindOf(src);
			if (k == null) return null;

			InfinityMaterials.InfinityCore core =
					new InfinityMaterials.InfinityCore(k);

			ItemConsume.remove(src);

			GLog.i("装备内部的核被取了出来（" + k.label + "）。");
			return core;
		}
	}

	//==================================================================
	//③ 100 碎片 → 1 无尽锭
	//==================================================================

	/**
	 * END(配方·合成锭): 100 个无尽碎片 → 1 个无尽锭。
	 *
	 * <p>判据用**数量**而不是"堆叠数"：炼金炉把同一格的材料作为**一个 Item**
	 * 传进来，所以只要它的 {@code quantity() >= 100} 就够。
	 */
	public static class ShardToIngot extends Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			//END(214 无尽碎片): 勾了这条挑战才有这套配方
			if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.infinityShardEnabled()) return false;
			if (ingredients.size() != 1) return false;
			Item it = ingredients.get(0);
			if (!(it instanceof InfinityMaterials.InfinityShard)) return false;
			return it.quantity() >= SHARDS_PER_INGOT;
		}

		@Override public int cost(ArrayList<Item> ingredients){ return 0; }

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients){
			return new InfinityMaterials.InfinityIngot();
		}

		@Override
		public Item brew(ArrayList<Item> ingredients){
			if (!testIngredients(ingredients)) return null;

			Item shards = ingredients.get(0);
			int made = shards.quantity() / SHARDS_PER_INGOT;
			int left = shards.quantity() % SHARDS_PER_INGOT;

			InfinityMaterials.InfinityIngot out =
					new InfinityMaterials.InfinityIngot();
			out.quantity(made);

			if (left <= 0){
				ItemConsume.remove(shards);
			} else {
				shards.quantity(left);
			}

			GLog.i("压成了 " + made + " 个无尽锭。");
			return out;
		}
	}

	//==================================================================
	//④ 2 锭 + 核心 → 顶级装备
	//==================================================================

	/**
	 * END(配方·顶级装备): 2 个无尽锭 + **对应种类**的核心 → 对应装备。
	 *
	 * <p>文档所有者定稿："二无尽加对应核心炼金合成对应装备"。
	 *
	 * <h3>"对应"怎么判定</h3>
	 * 材料里带一件**样品装备**（比如一把 +15 的普通剑）——
	 * 它既决定"合成什么"，也决定"要哪种核心"：
	 * <pre>
	 *   样品是近战武器 → 要【核心·剑】 → 寰宇支配之剑
	 *   样品是护甲     → 要【核心·甲】 → 虚空不灭之甲
	 *   样品是戒指     → 要【核心·戒】 → 轮回噬灭之戒
	 *   样品是远程武器 → 要【核心·弓】 → 天堂陨落长弓
	 * </pre>
	 * **核心种类不匹配就不成立** —— 否则"对应"二字没有意义。
	 */
	public static class ToEquipment extends Recipe {

		/** 需要的锭数。 */
		public static final int INGOTS = 2;

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			//END(214 无尽碎片): 勾了这条挑战才有这套配方
			if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.infinityShardEnabled()) return false;
			if (ingredients.size() < 2 || ingredients.size() > 3) return false;

			boolean hasCore = false;
			int ingotCount = 0;

			for (Item it : ingredients){
				if (it instanceof InfinityMaterials.InfinityCore){
					if (hasCore) return false;            //只允许一个核心
					hasCore = true;
				} else if (it instanceof InfinityMaterials.InfinityIngot){
					ingotCount += it.quantity();
				} else {
					return false;                          //不认识的料
				}
			}
			return hasCore && ingotCount >= INGOTS;        //核心决定成品类型
		}

		/** END: 从材料里取核心类型。 */
		private static InfinityMaterials.InfinityCore.Kind coreKind(ArrayList<Item> ingredients){
			for (Item it : ingredients){
				if (it instanceof InfinityMaterials.InfinityCore){
					return ((InfinityMaterials.InfinityCore) it).kind();
				}
			}
			return null;
		}

		@Override public int cost(ArrayList<Item> ingredients){ return 0; }

		/** END: 样品装备对应哪种成品。 */
		private static Class<? extends Item> outputFor(
				InfinityMaterials.InfinityCore.Kind k){
			if (k == null) return null;
			switch (k){
				case SWORD: return UniverseSword.class;
				case ARMOR: return VoidArmor.class;
				case RING:  return com.shatteredpixel.shatteredpixeldungeon
						.endcontent.items.ReincarnationRing.class;
				case BOW:   return HeavenFallBow.class;
				default:    return null;
			}
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients){
			InfinityMaterials.InfinityCore.Kind k = coreKind(ingredients);
			Class<? extends Item> c = outputFor(k);
			if (c == null) return null;
			try { return c.getDeclaredConstructor().newInstance(); }
			catch (Exception e){ return null; }
		}

		@Override
		public Item brew(ArrayList<Item> ingredients){
			if (!testIngredients(ingredients)) return null;

			InfinityMaterials.InfinityCore.Kind kind = coreKind(ingredients);
			Class<? extends Item> outCls = outputFor(kind);
			if (outCls == null) return null;

			Item out;
			try {
				out = outCls.getDeclaredConstructor().newInstance();
			} catch (Exception e){
				return null;
			}

			out.identify();
			Catalog.setSeen(out.getClass());

			//消耗：核心 + 2 个锭（锭可堆叠在同一格）
			int ingotsLeft = INGOTS;
			for (Item it : ingredients){
				if (it instanceof InfinityMaterials.InfinityCore){
					ItemConsume.remove(it);
				} else if (it instanceof InfinityMaterials.InfinityIngot){
					int take = Math.min(ingotsLeft, it.quantity());
					ingotsLeft -= take;
					ItemConsume.consume(it, take);
				}
			}

			GLog.p("无尽之力凝聚成形：" + out.name() + "。");
			return out;
		}
	}
}
