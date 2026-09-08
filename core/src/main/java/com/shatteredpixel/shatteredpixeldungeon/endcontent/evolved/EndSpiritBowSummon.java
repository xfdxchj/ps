package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * END 灵能弓· 成品 ③「唤魔灵弓」。
 *
 * 需求（最终要求）：“复刻被杀怪同款当友军，30% 生命，非 Boss”。
 *  被你真正击杀、且非 Boss 的敌人，有 20% 机会在其倒下的身边复制一只同款但我方的它，
 *  出生即把生命压到自身最大生命(HT)的约 30%。
 *
 * 引擎要点：
 *  - Weapon.proc 先于扣血完成；proc 内同帧不能判“已击杀”，故仿自然之怒排队 VFX_PRIO
 *    延迟一帧再判定 victim.isAlive()==false。
 *  - 复制用 victim.getClass() 新建同款，设 ALLY+HUNTING，排除 Boss(及其派生)以免拉Boss做友军。
 */
public class EndSpiritBowSummon extends SpiritBow {

	/** 击杀后触发复刻的成功率。 */
	private static final float SUMMON_CHANCE = 0.20f;

	@Override public String name() { return "唤魔灵弓"; }

	@Override
	public String desc() {
		return "进化·唤魔灵弓：被你真正击杀的敌人（Boss 除外）有 20% 概率在其倒下处复制一只同款但我们阵营的"
				+ "“盟友版”——出场时把生命压到它自身最大生命的约 30%（至少 1）。每一下都独立判定，没有次数上限。\n\n"
				+ "伤害比原版灵能弓高 20%，随角色等级成长、无法用升级卷轴强化。";
	}

	/** 进阶弓整体伤害比原版灵能弓高 20%（随角色等级成长、不可用升级卷轴）。 */
	@Override
	public int damageRoll( Char owner ){
		return Math.round( super.damageRoll( owner ) * 1.2f );
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		damage = super.proc(attacker, defender, damage);
		if (defender == null || !defender.isAlive() || defender == attacker) return damage;

		final Char victim = defender;

		//延迟一帧判亡后掷 SUMMON_CHANCE（对齐自然延迟；VFX_PRIO 优先结算）
		Actor.add(new Actor() {
			{
				actPriority = VFX_PRIO;
			}
			@Override
			protected boolean act() {
				if (!victim.isAlive() && victim instanceof Mob && Random.Float() < SUMMON_CHANCE){
					System.out.println("[SUMMON] 触发 20% 复刻被杀怪"); //临调试
					spawnCopyOfAlly( (Mob) victim );
				}
				Actor.remove(this);
				return true;
			}
		});

		return damage;
	}

	/** 复制一只同款被杀怪为友军，生命压到 ~30% HT；Boss 及无法实例化的跳过。 */
	private void spawnCopyOfAlly( Mob proto ){
		//排 Boss(及 Boss 化/首领类)
		if (botBlocked( proto )){
			System.out.println("[SUMMON] skip boss/not-clonable"); //临调试
			return;
		}

		Mob copy;
		try {
			copy = (Mob) com.watabou.utils.Reflection.newInstance( proto.getClass() );
		} catch (Exception e) {
			System.out.println("[SUMMON] copy-fail "+proto.getClass().getSimpleName()); //临调试
			return;
		}
		if (copy == null) return;

		int atPos = nearFreeCell( proto.pos );
		copy.alignment  = Char.Alignment.ALLY;   //友方
		try { copy.state = copy.HUNTING; } catch (Throwable ignore){}

		GameScene.add( copy );
		ScrollOfTeleportation.appear( copy, atPos );
		copy.HP = Math.max( 1, Math.round( copy.HT * 0.30f ) );  //生命~30%
		copy.HT = Math.max( copy.HT, copy.HP );

		System.out.println("[SUMMON] 已复制友军 "+copy.getClass().getSimpleName()
				+" hp="+copy.HP+"/"+copy.HT+" ally="+(copy.alignment==Char.Alignment.ALLY)); //临调试
	}

	private boolean botBlocked( Mob m ){
		if (Char.hasProp(m, com.shatteredpixel.shatteredpixeldungeon.actors.Char.Property.BOSS)) return true;
		//部分特殊占位(雕像/伪宝)本能生成无意义，留给防御性
		return false;
	}

	/** 在 atPos 附近挑一个可站且无人占用的格子放复制体。 */
	private int nearFreeCell( int atPos ){
		ArrayList<Integer> cands = new ArrayList<>();
		if (freeToDrop( atPos )) cands.add( atPos );
		for (int n : PathFinder.NEIGHBOURS8){
			int c = atPos + n;
			if (freeToDrop( c )) cands.add( c );
		}
		return cands.isEmpty() ? atPos : Random.element( cands );
	}

	private boolean freeToDrop( int c ){
		return Dungeon.level != null && Dungeon.level.insideMap( c )
				&& !Dungeon.level.solid[c]
				&& Dungeon.level.passable[c]
				&& Actor.findChar( c ) == null;
	}
}
