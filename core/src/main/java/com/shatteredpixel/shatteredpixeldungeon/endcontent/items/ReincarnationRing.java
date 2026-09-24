/*
 * 破碎的地牢 (End fork) — 顶级装备「轮回噬灭之戒」
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(顶级装备): 轮回噬灭之戒。
 *
 * <h3>数值（文档所有者定稿）</h3>
 * "拥有所有戒指的效果，同时效果提升 100%，升级效果提升 50%。"
 *
 * <h3>怎么做到"拥有所有戒指"</h3>
 * 原版的每一种戒指效果最终都要经过
 * {@code Ring.getBuffedBonus(Char, Class<? extends RingBuff>)} 这一个静态入口
 * （12 种戒指、15 个查询方法全部走它）。
 *
 * <p>所以实现放在**那里**：只要角色身上戴着本戒指，
 * 该入口就会额外返回一份等级 —— 等于"同时戴上所有戒指"。
 * 这样不必去改那 12 个戒指文件，也不会漏掉将来新增的戒指。
 *
 * <h3>公式</h3>
 * <pre>
 *   提供等级 = round( (本戒指等级 + 1) × 2  +  本戒指等级 × 0.5 )
 *              └── 效果提升 100% ──┘   └─ 升级效果提升 50% ─┘
 * </pre>
 *
 * <p>举例：+0 → 2 级；+3 → 10 级（原版 +3 戒指只给 4 级效果）；+10 → 27 级。
 */
public class ReincarnationRing extends Ring {

	{
		image = ItemSpriteSheet.GRIMM_INFINITY_RING;
	}

	public ReincarnationRing() {
		super();
	}

	@Override public String name(){ return "轮回噬灭之戒"; }

	/** END: 无尽戒的伤害倍率（+20%）。 */
	public static final float DAMAGE_MULT = 1.2f;
	/** END: 无尽戒的护甲倍率（+20%）。 */
	public static final float ARMOR_MULT = 1.2f;

	/** END: 角色当前是否佩戴着无尽戒（轮回噬灭之戒）。 */
	public static boolean isWearing(Char ch){
		return ch instanceof Hero
				&& ((Hero) ch).belongings.getItem(ReincarnationRing.class) != null;
	}

	/** END: 无尽戒的伤害加成倍率（未佩戴时返回 1.0）。 */
	public static float damageMultiplier(Char ch){
		return isWearing(ch) ? DAMAGE_MULT : 1f;
	}

	/** END: 无尽戒的护甲加成倍率（未佩戴时返回 1.0）。 */
	public static float armorMultiplier(Char ch){
		return isWearing(ch) ? ARMOR_MULT : 1f;
	}

	/** 本戒指没有自己的 RingBuff 子类 —— 效果统一由 Ring.getBuffedBonus 提供。 */
	@Override protected RingBuff buff(){
		return new Reincarnation();
	}

	@Override
	public String info(){
		return "戒指内侧刻着一圈咬住自己尾巴的蛇。\n\n" +
				"-拥有**所有戒指**的效果\n" +
				"-提供的效果等级**翻倍**（+100%）\n" +
				"-每升级 1 级额外折算 **0.5 级**（升级效果 +50%）\n" +
				"-**伤害提升 20%**\n" +
				"-**护甲提升 20%**" + com.shatteredpixel.shatteredpixeldungeon.endcontent.EndItemStats.block(this);
	}

	@Override
	public String desc(){
		return info();
	}

	/** END: 占位 buff —— 实际效果由 Ring.getBuffedBonus 统一提供。 */
	public class Reincarnation extends RingBuff {
		@Override
		public String desc(){
			return "轮回：你同时拥有所有戒指的力量。";
		}
	}
}
