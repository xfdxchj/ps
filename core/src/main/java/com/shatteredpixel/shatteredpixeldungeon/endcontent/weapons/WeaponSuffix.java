/*
 * 破碎的地牢 (End fork) — 武器后缀系统。
 *
 * 一把武器最多带 1 个后缀；后缀通过「后缀卷轴」炼金获得，对武器使用后随机附加。
 * 攻击时自动触发，带 10 回合冷却（饮血 30 回合）。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.Random;

import java.util.ArrayList;

public enum WeaponSuffix {

	COLD_CORE("冷核聚集", 10){
		@Override
		public int proc(Weapon w, Char attacker, Char defender, int damage){
			boolean frozen = defender.buff(Frost.class) != null;
			ArrayList<Char> area = near(defender, 1);
			if (frozen){
				//破冰：伤害 +50%
				Buff.detach(defender, Frost.class);
				CellEmitter.get(defender.pos).burst(Speck.factory(Speck.BLUE_LIGHT), 12);
				return Math.round(damage * 1.5f);
			} else {
				for (Char ch : area){
					Buff.prolong(ch, Frost.class, 3f);
				}
				CellEmitter.get(defender.pos).burst(Speck.factory(Speck.BLUE_LIGHT), 16);
				return 0;
			}
		}
	},

	TRIPLE_STRIKE("三连击", 10){
		@Override
		public int proc(Weapon w, Char attacker, Char defender, int damage){
			//主目标 3 段 0.6×；周围 1 格其它敌人各吃 0.6×
			for (Char ch : near(defender, 1)){
				if (ch == defender) continue;
				ch.damage(Math.round(damage * 0.6f), attacker);
			}
			return Math.round(damage * 0.6f * 3f);
		}
	},

	BLAZING_SUN("耀阳", 10){
		@Override
		public int proc(Weapon w, Char attacker, Char defender, int damage){
			for (Char ch : cross(defender)){
				Buff.affect(ch, Burning.class).reignite(ch, 3f);
				ch.damage(Math.round(damage * 0.3f), attacker);
				CellEmitter.get(ch.pos).burst(Speck.factory(Speck.LIGHT), 8);
			}
			return damage + 5;
		}
	},

	SHARD("碎片", 10){
		@Override
		public int proc(Weapon w, Char attacker, Char defender, int damage){
			int n = Random.IntRange(3, 5);
			ArrayList<Char> targets = enemiesAround(attacker, 4);
			if (targets.isEmpty()) targets.add(defender);
			for (int i = 0; i < n; i++){
				Char ch = Random.element(targets);
				if (ch == null || !ch.isAlive()) continue;
				ch.damage(Math.round(damage * 0.2f), attacker);
				CellEmitter.get(ch.pos).burst(Speck.factory(Speck.LIGHT), 4);
			}
			return damage;
		}
	},

	BLOOD_DRINK("饮血", 30){
		@Override
		public int proc(Weapon w, Char attacker, Char defender, int damage){
			int heal = Math.max(1, Math.round(damage * 0.15f));
			if (attacker.HP < attacker.HT){
				attacker.HP = Math.min(attacker.HT, attacker.HP + heal);
				if (attacker.sprite != null){
					attacker.sprite.showStatus(CharSprite.POSITIVE, "+" + heal);
				}
			}
			return damage;
		}
	},

	RIFT("裂空", 10){
		@Override
		public int proc(Weapon w, Char attacker, Char defender, int damage){
			//贯穿：以攻击者→目标方向再打后面最多 2 个敌人
			ArrayList<Char> line = lineThrough(attacker, defender, 3);
			for (Char ch : line){
				if (ch == defender) continue;
				ch.damage(damage, attacker);
				CellEmitter.get(ch.pos).burst(Speck.factory(Speck.LIGHT), 6);
			}
			return damage;
		}
	};

	/** 显示名。 */
	public final String label;

	/** 冷却回合数。 */
	public final int cooldown;

	WeaponSuffix(String label, int cooldown){
		this.label = label;
		this.cooldown = cooldown;
	}

	/** 攻击触发，返回修改后的伤害。 */
	public abstract int proc(Weapon w, Char attacker, Char defender, int damage);

	public static WeaponSuffix random(){
		return values()[Random.Int(values().length)];
	}

	public static WeaponSuffix byName(String name){
		if (name == null) return null;
		for (WeaponSuffix s : values()){
			if (s.name().equals(name)) return s;
		}
		return null;
	}

	//==== 几何辅助 ====

	/** 目标 3×3 内的角色。 */
	private static ArrayList<Char> near(Char center, int radius){
		ArrayList<Char> out = new ArrayList<>();
		if (center == null || Dungeon.level == null) return out;
		int w = Dungeon.level.width();
		for (int dy = -radius; dy <= radius; dy++){
			for (int dx = -radius; dx <= radius; dx++){
				int cell = center.pos + dx + dy*w;
				if (!Dungeon.level.insideMap(cell)) continue;
				Char ch = Actor.findChar(cell);
				if (ch != null && ch.isAlive()) out.add(ch);
			}
		}
		return out;
	}

	/** 十字范围内的角色。 */
	private static ArrayList<Char> cross(Char center){
		ArrayList<Char> out = new ArrayList<>();
		if (center == null || Dungeon.level == null) return out;
		int w = Dungeon.level.width();
		int[] ofs = {0, -1, 1, -w, w};
		for (int o : ofs){
			int cell = center.pos + o;
			if (!Dungeon.level.insideMap(cell)) continue;
			Char ch = Actor.findChar(cell);
			if (ch != null && ch.isAlive()) out.add(ch);
		}
		return out;
	}

	/** 攻击者周围 radius 内的敌人。 */
	private static ArrayList<Char> enemiesAround(Char at, int radius){
		ArrayList<Char> out = new ArrayList<>();
		if (at == null || Dungeon.level == null) return out;
		int w = Dungeon.level.width();
		for (int dy = -radius; dy <= radius; dy++){
			for (int dx = -radius; dx <= radius; dx++){
				int cell = at.pos + dx + dy*w;
				if (!Dungeon.level.insideMap(cell)) continue;
				Char ch = Actor.findChar(cell);
				if (ch != null && ch.isAlive() && ch.alignment == Char.Alignment.ENEMY) out.add(ch);
			}
		}
		return out;
	}

	/** 从 attacker 穿过 defender 的直线上的前 n 个敌人。 */
	private static ArrayList<Char> lineThrough(Char attacker, Char defender, int n){
		ArrayList<Char> out = new ArrayList<>();
		if (attacker == null || defender == null || Dungeon.level == null) return out;
		int w = Dungeon.level.width();
		int ax = attacker.pos % w, ay = attacker.pos / w;
		int dx = defender.pos % w, dy = defender.pos / w;
		int sx = Integer.compare(dx - ax, 0);
		int sy = Integer.compare(dy - ay, 0);
		if (sx == 0 && sy == 0) return out;
		for (int i = 1; i <= n; i++){
			int cell = attacker.pos + sx*i + sy*i*w;
			if (!Dungeon.level.insideMap(cell)) break;
			Char ch = Actor.findChar(cell);
			if (ch != null && ch.isAlive() && ch != attacker) out.add(ch);
		}
		return out;
	}

	/** 后缀技能冷却。 */
	public static class SuffixCd extends com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff {
		@Override public int icon(){ return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.WEAKNESS; }
		@Override public String name(){ return "后缀冷却"; }
		@Override public String toString(){ return name(); }
		@Override public String desc(){ return "后缀技能还需 " + dispTurns() + " 回合冷却。"; }
	}
}
