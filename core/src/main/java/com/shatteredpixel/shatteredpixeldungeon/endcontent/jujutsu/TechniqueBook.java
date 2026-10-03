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

public class TechniqueBook extends Item {

	public static final String AC_CANG = "苍";
	public static final String AC_HE   = "赫";
	public static final String AC_ZI   = "茈";
	public static final String AC_DOMAIN = "无量空处";

	public static final int MAX_LEVEL = 10;

	{
		image = ItemSpriteSheet.TECHNIQUE_BOOK;
		unique = true;
		bones = false;
	}

	@Override public String name(){ return "术式之书"; }
	@Override public String desc(){ return info(); }


	@Override public String defaultAction(){ return null; } //不能放快捷栏

	@Override public boolean isUpgradable(){ return true; }
	@Override public boolean isIdentified(){ return true; }
	@Override public int value(){ return 0; }

	@Override
	public Item upgrade(){
		if (level() < MAX_LEVEL) super.upgrade();
		return this;
	}

	@Override
	public String info(){
		return "五条悟的术式书。\n\n" +
				"苍：选一个位置，球飞过去，把视野内敌人拉向该位置并造成攻击力 x" + fmt(cangMult()) + " 伤害。\n" +
				"赫：选一个位置，球飞过去，把视野内敌人推离该位置并造成攻击力 x" + fmt(heMult()) + " 伤害。\n" +
				"茈：苍与赫的落点重合时自动触发，造成攻击力 x" + fmt(ziMult()) + " 无视护甲伤害。\n" +
				"无量空处：展开领域 3 回合，敌人无法行动并持续受伤。每层限一次。\n\n" +
				"当前等级 +" + level() + "（上限 +" + MAX_LEVEL + "）。";
	}

	private static String fmt(float v){ return String.format(java.util.Locale.US, "%.2f", v); }

	private float lvl(){ return level(); }
	private float cangMult(){ return 1.5f + 0.15f * lvl(); }
	private float heMult(){ return 2.0f + 0.20f * lvl(); }
	private float ziMult(){ return 4.0f + 0.40f * lvl(); }
	private float domainMult(){ return 2.0f + 0.20f * lvl(); }

	@Override
	public ArrayList<String> actions(Hero hero){
		ArrayList<String> actions = super.actions(hero);
		if (actions.isEmpty()) return actions;
		actions.add(AC_CANG);
		actions.add(AC_HE);
		if (Jujutsu.domainDepth != Dungeon.depth) actions.add(AC_DOMAIN);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action){
		super.execute(hero, action);
		if (hero == null) return;

		//END(修订): 苍/赫 都改成"选一个位置"，球飞过去再释放。
		//苍的落点由 Jujutsu 记下；赫落在同一格时自动触发茈。
		if (AC_CANG.equals(action)){
			if (hero.buff(Jujutsu.CangCd.class) != null){
				GLog.w("苍还在冷却。");
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
					JujutsuFx.cang(hero, target, () -> {
						for (Mob m : Jujutsu.visibleEnemies(hero)){
							pullToCell(m, target);
							m.damage(dmg, hero);
						}
						Jujutsu.noteCangLanded(target);
					});
					Buff.affect(hero, Jujutsu.CangCd.class, 5f);
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
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.selectCell(
					new com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector.Listener(){
				@Override public String prompt(){ return "选择赫的落点…"; }
				@Override public void onSelect(Integer cell){
					if (cell == null || cell < 0) return;
					final int target = cell;
					final int dmg = Math.round(hero.damageRoll() * heMult());
					JujutsuSfx.play(JujutsuSfx.HE);
					JujutsuFx.he(hero, target, () -> {
						for (Mob m : Jujutsu.visibleEnemies(hero)){
							pushFromCell(m, target);
							m.damage(dmg, hero);
						}
						//落点与最近一次苍重合 -> 触发茈
						if (Jujutsu.canZiAt(target)){
							int zi = Math.round(hero.damageRoll() * ziMult());
							JujutsuSfx.play(JujutsuSfx.ZI);
							JujutsuFx.ziAt(target);
							for (Mob m : enemiesNear(target, 2)){
								dealDirect(m, zi, hero);
							}
							Jujutsu.lastCangCell = -1;
							GLog.i("苍赫重叠——虚式·茈。");
						}
					});
					Buff.affect(hero, Jujutsu.HeCd.class, 5f);
					hero.spendAndNext(1f);
				}
			});
			return;
		}

		if (AC_DOMAIN.equals(action)){
			if (Jujutsu.domainDepth == Dungeon.depth){
				GLog.w("这一层已经展开过无量空处了。");
			} else {
				Jujutsu.domainDepth = Dungeon.depth;
				int dmg = Math.round(hero.damageRoll() * domainMult());
				Buff.affect(hero, DomainBuff.class).set(dmg);
				JujutsuSfx.play(JujutsuSfx.DOMAIN);
				JujutsuFx.domain(hero);
				GLog.i("领域展开——无量空处。");
			}
		}
		hero.spendAndNext(1f);
		Item.updateQuickslot();
	}

	/** 把目标拉到指定落点旁边最近的空格。 */
	private static void pullToCell(Mob m, int base){
		if (m == null || !m.isAlive()) return;
		if (Dungeon.level.distance(m.pos, base) > 8) return;
		int best = -1;
		for (int i : PathFinder.NEIGHBOURS8){
			int cell = base + i;
			if (!Dungeon.level.insideMap(cell)) continue;
			if (!Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]) continue;
			if (Actor.findChar(cell) != null) continue;
			best = cell; break;
		}
		if (best != -1) m.move(best, false);
	}

	/** 把目标推离指定落点。 */
	private static void pushFromCell(Mob m, int base){
		if (m == null || !m.isAlive()) return;
		int best = -1;
		int cur = Dungeon.level.distance(base, m.pos);
		for (int i : PathFinder.NEIGHBOURS8){
			int cell = m.pos + i;
			if (!Dungeon.level.insideMap(cell)) continue;
			if (!Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]) continue;
			if (Dungeon.level.distance(base, cell) <= cur) continue;
			if (Actor.findChar(cell) != null) continue;
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
			Jujutsu.playerDomainTurns = 3;
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
			Jujutsu.playerDomainTurns = Math.max(0, 3 - turns);
			if (turns >= 3){
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
		@Override public String desc(){ return "领域还剩 " + (3 - turns) + " 回合。"; }
	}
}
