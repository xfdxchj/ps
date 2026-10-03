/*
 * 破碎的地牢 (End fork) — 挑战「宿傩」的最终 Boss：两面宿傩（四阶段）。
 *
 * 替换 25 层的古神。无「适应」机制；免疫无量空处的眩晕。
 * 阶段：60% 领域 / 40% 四臂 / 10% 灶开。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SukunaSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Sukuna extends com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa {

	{
		spriteClass = SukunaSprite.class;

		HP = HT = 1400;
		EXP = 50;
		defenseSkill = 30;

		state = HUNTING;
		viewDistance = 20;

		properties.add(Property.BOSS);
		properties.add(Property.DEMONIC);
	}

	private int phase = 1;
	private int abilityCd = 3;
	private int domainTurns = 0;
	private int domainCd = 0;

	public boolean immuneToDomainStun(){ return true; }

	@Override public String name(){ return "两面宿傩"; }

	/** 不走古神的多阶段存活判定，死了就是死了。 */
	@Override public boolean isAlive(){ return HP > 0; }

	/** 不能走古神的 phase==0 无敌判定，否则宿傩会打不死。 */
	@Override public boolean isInvulnerable(Class effect){
		return buff(com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist
				.Challenge.SpectatorFreeze.class) != null
				|| buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
				.Invulnerability.class) != null;
	}

	/** 不走古神的 notice()：它会把古神 phase 设成 1，进而触发召唤拳头。 */
	@Override public void notice(){
		if (!com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar.isAssigned()){
			com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar.assignBoss(this);
			yell("「让我看看你能撑多久。」");
		}
	}

	@Override public int damageRoll(){ return Random.NormalIntRange(28, 42); }
	@Override public int attackSkill(Char target){ return 45; }
	@Override public int drRoll(){ return Random.NormalIntRange(0, 18); }

	/** 四臂全开：攻速 +50%。 */
	@Override public float attackDelay(){
		float base = super.attackDelay();
		return phase >= 3 ? Math.max(0.1f, base / 1.5f) : base;
	}

	@Override
	protected boolean act(){
		if (!isAlive()) return true;

		if (abilityCd > 0) abilityCd--;
		if (domainCd > 0) domainCd--;

		if (phase == 2 && domainTurns > 0){
			domainTick();
			spend(1f);
			return true;
		}

		if (abilityCd <= 0 && enemy != null && enemy.isAlive()){
			useSkill();
			abilityCd = (phase >= 3) ? 2 : 3; //END: 技能 CD，不再每回合无限打
			spend(1f);
			return true;
		}

		return mobAct();
	}

	private void useSkill(){
		switch (phase){
			case 1:
				if (Random.Int(2) == 0){
					JujutsuSfx.play(JujutsuSfx.KAI);
					GLog.w("宿傩：「解。」");
					JujutsuFx.slash(this, enemy, false);
					enemy.damage(Math.round(damageRoll() * 1.2f), this);
				} else {
					//捌：多段范围斩击
					JujutsuSfx.play(JujutsuSfx.BACHI);
					GLog.w("宿傩：「捌。」");
					JujutsuFx.slash(this, enemy, true);
					for (int i = 0; i < 3 && enemy.isAlive(); i++){
						try { com.shatteredpixel.shatteredpixeldungeon.effects.Wound.hit(enemy, Random.Float()*360f); } catch (Throwable ignored) {}
						enemy.damage(Math.round(damageRoll() * 0.6f), this);
					}
				}
				break;
			case 2:
				if (domainCd <= 0) startDomain();
				else {
					JujutsuSfx.play(JujutsuSfx.FIRE);
					GLog.w("宿傩：「开。」");
					enemy.damage(Math.round(damageRoll() * 1.5f), this);
					Buff.affect(enemy, Burning.class).reignite(enemy, 3f);
				}
				break;
			case 3:
				//空间斩：无视防御与闪避
				JujutsuSfx.play(JujutsuSfx.SPACE);
				GLog.w("宿傩：「空间斩。」");
				JujutsuFx.spaceSlash(this, enemy);
				dealDirect(enemy, Math.round(damageRoll() * 1.8f));
				break;
			case 4:
				//灶开：终局火焰
				JujutsuSfx.play(JujutsuSfx.FUGA);
				GLog.w("宿傩：「灶开。」");
				JujutsuFx.fuga(this, enemy);
				dealDirect(enemy, Math.round(damageRoll() * 3.0f));
				if (enemy.isAlive()) Buff.affect(enemy, Burning.class).reignite(enemy, 5f);
				break;
		}
	}

	private void dealDirect(Char target, int dmg){
		if (target == null || !target.isAlive()) return;
		if (target.sprite != null) target.sprite.showStatus(
				com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.NEGATIVE,
				Integer.toString(dmg));
		target.HP -= dmg;
		if (target.HP <= 0) target.die(this);
	}

	private void startDomain(){
		JujutsuSfx.play(JujutsuSfx.SHRINE);
		GLog.w("宿傩展开领域——伏魔御厨子。");
		JujutsuFx.shrine();
		domainTurns = 5;
		Jujutsu.sukunaDomainTurns = 5;
		JujutsuOverlay.castFlash(false);
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.flash(0x80FF0000);
		CellEmitter.get(pos).burst(Speck.factory(Speck.LIGHT), 12);
	}

	private void domainTick(){
		boolean clash = Jujutsu.domainClash();
		if (clash){
			GLog.i("领域互相抵消。");
		} else {
			GLog.w("伏魔御厨子：斩击不断。");
			if (enemy != null && enemy.isAlive()){
				try { com.shatteredpixel.shatteredpixeldungeon.effects.Wound.hit(enemy, Random.Float()*360f); } catch (Throwable ignored) {}
				enemy.damage(Math.round(damageRoll() * 0.8f), this);
			}
		}
		domainTurns--;
		Jujutsu.sukunaDomainTurns = Math.max(0, domainTurns);
		if (domainTurns <= 0){
			domainTurns = 0;
			Jujutsu.sukunaDomainTurns = 0;
			GLog.i("伏魔御厨子消散了。");
			domainCd = 20; //END: 20 回合领域 CD
			abilityCd = 2;
		}
	}

	@Override
	public void onAdd(){
		super.onAdd();
		//END(修复·古神替换判定): 注册到 Boss 血条，让 25 层把它当 Boss
		try { com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar.assignBoss(this); }
		catch (Throwable ignored) {}
		JujutsuSfx.play(JujutsuSfx.SUKUNA_INTRO);
		GLog.w("宿傩：「让我看看你能撑多久。」");
	}

	@Override
	public void damage(int dmg, Object src){
		super.damage(dmg, src);
		checkPhase();
	}

	private void checkPhase(){
		if (!isAlive()) return;
		int target = phase;
		if (HP <= HT * 0.10f)      target = 4;
		else if (HP <= HT * 0.40f) target = 3;
		else if (HP <= HT * 0.60f) target = 2;

		if (target > phase){
			phase = target;
			abilityCd = 1;
			JujutsuSfx.play(JujutsuSfx.SUKUNA_PHASE);
			switch (phase){
				case 2:
					startDomain();
					break;
				case 3:
					GLog.w("宿傩四臂全开。");
					break;
				case 4:
					GLog.w("宿傩：「灶开。」");
					break;
			}
		}
	}

	@Override
	public void die(Object cause){
		GLog.w("宿傩：「不错。」");
		Jujutsu.sukunaDomainTurns = 0;
		//END(真替换): 继承 YogDzewa.die()，解封 / bossSlain / 图鉴都由古神那套走完
		super.die(cause);
	}

	//END(真替换): 键名避开古神自己的 "phase"/"ability_cd" 等，
	//否则同一个 Bundle 键会被双方覆写，读档后古神 phase 变成宿傩的阶段。
	private static final String PHASE = "sukuna_phase";
	private static final String DOMAIN = "sukuna_domain_turns";
	private static final String ABILITY = "sukuna_ability_cd";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(PHASE, phase);
		bundle.put(DOMAIN, domainTurns);
		bundle.put(ABILITY, abilityCd);
		bundle.put("sukuna_domain_cd", domainCd);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		phase = bundle.getInt(PHASE);
		domainTurns = bundle.getInt(DOMAIN);
		abilityCd = bundle.getInt(ABILITY);
		domainCd = bundle.getInt("sukuna_domain_cd");
		try { com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar.assignBoss(this); }
		catch (Throwable ignored) {}
	}

	public int phase(){ return phase; }
}
