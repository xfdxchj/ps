/*
 * 破碎的地牢 (End fork) — 挑战 217「爆裂魔法」的法杖
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

/**
 * END(挑战 217 爆裂魔法): 一颗砸下去会炸开的火球。
 *
 * <h3>文档所有者定稿</h3>
 * <ul>
 *   <li>消耗 **1 充能**</li>
 *   <li>攻击方式类似**阵爆方石**（落点为中心炸开）</li>
 *   <li>**冲击波音效**</li>
 *   <li>伤害 **4-8**</li>
 *   <li>**进阶版**（{@link WandOfMeteorAdvanced}）范围 **5×5**，
 *       **3×3 内额外 25% 伤害**</li>
 * </ul>
 *
 * <h3>基础版 vs 进阶版</h3>
 * 两者是**两把独立的法杖**（文档所有者确认），各有各的注册与获取方式 ——
 * 不是同一把靠强化等级变形。进阶版见 {@link WandOfMeteorAdvanced}。
 *
 * <p>本类只实现**基础版**：落点 3×3，全额伤害。
 */
public class WandOfMeteor extends DamageWand {

	{
		image = ItemSpriteSheet.GRIMM_METEOR_STAFF;
		collisionProperties = Ballistica.PROJECTILE;
	}

	@Override public String name(){ return "爆炸法杖"; }

	//==================================================================
	//数值
	//==================================================================

	/** 基础伤害下限。 */
	public static final int BASE_MIN = 4;
	/** 基础伤害上限。 */
	public static final int BASE_MAX = 8;

	@Override public int min(int lvl){ return BASE_MIN; }
	@Override public int max(int lvl){ return BASE_MAX; }

	@Override public int initialCharges(){ return 2; }

	/**
	 * END(修订): 每次施法只消耗 **1 充能**。
	 *
	 * <p>文档所有者定稿："消耗 1 充能"。
	 * 原版法杖默认按强化等级增加消耗，这里固定为 1 ——
	 * 本杖的定位是"能连发的爆裂"，不是"一发定胜负"。
	 */
	@Override
	public int chargesPerCast(){ return 1; }

	/** END: 施法范围的偏移表。基础版 3×3。 */
	protected int[] blastArea(){
		return PathFinder.NEIGHBOURS9;
	}

	//==================================================================
	//END(修订·改用冲击波特效)
	//==================================================================
	//
	//文档所有者定稿："爆裂拿冲击波的贴图特效，而不是魔弹。"
	//
	//原来没覆写 {@code fx()}，于是走了 {@code DamageWand} 的默认实现 ——
	//那是**魔弹**（一发紫色小弹丸飞出去）。而本杖的定位是"落点炸开"，
	//用冲击波那套更贴切：
	//  · 弹道用 {@code MagicMissile.FORCE}（原版冲击波法杖用的就是这个）
	//  · 落点再放一次爆裂粒子
	//  · 音效用 ZAP + BLAST 两层

	@Override
	public void fx(Ballistica bolt, com.watabou.utils.Callback callback) {
		com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile.boltFromChar(
				curUser.sprite.parent,
				com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile.FORCE,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		//杖身粒子：橙红（与爆裂主题一致）
		particle.color( 0xCC4400 ); particle.am = 0.6f;
		particle.setLifespan(3f);
		particle.speed.polar(com.watabou.utils.Random.Float(com.watabou.utils.PointF.PI2),
				0.3f);
		particle.setSize( 1f, 2f);
		particle.radiateXY(2.5f);
	}

	/** END: 3×3 内（内圈）的伤害。基础版即全额。 */
	protected int innerDamage(){
		return damageRoll();
	}

	/** END: 内圈之外的伤害。基础版没有外圈，返回 0。 */
	protected int outerDamage(){
		return 0;
	}

	//==================================================================
	//施法
	//==================================================================

	@Override
	public void onZap(Ballistica bolt ){
		//冲击波音效（文档所有者指定）
		Sample.INSTANCE.play( Assets.Sounds.BLAST );

		int center = bolt.collisionPos;
		int[] area = blastArea();

		//视觉：落点炸开
		if (Dungeon.level.heroFOV[center]) {
			com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter
					.get(center).burst(
							com.shatteredpixel.shatteredpixeldungeon.effects.particles
									.BlastParticle.FACTORY, 16);
			com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter
					.get(center).burst(
							com.shatteredpixel.shatteredpixeldungeon.effects.particles
									.FlameParticle.FACTORY, 10);
		}

		//先按下范围内的所有格子（陷阱会被触发）—— 照 WandOfBlastWave 的写法
		for (int i : area){
			int c = center + i;
			if (c < 0 || c >= Dungeon.level.length()) continue;
			Dungeon.level.pressCell(c);
		}

		int inner = innerDamage();
		int outer = outerDamage();

		for (int i : area){
			int c = center + i;
			if (c < 0 || c >= Dungeon.level.length()) continue;

			Char ch = Actor.findChar(c);
			if (ch == null || !ch.isAlive()) continue;
			if (ch == curUser) continue;                          //不炸自己
			if (ch.alignment == Char.Alignment.ALLY) continue;    //不打盟友

			int dmg = isInner(center, c) ? inner : outer;
			if (dmg <= 0) continue;

			wandProc(ch, chargesPerCast());
			ch.damage(dmg, this);
			if (ch.sprite != null) ch.sprite.flash();
		}

		com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.log(
				com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.COMBAT,
				"爆裂魔法：落点=" + center
						+ "  范围大小=" + area.length
						+ "  内圈=" + inner + "  外圈=" + outer);
	}

	/**
	 * END: 目标格是否落在"内圈"（3×3）。
	 *
	 * <p>用**行列差**判断而不是距离 —— 原版的格子编号是
	 * {@code y * width + x}，所以要先拆回坐标再比。
	 */
	protected boolean isInner(int center, int cell){
		int w = Dungeon.level.width();
		int cx = center % w, cy = center / w;
		int tx = cell % w,   ty = cell / w;
		return Math.abs(cx - tx) <= 1 && Math.abs(cy - ty) <= 1;
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage){
		//普通命中也带一点爆裂：照外加一次小额伤害
		defender.damage(Math.max(1, damage / 4), attacker);
	}

	//==================================================================
	//说明
	//==================================================================

	@Override
	public String info(){
		return "杖头嵌着一颗永远在微微发热的石头。\n\n" +
				"伤害 **4-8**，每次施法消耗 **1 充能**。\n\n" +
				"-落点为中心**炸开**，3×3 范围内造成全额伤害\n" +
				"-附带冲击波音效\n" +
				"-存在**进阶版**：范围扩至 5×5，3×3 内伤害再提升 25%" + com.shatteredpixel.shatteredpixeldungeon.endcontent.EndItemStats.block(this);
	}

	@Override
	public String desc(){
		return info();
	}
}
