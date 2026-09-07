/*
 * Shattered Pixel Dungeon: End —《破碎的像素地牢：终焉扩展》
 * M2 真机制：灵炎(SpiritFire) buff —— 与原版 Burning 行为完全一致，仅"不会被水熄灭"。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TimeStasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.food.ChargrilledMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpiritFire extends Burning {

	private static final float DURATION = 8f;

	private float left;
	private boolean acted = false;
	private int burnIncrement = 0;

	private static final String LEFT = "left";
	private static final String ACTED = "acted";
	private static final String BURN = "burnIncrement";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, left);
		bundle.put(ACTED, acted);
		bundle.put(BURN, burnIncrement);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		left = bundle.getFloat(LEFT);
		acted = bundle.getBoolean(ACTED);
		burnIncrement = bundle.getInt(BURN);
	}

	@Override
	public boolean act() {
		// 与原版燃烧一致，仅去掉"站在水中会熄灭火焰"的判定
		if (target.isAlive() && !target.isImmune(Burning.class)) {

			acted = true;
			int damage = Random.NormalIntRange(1, 3 + Dungeon.scalingDepth() / 4);
			Buff.detach(target, Chill.class);

			if (target instanceof Hero
					&& target.buff(TimekeepersHourglass.timeStasis.class) == null
					&& target.buff(TimeStasis.class) == null) {

				Hero hero = (Hero) target;

				hero.damage(damage, this);
				burnIncrement++;

				if (Random.Int(3) < (burnIncrement - 3)) {
					burnIncrement = 0;

					ArrayList<Item> burnable = new ArrayList<>();
					if (!hero.belongings.lostInventory()) {
						for (Item i : hero.belongings.backpack.items) {
							if (!i.unique && (i instanceof Scroll || i instanceof MysteryMeat || i instanceof FrozenCarpaccio)) {
								burnable.add(i);
							}
						}
					}

					if (!burnable.isEmpty()) {
						Item toBurn = Random.element(burnable).detach(hero.belongings.backpack);
						GLog.w(Messages.capitalize(Messages.get(Burning.class, "burnsup", toBurn.title())));
						if (toBurn instanceof MysteryMeat || toBurn instanceof FrozenCarpaccio) {
							ChargrilledMeat steak = new ChargrilledMeat();
							if (!steak.collect(hero.belongings.backpack)) {
								Dungeon.level.drop(steak, hero.pos).sprite.drop();
							}
						}
						Heap.burnFX(hero.pos);
					}
				}

			} else {
				target.damage(damage, this);
			}

			if (target instanceof Thief && ((Thief) target).item != null) {

				Item item = ((Thief) target).item;

				if (!item.unique && item instanceof Scroll) {
					target.sprite.emitter().burst(ElmoParticle.FACTORY, 6);
					((Thief) target).item = null;
				} else if (item instanceof MysteryMeat) {
					target.sprite.emitter().burst(ElmoParticle.FACTORY, 6);
					((Thief) target).item = new ChargrilledMeat();
				}

			}

		} else {

			detach();
		}

		if (Dungeon.level.flamable[target.pos] && Blob.volumeAt(target.pos, Fire.class) == 0) {
			GameScene.add(Blob.seed(target.pos, 4, Fire.class));
		}

		spend(TICK);
		left -= TICK;

		if (left <= 0) {
			detach();
		}

		return true;
	}

	public void reignite(Char ch) {
		reignite(ch, DURATION);
	}

	public void reignite(Char ch, float duration) {
		if (ch.isImmune(Burning.class)) {
			if (ch.glyphLevel(Brimstone.class) >= 0) {
				float shieldChance = 2 * (Armor.Glyph.genericProcChanceMultiplier(ch) - 1f);
				int shieldCap = Math.round(shieldChance * 4f);
				int shieldGain = (int) shieldChance;
				if (Random.Float() < shieldChance % 1) shieldGain++;
				if (shieldCap > 0 && shieldGain > 0) {
					Barrier barrier = Buff.affect(ch, Barrier.class);
					if (barrier.shielding() < shieldCap) {
						barrier.incShield(Math.min(shieldGain, shieldCap - barrier.shielding()));
					}
				}
			}
		}
		if (left < duration) left = duration;
		acted = false;
	}

	public void extend(float duration) {
		left += duration;
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - left) / DURATION);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString((int) left);
	}

	@Override
	public String desc() {
		return Messages.get(Burning.class, "desc", dispTurns(left));
	}

	@Override
	public void onDeath() {
		Badges.validateDeathFromFire();
		Dungeon.fail(this);
		GLog.n(Messages.get(Burning.class, "ondeath"));
	}
}