package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs.TeleportCooldown;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Point;

/**
 * 刺杀匕首（基础/盗贼开局投掷匕首）。
 * <p>数值与原版初始投掷物 {@code ThrowingKnife} 一致（tier1: min 2+lvl、max 6+2lvl、力量同 tier）。
 * <p>传送是基础就带有的能力：投掷命中敌人（处于传送冷却之外）时，掷刀者会被传送到该敌背后并短暂隐身，
 * 随后进入一段“传送冷却”，不可连闪。基础即可传送，不需要先进化成成品。
 * <p>基础也可作为炼金基底,进化为成品（三叉戟 / 传送 / 处决）。
 */
public class AssassinDagger extends MissileWeapon {

	{//数值/感官同 ThrowingKnife(tier1)
		image = ItemSpriteSheet.THROWING_KNIFE;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.f;
		stackable = false;
		bones = false;
		tier = 1;
		baseUses = 5;
	}

	private static final float TELE_COOLDOWN = 100f;
	private static final float TELE_STEALTH = 1f;

	@Override public String name(){ return "刺杀匕首"; }

	//与 ThrowingKnife 一致的伤害区间/命中
	@Override public int min(int lvl){ return 2 + lvl; }
	@Override public int max(int lvl){
		return 6 * tier +                    //6 base
				(tier == 1 ? 2 * lvl : tier * lvl);
	}

	//无限耐久：抛投后永不耗损。
	@Override public float durabilityPerUse(int lvl){ return 0f; }

	/**
	 * 命中伤害回调用链：真实投掷命中敌人后若可传送，把掷刀者闪到敌人背后。
	 * proc 只会在真实命中的目标身上触发一次；据此实现“命中后传送”。
	 */
	@Override public int proc(Char attacker, Char defender, int damage){
		int out = super.proc(attacker, defender, damage);
		if (attacker instanceof Hero
				&& defender != null && defender != attacker && defender.isAlive()
				&& defender.alignment != Char.Alignment.ALLY
				&& attacker.buff(TeleportCooldown.class) == null){

			int moveTo = behindCell((Hero) attacker, defender);
			if (moveTo != -1 && moveTo != ((Hero) attacker).pos){
				ScrollOfTeleportation.appear((Hero) attacker, moveTo);
				Dungeon.level.occupyCell(attacker);
				Buff.affect(attacker, Invisibility.class, TELE_STEALTH);
				if (attacker == Dungeon.hero){
					Dungeon.observe();
					GameScene.updateFog();
				}
			}
			Buff.affect(attacker, TeleportCooldown.class, TELE_COOLDOWN);
		}
		return out;
	}

	/** 目标(被命中敌)8邻域中“远离掷刀人方向即背后侧”的可用空格；找不到返回 -1。 */
	private int behindCell(Char origin, Char target){
		int width = Dungeon.level.width();
		int height = Dungeon.level.height();
		Point to = Dungeon.level.cellToPoint(target.pos);
		Point from = Dungeon.level.cellToPoint(origin.pos);
		int dirX = Integer.signum(to.x - from.x);
		int dirY = Integer.signum(to.y - from.y);
		int best = -1;
		int bestScore = Integer.MIN_VALUE;
		for (int dy = -1; dy <= 1; dy++){
			for (int dx = -1; dx <= 1; dx++){
				if (dx == 0 && dy == 0) continue;
				int x = to.x + dx, y = to.y + dy;
				if (x < 0 || x >= width || y < 0 || y >= height) continue;
				int cell = Dungeon.level.pointToCell(new Point(x, y));
				if (!Dungeon.level.insideMap(cell)) continue;
				if (!Dungeon.level.passable[cell]) continue;
				if (com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(cell) != null) continue;
				int score = 0;
				if (dirX != 0) score += Integer.signum(dx) == dirX ? 3 : 0;
				if (dirY != 0) score += Integer.signum(dy) == dirY ? 3 : 0;
				if (score > bestScore){ bestScore = score; best = cell; }
			}
		}
		return best;
	}

	@Override public String info(){
		return "专为刺杀打造的基础投掷匕首,数值与原版初始飞刀一致。命中敌人时可借刀光传送到目标背后并短暂隐身(有传送冷却)。" +
				"\n亦可在炼金锅中消耗邪能碎片进化为成品:三叉戟 / 传送 / 处决。";
	}
}
