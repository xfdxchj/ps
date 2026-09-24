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

package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StormCloud;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArcaneArmor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barkskin;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FireImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FrostImbue;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Fury;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LifeLink;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Speed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Challenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.AuraOfProtection;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.BeamingRay;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.LifeLinkSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ShieldOfLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalSpire;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.PrismaticImage;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Bulk;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Swiftness;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfPsionicBlast;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.FerretTuft;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Blazing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Grim;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Shocking;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sickle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.ShockingDart;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GnollRockfallTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.TargetHealthIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

public abstract class Char extends Actor {
	
	public int pos = 0;
	
	public CharSprite sprite;
	
	public int HT;
	public int HP;
	
	protected float baseSpeed	= 1;
	protected PathFinder.Path path;

	public int paralysed	    = 0;
	public boolean rooted		= false;
	public boolean flying		= false;
	public int invisible		= 0;

	//these are relative to the hero
	public enum Alignment{
		ENEMY,
		NEUTRAL,
		ALLY
	}
	public Alignment alignment;
	
	public int viewDistance	= 8;
	
	public boolean[] fieldOfView = null;
	
	private LinkedHashSet<Buff> buffs = new LinkedHashSet<>();
	
	@Override
	protected boolean act() {
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );

		//throw any items that are on top of an immovable char
		if (properties().contains(Property.IMMOVABLE)){
			throwItems();
		}
		return false;
	}

	protected void throwItems(){
		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null && heap.type == Heap.Type.HEAP
				&& !(heap.peek() instanceof Tengu.BombAbility.BombItem)
				&& !(heap.peek() instanceof Tengu.ShockerAbility.ShockerItem)) {
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int n : PathFinder.NEIGHBOURS8){
				if (Dungeon.level.passable[pos+n]){
					candidates.add(pos+n);
				}
			}
			if (!candidates.isEmpty()){
				Dungeon.level.drop( heap.pickUp(), Random.element(candidates) ).sprite.drop( pos );
			}
		}
	}

	public String name(){
		//==== END(挑战 63 鼠鼠可爱): 怪物名字全部显示为"小鼠" ====
		//文档所有者说明："怪物贴图、**文本**、近战后 UI 显示都变成小鼠"。
		//
		//放在 Char 基类的 name() 里 —— 那是**所有**显示名字的路径
		//（点击窗口、击杀提示、血条、图鉴）的唯一收口点，
		//改一处即全覆盖，不必逐个窗口去改。
		//
		//只对怪物生效（玩家自己的名字不该变成小鼠）。
		int[] ratName = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.cuteRatName(this);
		if (ratName != null) {
			return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
					com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat.class,
					"name");
		}
		return Messages.get(this, "name");
	}

	public boolean canInteract(Char c){
		if (Dungeon.level.adjacent( pos, c.pos )){
			return true;
		} else if (c instanceof Hero
				&& alignment == Alignment.ALLY
				&& !hasProp(this, Property.IMMOVABLE)
				&& Dungeon.level.distance(pos, c.pos) <= 2*Dungeon.hero.pointsInTalent(Talent.ALLY_WARP)){
			return true;
		} else {
			return false;
		}
	}
	
	//swaps places by default
	public boolean interact(Char c){

		//don't allow char to swap onto hazard unless they're flying
		//you can swap onto a hazard though, as you're not the one instigating the swap
		if (!Dungeon.level.passable[pos] && !c.flying){
			return true;
		}

		//can't swap into a space without room
		if (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[c.pos]
			|| c.properties().contains(Property.LARGE) && !Dungeon.level.openSpace[pos]){
			return true;
		}

		//we do a little raw position shuffling here so that the characters are never
		// on the same cell when logic such as occupyCell() is triggered
		int oldPos = pos;
		int newPos = c.pos;

		//can't swap or ally warp if either char is immovable
		if (hasProp(this, Property.IMMOVABLE) || hasProp(c, Property.IMMOVABLE)){
			return true;
		}

		//warp instantly with allies in this case
		if (c == Dungeon.hero && Dungeon.hero.hasTalent(Talent.ALLY_WARP)){
			PathFinder.buildDistanceMap(c.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
			if (PathFinder.distance[pos] == Integer.MAX_VALUE){
				return true;
			}
			pos = newPos;
			c.pos = oldPos;
			ScrollOfTeleportation.appear(this, newPos);
			ScrollOfTeleportation.appear(c, oldPos);
			Dungeon.observe();
			GameScene.updateFog();
			return true;
		}

		//can't swap places if one char has restricted movement
		if (paralysed > 0 || c.paralysed > 0 || rooted || c.rooted
				|| buff(Vertigo.class) != null || c.buff(Vertigo.class) != null){
			return true;
		}

		c.pos = oldPos;
		moveSprite( oldPos, newPos );
		move( newPos );

		c.pos = newPos;
		c.sprite.move( newPos, oldPos );
		c.move( oldPos );
		
		c.spend( 1 / c.speed() );

		if (c == Dungeon.hero){
			if (Dungeon.hero.subClass == HeroSubClass.FREERUNNER){
				Buff.affect(Dungeon.hero, Momentum.class).gainStack();
			}

			Dungeon.hero.busy();
		}
		
		return true;
	}
	
	protected boolean moveSprite( int from, int to ) {
		
		if (sprite.isVisible() && sprite.parent != null && (Dungeon.level.heroFOV[from] || Dungeon.level.heroFOV[to])) {
			sprite.move( from, to );
			return true;
		} else {
			sprite.turnTo(from, to);
			sprite.place( to );
			return true;
		}
	}

	public void hitSound( float pitch ){
		Sample.INSTANCE.play(Assets.Sounds.HIT, 1, pitch);
	}

	public boolean blockSound( float pitch ) {
		return false;
	}
	
	protected static final String POS       = "pos";
	protected static final String TAG_HP    = "HP";
	protected static final String TAG_HT    = "HT";
	protected static final String TAG_SHLD  = "SHLD";
	protected static final String BUFFS	    = "buffs";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );
		
		bundle.put( POS, pos );
		bundle.put( TAG_HP, HP );
		bundle.put( TAG_HT, HT );
		bundle.put( BUFFS, buffs );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );
		
		pos = bundle.getInt( POS );
		HP = bundle.getInt( TAG_HP );
		HT = bundle.getInt( TAG_HT );
		
		for (Bundlable b : bundle.getCollection( BUFFS )) {
			if (b != null) {
				((Buff)b).attachTo( this );
			}
		}
	}

	final public boolean attack( Char enemy ){
		//==== END(诊断·突然无法近战) ====
		//文档所有者反馈："有的时候突然就无法近战打怪了。"
		//
		//把**每一次近战尝试**打出来。日志能区分几种情况：
		//  · 完全没有这条日志        → 点击根本没走到 attack（UI/输入层问题）
		//  · 有日志、但 ok=false     → 命中判定失败（距离/看不见/闪避）
		//  · 有日志、enemy=null      → 目标丢失
		//
		//用 Dbg.COMBAT 分类，可以和"伤害结算"的日志对着看。
		com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.log(
				com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.COMBAT,
				"近战尝试 " + getClass().getSimpleName()
						+ (enemy == null ? " → null（目标丢失！）"
								: (" → " + enemy.getClass().getSimpleName()
										+ "  位置=" + pos + "→" + enemy.pos
										+ "  距离=" + (com.shatteredpixel.shatteredpixeldungeon
												.Dungeon.level == null ? -1
												: com.shatteredpixel.shatteredpixeldungeon
														.Dungeon.level.distance(pos, enemy.pos))
										+ "  存活=" + enemy.isAlive())));

		return attack(enemy, 1f, 0f, 1f);
	}

	/**
	 * END(136/133 多段武器): 防止多段攻击递归重入的标记。
	 *
	 * <p>多段攻击的实现是"调 N 次 attack()"，而每次 attack() 都会再走一遍
	 * 本方法开头的多段判定 —— 那会无限递归。所以第一段进入时打标记，
	 * 后续几段直接跳过判定。
	 */
	private boolean multiHitInProgress = false;

	/**
	 * END(136/133): 本次攻击要打几段、每段伤害倍率是多少、是否必中。
	 *
	 * <p>返回 {@code null} 表示"不需要多段"（普通武器）。
	 *
	 * <h3>为什么要问"装备的武器"，而不是让武器覆写本方法</h3>
	 * 本方法在 {@code Char} 上，而武器类（{@code MeleeWeapon} 的子类）
	 * **不是 Char 的子类** —— 它没法覆写这里。
	 *
	 * <p>所以方向反过来：由 {@code Char} **主动去问**当前装备的武器
	 * "你这次要打几段"。判定条件是"武器实现了多段接口"。
	 */
	protected int[] multiHitProfile(Char enemy) {
		//看当前武器是否声明了多段配置
		com.shatteredpixel.shatteredpixeldungeon.items.Item weapon = null;
		if (this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) {
			weapon = ((com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) this)
					.belongings.attackingWeapon();
		}
		if (weapon instanceof MultiHitWeapon) {
			return ((MultiHitWeapon) weapon).multiHitProfile(this, enemy);
		}
		return null;
	}

	/**
	 * END(136/133): 多段武器接口。
	 *
	 * <p>武器实现它来描述"我这次打几段"。返回值含义：
	 * <pre>
	 *   [0] = 段数（≥1）
	 *   [1] = 每段伤害倍率 × 100
	 *   [2] = 命中倍率 × 100（缺省 100 = 不变；给很大的值即为"必定命中"）
	 * </pre>
	 *
	 * <p>返回 {@code null} 表示本次不需要多段。
	 */
	public interface MultiHitWeapon {
		int[] multiHitProfile(Char attacker, Char enemy);

		/** 多段全部打完后调用（用于推进循环状态）。 */
		void onMultiHitFinished(Char attacker, Char enemy);
	}

	/**
	 * END(136/133): 多段攻击结束后调用一次（推进武器自己的循环状态）。
	 */
	protected void onMultiHitFinished(Char enemy) {
		com.shatteredpixel.shatteredpixeldungeon.items.Item weapon = null;
		if (this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) {
			weapon = ((com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) this)
					.belongings.attackingWeapon();
		}
		if (weapon instanceof MultiHitWeapon) {
			((MultiHitWeapon) weapon).onMultiHitFinished(this, enemy);
		}
	}

	/**
	 * END(136 勇剑): 本次是否需要推进"单段循环"。
	 *
	 * <p>勇剑第 2 段只有 1 下（不走多段展开），但仍要推进三段循环。
	 * 那种情况武器会在 {@code multiHitProfile()} 里设一个待推进标记，
	 * 基类打完后调本方法取走。
	 *
	 * <p>默认返回 false —— 其它武器不需要。
	 */
	protected boolean consumePendingAdvance() {
		return false;
	}
	
	public boolean attack( Char enemy, float dmgMulti, float dmgBonus, float accMulti ) {

		if (enemy == null) return false;

		//==== END(136/133 多段武器): 在这里展开多段攻击 ====
		//文档所有者实测："多段武器好像没有多段" ——
		//根因：勇剑/神天使双剑的状态机写了，但**没有任何地方驱动它**。
		//
		//修法：在攻击流程的收口点（本方法）检测武器的多段配置，
		//命中后按倍率**重复调用自己**若干次。
		//
		//为什么用 multiHitInProgress 防重入：
		//递归调用会再次走到这里，没有标记就会无限递归。
		if (!multiHitInProgress) {
			int[] profile = multiHitProfile(enemy);
			//只要武器**显式声明**了多段配置（哪怕只有 1 段）就展开 ——
			//勇剑第 2 段正是"1 段但必中且要推进循环"。
			if (profile != null && profile.length >= 2 && profile[0] >= 1) {
				multiHitInProgress = true;
				try {
					int hits = profile[0];
					float mult = profile[1] / 100f;
					float acc = profile.length >= 3 ? profile[2] / 100f : 1f;

					boolean anyHit = false;
					for (int i = 0; i < hits; i++) {
						if (!enemy.isAlive()) break;      //目标死了就停
						boolean r = attack(enemy, mult, dmgBonus, acc);
						anyHit = anyHit || r;
					}
					if (anyHit) onMultiHitFinished(enemy);
					return anyHit;
				} finally {
					multiHitInProgress = false;
				}
			}
		}

		//==== END(136 勇剑): "单段但要多段框架"的情况 ====
		//勇剑第 2 段是"1 次必中"，不需要循环打 N 下，
		//但它**仍要推进三段循环**。那种情况由武器返回 {1, 100, 大命中}，
		//这里识别到"段数 = 1 但被显式声明"就照常展开（打 1 下 + 推进）。
		if (!multiHitInProgress) {
			multiHitInProgress = true;
			try {
				//真正的一次攻击（原逻辑）
				boolean r = attackOnce(enemy, dmgMulti, dmgBonus, accMulti);
				//多段武器的收尾：走 onMultiHitFinished；
				//单段但需要推进的（勇剑第 2 段）走 consumePendingAdvance。
				onMultiHitFinished(enemy);
				consumePendingAdvance();
				return r;
			} finally {
				multiHitInProgress = false;
			}
		}

		return attackOnce(enemy, dmgMulti, dmgBonus, accMulti);
	}

	/** END(重构): 单次攻击的实际逻辑（原 attack() 的正文）。 */
	private boolean attackOnce(Char enemy, float dmgMulti, float dmgBonus, float accMulti) {

		//END(重构): visibleFight 原本定义在 attack() 里，
		//拆出 attackOnce() 后必须挪进来 —— 否则这里引用不到（编译报"找不到符号"）。
		boolean visibleFight = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[enemy.pos];

		if (enemy.isInvulnerable(getClass())) {

			if (visibleFight) {
				enemy.sprite.showStatus( CharSprite.POSITIVE, Messages.get(this, "invulnerable") );

				Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1f, Random.Float(0.96f, 1.05f));
			}

			return false;

		} else if (hit( this, enemy, accMulti, false )) {
			
			int dr = Math.round(enemy.drRoll() * AscensionChallenge.statModifier(enemy));

			//==== END(挑战 1 牢地碎破): 护甲覆写 ====
			//drRoll() 同样是方法，子类各自覆写，只能在这里按表覆盖。
			dr = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.crumblingArmor(enemy, dr);

			//==== END(轮回诅咒⑨ 铁鳞): 怪物护甲 +50% ====
			//文档所有者定稿："护甲提升 50%"，主语是怪物。
			//
			//放在这里而不是逐个改 100 个 mob 的 drRoll()：
			//本处是**全游戏唯一的护甲结算点**，改这一处就不会漏。
			//只对怪物生效（玩家有自己的护甲来源，不吃这条诅咒）。
			if (enemy instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
				float reincArmor = com.shatteredpixel.shatteredpixeldungeon.endcontent
						.Reincarnation.mobArmorMultiplier();
				if (reincArmor != 1f) {
					dr = Math.round(dr * reincArmor);
				}
			}
			
			if (this instanceof Hero){
				Hero h = (Hero)this;
				if (h.belongings.attackingWeapon() instanceof MissileWeapon
						&& h.subClass == HeroSubClass.SNIPER
						&& !Dungeon.level.adjacent(h.pos, enemy.pos)){
					dr = 0;
				}

				if (h.buff(MonkEnergy.MonkAbility.UnarmedAbilityTracker.class) != null){
					dr = 0;
				}

				//==== END(顶级装备·天堂陨落长弓): 无视护甲 ====
				//文档所有者定稿："无视护甲"。
				//
				//照上面"狙击手无视护甲"的既有写法 —— 直接把这个目标的
				//护甲值清零，后面的伤害计算自然就不减了。
				if (com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons
						.HeavenFallBow.isIgnoringArmor()) {
					dr = 0;
				}
			}

			//we use a float here briefly so that we don't have to constantly round while
			// potentially applying various multiplier effects
			float dmg;
			Preparation prep = buff(Preparation.class);
			if (prep != null){
				dmg = prep.damageRoll(this);
				if (this == Dungeon.hero && Dungeon.hero.hasTalent(Talent.BOUNTY_HUNTER)) {
					Buff.affect(Dungeon.hero, Talent.BountyHunterTracker.class, 0.0f);
				}
			} else {
				dmg = damageRoll();
			}

			//==== END(挑战 1 牢地碎破): 伤害覆写 ====
			//damageRoll() 是方法，各 mob 子类各自覆写，只能在这里按表覆盖。
			//放在"拿到基础伤害之后、所有倍率之前" —— 与 140 枪枪爆头同一位置，
			//两者互斥（140 只有玩家、1 只有怪物），不会互相干扰。
			dmg = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.crumblingDamage(this, dmg);

			//==== END(挑战 140 枪枪爆头): 距离 >=5 时远程伤害必为最大值 ====
			//放在拿到基础伤害之后、所有倍率之前 ——
			//"必定最大值"改的是基础掷骰结果，后续增益照常作用。
			//判据在 ChallengeEffects.isHeadshot 里（只对玩家、距离 >=5 格）。
			if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.isHeadshot(this, enemy)) {
				com.shatteredpixel.shatteredpixeldungeon.items.Item w =
						(this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)
								? ((com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) this)
										.belongings.attackingWeapon()
								: null;
				if (w instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon
						.missiles.MissileWeapon) {
					dmg = ((com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles
							.MissileWeapon) w).max();
				}
			}

			dmg = dmg*dmgMulti;

			//flat damage bonus is affected by multipliers
			dmg += dmgBonus;

			//==== END(挑战 158 神圣之力): 对恶魔类目标额外伤害 ====
			//与 153 恶魔地牢联动：勾了 153 之后所有怪都带 DEMONIC 标记，
			//本条的收益因此最大化。
			//未勾选 158 或目标不是恶魔时返回 1.0，等价于原版。
			dmg *= com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.holyPowerDamageMultiplier(enemy);

			//==== END(挑战 134 黄金蜂蜜酒): 发狂时攻击 +50% ====
			//未处于发狂状态时返回 1.0，等价于原版。
			dmg *= com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.GoldenMead.madnessAttackMultiplier(this);

			//==== END(挑战 124 野生狗奶): 全属性 ×0.25 ====
			//原表："使用后全属性降低 75%"
			//只作用于**伤害输出**这一处；命中/闪避的降低见 Char.hit()。
			if (com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.WildDogMilk.isActive(this)) {
				dmg *= com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
						.WildDogMilk.STAT_MULT;
			}

			//==== END(无尽戒): 伤害 +20% ====
			//佩戴轮回噬灭之戒时，所有伤害提升 20%。
			//只对佩戴者生效（mob 不会携带戒指），未佩戴返回 1.0。
			dmg *= com.shatteredpixel.shatteredpixeldungeon.endcontent.items
					.ReincarnationRing.damageMultiplier(this);

			//==== END(挑战 20 等我启动): 对同一目标的连击递增 ====
			//原表："对同一目标伤害：第一次 20%，第二次 50%，第三次及以后 110%"
			//
			//计数记在**攻击方**身上、以目标为键：换目标就重新从第一次算。
			//未勾选 20 时原样返回 dmg，等价于原版。
			dmg = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.applyMomentumDamage(this, enemy, dmg);

			//==== END(挑战·伤害管线 第2步): 攻击方增益，**加法叠加** ====
			//玻璃大炮 +20% / 破釜沉舟 +30% / 极致攻哈 +20% / 狂暴 +20%（怪物侧）
			//注意：必须加法，若各自连乘结果会偏大（×1.872 而非 ×1.70）。
			//放在 dmgBonus 之后、既有乘算增益之前，作为"基础伤害上的加成"。
			float challengeBonus = com.shatteredpixel.shatteredpixeldungeon.endcontent
					.challenge.ChallengeEffects.additiveBonus(this);
			if (challengeBonus != 0f) {
				dmg *= (1f + challengeBonus);
			}

			//==== END(挑战·伤害管线 第3步): 低血倍率，**乘法叠加** ====
			//亡者之怒 ×2（玩家 HP<10%）/ 越战越勇（怪物按已损生命）
			float challengeMult = com.shatteredpixel.shatteredpixeldungeon.endcontent
					.challenge.ChallengeEffects.lowHpMultiplier(this);
			if (challengeMult != 1f) {
				dmg *= challengeMult;
			}

			//==== END(修复·152 和平地牢): 违反合约后怪物伤害 +50% ====
			//文档所有者实测："违反和平后没有buff" ——
			//根因：我写了 peaceBrokenStatMult()，但**没有任何地方调用它**。
			//
			//现在生命 +50% 在挂标记时直接加了（一次性），
			//伤害 +50% 在这里每次结算时乘（伤害是每回合重算的）。
			if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.isPeaceBroken(this)) {
				dmg *= com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.PeaceBrokenMark.DMG_MULT;
			}

			//==== END(挑战 119 怪物浪潮): 怪物输出 ×0.2 ====
			//HP 已在 Level.createMob 里削过了；命中/闪避/伤害是算出来的，
			//只能在这里（伤害计算侧）统一削弱。Boss 不削弱。
			if (!(this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)
					&& this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
				float waveStat = com.shatteredpixel.shatteredpixeldungeon.endcontent
						.challenge.ChallengeEffects.mobStatMultiplier(
								(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) this);
				if (waveStat != 1f) {
					dmg *= waveStat;
				}
			}

			if (enemy.buff(GuidingLight.Illuminated.class) != null){
				enemy.buff(GuidingLight.Illuminated.class).detach();
				if (this == Dungeon.hero && Dungeon.hero.hasTalent(Talent.SEARING_LIGHT)){
					dmg += 1 + 2*Dungeon.hero.pointsInTalent(Talent.SEARING_LIGHT);
				}
				if (this != Dungeon.hero && Dungeon.hero.subClass == HeroSubClass.PRIEST){
					enemy.damage(5+Dungeon.hero.lvl, GuidingLight.INSTANCE);
				}
			}

			Berserk berserk = buff(Berserk.class);
			if (berserk != null) dmg = berserk.damageFactor(dmg);

			if (buff( Fury.class ) != null) {
				dmg *= 1.5f;
			}
			//END 破印·狂暴：攻击伤害 +100%（不影响攻速）
			if (buff( com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EndRageAttack.class ) != null){
				dmg *= 2f;
			}

			if (buff( PowerOfMany.PowerBuff.class) != null){
				if (buff( BeamingRay.BeamingRayBoost.class) != null
					&& buff( BeamingRay.BeamingRayBoost.class).object == enemy.id()){
					dmg *= 1.3f + 0.05f*Dungeon.hero.pointsInTalent(Talent.BEAMING_RAY);
				} else {
					dmg *= 1.25f;
				}
			}

			for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
				//END(挑战 14 精英强化): 精英怪攻击 ×1.2。
				//必须乘在**调用侧** —— ChampionEnemy 的子类（Blazing/Projecting…）
				//都是直接 return 常量、不调 super，改基类方法不会生效。
				dmg *= buff.meleeDamageFactor()
						* com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
								.ChallengeEffects.eliteStatMultiplier();
			}

			dmg *= AscensionChallenge.statModifier(this);

			//friendly endure
			Endure.EndureTracker endure = buff(Endure.EndureTracker.class);
			if (endure != null) dmg = endure.damageFactor(dmg);

			//enemy endure
			endure = enemy.buff(Endure.EndureTracker.class);
			if (endure != null){
				dmg = endure.adjustDamageTaken(dmg);
			}

			if (enemy.buff(ScrollOfChallenge.ChallengeArena.class) != null){
				dmg *= 0.67f;
			}

			if (Dungeon.hero.alignment == enemy.alignment
					&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
					&& (Dungeon.level.distance(enemy.pos, Dungeon.hero.pos) <= 2 || enemy.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)){
				dmg *= 0.9f - 0.1f*Dungeon.hero.pointsInTalent(Talent.AURA_OF_PROTECTION);
			}

			if (enemy.buff(MonkEnergy.MonkAbility.Meditate.MeditateResistance.class) != null){
				dmg *= 0.2f;
			}

			if ( buff(Weakness.class) != null ){
				dmg *= 0.67f;
			}

			//characters influenced by aggression deal 1/2 damage to bosses
			if ( enemy.buff(StoneOfAggression.Aggression.class) != null
					&& enemy.alignment == alignment
					&& (Char.hasProp(enemy, Property.BOSS) || Char.hasProp(enemy, Property.MINIBOSS))){
				dmg *= 0.5f;
				//yog-dzewa specifically takes 1/4 damage
				if (enemy instanceof YogDzewa){
					dmg *= 0.5f;
				}
			}
			
			int effectiveDamage = enemy.defenseProc( this, Math.round(dmg) );
			//do not trigger on-hit logic if defenseProc returned a negative value
			if (effectiveDamage >= 0) {
				effectiveDamage = Math.max(effectiveDamage - dr, 0);

				if (enemy.buff(Viscosity.ViscosityTracker.class) != null) {
					effectiveDamage = enemy.buff(Viscosity.ViscosityTracker.class).deferDamage(effectiveDamage);
					enemy.buff(Viscosity.ViscosityTracker.class).detach();
				}

				//vulnerable specifically applies after armor reductions
				if (enemy.buff(Vulnerable.class) != null) {
					effectiveDamage *= 1.33f;
				}

				effectiveDamage = attackProc(enemy, effectiveDamage);
			}
			if (visibleFight) {
				if (effectiveDamage > 0 || !enemy.blockSound(Random.Float(0.96f, 1.05f))) {
					hitSound(Random.Float(0.87f, 1.15f));
				}
			}

			// If the enemy is already dead, interrupt the attack.
			// This matters as defence procs can sometimes inflict self-damage, such as armor glyphs.
			if (!enemy.isAlive()){
				return true;
			}

			//==== END(移植·复仇狂怒 79): 带狂怒的怪物攻击 ×2 ====
			effectiveDamage = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.revengeFuryAttackDamage(this, effectiveDamage);

			enemy.damage( effectiveDamage, this );

			if (buff(FireImbue.class) != null)  buff(FireImbue.class).proc(enemy);
			if (buff(FrostImbue.class) != null) buff(FrostImbue.class).proc(enemy);

			if (enemy.isAlive() && enemy.alignment != alignment && prep != null && prep.canKO(enemy)){
				enemy.HP = 0;
				if (enemy.buff(Brute.BruteRage.class) != null){
					enemy.buff(Brute.BruteRage.class).detach();
				}
				if (!enemy.isAlive()) {
					enemy.die(this);
				} else {
					//helps with triggering any on-damage effects that need to activate
					enemy.damage(-1, this);
					DeathMark.processFearTheReaper(enemy);
				}
				if (enemy.sprite != null) {
					enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Preparation.class, "assassinated"));
				}
			}

			Talent.CombinedLethalityAbilityTracker combinedLethality = buff(Talent.CombinedLethalityAbilityTracker.class);
			if (combinedLethality != null && this instanceof Hero && ((Hero) this).belongings.attackingWeapon() instanceof MeleeWeapon && combinedLethality.weapon != ((Hero) this).belongings.attackingWeapon()){
				if ( enemy.isAlive() && enemy.alignment != alignment && !Char.hasProp(enemy, Property.BOSS)
						&& !Char.hasProp(enemy, Property.MINIBOSS) &&
						(enemy.HP/(float)enemy.HT) <= 0.4f*((Hero)this).pointsInTalent(Talent.COMBINED_LETHALITY)/3f) {
					enemy.HP = 0;
					if (enemy.buff(Brute.BruteRage.class) != null){
						enemy.buff(Brute.BruteRage.class).detach();
					}
					if (!enemy.isAlive()) {
						enemy.die(this);
					} else {
						//helps with triggering any on-damage effects that need to activate
						enemy.damage(-1, this);
						DeathMark.processFearTheReaper(enemy);
					}
					if (enemy.sprite != null) {
						enemy.sprite.showStatus(CharSprite.NEGATIVE, Messages.get(Talent.CombinedLethalityAbilityTracker.class, "executed"));
					}
				}
				combinedLethality.detach();
			}

			if (enemy.sprite != null) {
				enemy.sprite.bloodBurstA(sprite.center(), effectiveDamage);
				enemy.sprite.flash();
			}

			if (!enemy.isAlive() && visibleFight) {
				if (enemy == Dungeon.hero) {
					
					if (this == Dungeon.hero) {
						return true;
					}

					if (this instanceof WandOfLivingEarth.EarthGuardian
							|| this instanceof MirrorImage || this instanceof PrismaticImage){
						Badges.validateDeathFromFriendlyMagic();
					}
					Dungeon.fail( this );
					GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name())) );
					
				} else if (this == Dungeon.hero) {
					GLog.i( Messages.capitalize(Messages.get(Char.class, "defeat", enemy.name())) );
				}
			}

			//==== END(挑战·战斗触发类): 命中结算 ====
			//顺序很重要：
			//  1) 23 血流成河 —— 给目标挂流血
			//  2) 17 情人节   —— 玩家攻击时概率魅惑目标
			//  3) 87 盗贼鼠群 —— 怪物攻击时偷玩家金币
			//  4) 13 狂热    —— 攻击方（怪物）累加攻速层数
			//  5) 24 以牙还牙 —— 最后做，因为它会发起一次新攻击
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
					.onAttackHitBleed(enemy);
			//==== END(挑战 152 和平地牢): 玩家动手即"违反合约" ====
			//文档所有者说明："直到你违反了和平合约"。
			//玩家只要**命中过任何怪物**就算违反，此后本层怪物恢复攻击性。
			//换层时由 Dungeon.newLevel() 调用 resetPeaceful() 重置。
			if (this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
					&& enemy instanceof com.shatteredpixel.shatteredpixeldungeon.actors
							.mobs.Mob) {
				com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.breakPeace();
			}
			//==== END(挑战 8 混乱): 命中时给被打的一方挂随机 buff ====
			//原表："战斗过程中产生随机 buff" —— 没指定对象，
			//这里选"防守方"，因为"谁挨打谁出状况"最直观，
			//而且玩家与怪物都会中招，符合"双刃剑"的倾向。
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.rollChaosBuff(enemy);
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
					.onHeroAttackCharm(this, enemy);
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
					.onMobStealGold(this, enemy);
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
					.onMobAttackHit(this);
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
					.onHeroAttack(this, enemy);

			//==== END(挑战 127 黑兔戒指): 每回合第一次命中返还一个回合 ====
			//放在最后：其余触发类效果都结算完了再决定"要不要把回合还给玩家"。
			//只对玩家生效；每回合最多一次（计数在 RabbitRing 内部管理）。
			//
			//==== END(修复·黑兔戒指不生效·第二版) ====
			//文档所有者反馈（第二次）："没效果。"
			//
			//**上一版为什么还是没用**：
			//我原来在这里直接 {@code spend(-Actor.TICK)} —— 但攻击的调用方是
			//{@code Hero.onAttackComplete()}：
			//<pre>
			//  boolean hit = attack(attackTarget);   // ← 我在这里 spend(-TICK)
			//  Invisibility.dispel();
			//  spend( attackDelay() );               // ← 紧接着又花掉一整回合
			//</pre>
			//返还的 1 与紧接着花掉的 1 **正好抵消** —— 完全没效果。
			//
			//**正确做法**：不在这里改时间，而是**挂一个标记**，
			//让 {@code Hero.onAttackComplete()} 知道"这一击是免费的"，
			//于是它跳过那次 {@code spend(attackDelay())}。
			//这才是"一回合攻击两次"的真正含义 —— 第二次出手不消耗回合。
			if (this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
					&& com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
							.RabbitRing.shouldRefundTurn(
									(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) this)) {
				com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
						.RabbitRing.markFreeAttack(
								(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero) this);
			}

			return true;
			
		} else {

			if (enemy.sprite != null){
				if (hitMissIcon != -1){
					//dooking is a playful sound Ferrets can make, like low pitched chirping
					// I doubt this will translate, so it's only in English
					if (hitMissIcon == FloatingText.MISS_TUFT && Messages.lang() == Languages.ENGLISH && Random.Int(10) == 0) {
						enemy.sprite.showStatusWithIcon(CharSprite.NEUTRAL, "dooked", hitMissIcon);
					} else {
						enemy.sprite.showStatusWithIcon(CharSprite.NEUTRAL, enemy.defenseVerb(), hitMissIcon);
					}
					hitMissIcon = -1;
				} else {
					enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
				}
			}
			if (visibleFight) {
				//TODO enemy.defenseSound? currently miss plays for monks/crab even when they parry
				Sample.INSTANCE.play(Assets.Sounds.MISS);
			}

			return false;
			
		}

	}

	public static int INFINITE_ACCURACY = 1_000_000;
	public static int INFINITE_EVASION = 1_000_000;

	final public static boolean hit( Char attacker, Char defender, boolean magic ) {
		return hit(attacker, defender, magic ? 2f : 1f, magic);
	}

	public static boolean hit( Char attacker, Char defender, float accMulti, boolean magic ) {
		float acuStat = attacker.attackSkill( defender );
		float defStat = defender.defenseSkill( attacker );

		//==== END(挑战 1 牢地碎破): 命中/闪避覆写 ====
		//attackSkill/defenseSkill 都是**方法**，各 mob 子类各自覆写，
		//改字段没有用。所以在这里（全游戏唯一的命中判定点）统一按表覆写。
		//未勾选 1 时两个方法原样返回，本段等价于不存在。
		acuStat = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.crumblingAccuracy(attacker, acuStat);
		defStat = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.crumblingEvasion(defender, defStat);

		//==== END(挑战 124 野生狗奶): 命中与闪避 ×0.25 ====
		//原表："全属性降低 75%" —— 命中与闪避也算"属性"。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
				.WildDogMilk.isActive(attacker)) {
			acuStat *= com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.WildDogMilk.STAT_MULT;
		}
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
				.WildDogMilk.isActive(defender)) {
			defStat *= com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.WildDogMilk.STAT_MULT;
		}

		//==== END(挑战 143 吾为王者): Boss 命中/闪避 +20% ====
		//放在碎破覆写**之后**：碎破给的是"配置表里的绝对值"，
		//而 143 是在那个值之上再乘 1.2。顺序反过来结果不同。
		//未勾选 143 或目标不是 Boss 时返回 1.0，等价于原版。
		acuStat *= com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.kingStatMultiplier(attacker);
		defStat *= com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.kingStatMultiplier(defender);

		if (defender instanceof Hero && ((Hero) defender).damageInterrupt){
			((Hero) defender).interrupt();
		}

		//invisible chars always hit (for the hero this is surprise attacking)
		if (attacker.invisible > 0 && attacker.canSurpriseAttack()){
			acuStat = INFINITE_ACCURACY;
		}

		if (defender.buff(MonkEnergy.MonkAbility.Focus.FocusBuff.class) != null){
			defStat = INFINITE_EVASION;
		}

		//if accuracy or evasion are large enough, treat them as infinite.
		//note that infinite evasion beats infinite accuracy
		if (defStat >= INFINITE_EVASION){
			hitMissIcon = FloatingText.getMissReasonIcon(attacker, acuStat, defender, INFINITE_EVASION);
			return false;
		} else if (acuStat >= INFINITE_ACCURACY){
			hitMissIcon = FloatingText.getHitReasonIcon(attacker, INFINITE_ACCURACY, defender, defStat);
			return true;
		}

		float acuRoll = Random.Float( acuStat );
		if (attacker.buff(Bless.class) != null) acuRoll *= 1.25f;
		if (attacker.buff(  Hex.class) != null) acuRoll *= 0.8f;
		if (attacker.buff( Daze.class) != null) acuRoll *= 0.5f;
		for (ChampionEnemy buff : attacker.buffs(ChampionEnemy.class)){
			//END(挑战 14): 精英命中 ×1.2（调用侧相乘，子类不调 super）
			acuRoll *= buff.evasionAndAccuracyFactor()
					* com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.eliteStatMultiplier();
		}
		acuRoll *= AscensionChallenge.statModifier(attacker);
		if (Dungeon.hero.heroClass != HeroClass.CLERIC
				&& Dungeon.hero.hasTalent(Talent.BLESS)
				&& attacker.alignment == Alignment.ALLY){
			// + 3%/5%
			acuRoll *= 1.01f + 0.02f*Dungeon.hero.pointsInTalent(Talent.BLESS);
		}
		acuRoll *= accMulti;

		float defRoll = Random.Float( defStat );
		if (defender.buff(Bless.class) != null) defRoll *= 1.25f;
		if (defender.buff(  Hex.class) != null) defRoll *= 0.8f;
		if (defender.buff( Daze.class) != null) defRoll *= 0.5f;
		for (ChampionEnemy buff : defender.buffs(ChampionEnemy.class)){
			//END(挑战 14): 精英闪避 ×1.2（调用侧相乘，子类不调 super）
			defRoll *= buff.evasionAndAccuracyFactor()
					* com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.eliteStatMultiplier();
		}
		defRoll *= AscensionChallenge.statModifier(defender);
		if (Dungeon.hero.heroClass != HeroClass.CLERIC
				&& Dungeon.hero.hasTalent(Talent.BLESS)
				&& defender.alignment == Alignment.ALLY){
			// + 3%/5%
			defRoll *= 1.01f + 0.02f*Dungeon.hero.pointsInTalent(Talent.BLESS);
		}
		defRoll *= FerretTuft.evasionMultiplier();

		if (acuRoll >= defRoll){
			hitMissIcon = FloatingText.getHitReasonIcon(attacker, acuRoll, defender, defRoll);
			return true;
		} else {
			hitMissIcon = FloatingText.getMissReasonIcon(attacker, acuRoll, defender, defRoll);
			return false;
		}
	}

	private static int hitMissIcon = -1;

	public int attackSkill( Char target ) {
		return 0;
	}
	
	public int defenseSkill( Char enemy ) {
		return 0;
	}
	
	public String defenseVerb() {
		return Messages.get(this, "def_verb");
	}
	
	public int drRoll() {
		int dr = 0;

		dr += Random.NormalIntRange( 0 , Barkskin.currentLevel(this) );

		return dr;
	}
	
	public int damageRoll() {
		return 1;
	}
	
	//TODO it would be nice to have a pre-armor and post-armor proc.
	// atm attack is always post-armor and defence is already pre-armor
	
	public int attackProc( Char enemy, int damage ) {
		for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
			buff.onAttackProc( enemy );
		}
		return damage;
	}
	
	public int defenseProc( Char enemy, int damage ) {

		Earthroot.Armor armor = buff( Earthroot.Armor.class );
		if (armor != null) {
			damage = armor.absorb( damage );
		}

		ShieldOfLight.ShieldOfLightTracker shield = buff( ShieldOfLight.ShieldOfLightTracker.class);
		if (shield != null && shield.object == enemy.id()){
			int min = 1 + Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT);
			damage -= Random.NormalIntRange(min, 2*min);
			damage = Math.max(damage, 0);
		} else if (this == Dungeon.hero
				&& Dungeon.hero.heroClass != HeroClass.CLERIC
				&& Dungeon.hero.hasTalent(Talent.SHIELD_OF_LIGHT)
				&& TargetHealthIndicator.instance.target() == enemy){
			//33/50%
			if (Random.Int(6) < 1+Dungeon.hero.pointsInTalent(Talent.SHIELD_OF_LIGHT)){
				damage -= 1;
			}
		}

		// hero and pris images skip this as they already benefit from hero's armor glyph proc
		if (!(this instanceof Hero || this instanceof PrismaticImage)) {
			if (Dungeon.hero.alignment == alignment && Dungeon.hero.belongings.armor() != null
					&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
					&& (Dungeon.level.distance(pos, Dungeon.hero.pos) <= 2 || buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)) {
				damage = Dungeon.hero.belongings.armor().proc( enemy, this, damage );
			}
		}

		return damage;
	}

	//Returns the level a glyph is at for a char, or -1 if they are not benefitting from that glyph
	//This function is needed as (unlike enchantments) many glyphs trigger in a variety of cases
	public int glyphLevel(Class<? extends Armor.Glyph> cls){
		if (Dungeon.hero != null && Dungeon.level != null
				&& this != Dungeon.hero && Dungeon.hero.alignment == alignment
				&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
				&& (Dungeon.level.distance(pos, Dungeon.hero.pos) <= 2 || buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)) {
			return Dungeon.hero.glyphLevel(cls);
		} else {
			return -1;
		}
	}
	
	public float speed() {
		float speed = baseSpeed;
		if ( buff( Cripple.class ) != null ) speed /= 2f;
		if ( buff( Stamina.class ) != null) speed *= 1.5f;
		if ( buff( Adrenaline.class ) != null) speed *= 2f;
		if ( buff( Haste.class ) != null) speed *= 3f;
		if ( buff( Dread.class ) != null) speed *= 2f;

		speed *= Swiftness.speedBoost(this, glyphLevel(Swiftness.class));
		speed *= Flow.speedBoost(this, glyphLevel(Flow.class));
		speed *= Bulk.speedBoost(this, glyphLevel(Bulk.class));

		//==== END(挑战): 速度类规则 ====
		//破釜沉舟（玩家 HP<30% 时攻速 ×1.2）。
		//放在**基类**里，Hero/Mob 覆写 speed() 时都会经 super.speed() 走到这里。
		speed *= com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.speedModifier(this);

		//==== END(挑战 29 雇佣童工): 童工移速 ×2 ====
		//用独立标记而不是挂 Haste：Haste 的语义是"暂时的加速"，
		//而且会与玩家自己施放的加速混淆（读界面时看不出是规则还是自己上的）。
		if (buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
				.ChildLaborMark.class) != null) {
			speed *= com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.childLaborSpeedMultiplier();
		}

		return speed;
	}

	//currently only used by invisible chars, or by the hero
	public boolean canSurpriseAttack(){
		return true;
	}
	
	//used so that buffs(Shieldbuff.class) isn't called every time unnecessarily
	private int cachedShield = 0;
	public boolean needsShieldUpdate = true;
	
	public int shielding(){
		if (!needsShieldUpdate){
			return cachedShield;
		}
		
		cachedShield = 0;
		for (ShieldBuff s : buffs(ShieldBuff.class)){
			cachedShield += s.shielding();
		}
		needsShieldUpdate = false;
		return cachedShield;
	}
	
	public void damage( int dmg, Object src ) {
		
		if (!isAlive() || dmg < 0) {
			return;
		}

		if(isInvulnerable(src.getClass())){
			sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
			return;
		}

		if (!(src instanceof LifeLink || src instanceof Hunger) && buff(LifeLink.class) != null){
			HashSet<LifeLink> links = buffs(LifeLink.class);
			for (LifeLink link : links.toArray(new LifeLink[0])){
				if (Actor.findById(link.object) == null){
					links.remove(link);
					link.detach();
				}
			}
			dmg = (int)Math.ceil(dmg / (float)(links.size()+1));
			for (LifeLink link : links){
				Char ch = (Char)Actor.findById(link.object);
				if (ch != null) {
					ch.damage(dmg, link);
					if (!ch.isAlive()) {
						link.detach();
						if (ch == Dungeon.hero){
							Badges.validateDeathFromFriendlyMagic();
							Dungeon.fail(src);
							GLog.n( Messages.get(LifeLink.class, "ondeath") );
						}
					}
				}
			}
		}

		//temporarily assign to a float to avoid rounding a bunch
		float damage = dmg;

		//if dmg is from a character we already reduced it in Char.attack
		if (!(src instanceof Char)) {
			if (Dungeon.hero.alignment == alignment
					&& Dungeon.hero.buff(AuraOfProtection.AuraBuff.class) != null
					&& (Dungeon.level.distance(pos, Dungeon.hero.pos) <= 2 || buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null)) {
				damage *= 0.9f - 0.1f*Dungeon.hero.pointsInTalent(Talent.AURA_OF_PROTECTION);
			}
		}

		if (buff(PowerOfMany.PowerBuff.class) != null){
			if (buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
				damage *= 0.70f - 0.05f*Dungeon.hero.pointsInTalent(Talent.LIFE_LINK);
			} else {
				damage *= 0.75f;
			}
		}

		Terror t = buff(Terror.class);
		if (t != null){
			t.recover();
		}
		Dread d = buff(Dread.class);
		if (d != null){
			d.recover();
		}
		Charm c = buff(Charm.class);
		if (c != null){
			c.recover(src);
		}
		if (this.buff(Frost.class) != null){
			Buff.detach( this, Frost.class );
		}
		if (this.buff(MagicalSleep.class) != null){
			Buff.detach(this, MagicalSleep.class);
		}
		if (this.buff(Doom.class) != null && !isImmune(Doom.class)){
			damage *= 1.67f;
		}
		if (alignment != Alignment.ALLY && this.buff(DeathMark.DeathMarkTracker.class) != null){
			damage *= 1.25f;
		}

		if (buff(Sickle.HarvestBleedTracker.class) != null){
			buff(Sickle.HarvestBleedTracker.class).detach();

			if (!isImmune(Bleeding.class)){
				Bleeding b = buff(Bleeding.class);
				if (b == null){
					b = new Bleeding();
				}
				b.announced = false;
				b.set(dmg, Sickle.HarvestBleedTracker.class);
				b.attachTo(this);
				sprite.showStatus(CharSprite.WARNING, Messages.titleCase(b.name()) + " " + (int)b.level());
				return;
			}
		}

		Class<?> srcClass = src.getClass();
		if (isImmune( srcClass )) {
			damage = 0;
		} else {
			damage *= resist( srcClass );
		}

		dmg = Math.round(damage);

		//==== END(挑战 194 怪物之王): 怪物获得 20% 免伤 ====
		//文档所有者定稿："选择所有怪物增强类后，怪物获得 20% 免伤。"
		//
		//放在"最终伤害已算出、但还没扣血"的位置 ——
		//这样它与护甲、抗性、精英减伤的结算顺序是"最后一道关卡"。
		//
		//未勾选 194（或目标不是怪物）时返回 false，等价于原版。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.kingDamageReduction(this)) {
			dmg = Math.max(1, Math.round(dmg
					* (1f - com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.KING_DAMAGE_REDUCTION)));
		}

		//==== END(轮回诅咒⑤ 顽抗): 怪物受到的伤害 -10% ====
		//文档所有者定稿："减伤 10%"，主语是怪物。
		//与 194 怪物之王是同一个位置，两者**可叠加**（都是减伤）。
		if (this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
			float reincDr = com.shatteredpixel.shatteredpixeldungeon.endcontent
					.Reincarnation.mobDamageReduction();
			if (reincDr > 0f) {
				dmg = Math.max(1, Math.round(dmg * (1f - reincDr)));
			}
		}

		//==== END(202 为何无泪): 玩家受到的伤害 +33%（集齐全部"为何无X"后 +50%）====
		//与 194 是同一个位置（最终伤害、扣血之前），两者对**不同对象**生效
		//（194 作用于怪物，202 作用于玩家），不会互相干扰。
		dmg = Math.round(dmg * com.shatteredpixel.shatteredpixeldungeon.endcontent
				.challenge.ChallengeEffects.whyDamageTakenMult(this));

		//==== END(顶级装备·虚空不灭之甲): 免伤 + 概率完全免疫 ====
		//文档所有者定稿："免伤 15+升级等级%，最大 50；15+升级等级% 概率免疫伤害"。
		//
		//放在这里（最终伤害、扣血之前）—— 与 194/202 是同一个"最后一道关卡"，
		//顺序上排在挑战之后，所以虚空甲是**最后生效**的一层保护。
		//
		//absorb() 返回 -1 表示"这次完全免疫"，直接 return 不扣血。
		int voidResult = com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons
				.VoidArmor.absorb(this, dmg);
		if (voidResult == -1) {
			if (sprite != null) {
				sprite.showStatus(
						com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite
								.POSITIVE, "免疫");
			}
			return;
		}
		dmg = voidResult;

		//we ceil these specifically to favor the player vs. champ dmg reduction
		// most important vs. giant champions in the earlygame
		for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
			dmg = (int) Math.ceil(dmg * buff.damageTakenFactor());
		}

		//==== END(挑战·伤害管线 第5步): 目标方减伤 —— 脆弱 +13% ====
		//放在护甲减免（Char.attack 里已算）与冠军减伤之后，即"承伤系数"的最后一环。
		//对玩家与怪物**都**生效（这才是"双刃剑"）。
		dmg = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.damageTaken(this, dmg);

		//==== END(挑战·伤害管线 第6步): 最终拦截 ====
		//物极必反（完全免疫）/ 九九归一（9 的倍数变 1）。
		//
		//END(修复·误报无敌): 必须用 IMMUNE 哨兵值判断，**不能**用 dmg <= 0 ——
		//护甲完全吸收时伤害本来就是 0，用 <=0 判断会把"没打穿护甲"
		//误报成"无敌"（玩家实测到的"没勾挑战也有概率触发无敌"）。
		//而且这两条都没勾时，finalIntercept 原样返回，下面这段等价于不存在。
		dmg = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.finalIntercept(this, dmg);
		if (dmg == com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.IMMUNE) {
			//确实被 22 物极必反完全免疫
			sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
			return;
		}

		//==== END(挑战 142 无下限术士): 怪物受远程攻击 13% 完全免疫 ====
		//放在 22 的拦截**之后**：两者都是"完全免疫"，
		//放在同一位置语义一致（都是第 6 步的最终拦截）。
		//只对怪物生效，且只在伤害来自远程武器时判定。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.rollRangedImmunity(this, src)) {
			if (sprite != null) {
				sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));
			}
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
					com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
							com.shatteredpixel.shatteredpixeldungeon.endcontent
									.challenge.ChallengeEffects.class,
							"no_lower_limit_immune"));
			return;
		}
		
		//TODO improve this when I have proper damage source logic
		if (AntiMagic.RESISTS.contains(src.getClass())){

			//==== END(挑战 141 禁魔空间): 所有魔法伤害 -20% ====
			//放在 AntiMagic 减免**之前**：这是"魔法伤害"这一类的整体压制，
			//应该作用在原始魔法伤害上，而不是在抗魔减免之后。
			//玩家与怪物**都**受影响（原表："包括玩家与怪物"）。
			dmg = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.magicDamageTaken(dmg);

			dmg -= AntiMagic.drRoll(this, glyphLevel(AntiMagic.class));
			if (buff(ArcaneArmor.class) != null) {
				dmg -= Random.NormalIntRange(0, buff(ArcaneArmor.class).level());
			}
			if (dmg < 0) dmg = 0;
		}
		
		if (buff( Paralysis.class ) != null) {
			buff( Paralysis.class ).processDamage(dmg);
		}

		BrokenSeal.WarriorShield shield = buff(BrokenSeal.WarriorShield.class);
		if (!(src instanceof Hunger)
				&& dmg > 0
				//either HP is already half or below (ignoring shield)
				// or the hit will reduce it to half or below
				&& (HP <= HT/2 || HP + shielding() - dmg <= HT/2)
				&& shield != null && !shield.coolingDown()){
			sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(buff(BrokenSeal.WarriorShield.class).maxShield()), FloatingText.SHIELDING);
			shield.activate();
		}

		int shielded = dmg;
		dmg = ShieldBuff.processDamage(this, dmg, src);
		shielded -= dmg;

		//==== END(挑战 65 及时雨 / 104 命悬一线): 致命伤保命 ====
		//放在护盾处理**之后**：只有真正会扣掉最后一点生命的伤害才算"致命伤"，
		//被护盾挡下的不算。
		//触发顺序按原表要求：先 65 及时雨（整局第一次必保），
		//之后才轮到 104 命悬一线（每次 13%）。
		if (dmg >= HP
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.survivingFatalBlow(this, dmg)) {
			dmg = Math.max(0, HP - 1);          //保留 1 点生命
			if (sprite != null) {
				sprite.showStatus(CharSprite.POSITIVE,
						Messages.get(this, "invulnerable"));
			}
			com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
					com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
							com.shatteredpixel.shatteredpixeldungeon.endcontent
									.challenge.ChallengeEffects.class,
							"close_call_survive"));
		}

		//==== END(挑战 128 镇魂歌): 不死状态 ====
		//放在 65/104 之后：那两条是"挑战自带的保命"，
		//镇魂歌是**玩家主动使用的道具**，优先级排在它们之后。
		//
		//==== END(挑战 128 镇魂歌): 期间免疫死亡，结束后**不结算** ====
		//文档所有者定稿："镇魂是期间免疫死亡，不会结束后结算死亡。"
		//
		//所以它与 65/104 一样是**纯粹的保命**，不是"推迟死亡"。
		//（早先的实现会在 buff 结束时补算死亡，那是旧理解，已废弃。）
		if (dmg >= HP
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
						.SoulRequiem.surviveFatal(this, dmg)) {
			return;                             //HP 已在方法内压到 1，不再扣血
		}

		//==== END(顶级装备·虚空不灭之甲): 致命伤触发祝福十字架 ====
		//文档所有者定稿："受到致命伤触发祝福十字架效果，cd 50 回合"。
		//
		//放在 128/124 之前 —— 虚空甲是**装备自带**的保命，
		//优先级高于"靠道具/状态临时获得的保命"。
		if (dmg >= HP
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons
						.VoidArmor.secondLife(this, dmg)) {
			return;                             //HP 已在方法内压到 1
		}

		//==== END(挑战 124 野生狗奶): 状态期间不会死 ====
		//原表："效果持续期间无法死亡（生命值最低为 1）"
		//与镇魂歌的区别：镇魂歌是"3 回合后仍会还债"，
		//狗奶是**纯粹的续命** —— 状态结束就恢复正常，不结算期间的死亡。
		if (dmg >= HP
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
						.WildDogMilk.surviveFatal(this, dmg)) {
			return;
		}

		//==== END(挑战 161 钱就是命): 用金币抵消致命伤 ====
		//原表（文档所有者说明）："在受到致命伤时，用等量金币抵消"
		//排在最后：如果前面的保命手段都没接住，才轮到花钱买命。
		if (dmg >= HP
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.payToSurvive(this, dmg)) {
			return;
		}

		//==== END(轮回诅咒① 不灭): 怪物致命伤时触发一次无敌 ====
		//文档所有者定稿："受到致命伤触发一次无敌"，主语是怪物。
		//
		//与 192 不死之身的区别（两者可以同时存在）：
		//  · 192 不死之身 → 生命归零时改为**麻痹 50 回合**（永远打不死）
		//  · ① 不灭       → 只挡**一次**，之后就正常死亡
		//
		//所以它排在 192 **之前**：先消耗"不灭"的那一次，用掉之后
		//再轮到 192 的麻痹逻辑。
		//用 buff 记录"这次无敌是否已用过"，所以一只怪只能挡一次。
		if (dmg >= HP
				&& this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent
						.Reincarnation.mobCanSurviveFatal()
				&& buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
						.ReincarnationUndying.class) == null) {
			//挂上标记：这只怪的"不灭"已经用掉了
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					this,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.ReincarnationUndying.class);

			HP = 1;
			if (sprite != null) {
				sprite.showStatus(
						com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite
								.POSITIVE, "不灭");
			}
			com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.log(
					com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.COMBAT,
					"【轮回·不灭】" + getClass().getSimpleName() + " 挡下了致命伤");
			return;
		}

		//==== END(挑战 192 不死之身): 怪物生命归零时改为麻痹 50 回合 ====
		//文档所有者定稿："怪物无法死亡，改为麻痹 50 回合。"
		//
		//接在 HP 扣除**之前** —— 命中就把生命压到 1 并挂 50 回合麻痹，
		//怪物永远不会走到 die() 那条路径。
		//
		//Boss 不参与（把 Boss 变成打不死的会让整局无法通关）。
		if (dmg >= HP
				&& this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.shouldSurviveDeath(
								(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob)
										this)) {
			HP = 1;
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
					this,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.Paralysis.class,
					com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.UNDYING_PARALYSIS);
			if (sprite != null) {
				sprite.showStatus(
						com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite
								.NEGATIVE, "沉睡");
			}
			return;
		}

		//==== END(移植·复仇狂怒 79): 受伤 +20%×层 ====
		dmg = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
			.ChallengeEffects.revengeFuryTakenDamage(this, dmg);

		//==== END(诊断·伤害为 0): 在真正扣血前打印最终值 ====
		//文档所有者反馈："有的时候伤害为 0，没有九九归一这种挑战的时候触发的。"
		//
		//把**扣血前的最终伤害**打出来。如果日志里出现 dmg=0，
		//回溯同一行的"来源/目标/各自的上限"就能定位是哪一步吃掉的：
		//  · 目标生命 5、伤害 0        → 是伤害管线里被减没的
		//  · 来源是某法杖/某武器      → 定位到具体道具
		com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.log(
				com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.COMBAT,
				"伤害结算 " + (src == null ? "?" : src.getClass().getSimpleName())
						+ " → " + getClass().getSimpleName()
						+ "  dmg=" + dmg
						+ "  HP前=" + HP + "/" + HT
						+ "  dr=" + drRoll()
						+ (this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero
								? "  [玩家]" : ""));

		HP -= dmg;

		//==== END(挑战 144 破碎权柄): Boss 掉到 33% 后每 5 回合召唤稀有怪 ====
		//照本 fork 已有的分阶段 Boss 写法（见 Pompeii / Talu_BlackSnake）：
		//在 super.damage() 之后判定阈值，命中就切阶段。
		//
		//区别：那些是"逐个 Boss 改自己的类"，而 144 要覆盖**所有** Boss，
		//所以放在 Char.damage() 这个公共路径上。
		//未勾选 144 时 brokenPowerActive 返回 false，等价于原版。
		if (this instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.tryBrokenPowerSummon(
							(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob) this);
		}

		//==== END(挑战 78 烈火焚身): 玩家受击 13% 概率燃烧 ====
		//只在实际掉血时触发；护盾完全吸收(dmg==0)不算"受击"。
		if (dmg > 0) {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
					.onHeroDamagedBurn(this);
		}

		if (HP > 0 && buff(Grim.GrimTracker.class) != null){

			float finalChance = buff(Grim.GrimTracker.class).maxChance;
			finalChance *= (float)Math.pow( ((HT - HP) / (float)HT), 2);

			if (Random.Float() < finalChance) {
				int extraDmg = Math.round(HP*resist(Grim.class));
				dmg += extraDmg;
				HP -= extraDmg;

				sprite.emitter().burst( ShadowParticle.UP, 5 );
				if (!isAlive() && buff(Grim.GrimTracker.class).qualifiesForBadge){
					Badges.validateGrimWeapon();
				}
			}
		}

		if (HP < 0 && src instanceof Char && alignment == Alignment.ENEMY){
			if (((Char) src).buff(Kinetic.KineticTracker.class) != null){
				int dmgToAdd = -HP;
				dmgToAdd -= ((Char) src).buff(Kinetic.KineticTracker.class).conservedDamage;
				dmgToAdd = Math.round(dmgToAdd * Weapon.Enchantment.genericProcChanceMultiplier((Char) src));
				if (dmgToAdd > 0) {
					Buff.affect((Char) src, Kinetic.ConservedDamage.class).setBonus(dmgToAdd);
				}
				((Char) src).buff(Kinetic.KineticTracker.class).detach();
			}
		}
		
		if (sprite != null) {
			//defaults to normal damage icon if no other ones apply
			int                                                         icon = FloatingText.PHYS_DMG;
			if (NO_ARMOR_PHYSICAL_SOURCES.contains(src.getClass()))     icon = FloatingText.PHYS_DMG_NO_BLOCK;
			if (AntiMagic.RESISTS.contains(src.getClass()))             icon = FloatingText.MAGIC_DMG;
			if (src instanceof Pickaxe)                                 icon = FloatingText.PICK_DMG;

			//special case for sniper when using ranged attacks
			if (src == Dungeon.hero
					&& Dungeon.hero.subClass == HeroSubClass.SNIPER
					&& !Dungeon.level.adjacent(Dungeon.hero.pos, pos)
					&& Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon){
				icon = FloatingText.PHYS_DMG_NO_BLOCK;
			}

			//special case for monk using unarmed abilities
			if (src == Dungeon.hero
					&& Dungeon.hero.buff(MonkEnergy.MonkAbility.UnarmedAbilityTracker.class) != null){
				icon = FloatingText.PHYS_DMG_NO_BLOCK;
			}

			if (src instanceof Hunger)                                  icon = FloatingText.HUNGER;
			if (src instanceof Burning)                                 icon = FloatingText.BURNING;
			if (src instanceof Chill || src instanceof Frost)           icon = FloatingText.FROST;
			if (src instanceof GeyserTrap || src instanceof StormCloud) icon = FloatingText.WATER;
			if (src instanceof Burning)                                 icon = FloatingText.BURNING;
			if (src instanceof Electricity)                             icon = FloatingText.SHOCKING;
			if (src instanceof Bleeding)                                icon = FloatingText.BLEEDING;
			if (src instanceof ToxicGas)                                icon = FloatingText.TOXIC;
			if (src instanceof Corrosion)                               icon = FloatingText.CORROSION;
			if (src instanceof Poison)                                  icon = FloatingText.POISON;
			if (src instanceof Ooze)                                    icon = FloatingText.OOZE;
			if (src instanceof Viscosity.DeferedDamage)                 icon = FloatingText.DEFERRED;
			if (src instanceof Corruption)                              icon = FloatingText.CORRUPTION;
			if (src instanceof AscensionChallenge)                      icon = FloatingText.AMULET;

			if ((icon == FloatingText.PHYS_DMG || icon == FloatingText.PHYS_DMG_NO_BLOCK) && hitMissIcon != -1){
				if (icon == FloatingText.PHYS_DMG_NO_BLOCK) hitMissIcon += 18; //extra row
				icon = hitMissIcon;
			}
			hitMissIcon = -1;

			sprite.showStatusWithIcon(CharSprite.NEGATIVE, Integer.toString(dmg + shielded), icon);
		}

		//==== END(移植·同仇敌忾 78): HP 清零前先记下过量伤害 ====
		int overkillDamage = (HP < 0) ? -HP : 0;
		if (HP < 0) HP = 0;

		//==== END(移植·登神长阶): 怪物 13% 概率原地复活（最多 6 次）====
		if (!isAlive() && com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
			.ChallengeEffects.tryAscensionRevive(this)) {
			//已复活：不再走 die()，本方法到此为止
		} else if (!isAlive()) {
			//==== END(移植·同仇敌忾 78): 过量伤害转嫁 ====
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.revengeSpread(this, overkillDamage, src);
			die( src );
		} else if (HP == 0 && buff(DeathMark.DeathMarkTracker.class) != null){
			DeathMark.processFearTheReaper(this);
		}
	}

	//these are misc. sources of physical damage which do not apply armor, they get a different icon
	private static HashSet<Class> NO_ARMOR_PHYSICAL_SOURCES = new HashSet<>();
	{
		NO_ARMOR_PHYSICAL_SOURCES.add(CrystalSpire.SpireSpike.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollGeomancer.Boulder.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollGeomancer.GnollRockFall.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(GnollRockfallTrap.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DwarfKing.KingDamager.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DwarfKing.Summoning.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(LifeLink.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(Chasm.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(WandOfBlastWave.Knockback.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(Heap.class); //damage from wraiths attempting to spawn from heaps
		NO_ARMOR_PHYSICAL_SOURCES.add(Necromancer.SummoningBlockDamage.class);
		NO_ARMOR_PHYSICAL_SOURCES.add(DriedRose.GhostHero.NoRoseDamage.class);
	}
	
	public void destroy() {
		HP = 0;
		Actor.remove( this );

		for (Char ch : Actor.chars().toArray(new Char[0])){
			if (ch.buff(Charm.class) != null && ch.buff(Charm.class).object == id()){
				ch.buff(Charm.class).detach();
			}
			if (ch.buff(Dread.class) != null && ch.buff(Dread.class).object == id()){
				ch.buff(Dread.class).detach();
			}
			if (ch.buff(Terror.class) != null && ch.buff(Terror.class).object == id()){
				ch.buff(Terror.class).detach();
			}
			if (ch.buff(SnipersMark.class) != null && ch.buff(SnipersMark.class).object == id()){
				ch.buff(SnipersMark.class).detach();
			}
			if (ch.buff(Talent.FollowupStrikeTracker.class) != null
					&& ch.buff(Talent.FollowupStrikeTracker.class).object == id()){
				ch.buff(Talent.FollowupStrikeTracker.class).detach();
			}
			if (ch.buff(Talent.DeadlyFollowupTracker.class) != null
					&& ch.buff(Talent.DeadlyFollowupTracker.class).object == id()){
				ch.buff(Talent.DeadlyFollowupTracker.class).detach();
			}
		}
	}
	
	public void die( Object src ) {
		//==== END(移植·复仇狂怒 79): 有怪死亡 → 视野内的同伴进入狂怒 ====
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
			.ChallengeEffects.onMobDeath(this);
		destroy();
		if (src != Chasm.class) {
			sprite.die();
			if (!flying && Dungeon.level != null && sprite instanceof MobSprite && Dungeon.level.map[pos] == Terrain.CHASM){
				((MobSprite) sprite).fall();
			}
		}
	}

	//we cache this info to prevent having to call buff(...) in isAlive.
	//This is relevant because we call isAlive during drawing, which has both performance
	//and thread coordination implications
	public boolean deathMarked = false;
	
	public boolean isAlive() {
		return HP > 0 || deathMarked;
	}

	public boolean isActive() {
		return isAlive();
	}

	@Override
	protected void spendConstant(float time) {
		TimekeepersHourglass.timeFreeze freeze = buff(TimekeepersHourglass.timeFreeze.class);
		if (freeze != null) {
			freeze.processTime(time);
			return;
		}

		Swiftthistle.TimeBubble bubble = buff(Swiftthistle.TimeBubble.class);
		if (bubble != null){
			bubble.processTime(time);
			return;
		}

		super.spendConstant(time);
	}

	@Override
	protected void spend( float time ) {

		float timeScale = 1f;
		if (buff( Slow.class ) != null) {
			timeScale *= 0.5f;
			//slowed and chilled do not stack
		} else if (buff( Chill.class ) != null) {
			timeScale *= buff( Chill.class ).speedFactor();
		}
		if (buff( Speed.class ) != null) {
			timeScale *= 2.0f;
		}
		
		super.spend( time / timeScale );
	}
	
	public synchronized LinkedHashSet<Buff> buffs() {
		return new LinkedHashSet<>(buffs);
	}
	
	@SuppressWarnings("unchecked")
	//returns all buffs assignable from the given buff class
	public synchronized <T extends Buff> HashSet<T> buffs( Class<T> c ) {
		HashSet<T> filtered = new HashSet<>();
		for (Buff b : buffs) {
			if (c.isInstance( b )) {
				filtered.add( (T)b );
			}
		}
		return filtered;
	}

	@SuppressWarnings("unchecked")
	//returns an instance of the specific buff class, if it exists. Not just assignable
	public synchronized  <T extends Buff> T buff( Class<T> c ) {
		for (Buff b : buffs) {
			if (b.getClass() == c) {
				return (T)b;
			}
		}
		return null;
	}

	public synchronized boolean isCharmedBy( Char ch ) {
		int chID = ch.id();
		for (Buff b : buffs) {
			if (b instanceof Charm && ((Charm)b).object == chID) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean add( Buff buff ) {

		if (buff(PotionOfCleansing.Cleanse.class) != null) { //cleansing buff
			if (buff.type == Buff.buffType.NEGATIVE
					&& !(buff instanceof AllyBuff)
					&& !(buff instanceof LostInventory)){
				return false;
			}
		}

		if (sprite != null && buff(Challenge.SpectatorFreeze.class) != null){
			return false; //can't add buffs while frozen and game is loaded
		}

		buffs.add( buff );
		if (Actor.chars().contains(this)) Actor.add( buff );

		if (sprite != null && buff.announced) {
			switch (buff.type) {
				case POSITIVE:
					sprite.showStatus(CharSprite.POSITIVE, Messages.titleCase(buff.name()));
					break;
				case NEGATIVE:
					sprite.showStatus(CharSprite.WARNING, Messages.titleCase(buff.name()));
					break;
				case NEUTRAL:
				default:
					sprite.showStatus(CharSprite.NEUTRAL, Messages.titleCase(buff.name()));
					break;
			}
		}

		return true;

	}
	
	public synchronized boolean remove( Buff buff ) {
		
		buffs.remove( buff );
		Actor.remove( buff );

		return true;
	}
	
	public synchronized void remove( Class<? extends Buff> buffClass ) {
		for (Buff buff : buffs( buffClass )) {
			remove( buff );
		}
	}
	
	@Override
	protected synchronized void onRemove() {
		for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
			buff.detach();
		}
	}
	
	public synchronized void updateSpriteState() {
		for (Buff buff:buffs) {
			buff.fx( true );
		}
	}
	
	public float stealth() {
		float stealth = 0;

		stealth += Obfuscation.stealthBoost(this, glyphLevel(Obfuscation.class));

		return stealth;
	}

	public final void move( int step ) {
		move( step, true );
	}

	//travelling may be false when a character is moving instantaneously, such as via teleportation
	public void move( int step, boolean travelling ) {

		if (travelling && Dungeon.level.adjacent( step, pos ) && buff( Vertigo.class ) != null) {
			sprite.interruptMotion();
			int newPos = pos + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			if (!(Dungeon.level.passable[newPos] || Dungeon.level.avoid[newPos])
					|| (properties().contains(Property.LARGE) && !Dungeon.level.openSpace[newPos])
					|| Actor.findChar( newPos ) != null)
				return;
			else {
				sprite.move(pos, newPos);
				step = newPos;
			}
		}

		if (Dungeon.level.map[pos] == Terrain.OPEN_DOOR) {
			Door.leave( pos );
		}

		pos = step;
		
		if (this != Dungeon.hero) {
			sprite.visible = Dungeon.level.heroFOV[pos];
		}
		
		Dungeon.level.occupyCell(this );
	}
	
	public int distance( Char other ) {
		return Dungeon.level.distance( pos, other.pos );
	}

	public boolean[] modifyPassable( boolean[] passable){
		//do nothing by default, but some chars can pass over terrain that others can't
		return passable;
	}
	
	public void onMotionComplete() {
		//Does nothing by default
		//The main actor thread already accounts for motion,
		// so calling next() here isn't necessary (see Actor.process)
	}
	
	public void onAttackComplete() {
		next();
	}
	
	public void onOperateComplete() {
		next();
	}
	
	protected final HashSet<Class> resistances = new HashSet<>();
	
	//returns percent effectiveness after resistances
	//TODO currently resistances reduce effectiveness by a static 50%, and do not stack.
	public float resist( Class effect ){
		HashSet<Class> resists = new HashSet<>(resistances);
		for (Property p : properties()){
			resists.addAll(p.resistances());
		}
		for (Buff b : buffs()){
			resists.addAll(b.resistances());
		}
		
		float result = 1f;
		for (Class c : resists){
			if (c.isAssignableFrom(effect)){
				result *= 0.5f;
			}
		}
		return result * RingOfElements.resist(this, effect);
	}
	
	protected final HashSet<Class> immunities = new HashSet<>();
	
	public boolean isImmune(Class effect ){
		HashSet<Class> immunes = new HashSet<>(immunities);
		for (Property p : properties()){
			immunes.addAll(p.immunities());
		}
		for (Buff b : buffs()){
			immunes.addAll(b.immunities());
		}
		if (glyphLevel(Brimstone.class) >= 0){
			immunes.add(Burning.class);
		}
		
		for (Class c : immunes){
			if (c.isAssignableFrom(effect)){
				return true;
			}
		}
		return false;
	}

	//similar to isImmune, but only factors in damage.
	//Is used in AI decision-making
	public boolean isInvulnerable( Class effect ){
		return buff(Challenge.SpectatorFreeze.class) != null || buff(Invulnerability.class) != null;
	}

	protected HashSet<Property> properties = new HashSet<>();

	public HashSet<Property> properties() {
		HashSet<Property> props = new HashSet<>(properties);
		//TODO any more of these and we should make it a property of the buff, like with resistances/immunities
		if (buff(ChampionEnemy.Giant.class) != null) {
			props.add(Property.LARGE);
		}
		return props;
	}

	public enum Property{
		BOSS ( new HashSet<Class>( Arrays.asList(Grim.class, GrimTrap.class, ScrollOfRetribution.class, ScrollOfPsionicBlast.class)),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
		MINIBOSS ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class) )),
		BOSS_MINION,
		UNDEAD,
		DEMONIC,
		//END(移植自魔绫·挑战区): 空洞遗迹(Hollow)生物属性
		HOLLOW,
		PETS, //END(移植自魔绫): 宠物
		INORGANIC ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(Bleeding.class, ToxicGas.class, Poison.class) )),
		FIERY ( new HashSet<Class>( Arrays.asList(WandOfFireblast.class, Elemental.FireElemental.class)),
				new HashSet<Class>( Arrays.asList(Burning.class, Blazing.class))),
		ICY ( new HashSet<Class>( Arrays.asList(WandOfFrost.class, Elemental.FrostElemental.class)),
				new HashSet<Class>( Arrays.asList(Frost.class, Chill.class))),
		ACIDIC ( new HashSet<Class>( Arrays.asList(Corrosion.class)),
				new HashSet<Class>( Arrays.asList(Ooze.class))),
		ELECTRIC ( new HashSet<Class>( Arrays.asList(WandOfLightning.class, Shocking.class, Potential.class,
										Electricity.class, ShockingDart.class, Elemental.ShockElemental.class )),
				new HashSet<Class>()),
		IMMOVABLE ( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(Vertigo.class) )),
		//A character that acts in an unchanging manner. immune to AI state debuffs or stuns/slows
		STATIC( new HashSet<Class>(),
				new HashSet<Class>( Arrays.asList(AllyBuff.class, Dread.class, Terror.class, Amok.class, Charm.class, Sleep.class,
									Paralysis.class, Frost.class, Chill.class, Slow.class, Speed.class) )),

		//END(port from Arknights): 方舟新增的生物属性。
		//为避免引入尚未搬运的 buff 类，这里只声明枚举值，抗性/免疫集合留空
		//（真实抗性在搬入相关 buff 后可按需补回）。
		LARGE,
		NO_KNOCKBACK,
		DRONE,
		SARKAZ,
		NPC,
		/** 海嗣（伊比利亚区生物） */
		SEA,
		/** 被源石感染 */
		INFECTED;

		private HashSet<Class> resistances;
		private HashSet<Class> immunities;
		
		Property(){
			this(new HashSet<Class>(), new HashSet<Class>());
		}
		
		Property( HashSet<Class> resistances, HashSet<Class> immunities){
			this.resistances = resistances;
			this.immunities = immunities;
		}
		
		public HashSet<Class> resistances(){
			return new HashSet<>(resistances);
		}
		
		public HashSet<Class> immunities(){
			return new HashSet<>(immunities);
		}

	}

	public static boolean hasProp( Char ch, Property p){
		return (ch != null && ch.properties().contains(p));
	}

	//END(port from Arknights): 踩到海怪时的触发（方舟方法，本 fork 原无）
	//默认实现：无特殊反应，交回给 SeaTerror 处理伤害/效果。
	public void activateSeaTerror() {
		//no-op by default
	}
}
