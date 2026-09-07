/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：WandOfBlastWave → 震岳法杖（进化新物品 + 独特附魔光泽）。
 * 对应开发.txt 描述：冲击波:伤害+50%、撞墙眩晕翻倍、可调冲击距离(1/3/5格)。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class EvolvedWandOfBlastWave extends WandOfBlastWave {

	public static final String AC_SET_THRUST = "SET_THRUST";

	//END M2：可调冲击距离的档位（1/3/5 格）
	public int thrustDistance = 1;

	private static final String THRUST = "thrust_distance";

	@Override
	public String name() {
		return "震岳法杖";
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return new ItemSprite.Glowing( 13148415, 1.3f );
	}

	//END M2 真机制：冲击波伤害 +50%
	@Override
	public int min(int lvl){
		return Math.round( super.min(lvl) * 1.5f );
	}
	@Override
	public int max(int lvl){
		return Math.round( super.max(lvl) * 1.5f );
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(THRUST, thrustDistance);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		thrustDistance = bundle.getInt(THRUST);
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SET_THRUST);
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (action.equals(AC_SET_THRUST)) {
			return "调整冲击距离";
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {
		if (action.equals(AC_SET_THRUST)) {
			thrustDistance = (thrustDistance == 5) ? 1 : (thrustDistance == 1) ? 3 : 5;
			GLog.i("冲击距离已设为 " + thrustDistance + " 格");
		} else {
			super.execute(hero, action);
		}
	}

	//END M2 真机制：撞墙眩晕翻倍、可调冲击距离(1/3/5格)
	@Override
	public void onZap(Ballistica bolt) {
		Sample.INSTANCE.play( Assets.Sounds.BLAST );
		BlastWave.blast(bolt.collisionPos);

		for (int i : PathFinder.NEIGHBOURS9){
			if (!(Dungeon.level.traps.get(bolt.collisionPos+i) instanceof TenguDartTrap)) {
				Dungeon.level.pressCell(bolt.collisionPos + i);
			}
		}

		//周围 8 格：冲击距离为所选档位的一半（向上取整）
		int sideStrength = Math.max(1, Math.round(thrustDistance / 2f));

		for (int i  : PathFinder.NEIGHBOURS8){
			Char ch = Actor.findChar(bolt.collisionPos + i);

			if (ch != null){
				wandProc(ch, chargesPerCast());
				if (ch.alignment != Char.Alignment.ALLY) ch.damage(damageRoll(), this);

				if ((ch.isAlive() || ch.flying || !Dungeon.level.pit[ch.pos])
						&& ch.pos == bolt.collisionPos + i) {
					Ballistica trajectory = new Ballistica(ch.pos, ch.pos + i, Ballistica.MAGIC_BOLT);
					throwCharEvolved(ch, trajectory, sideStrength, false, true, this);
				}

			}
		}

		//中心目标：冲击距离为所选档位
		Char ch = Actor.findChar(bolt.collisionPos);
		if (ch != null){
			wandProc(ch, chargesPerCast());
			ch.damage(damageRoll(), this);

			if ((ch.isAlive() || ch.flying || !Dungeon.level.pit[ch.pos])
					&& bolt.path.size() > bolt.dist+1 && ch.pos == bolt.collisionPos) {
				Ballistica trajectory = new Ballistica(ch.pos, bolt.path.get(bolt.dist + 1), Ballistica.MAGIC_BOLT);
				throwCharEvolved(ch, trajectory, thrustDistance, false, true, this);
			}
		}

	}

	//复制自父类 throwChar，撞墙眩晕翻倍
	public static void throwCharEvolved(final Char ch, final Ballistica trajectory, int power,
	                                    boolean closeDoors, boolean collideDmg, Object cause){
		if (ch.properties().contains(Char.Property.BOSS)) {
			power = (power+1)/2;
		}

		int dist = Math.min(trajectory.dist, power);

		boolean collided = dist == trajectory.dist;

		if (dist <= 0
				|| ch.rooted
				|| ch.properties().contains(Char.Property.IMMOVABLE)) return;

		if (Char.hasProp(ch, Char.Property.LARGE)) {
			for (int i = 1; i <= dist; i++) {
				if (!Dungeon.level.openSpace[trajectory.path.get(i)]){
					dist = i-1;
					collided = true;
					break;
				}
			}
		}

		if (Actor.findChar(trajectory.path.get(dist)) != null){
			dist--;
			collided = true;
		}

		if (dist < 0) return;

		final int newPos = trajectory.path.get(dist);

		if (newPos == ch.pos) return;

		final int finalDist = dist;
		final boolean finalCollided = collided && collideDmg;
		final int initialpos = ch.pos;

		Actor.add(new Pushing(ch, ch.pos, newPos, new Callback() {
			public void call() {
				if (initialpos != ch.pos || Actor.findChar(newPos) != null) {
					ch.sprite.place(ch.pos);
					return;
				}
				int oldPos = ch.pos;
				ch.pos = newPos;
				if (finalCollided && ch.isActive()) {
					ch.damage(Random.NormalIntRange(finalDist, 2*finalDist), new Knockback());
					if (ch.isActive()) {
						//END M2：撞墙眩晕翻倍（原为 1 + finalDist/2f）
						Paralysis.prolong(ch, Paralysis.class, 2 + finalDist);
					} else if (ch == Dungeon.hero){
						if (cause instanceof WandOfBlastWave){
							Badges.validateDeathFromFriendlyMagic();
						}
						GLog.n(Messages.get(WandOfBlastWave.class, "knockback_ondeath"));
						Dungeon.fail(cause);
					}
				}
				if (closeDoors && Dungeon.level.map[oldPos] == Terrain.OPEN_DOOR){
					Door.leave(oldPos);
				}
				Dungeon.level.occupyCell(ch);
				if (ch == Dungeon.hero){
					Dungeon.observe();
					GameScene.updateFog();
				} else if (Dungeon.level.heroFOV[initialpos] != Dungeon.level.heroFOV[newPos]){
					Dungeon.observe();
				}
			}
		}));
	}
	// ---- 终焉·进化基础(统一13把)：等级归零后强度锚定+8并成长+20%、最大充能20 ----
	@Override
	public int buffedLvl() {
		return 8 + Math.round(super.buffedLvl() * 1.2f);
	}

	@Override
	public void updateLevel() {
		maxCharges = 20;
		curCharges = Math.min(curCharges, maxCharges);
	}
}