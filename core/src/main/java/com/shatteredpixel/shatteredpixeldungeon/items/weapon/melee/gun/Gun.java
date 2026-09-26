/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * END(ReReARPD gun port):
 * Based on ReReARPD's guns, with three fork-specific changes:
 *  1. no global bullet system (Dungeon.bullet / bullet items / ammo cost); only magazine + reload turns
 *  2. shot damage uses the thrown-weapon formula (level + strength + Ring of Sharpshooting), not gun-only bonuses
 *  3. all reload times are halved (see RELOAD_MULT)
 * No Gunner class: any hero can equip and fire these.
 */
public class Gun extends MeleeWeapon {

	public static final String AC_SHOOT = "SHOOT";
	public static final String AC_RELOAD = "RELOAD";

	protected int max_round;
	protected int round;
	protected float reload_time = 2.0f;
	protected int shotPerShoot = 1;
	protected float shootingSpeed = 1.0f;
	protected float shootingAccuracy = 1.0f;
	protected float adjacentShootingAccuracy = 1.0f;
	protected boolean explode = false;
	protected boolean spread = false;

	public static final String TXT_STATUS = "%d/%d";

	//END(user): no reload ring, all reload times are directly reduced by half
	public static final float RELOAD_MULT = 0.5f;

	public BarrelMod barrelMod = BarrelMod.NORMAL_BARREL;
	public MagazineMod magazineMod = MagazineMod.NORMAL_MAGAZINE;
	public BulletMod bulletMod = BulletMod.NORMAL_BULLET;
	public WeightMod weightMod = WeightMod.NORMAL_WEIGHT;
	public AttachMod attachMod = AttachMod.NORMAL_ATTACH;
	public EnchantMod enchantMod = EnchantMod.NORMAL_ENCHANT;
	public InscribeMod inscribeMod = InscribeMod.NORMAL;

	private static final String ROUND = "round";
	private static final String BARREL_MOD = "barrelMod";
	private static final String MAGAZINE_MOD = "magazineMod";
	private static final String BULLET_MOD = "bulletMod";
	private static final String WEIGHT_MOD = "weightMod";
	private static final String ATTACH_MOD = "attachMod";
	private static final String ENCHANT_MOD = "enchantMod";
	private static final String INSCRIBE_MOD = "inscribeMod";

	private CellSelector.Listener shooter;

