/*
 * 破碎的地牢 (End fork) — 挑战「无量空处」的最终 Boss：两面宿傩（四阶段）。
 *
 * 替换 25 层的古神。无「适应」机制；免疫无量空处的眩晕。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.YogSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Sukuna extends Mob {

	{
		spriteClass = YogSprite.class;

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
	private boolean phaseTransitioned = false;

	public boolean immuneToDomainStun(){ return true; }

	@Override public int damageRoll(){ return Random.NormalIntRange(28, 42); }
	@Override public int attackSkill(Char target){ return 45; }
	@Override public int drRoll(){ return Random.NormalIntRange(0, 18); }

	@Override
	public boolean isAlive(){ return super.isAlive(); }

	@Override
	protected boolean act(){
		if (!isAlive()) return true;

		if (abilityCd > 0) abilityCd--;

		if (phase == 3 && domainTurns > 0){
			domainTick();
			spend(1f);
			return true;
		}

		if (abilityCd <= 0 && enemy != null && enemy.isAlive()){
			useSkill();
			abilityCd = phase == 4 ? 1 : 2;
			spend(1f);
			return true;
		}

		return super.act();
	}

	private void useSkill(){
		switch (phase){
			case 1:
				//解：单体远程斩击
				GLog.w("宿傩：「解。」");
				enemy.damage(Math.round(damageRoll() * 1.2f), this);
				break;
			case 2:
				//开：直线火焰 + 燃烧
				GLog.w("宿傩：「开。」");
				enemy.damage(Math.round(damageRoll() * 1.5f), this);
				Buff.affect(enemy, Burning.class).reignite(enemy, 3f);
				if (Random.Int(2) == 0) summonShikigami(2);
				break;
			case 3:
				startDomain();
				break;
			case 4:
				//空间斩：无视防御与闪避
				GLog.w("宿傩：「空间斩。」");
				int dmg = Math.round(damageRoll() * 1.8f);
				if (enemy.sprite != null) enemy.sprite.showStatus(
						com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.NEGATIVE,
						Integer.toString(dmg));
				enemy.HP -= dmg;
				if (enemy.HP <= 0) enemy.die(this);
				break;
		}
	}

	private void startDomain(){
		GLog.w("宿傩展开领域——伏魔御厨子。");
		domainTurns = 5;
		coresLeft = 4;
		spawnCores();
		CellEmitter.get(pos).burst(Speck.factory(Speck.LIGHT), 12);
	}

	private void domainTick(){
		GLog.w("伏魔御厨子：斩击不断。");
		if (enemy != null && enemy.isAlive()){
			enemy.damage(Math.round(damageRoll() * 0.8f), this);
		}
		domainTurns--;
		if (domainTurns <= 0 || coresLeft <= 0){
			domainTurns = 0;
			coresLeft = 0;
			GLog.i("伏魔御厨子消散了。");
			abilityCd = 2;
		}
	}

	public void onCoreKilled(){
		if (domainTurns > 0){
			coresLeft--;
			if (coresLeft <= 0){
				domainTurns = 0;
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
			core.pos = cell;
			core.HP = core.HT = 80;
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.add(core);
		}
	}

	private void summonShikigami(int n){
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
	public void damage(int dmg, Object src){
		super.damage(dmg, src);
		if (!isAlive()) return;
		if (!phaseTransitioned){
			if (phase == 1 && HP <= HT * 0.75f){
				phase = 2; phaseTransitioned = true;
				GLog.w("宿傩切换形态：火焰。");
				abilityCd = 1;
			} else if (phase == 2 && HP <= HT * 0.50f){
				phase = 3; phaseTransitioned = true;
				GLog.w("宿傩切换形态：领域。");
				abilityCd = 1;
			} else if (phase == 3 && HP <= HT * 0.25f){
				phase = 4; phaseTransitioned = true;
				GLog.w("宿傩四臂全开。");
				abilityCd = 1;
			}
			if (phaseTransitioned && phase > 1) phaseTransitioned = false;
		}
	}

	@Override
	public void die(Object cause){
		GLog.w("宿傩：「不错。」");
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
	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);
		phase = bundle.getInt(PHASE);
		domainTurns = bundle.getInt(DOMAIN);
		coresLeft = bundle.getInt(CORES);
		abilityCd = bundle.getInt(ABILITY);
	}

	public int phase(){ return phase; }
}
