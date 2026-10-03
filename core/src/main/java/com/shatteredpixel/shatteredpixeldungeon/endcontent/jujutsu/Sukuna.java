/*
 * 破碎的地牢 (End fork) — 挑战「宿傩」的最终 Boss：两面宿傩（四阶段）。
 *
 * 替换 25 层的古神。无「适应」机制；免疫无量空处的眩晕。
 * 阶段：75% 召唤式神 / 60% 领域 / 40% 四臂 / 10% 灶开。
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

public class Sukuna extends Mob {

	{
		spriteClass = SukunaSprite.class;

		HP = HT = 1400;
		EXP = 50;
		defenseSkill = 30;

		state = HUNTING;
		viewDistance = 20;

		properties.add(Property.BOSS);
		properties.add(Property.IMMOVABLE);
		properties.add(Property.DEMONIC);
		properties.add(Property.STATIC);
	}

	private int phase = 1;
	private int abilityCd = 3;
	private int domainTurns = 0;
	private int coresLeft = 0;
	private int domainCd = 0;

	public boolean immuneToDomainStun(){ return true; }

	@Override public String name(){ return "两面宿傩"; }

	@Override public int damageRoll(){ return Random.NormalIntRange(28, 42); }
	@Override public int attackSkill(Char target){ return 45; }
	@Override public int drRoll(){ return Random.NormalIntRange(0, 18); }

	/** 四臂全开：攻速 +50%。 */
	@Override public float attackDelay(){
		float base = super.attackDelay();
		return phase >= 4 ? Math.max(0.1f, base / 1.5f) : base;
	}

	@Override
	protected boolean act(){
		if (!isAlive()) return true;

		if (abilityCd > 0) abilityCd--;
		if (domainCd > 0) domainCd--;

		if (phase == 3 && domainTurns > 0){
			domainTick();
			spend(1f);
			return true;
		}

		if (abilityCd <= 0 && enemy != null && enemy.isAlive()){
			useSkill();
			abilityCd = (phase >= 4) ? 2 : 3; //END: 技能 CD，不再每回合无限打
			spend(1f);
			return true;
		}

		//不普攻：站着等技能 CD
		spend(1f);
		return true;
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
				JujutsuSfx.play(JujutsuSfx.FIRE);
				GLog.w("宿傩：「开。」");
				enemy.damage(Math.round(damageRoll() * 1.5f), this);
				Buff.affect(enemy, Burning.class).reignite(enemy, 3f);
				break;
			case 3:
				if (domainCd <= 0) startDomain();
				else {
					GLog.w("宿傩：「开。」");
					enemy.damage(Math.round(damageRoll() * 1.5f), this);
					Buff.affect(enemy, Burning.class).reignite(enemy, 3f);
				}
				break;
			case 4:
				//空间斩：无视防御与闪避
				JujutsuSfx.play(JujutsuSfx.SPACE);
				GLog.w("宿傩：「空间斩。」");
				JujutsuFx.spaceSlash(this, enemy);
				dealDirect(enemy, Math.round(damageRoll() * 1.8f));
				break;
			case 5:
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
		coresLeft = 4;
		spawnCores();
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
		if (domainTurns <= 0 || coresLeft <= 0){
			domainTurns = 0;
			Jujutsu.sukunaDomainTurns = 0;
			coresLeft = 0;
			GLog.i("伏魔御厨子消散了。");
			domainCd = 20; //END: 20 回合领域 CD
			abilityCd = 2;
		}
	}

	public void onCoreKilled(){
		if (domainTurns > 0){
			coresLeft--;
			if (coresLeft <= 0){
				domainTurns = 0;
				domainCd = 20;
				GLog.i("四核尽毁。伏魔御厨子崩解。");
			}
		}
	}

	private void spawnCores(){
		for (int i = 0; i < 4; i++){
			int cell = findSpawnCell();
			if (cell == -1) continue;
			SukunaMinion core = new SukunaMinion();
			core.isCore = true;
			core.makeCore();
			core.pos = cell;
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(core);
		}
	}

	private void summonShikigami(int n){
		GLog.w("宿傩召来式神。");
		for (int i = 0; i < n; i++){
			int cell = findSpawnCell();
			if (cell == -1) continue;
			SukunaMinion m = new SukunaMinion();
			m.isCore = false;
			m.pos = cell;
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(m);
		}
	}

	private int findSpawnCell(){
		for (int i = 0; i < 40; i++){
			int cell = pos + PathFinder.NEIGHBOURS8[Random.Int(8)] * (1 + Random.Int(4));
			if (!Dungeon.level.insideMap(cell)) continue;
			if (!Dungeon.level.passable[cell]) continue;
			if (Actor.findChar(cell) != null) continue;
			return cell;
		}
		return -1;
	}

	@Override
	public void onAdd(){
		super.onAdd();
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
		if (HP <= HT * 0.10f)      target = 5;
		else if (HP <= HT * 0.40f) target = 4;
		else if (HP <= HT * 0.60f) target = 3;
		else if (HP <= HT * 0.75f) target = 2;

		if (target > phase){
			phase = target;
			abilityCd = 1;
			JujutsuSfx.play(JujutsuSfx.SUKUNA_PHASE);
			switch (phase){
				case 2:
					summonShikigami(2);
					break;
				case 3:
					startDomain();
					break;
				case 4:
					GLog.w("宿傩四臂全开。");
					break;
				case 5:
					GLog.w("宿傩：「灶开。」");
					break;
			}
		}
	}

	@Override
	public void die(Object cause){
		GLog.w("宿傩：「不错。」");
		Jujutsu.sukunaDomainTurns = 0;
		super.die(cause);
	}

	private static final String PHASE = "phase";
	private static final String DOMAIN = "domain_turns";
	private static final String CORES = "cores_left";
	private static final String ABILITY = "ability_cd";

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);
		bundle.put(PHASE, phase);
		bundle.put(DOMAIN, domainTurns);
		bundle.put(CORES, coresLeft);
		bundle.put(ABILITY, abilityCd);
		bundle.put("domain_cd", domainCd);
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		phase = bundle.getInt(PHASE);
		domainTurns = bundle.getInt(DOMAIN);
		coresLeft = bundle.getInt(CORES);
		abilityCd = bundle.getInt(ABILITY);
		domainCd = bundle.getInt("domain_cd");
	}

	public int phase(){ return phase; }
}
