/*
 * 破碎的地牢 (End fork) — 顶级装备「天堂陨落长弓」
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(顶级装备): 天堂陨落长弓。
 *
 * <h3>数值（文档所有者定稿）</h3>
 * <ul>
 *   <li>改为 **3 连射**</li>
 *   <li>**无视护甲**</li>
 *   <li>击中敌人后向**四个方向分裂**</li>
 *   <li>**神射戒**对其效果提升 **200%**；若戴的是**轮回戒**（无尽戒），则提升 **300%**</li>
 * </ul>
 *
 * <h3>关于"神射戒 / 无尽戒"的加成</h3>
 * 原版神射戒通过 {@code RingOfSharpshooting.levelDamageBonus()} 给投掷武器加伤害。
 * 本弓让那一份加成的倍率变得极高：
 * 神射戒 ×3（+200%）、轮回戒 ×4（+300%）。
 */
public class HeavenFallBow extends MissileWeapon {

	{
		image = ItemSpriteSheet.GRIMM_INFINITY_BOW;
		hitSound = Assets.Sounds.HIT_ARROW;
		hitSoundPitch = 0.9f;
		tier = 5;
	}

	/**
	 * END: 这是"弓"而不是"飞镖"。
	 *
	 * <p>原版投掷武器的默认数量是 3（见 MissileWeapon 的构造块），
	 * 但本弓是**唯一装备**，只给 1 件。数量被 {@link #defaultQuantity()} 覆盖。
	 */
	@Override public int defaultQuantity(){ return 1; }

	{
		//父类构造块已经把 quantity 设成 defaultQuantity()，
		//但那时子类的 override 还没生效 —— 所以这里再显式设一次。
		stackable = false;
		quantity = 1;
	}

	@Override public String name(){ return "天堂陨落长弓"; }

	/** 连射次数。 */
	public static final int BURST = 3;

	/**
	 * END: 3 连射。
	 *
	 * <p>覆写 {@code MissileWeapon.burstCount()} —— 那是一条通用机制，
	 * 由 {@code onThrow()} 读取，返回几就补打几发。
	 */
	@Override
	public int burstCount(){
		return BURST;
	}

	/** 神射戒的加成倍率。 */
	public static final float SHARP_RING_MULT = 3.0f;      //+200%
	/** 轮回戒（无尽戒）的加成倍率。 */
	public static final float REINCARNATION_MULT = 4.0f;   //+300%

	@Override
	public String info(){
		return "弓弦上残留着某种从天而降的东西。\n\n" +
				"每次攻击**连射 3 发**，且**无视目标护甲**。\n\n" +
				"-命中后弹道向**四个方向分裂**\n" +
				"-**神射戒**对其伤害加成提升 **200%**\n" +
				"-若换成**轮回噬灭之戒**，加成提升 **300%**\n" +
				"-不可叠加（只带 1 件）" + com.shatteredpixel.shatteredpixeldungeon.endcontent.EndItemStats.block(this);
	}

	@Override
	public String desc(){
		return info();
	}

	/**
	 * END: 神射戒 / 轮回戒的额外加成。
	 *
	 * <p>原版 {@code RingOfSharpshooting.levelDamageBonus(target)} 返回的是
	 * "戒指等级 × 2 + 1"这样的基础值。这里把它按倍率放大。
	 *
	 * @return 额外伤害（0 表示没戴相关戒指）
	 */
	public static int ringBonus(Char owner){
		if (owner == null) return 0;

		float mult = 0f;

		//神射戒
		if (com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero.class
				.isInstance(owner)) {
			com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero h =
					(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) owner;

			//轮回戒优先（+300% > +200%），不叠加
			if (h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon
					.endcontent.items.ReincarnationRing.class) != null) {
				mult = REINCARNATION_MULT;
			} else if (h.belongings.getItem(RingOfSharpshooting.class) != null) {
				mult = SHARP_RING_MULT;
			}
		}

		if (mult <= 0f) return 0;

		int base = RingOfSharpshooting.levelDamageBonus(owner);
		if (base <= 0) return 0;

		return Math.round(base * mult);
	}

	@Override
	public int damageRoll(Char owner){
		int dmg = super.damageRoll(owner);
		return dmg + ringBonus(owner);
	}

	/**
	 * END: 无视护甲。
	 *
	 * <p>{@code MissileWeapon} 默认走 {@code Char.damage()} 的护甲减免路径。
	 * 这里覆写 {@code proc()}，在伤害结算里跳过目标的护甲 ——
	 * 做法是标记"这一击无视护甲"，由 {@code Char.damage()} 查询。
	 */
	public static final String IGNORE_ARMOR = "heaven_bow_ignore_armor";

	private static boolean ignoringArmor = false;

	/** END: 当前这一击是否处于"无视护甲"状态。 */
	public static boolean isIgnoringArmor(){ return ignoringArmor; }

	@Override
	public int proc(Char attacker, Char defender, int damage){
		ignoringArmor = true;
		try{
			damage = super.proc(attacker, defender, damage);
		} finally {
			ignoringArmor = false;
		}

		//==== END: 击中后向四个方向分裂 ====
		//文档所有者定稿："击中敌人向四个方向分裂"。
		//
		//从被击中者位置向上下左右各造成一次半额伤害 ——
		//这样"分裂"是会波及周围敌人的，而不是只打一个。
		if (defender != null && attacker != null){
			splitToFourDirections(attacker, defender, damage);
		}

		return damage;
	}

	/** END: 向四个方向分裂。 */
	private static void splitToFourDirections(Char attacker, Char center, int damage){
		if (Dungeon.level == null) return;

		int secondary = Math.max(1, damage / 3);
		int[] dirs = com.watabou.utils.PathFinder.NEIGHBOURS4;

		for (int d : dirs){
			int cell = center.pos + d;
			if (cell < 0 || cell >= Dungeon.level.length()) continue;

			Char victim = com.shatteredpixel.shatteredpixeldungeon.actors.Actor
					.findChar(cell);
			if (victim == null || victim == attacker || victim == center) continue;
			if (victim.alignment == attacker.alignment) continue;   //不打自己人

			try{
				victim.damage(secondary, attacker);
				if (victim.sprite != null) victim.sprite.flash();
			} catch (Throwable ignored){
				//分裂失败不影响主箭
			}
		}
	}
}
