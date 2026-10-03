/*
 * 破碎的地牢 (End fork) — 挑战「无量空处」：五条悟能力包。
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 *
 * 被动：六眼（免疫致盲）、无下限（受伤 -30%，近战攻击者 100% 弹开 1 格，
 * 弹开后有 2 回合 CD）、反转术式（每回合回复 2% 最大生命，击杀额外 5%）。
 * 主动：术式之书（见 TechniqueBook）。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public final class Jujutsu {

	private Jujutsu() {}

	/** 注册表位号（= 表 ID）。 */
	public static final int BOOK = 235;
	public static final int GOJO = 236;
	public static final int SUKUNA = 237;
	public static final int KAISEN = 238;

	private static boolean on(int id){
		return Dungeon.challengeMask != null && Dungeon.challengeMask.has(id);
	}

	public static boolean bookActive(){ return on(BOOK); }
	public static boolean gojoActive(){ return on(GOJO); }
	public static boolean sukunaActive(){ return on(SUKUNA); }
	public static boolean kaisenActive(){ return on(KAISEN); }

	/** 六眼：法杖充能效率 +100%。 */
	public static float chargeMultiplier(){
		return gojoActive() ? 2f : 1f;
	}

	/** 无下限减伤：受到伤害 x0.70。 */
	public static int reduceDamage(int dmg){
		if (!gojoActive()) return dmg;
		return Math.max(1, Math.round(dmg * 0.70f));
	}

	/** 反转术式：击杀回复 5% 最大生命。 */
	public static void onKill(Char target, Object src){
		if (!gojoActive()) return;
		Hero hero = Dungeon.hero;
		if (hero == null || target == null || target.alignment == Char.Alignment.ALLY) return;
		if (!(src instanceof Hero) && src != hero) return;
		int heal = Math.max(1, Math.round(hero.HT * 0.05f));
		if (hero.HP < hero.HT) hero.HP = Math.min(hero.HT, hero.HP + heal);
	}

	/**
	 * 无下限：近战攻击者 100% 弹开 1 格，弹开后 2 回合 CD。
	 * 返回 true 表示真的弹开了。
	 */
	public static boolean knockback(Hero hero, Char attacker){
		if (!gojoActive() || hero == null || attacker == null) return false;
		if (attacker.buff(LimitlessCd.class) != null) return false;
		if (Dungeon.level.distance(hero.pos, attacker.pos) > 1) return false;

		int best = -1;
		int curDist = Dungeon.level.distance(hero.pos, attacker.pos);
		for (int i : PathFinder.NEIGHBOURS8){
			int cell = attacker.pos + i;
			if (!Dungeon.level.insideMap(cell)) continue;
			if (!Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]) continue;
			if (Dungeon.level.distance(hero.pos, cell) <= curDist) continue;
			if (Actor.findChar(cell) != null) continue;
			best = cell;
			break;
		}
		if (best == -1) return false;

		attacker.move(best, false);
		Buff.affect(attacker, LimitlessCd.class, 2f);
		if (attacker.sprite != null) attacker.sprite.showStatus(
				com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE, "弹开");
		return true;
	}

	/** 视野内的敌对怪物。 */
	public static ArrayList<Mob> visibleEnemies(Hero hero){
		ArrayList<Mob> out = new ArrayList<>();
		if (hero == null || Dungeon.level == null) return out;
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
			if (m == null || !m.isAlive() || m.alignment != Char.Alignment.ENEMY) continue;
			//视野内，或被灵视药水标记的敌人也算（可全图拉）
			boolean seen = Dungeon.level.heroFOV[m.pos]
					|| hero.mindVisionEnemies.contains(m);
			if (!seen) continue;
			out.add(m);
		}
		return out;
	}

	//==== 反转术式：每回合 2% 回复 ====
	public static class ReverseTechnique extends Buff {
		{
			type = buffType.POSITIVE;
			announced = false;
		}
		@Override
		public boolean act(){
			if (target != null && target.HP < target.HT){
				int heal = Math.max(1, Math.round(target.HT * 0.02f));
				target.HP = Math.min(target.HT, target.HP + heal);
			}
			spend(TICK);
			return true;
		}
		@Override public int icon(){ return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.HEALING; }
		@Override public String name(){ return "反转术式"; }
		@Override public String toString(){ return name(); }
		@Override public String desc(){ return "每回合回复 2% 最大生命。"; }
	}

	//==== 无下限弹开冷却 ====
	public static class LimitlessCd extends FlavourBuff {
		@Override public int icon(){ return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.WEAKNESS; }
		@Override public String name(){ return "无下限冷却"; }
		@Override public String toString(){ return name(); }
		@Override public String desc(){ return "近战弹开还需 " + dispTurns() + " 回合冷却。"; }
	}

	/** 无量空处：300 回合冷却。 */
	public static class DomainCd extends FlavourBuff {
		@Override public int icon(){ return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.MIND_VISION; }
		@Override public String name(){ return "无量空处·冷却"; }
		@Override public String toString(){ return name(); }
		@Override public String desc(){ return "无量空处还需 " + dispTurns() + " 回合冷却。"; }
	}

	//==== 术式冷却（苍 / 赫）====
	public static class CangCd extends FlavourBuff {
		@Override public int icon(){ return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.MARK; }
		@Override public String name(){ return "苍·冷却"; }
		@Override public String toString(){ return name(); }
		@Override public String desc(){ return "苍还需 " + dispTurns() + " 回合冷却。"; }
	}
	public static class HeCd extends FlavourBuff {
		@Override public int icon(){ return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.MARK; }
		@Override public String name(){ return "赫·冷却"; }
		@Override public String toString(){ return name(); }
		@Override public String desc(){ return "赫还需 " + dispTurns() + " 回合冷却。"; }
	}

	/** 茈解锁状态（本局内）与 无量空处每层一次。 */
	public static boolean purpleUnlocked = false;
	public static boolean usedCang = false;
	public static boolean usedHe = false;
	public static int domainDepth = -1;

	/** 记录苍/赫的使用：先用苍再用赫即解锁茈。 */
	public static void noteCang(){
		usedCang = true;
	}
	public static void noteHe(){
		usedHe = true;
		if (usedCang) purpleUnlocked = true;
	}

	//==== 领域展开：剩余回合 + 对撞 ====
	/** 无量空处剩余回合。 */
	public static int playerDomainTurns = 0;
	/** 伏魔御厨子剩余回合。 */
	public static int sukunaDomainTurns = 0;

	//==== 苍/赫 落点：重合时自动触发茈 ====
	public static int lastCangCell = -1;
	public static float lastCangAt = Float.NEGATIVE_INFINITY;

	public static void noteCangLanded(int cell){
		lastCangCell = cell;
		lastCangAt = com.shatteredpixel.shatteredpixeldungeon.actors.Actor.now();
	}

	public static boolean canZiAt(int cell){
		return cell == lastCangCell
				&& (com.shatteredpixel.shatteredpixeldungeon.actors.Actor.now() - lastCangAt) <= 3f;
	}

	/** 两个领域同时存在 -> 互相抵消，双方都失效。 */
	public static boolean domainClash(){
		return playerDomainTurns > 0 && sukunaDomainTurns > 0;
	}

	public static void resetRun(){
		purpleUnlocked = false;
		usedCang = false;
		usedHe = false;
		domainDepth = -1;
		lastCangCell = -1;
		lastCangAt = Float.NEGATIVE_INFINITY;
	}
}