	public Gun() {
		defaultAction = AC_SHOOT;
		usesTargeting = true;
		hitSound = "sounds/hit_crush.mp3";
		hitSoundPitch = 0.8f;

		shooter = new CellSelector.Listener() {
			@Override
			public void onSelect(Integer target) {
				if (target != null) {
					if (target == curUser.pos) {
						execute(Dungeon.hero, AC_RELOAD);
					} else {
						knockBullet().cast(curUser, target);
					}
				}
			}

			@Override
			public String prompt() {
				return Messages.get(Gun.class, "prompt");
			}
		};
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(ROUND, round);
		bundle.put(BARREL_MOD, barrelMod);
		bundle.put(MAGAZINE_MOD, magazineMod);
		bundle.put(BULLET_MOD, bulletMod);
		bundle.put(WEIGHT_MOD, weightMod);
		bundle.put(ATTACH_MOD, attachMod);
		bundle.put(ENCHANT_MOD, enchantMod);
		bundle.put(INSCRIBE_MOD, inscribeMod);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		round = bundle.getInt(ROUND);
		barrelMod = bundle.getEnum(BARREL_MOD, BarrelMod.class);
		magazineMod = bundle.getEnum(MAGAZINE_MOD, MagazineMod.class);
		bulletMod = bundle.getEnum(BULLET_MOD, BulletMod.class);
		weightMod = bundle.getEnum(WEIGHT_MOD, WeightMod.class);
		attachMod = bundle.getEnum(ATTACH_MOD, AttachMod.class);
		enchantMod = bundle.getEnum(ENCHANT_MOD, EnchantMod.class);
		inscribeMod = bundle.getEnum(INSCRIBE_MOD, InscribeMod.class);
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero)) {
			actions.add(AC_SHOOT);
			actions.add(AC_RELOAD);
		}
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (action.equals(AC_SHOOT)) {
			return Messages.get(Gun.class, "ac_shoot");
		} else if (action.equals(AC_RELOAD)) {
			return Messages.get(Gun.class, "ac_reload");
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_SHOOT)) {
			if (!isEquipped(hero)) {
				usesTargeting = false;
				GLog.w(Messages.get(Gun.class, "not_equipped"));
			} else if (round <= 0) {
				execute(hero, AC_RELOAD);
			} else {
				usesTargeting = true;
				curUser = hero;
				curItem = this;
				GameScene.selectCell(shooter);
			}
		}

		if (action.equals(AC_RELOAD)) {
			if (isAllLoaded()) {
				GLog.w(Messages.get(Gun.class, "already_loaded"));
			} else {
				reload();
			}
		}
	}

	public boolean isAllLoaded() {
		return round >= maxRound();
	}

	public void reload() {
		onReload();
		quickReload();
		Dungeon.hero.busy();
		Dungeon.hero.sprite.operate(Dungeon.hero.pos);
		Sample.INSTANCE.play("sounds/unlock.mp3");
		Dungeon.hero.spendAndNext(reloadTime(Dungeon.hero));
		GLog.i(Messages.get(Gun.class, "reload"));
	}

	public void onReload() {
		//this fork has no elemental bullets / gunner talents, reload only does the base work
	}

	public void quickReload() {
		round = maxRound();
		updateQuickslot();
	}

	public void manualReload() {
		manualReload(1, false);
	}

	public void manualReload(int amount, boolean overReload) {
		round += amount;
		if (overReload) {
			if (round > maxRound() + amount) round = maxRound() + amount;
		} else if (round > maxRound()) {
			round = maxRound();
		}
		updateQuickslot();
	}

	public boolean isReloaded() {
		return round >= maxRound();
	}

	public int shotPerShoot() {
		return shotPerShoot + inscribeMod.shotBonus();
	}

	public int maxRound() {
		int amount = max_round;
		amount = magazineMod.magazineFactor(amount);
		return Math.max(0, amount);
	}

	public int round() {
		return round;
	}

	public void useRound() {
		round--;
	}

	public float reloadTime(Char user) {
		float amount = reload_time * RELOAD_MULT;
		amount = magazineMod.reloadTimeFactor(amount);
		return Math.max(0f, amount);
	}

	public int bulletUse() {
		return Math.max(0, (maxRound() - round) * shotPerShoot());
	}

	@Override
	public int STRReq(int lvl) {
		int req = STRReq(tier, lvl);
		if (masteryPotionBonus) req -= 2;
		return req;
	}

	@Override
	public int max(int lvl) {
		return 3 * (tier + 1) + lvl * (tier + 1);
	}

	//==== shot damage uses the thrown-weapon formula (level + Ring of Sharpshooting), no gun-only bonus ====
	protected static int sharpshootingBonus() {
		return Dungeon.hero != null ? RingOfSharpshooting.levelDamageBonus(Dungeon.hero) : 0;
	}

	public int bulletMin(int lvl) {
		return Math.max(0, tier + lvl);
	}

	public int bulletMin() {
		return bulletMin(buffedLvl() + sharpshootingBonus());
	}

	protected int baseBulletMax(int lvl) {
		return 0;
	}

	public int bulletMax(int lvl) {
		return Math.max(0, baseBulletMax(lvl));
	}

	public int bulletMax() {
		return bulletMax(buffedLvl() + sharpshootingBonus());
	}

	protected int bulletDamage() {
		int damage = Random.NormalIntRange(bulletMin(), bulletMax());
		damage = augment.damageFactor(damage);
		return damage;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (attachMod == AttachMod.FLASH_ATTACH
				&& Random.Int(10) > 5 + Dungeon.level.distance(attacker.pos, defender.pos) - 1) {
			Buff.prolong(defender, Blindness.class, 2f);
		}
		return super.proc(attacker, defender, damage);
	}

	@Override
	public String info() {
		String info = super.info();
		if (levelKnown) {
			info += "\n\n" + Messages.get(Gun.class, "gun_desc",
					shotPerShoot(),
					augment.damageFactor(bulletMin()),
					augment.damageFactor(bulletMax()),
					round, maxRound(),
					new DecimalFormat("#.##").format(reloadTime(Dungeon.hero)),
					bulletUse());
		} else {
			info += "\n\n" + Messages.get(Gun.class, "gun_typical_desc",
					shotPerShoot(),
					augment.damageFactor(bulletMin(0)),
					augment.damageFactor(bulletMax(0)),
					round, maxRound(),
					new DecimalFormat("#.##").format(reloadTime(Dungeon.hero)),
					bulletUse());
		}

		ArrayList<String> mods = new ArrayList<>();
		if (barrelMod != BarrelMod.NORMAL_BARREL) mods.add(Messages.get(Gun.class, barrelMod.name().toLowerCase(Locale.ENGLISH)));
		if (magazineMod != MagazineMod.NORMAL_MAGAZINE) mods.add(Messages.get(Gun.class, magazineMod.name().toLowerCase(Locale.ENGLISH)));
		if (bulletMod != BulletMod.NORMAL_BULLET) mods.add(Messages.get(Gun.class, bulletMod.name().toLowerCase(Locale.ENGLISH)));
		if (weightMod != WeightMod.NORMAL_WEIGHT) mods.add(Messages.get(Gun.class, weightMod.name().toLowerCase(Locale.ENGLISH)));
		if (attachMod != AttachMod.NORMAL_ATTACH) mods.add(Messages.get(Gun.class, attachMod.name().toLowerCase(Locale.ENGLISH)));
		if (enchantMod != EnchantMod.NORMAL_ENCHANT) mods.add(Messages.get(Gun.class, enchantMod.name().toLowerCase(Locale.ENGLISH)));
		if (inscribeMod != InscribeMod.NORMAL) mods.add(Messages.get(Gun.class, inscribeMod.name().toLowerCase(Locale.ENGLISH)));
		if (!mods.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mods.size(); i++) {
				if (i > 0) sb.append(", ");
				sb.append(mods.get(i));
			}
			info += "\n\n" + Messages.get(Gun.class, "modded", sb.toString());
		}
		return info;
	}

	@Override
	public String status() {
		return Messages.format(TXT_STATUS, round, maxRound());
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return knockBullet().targetingPos(user, dst);
	}

	public Bullet knockBullet() {
		return new Bullet();
	}

	public static enum BarrelMod {
		NORMAL_BARREL(1.0f, 1.0f),
		SHORT_BARREL(1.5f, 0.5f),
		LONG_BARREL(0.75f, 1.25f);

		private final float meleeAccFactor;
		private final float rangedAccFactor;

		BarrelMod(float meleeMulti, float rangedMulti) {
			meleeAccFactor = meleeMulti;
			rangedAccFactor = rangedMulti;
		}

		public float bulletAccuracyFactor(float accuracy, boolean adjacent) {
			return adjacent ? accuracy * meleeAccFactor : accuracy * rangedAccFactor;
		}
	}

	public static enum MagazineMod {
		NORMAL_MAGAZINE(1.0f, 0),
		LARGE_MAGAZINE(1.5f, 1),
		QUICK_MAGAZINE(0.5f, -1);

		private final float magazineFactor;
		private final int reloadTimeAdd;

		MagazineMod(float magMulti, int reloadAdd) {
			magazineFactor = magMulti;
			reloadTimeAdd = reloadAdd;
		}

		public int magazineFactor(int magazine) {
			return (int)Math.floor(magazine * this.magazineFactor);
		}

		public float reloadTimeFactor(float time) {
			return time + reloadTimeAdd;
		}
	}

	public static enum BulletMod {
		NORMAL_BULLET(1.0f, 1.0f),
		AP_BULLET(0.0f, 0.8f),
		HP_BULLET(2.0f, 1.3f);

		private final float armorMulti;
		private final float dmgMulti;

		BulletMod(float armorMulti, float dmgMulti) {
			this.armorMulti = armorMulti;
			this.dmgMulti = dmgMulti;
		}

		public float armorFactor() {
			return armorMulti;
		}

		public int damageFactor(int damage) {
			return Math.round(damage * dmgMulti);
		}
	}

	public static enum WeightMod {
		NORMAL_WEIGHT,
		LIGHT_WEIGHT,
		HEAVY_WEIGHT;
	}

	public static enum AttachMod {
		NORMAL_ATTACH,
		LASER_ATTACH,
		FLASH_ATTACH;
	}

	public static enum EnchantMod {
		NORMAL_ENCHANT(1.0f, 1.0f),
		AMP_ENCHANT(2.0f, 0.75f),
		SUP_ENCHANT(0.5f, 1.25f);

		private final float enchantMulti;
		private final float dmgMulti;

		EnchantMod(float enchantMulti, float dmgMulti) {
			this.enchantMulti = enchantMulti;
			this.dmgMulti = dmgMulti;
		}

		public float enchantFactor() {
			return enchantMulti;
		}

		public int damageFactor(int damage) {
			return Math.round(damage * dmgMulti);
		}
	}

	public static enum InscribeMod {
		NORMAL(0),
		INSCRIBED(1);

		private final int shotBonus;

		InscribeMod(int shotBonus) {
			this.shotBonus = shotBonus;
		}

		public int shotBonus() {
			return shotBonus;
		}
	}

	public class Bullet extends MissileWeapon {

		public boolean isBurst;

		public Bullet() {
			hitSound = "sounds/puff.mp3";
			tier = Gun.this.tier;
			levelKnown = true;
			isBurst = false;
		}

		@Override
		public int min() {
			return Gun.this.bulletMin();
		}

		@Override
		public int min(int lvl) {
			return Gun.this.bulletMin(lvl);
		}

		@Override
		public int max() {
			return Gun.this.bulletMax();
		}

		@Override
		public int max(int lvl) {
			return Gun.this.bulletMax(lvl);
		}

		public BulletMod whatBullet() {
			return Gun.this.bulletMod;
		}

		public EnchantMod whatEnchant() {
			return Gun.this.enchantMod;
		}

		@Override
		public boolean isIdentified() {
			return true;
		}

		@Override
		public int proc(Char attacker, Char defender, int damage) {
			damage = whatBullet().damageFactor(damage);
			damage = whatEnchant().damageFactor(damage);
			if (Gun.this.spread) {
				int distance = Math.max(0, Dungeon.level.distance(attacker.pos, defender.pos) - 1);
				damage = Math.round(damage * (float)Math.pow(0.9f, distance));
			}
			return super.proc(attacker, defender, damage);
		}

		@Override
		public int buffedLvl() {
			return Gun.this.buffedLvl();
		}

		@Override
		public boolean hasEnchant(Class<? extends Weapon.Enchantment> type, Char owner) {
			return Gun.this.hasEnchant(type, owner);
		}

		@Override
		public float delayFactor(Char user) {
			return Gun.this.delayFactor(user) * Gun.this.shootingSpeed;
		}

		@Override
		public float accuracyFactor(Char owner, Char target) {
			float acc = super.accuracyFactor(owner, target);
			acc *= Gun.this.shootingAccuracy;
			if (Gun.this.attachMod == AttachMod.LASER_ATTACH) acc *= 1.25f;
			return Gun.this.barrelMod.bulletAccuracyFactor(acc, Dungeon.level.adjacent(owner.pos, target.pos));
		}

		@Override
		protected float adjacentAccFactor(Char owner, Char target) {
			return Gun.this.adjacentShootingAccuracy;
		}

		@Override
		public int STRReq(int lvl) {
			return Gun.this.STRReq();
		}

		@Override
		protected void onThrow(int cell) {
			if (Gun.this.explode) {
				ArrayList<Char> targets = new ArrayList<>();
				for (int i : PathFinder.NEIGHBOURS9) {
					int c = cell + i;
					if (c < 0 || c >= Dungeon.level.length()) continue;
					if (Dungeon.level.heroFOV[c]) {
						CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
						CellEmitter.center(cell).burst(BlastParticle.FACTORY, 4);
					}
					if (Dungeon.level.flamable[c]) {
						Dungeon.level.destroy(c);
						GameScene.updateMap(c);
					}
					Char ch = Actor.findChar(c);
					if (ch == null || targets.contains(ch)) continue;
					targets.add(ch);
				}
				for (Char target : targets) {
					for (int i = 0; i < Gun.this.shotPerShoot(); i++) {
						curUser.shoot(target, this);
					}
					if (target == curUser && !target.isAlive()) {
						Dungeon.fail(getClass());
						Badges.validateDeathFromFriendlyMagic();
						GLog.n(Messages.get(Gun.class, "ondeath"));
					}
				}
				Sample.INSTANCE.play("sounds/blast.mp3");
			} else {
				Char enemy = Actor.findChar(cell);
				for (int i = 0; i < Gun.this.shotPerShoot(); i++) {
					if (enemy == null || enemy == curUser) {
						parent = null;
						CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
						CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
						continue;
					}
					if (!curUser.shoot(enemy, this)) {
						CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
						CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
					}
				}
			}
			onShoot();
		}

		public void onShoot() {
			if (Gun.this.round > 0) Gun.this.round--;
			aggro();
			updateQuickslot();
		}

		private void aggro() {
			for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
				if (mob.paralysed > 0
						|| Dungeon.level.distance(curUser.pos, mob.pos) > 4
						|| mob.state == mob.HUNTING) continue;
				mob.beckon(curUser.pos);
			}
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play("sounds/hit_crush.mp3", 1f, Random.Float(0.33f, 0.66f));
		}
	}
}
