/*
 * 破碎的地牢 (End fork) — 挑战「无量空处」的主动技能物品：术式之书。
 *
 * 苍 / 赫 / 茈 / 无量空处。升级卷轴可强化，每级按技能各自成长。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class TechniqueBook extends com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact {

	public static final String AC_CANG = "苍";
	public static final String AC_HE   = "赫";
	public static final String AC_ZI   = "茈";
	public static final String AC_DOMAIN = "无量空处";
	public static final String AC_OPEN = "术式";

	public static final int MAX_LEVEL = 10;

	{
		image = ItemSpriteSheet.TECHNIQUE_BOOK;
		chargeCap = 10;
		charge = 10;
		//END(修复·英雄变成 21 亿): Artifact.visiblyUpgraded() 用 levelCap 当除数，
		//levelCap 默认 0 时 level()>0 会得到 +Infinity -> Math.round = Integer.MAX_VALUE。
		levelCap = MAX_LEVEL;
		defaultAction = AC_OPEN; //快捷栏使用打开术式菜单
		unique = true;
		bones = false;
	}

	@Override public String actionName(String action, Hero hero){
		if (AC_CANG.equals(action)) return "苍";
		if (AC_HE.equals(action)) return "赫";
		if (AC_DOMAIN.equals(action)) return "无量空处";
		if (AC_OPEN.equals(action)) return "术式";
		return super.actionName(action, hero);
	}

	@Override public String name(){ return "术式之书"; }
	@Override public String desc(){ return info(); }


	@Override public boolean isUpgradable(){ return false; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	@Override
	public String info(){
		return "五条悟的术式书。\n\n" +
				"苍：选一个位置，球飞过去，把视野内敌人拉向该位置并造成攻击力 x" + fmt(cangMult()) + " 伤害；之后留下 5 回合引力场。\n" +
				"赫：选一个位置，球飞过去，把视野内敌人推离该位置并造成攻击力 x" + fmt(heMult()) + " 伤害；之后留下 5 回合斥力场。\n" +
				"茈：苍与赫的落点重合时自动触发，造成攻击力 x" + fmt(ziMult()) + " 无视护甲伤害。\n" +
				"无量空处：展开领域 20 回合，敌人无法行动并持续受伤；300 回合冷却。\n\n" +
				"充能 " + charge + "/" + chargeCap + "，每 20 回合回 1 点；释放技能会提升伤害等级。";
	}

	private static String fmt(float v){ return String.format(java.util.Locale.US, "%.2f", v); }

		/** 技能释放次数决定等级：每 5 次 +1 级，上限 +10。 */
	@Override public int level(){
		return Math.min(MAX_LEVEL, exp / 5);
	}

	private float lvl(){ return level(); }

	/** 每次释放技能 +1 经验。 */
	private void gainUse(){
		exp++;
	}
	private float cangMult(){ return 1.0f + 0.15f * lvl(); }
	private float heMult(){ return 1.0f + 0.20f * lvl(); }
	private float ziMult(){ return (4.0f + 0.40f * lvl()) * 0.7f; }
	private float domainMult(){ return (2.0f + 0.20f * lvl()) * 0.7f; }

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_OPEN);
		actions.add(AC_CANG);
		actions.add(AC_HE);
		if (hero.buff(Jujutsu.DomainCd.class) == null) actions.add(AC_DOMAIN);
		return actions;
	}

	//充能消耗
	public static final int COST_CANG = 2;
	public static final int COST_HE = 2;
	public static final int COST_DOMAIN = 5;

	private boolean spendCharge(int cost){
		if (charge < cost) return false;
		charge -= cost;
		return true;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (hero == null) return;

		if (AC_OPEN.equals(action)){
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
					new WndJujutsuSpells(this, hero));
			return;
		}

		//END(修订): 苍/赫 都改成"选一个位置"，球飞过去再释放。
		//苍的落点由 Jujutsu 记下；赫落在同一格时自动触发茈。
		if (AC_CANG.equals(action)){
			if (hero.buff(Jujutsu.CangCd.class) != null){
				GLog.w("苍还在冷却。");
				hero.spendAndNext(1f);
				return;
			}
			if (!spendCharge(COST_CANG)){
				GLog.w("充能不足。");
				hero.spendAndNext(1f);
				return;
			}
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.selectCell(
					new com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener(){
				@Override public String prompt(){ return "选择苍的落点…"; }
				@Override public void onSelect(Integer cell){
					if (cell == null || cell < 0) return;
					final int target = cell;
					final int dmg = Math.round(hero.damageRoll() * cangMult());
					JujutsuSfx.play(JujutsuSfx.CANG);
					gainUse();
					Buff.affect(hero, Jujutsu.CangCd.class, 15f);
					//END(修订): 球飞过去 -> 停在那一格 -> 成为 5 回合引力场。
					//到达前不在落点预生成球；到达后才结算牵引/伤害。
					JujutsuFx.cang(hero, target, () -> {
						for (Mob m : Jujutsu.visibleEnemies(hero)){
							pullToCell(m, target);
							m.damage(dmg, hero);
						}
						Jujutsu.noteCangLanded(target);
						CangField f = Buff.affect(hero, CangField.class);
						f.reset(target);
					});
					hero.spendAndNext(1f);
				}
			});
			return;
		}

		if (AC_HE.equals(action)){
			if (hero.buff(Jujutsu.HeCd.class) != null){
				GLog.w("赫还在冷却。");
				hero.spendAndNext(1f);
				return;
			}
			if (!spendCharge(COST_HE)){
				GLog.w("充能不足。");
				hero.spendAndNext(1f);
				return;
			}
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.selectCell(
					new com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener(){
				@Override public String prompt(){ return "选择赫的落点…"; }
				@Override public void onSelect(Integer cell){
					if (cell == null || cell < 0) return;
					final int target = cell;
					final int dmg = Math.round(hero.damageRoll() * heMult());
					JujutsuSfx.play(JujutsuSfx.HE);
					gainUse();
					Buff.affect(hero, Jujutsu.HeCd.class, 15f);
					//END(修订): 球飞过去 -> 到达后才推开；
					//若与苍的球停在同格：两球一起消失并触发茈。
					JujutsuFx.he(hero, target, () -> {
						for (Mob m : Jujutsu.visibleEnemies(hero)){
							pushFromCell(m, target);
							m.damage(dmg, hero);
						}
						if (Jujutsu.canZiAt(target)){
							//碰撞：赫球也不停留，苍球消失
							JujutsuBolt.cancelPersist(JujutsuBolt.HE);
							JujutsuBolt.dismiss(JujutsuBolt.CANG);
							CangField cf = hero.buff(CangField.class);
							if (cf != null) cf.detach();

							int zi = Math.round(hero.damageRoll() * ziMult());
							JujutsuSfx.play(JujutsuSfx.ZI);
							JujutsuFx.ziAt(target);
							for (Mob m : enemiesNear(target, 2)){
								dealDirect(m, zi, hero);
							}
							Jujutsu.lastCangCell = -1;
							GLog.i("苍赫重叠——虚式·茈。");
						} else {
							HeField f = Buff.affect(hero, HeField.class);
							f.reset(target);
						}
					});
					hero.spendAndNext(1f);
				}
			});
			return;
		}

		if (AC_DOMAIN.equals(action)){
			if (hero.buff(Jujutsu.DomainCd.class) != null){
				GLog.w("无量空处还在冷却。");
			} else if (!spendCharge(COST_DOMAIN)){
				GLog.w("充能不足。");
			} else {
				int dmg = Math.round(hero.damageRoll() * domainMult());
				Buff.affect(hero, DomainBuff.class).set(dmg);
				Buff.affect(hero, Jujutsu.DomainCd.class, 300f);
				JujutsuSfx.play(JujutsuSfx.DOMAIN);
				gainUse();
				com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.flash(0x80FFFFFF);
				JujutsuFx.domain(hero);
				GLog.i("领域展开——无量空处。");
			}
		}
		hero.spendAndNext(1f);
		Item.updateQuickslot();
	}

	/** 向落点移动 1 格。 */
	private static void pullToCell(Mob m, int base){
		if (m == null || !m.isAlive()) return;
		int cur = Dungeon.level.distance(m.pos, base);
		int best = -1;
		for (int i : PathFinder.NEIGHBOURS8){
			int cell = m.pos + i;
			if (!Dungeon.level.insideMap(cell)) continue;
			if (!Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]) continue;
			if (Actor.findChar(cell) != null) continue;
			if (Dungeon.level.distance(cell, base) >= cur) continue;
			best = cell; break;
		}
		if (best != -1) m.move(best, false);
	}

	/** 远离落点移动 1 格。 */
	private static void pushFromCell(Mob m, int base){
		if (m == null || !m.isAlive()) return;
		int cur = Dungeon.level.distance(m.pos, base);
		int best = -1;
		for (int i : PathFinder.NEIGHBOURS8){
			int cell = m.pos + i;
			if (!Dungeon.level.insideMap(cell)) continue;
			if (!Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]) continue;
			if (Actor.findChar(cell) != null) continue;
			if (Dungeon.level.distance(cell, base) <= cur) continue;
			best = cell; break;
		}
		if (best != -1) m.move(best, false);
	}

	/** 落点半径内的敌人。 */
	private static ArrayList<Mob> enemiesNear(int base, int radius){
		ArrayList<Mob> out = new ArrayList<>();
		if (Dungeon.level == null) return out;
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
			if (m != null && m.isAlive() && m.alignment == com.shatteredpixel.shatteredpixeldungeon
					.actors.Char.Alignment.ENEMY && Dungeon.level.distance(base, m.pos) <= radius){
				out.add(m);
			}
		}
		return out;
	}

	/** 无视护甲的直伤。 */
	private static void dealDirect(Mob m, int dmg, Hero src){
		if (m == null || !m.isAlive()) return;
		if (m.sprite != null) m.sprite.showStatus(CharSprite.NEGATIVE, Integer.toString(dmg));
		m.HP -= dmg;
		if (m.HP <= 0) m.die(src);
	}

	@Override
	protected com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.ArtifactBuff passiveBuff(){
		return new Recharge();
	}

	/** 自动充能：每回合 +0.5，2 回合 1 点。 */
	public class Recharge extends com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.ArtifactBuff {
		@Override
		public boolean act(){
			partialCharge += 0.05f; //20 回合 1 点
			if (partialCharge >= 1f){
				partialCharge -= 1f;
				charge = Math.min(chargeCap, charge + 1);
			}
			spend(TICK);
			return true;
		}
	}

	/** 苍/赫 的持续牵引/推移场，5 回合，每回合拉/推 1 格。 */
	public abstract static class Field extends Buff {
		public int cell = -1;
		private int turns = 5;
		{ type = buffType.POSITIVE; announced = false; }
		public void reset(int cell){
			this.cell = cell; this.turns = 5;
			//END(修订): 视觉球由 JujutsuBolt 自己负责（飞到落点后停留），
			//这里只记录牵引/推移位置与回合数。
		}
		protected abstract int fieldKind();
		@Override
		public void detach(){
			super.detach();
			//场结束时让停留的光球消失
			JujutsuBolt.dismiss(fieldKind());
		}
		@Override public boolean act(){
			if (cell < 0 || Dungeon.hero == null){ detach(); return true; }
			for (Mob m : Jujutsu.visibleEnemies(Dungeon.hero)){
				move(m, cell);
			}
			turns--;
			if (turns <= 0){ detach(); return true; }
			spend(TICK);
			return true;
		}
		protected abstract void move(Mob m, int cell);
		@Override public int icon(){ return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.MIND_VISION; }
		@Override public String toString(){ return name(); }
		@Override public String desc(){ return "剩余 " + turns + " 回合。"; }
	}

	/** 苍：每回合拉 1 格。 */
	public static class CangField extends Field {
		@Override protected void move(Mob m, int cell){ pullToCell(m, cell); }
		@Override protected int fieldKind(){ return 0; }
		@Override public String name(){ return "苍·引力场"; }
	}

	/** 赫：每回合推 1 格。 */
	public static class HeField extends Field {
		@Override protected void move(Mob m, int cell){ pushFromCell(m, cell); }
		@Override protected int fieldKind(){ return 1; }
		@Override public String name(){ return "赫·斥力场"; }
	}

	/** 无量空处：3 回合领域。 */
	public static class DomainBuff extends Buff {
		private int turns = 0;
		private int dmg = 0;
		{
			type = buffType.POSITIVE;
			announced = false;
		}
		public void set(int d){
			dmg = d;
			Jujutsu.playerDomainTurns = 20;
			JujutsuOverlay.castFlash(true);
		}
		@Override
		public boolean act(){
			Hero hero = Dungeon.hero;
			if (hero == null || !hero.isAlive()){ detach(); return true; }
			boolean clash = Jujutsu.domainClash();
			if (!clash){
				for (Mob m : Jujutsu.visibleEnemies(hero)){
					if (!(m instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs
							.YogDzewa) && !(m instanceof com.shatteredpixel.shatteredpixeldungeon
							.endcontent.jujutsu.Sukuna)){
						Buff.affect(m, Paralysis.class, 1f);
					}
					m.damage(dmg, hero);
				}
			}
			turns++;
			Jujutsu.playerDomainTurns = Math.max(0, 20 - turns);
			if (turns >= 20){
				detach();
				if (!clash){
					for (Mob m : Jujutsu.visibleEnemies(hero)){
						if (!(m instanceof com.shatteredpixel.shatteredpixeldungeon.endcontent
								.jujutsu.Sukuna)){
							Buff.affect(m, Paralysis.class, 1f);
						}
					}
				}
				GLog.i(clash ? "领域互相抵消。" : "领域收束。");
				return true;
			}
			spend(TICK);
			return true;
		}
		@Override
		public void detach(){
			super.detach();
			Jujutsu.playerDomainTurns = 0;
		}
		@Override public int icon(){ return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.MIND_VISION; }
		@Override public String name(){ return "无量空处"; }
		@Override public String toString(){ return name(); }
		@Override public String desc(){ return "领域还剩 " + (20 - turns) + " 回合。"; }
	}
}
