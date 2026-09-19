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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GreaterHaste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SoulMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.Feint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.GuidingLight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Stasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.DirectableAlly;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Surprise;
import com.shatteredpixel.shatteredpixeldungeon.effects.Wound;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ExoticCrystals;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Lucky;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

public abstract class Mob extends Char {

	{
		actPriority = MOB_PRIO;
		
		alignment = Alignment.ENEMY;
	}

	public AiState SLEEPING     = new Sleeping();
	public AiState HUNTING		= new Hunting();
	public AiState INVESTIGATING= new Investigating();
	public AiState WANDERING	= new Wandering();
	public AiState FLEEING		= new Fleeing();
	public AiState PASSIVE		= new Passive();
	public AiState state = SLEEPING;
	
	public Class<? extends CharSprite> spriteClass;
	
	protected int target = -1;
	
	public int defenseSkill = 0;
	
	public int EXP = 1;
	public int maxLvl = Hero.MAX_LEVEL-1;
	
	protected Char enemy;
	protected int enemyID = -1; //used for save/restore
	protected boolean enemySeen;
	protected boolean alerted = false;

	protected static final float TIME_TO_WAKE_UP = 1f;

	protected boolean firstAdded = true;
	protected void onAdd(){
		if (firstAdded) {
			//modify health for ascension challenge if applicable, only on first add
			float percent = HP / (float) HT;
			HT = Math.round(HT * AscensionChallenge.statModifier(this));
			HP = Math.round(HT * percent);
			firstAdded = false;

			//==== END(挑战 1 牢地碎破): 按配置表覆写生命上限 ====
			//为什么放在 onAdd() 而不是 Level.createMob()：
			//createMob() 只覆盖"关卡刷出的普通怪"。而 Boss 的召唤物
			//（古神的六只拳头、幼虫、YogEye/YogScorpio/YogRipper）
			//走 Reflection.newInstance 直接创建，**完全绕过 createMob()**。
			//
			//为什么也不放在 GameScene.add(Mob)：
			//全仓有 60 多处直接 `Dungeon.level.mobs.add(...)` 绕过了它 ——
			//其中包括 Goo 的四种 Boss 房间（DiamondGooRoom 等），
			//放在那里会导致 25F 的 Goo 不被覆写。
			//
			//onAdd() 由 Actor.add() 调用，是**所有 Actor 入场的最终汇聚点**，
			//且被 firstAdded 保证只执行一次，因此是唯一不漏的落点。
			//
			//注意顺序：放在 AscensionChallenge 之后。
			//挑战 1 给的是"配置表里的绝对值"，应当**覆盖**前面的百分比修正，
			//否则两者叠加会得到既不是原版、也不是配置表的结果。
			//未勾选 1 或表里没有该怪物时原样返回，本段等价于不存在。
			int tableHP = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.crumblingHP(this, HT);
			if (tableHP != HT) {
				HT = tableHP;
				HP = tableHP;      //召唤物/新刷出的怪都是满血入场
			}

			//==== END(挑战 153 恶魔地牢): 所有怪物变为恶魔类 ====
			//原表："所有怪物变为恶魔类（**只是代码**）" ——
			//只加 DEMONIC 属性标记，不改外观/数值/AI。
			//这样 158 神圣之力的"对恶魔额外伤害"才能生效。
			//
			//放在 onAdd 而不是构造函数：属性集可能在子类构造块里被反复设置，
			//而且 onAdd 有 firstAdded 保证只跑一次。
			if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.demonsEnabled()) {
				properties.add(Property.DEMONIC);
			}

			//==== END(挑战 29 雇佣童工): 13% 怪物变成"童工" ====
			//原表："怪物 13% 概率被替换：生命=原 20%，移速×2"
			//
			//放在 onAdd 的最末尾：它是**最终覆盖**，必须跑在
			//牢地碎破（配置表绝对值）之后，否则会被那张表冲掉。
			//
			//Boss 不参与（把 Boss 削到 20% 血会让整局失去意义）。
			if (!properties.contains(Property.BOSS)
					&& !properties.contains(Property.MINIBOSS)
					&& com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.rollChildLabor()) {
				int newHP = Math.max(1, Math.round(HT
						* com.shatteredpixel.shatteredpixeldungeon.endcontent
								.challenge.ChallengeEffects.childLaborHpMultiplier()));
				HT = newHP;
				HP = newHP;

				//移速 ×2：挂一个永久 Haste（原版有现成的）
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
						this,
						com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.Haste.class,
						99999f);

				//标记成童工，供"移速倍率"查询识别
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
						this,
						com.shatteredpixel.shatteredpixeldungeon.actors.buffs
								.ChildLaborMark.class,
						99999f);
			}

			//==== END(挑战 73 神秘复苏): 13% 的怪物直接变成幽灵 ====
			//原表："13% 生成幽灵"
			//
			//做法与 29 童工不同：那条是改数值，这条是**换怪**。
			//但 Mob.onAdd 里已经没法把 this 换成另一个类，
			//所以改为"把它变成幽灵的外观 + 数值"——
			//即：把贴图换成 WraithSprite、HP 压到 1、清掉经验。
			//这样它在行为上就是一只幽灵，而且不需要新建实例（避免递归 onAdd）。
			if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.rollMysticRevival(this)) {
				becomeWraith();
			}


			//==== END(挑战 148 飞天神偷): 怪物 13% 获得隐身 ====
			//一次性判定（onAdd 只跑一次），不是每回合重掷 ——
			//否则怪物会一会儿可见一会儿不可见，非常闪烁。
			//只加 invisibility 字段而不是挂 Invisibility buff：
			//buff 有时长，会到期消失；这里要的是"这只怪就是隐身的"。
			if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.rollFlyingThiefInvisible(this)) {
				invisible = 1;
			}
		}
	}

	private static final String STATE	= "state";
	private static final String SEEN	= "seen";
	private static final String TARGET	= "target";
	private static final String MAX_LVL	= "max_lvl";

	private static final String ENEMY_ID	= "enemy_id";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );

		if (state == SLEEPING) {
			bundle.put( STATE, Sleeping.TAG );
		} else if (state == WANDERING) {
			bundle.put( STATE, Wandering.TAG );
		} else if (state == INVESTIGATING) {
			bundle.put( STATE, Investigating.TAG );
		} else if (state == HUNTING) {
			bundle.put( STATE, Hunting.TAG );
		} else if (state == FLEEING) {
			bundle.put( STATE, Fleeing.TAG );
		} else if (state == PASSIVE) {
			bundle.put( STATE, Passive.TAG );
		}
		bundle.put( SEEN, enemySeen );
		bundle.put( TARGET, target );
		bundle.put( MAX_LVL, maxLvl );

		if (enemy != null) {
			bundle.put(ENEMY_ID, enemy.id() );
		}
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );

		String state = bundle.getString( STATE );
		if (state.equals( Sleeping.TAG )) {
			this.state = SLEEPING;
		} else if (state.equals( Wandering.TAG )) {
			this.state = WANDERING;
		} else if (state.equals( Investigating.TAG )) {
			this.state = INVESTIGATING;
		} else if (state.equals( Hunting.TAG )) {
			this.state = HUNTING;
		} else if (state.equals( Fleeing.TAG )) {
			this.state = FLEEING;
		} else if (state.equals( Passive.TAG )) {
			this.state = PASSIVE;
		}

		enemySeen = bundle.getBoolean( SEEN );

		target = bundle.getInt( TARGET );

		if (bundle.contains(MAX_LVL)) maxLvl = bundle.getInt(MAX_LVL);

		if (bundle.contains(ENEMY_ID)) {
			enemyID = bundle.getInt(ENEMY_ID);
		}

		//no need to actually save this, must be false
		firstAdded = false;
	}

	//mobs need to remember their targets after every actor is added
	public void restoreEnemy(){
		if (enemyID != -1 && enemy == null) enemy = (Char)Actor.findById(enemyID);
	}
	
	public CharSprite sprite() {
		CharSprite s = Reflection.newInstance(spriteClass);

		//==== END(挑战 63 鼠鼠可爱): 所有怪物都变成小鼠 ====
		//文档所有者说明："怪物贴图、文本、近战后 UI 显示都变成小鼠"。
		//
		//与 138 的区别：138 是**随机**换（而且记在 buff 上防闪烁），
		//63 是**固定**换成小鼠 —— 所以不需要掷色子，也不需要记状态。
		//
		//放在 138 之前：两条同时勾选时，63 的"全都是小鼠"优先
		//（否则 138 的随机会把小鼠又随机掉，那 63 就形同虚设）。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.cuteRatsEnabled(this)) {
			s = new com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite();
			return s;
		}

		//==== END(诊断·138 贴图): 记录每次创建 sprite 时用了哪个类 ====
		//用来回答"为什么 UI 里显示的是原怪物贴图"：
		//如果这里每次都打印**原始类**，说明 mark/spriteClassName 没生效；
		//如果打印的是随机类，说明 UI 那条路径另有问题。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.ABSURD_SPRITE_DEBUG) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChallengeAbsurdMark mk =
					buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.ChallengeAbsurdMark.class);
			System.out.println("[荒诞贴图] " + getClass().getSimpleName()
					+ " 原始=" + spriteClass.getSimpleName()
					+ " buff=" + (mk == null ? "无" : (mk.spriteClassName == null ? "未掷" : mk.spriteClassName)));
		}

		//==== END(挑战 138 荒诞世界): 怪物贴图随机变化 ====
		//**纯外观**：只换贴图类，属性、AI、行为一律不变（原表要求）。
		//
		//关键：随机结果必须**固定**在 buff 上，不能每次调用都随机 ——
		//本方法是每次调用都新建 sprite，直接随机会导致贴图疯狂闪烁。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.onAbsurdWorld(this)) {

			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChallengeAbsurdMark mark =
					buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.ChallengeAbsurdMark.class);

			//第一次：掷一次色子并记下来
			if (mark != null && mark.spriteClassName == null
					&& com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.rollAbsurdSprite()) {
				mark.spriteClassName = pickRandomSpriteClassName();
			}

			//之后（含读档）：始终用记下来的那个
			if (mark != null && mark.spriteClassName != null) {
				try {
					Class<?> c = Class.forName(mark.spriteClassName);
					if (com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.class
							.isAssignableFrom(c)) {
						@SuppressWarnings("unchecked")
						Class<? extends com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite> sc =
								(Class<? extends com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite>) c;
						s = Reflection.newInstance(sc);
					}
				} catch (Exception e) {
					//类名失效（版本变更等）就保持原贴图，纯外观规则绝不能影响游戏
				}
			}
		}

		//==== END(挑战 10 巨型化): 体型放大 ====
		//在贴图确定**之后**设置：若 138 换了贴图，放大仍要生效。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.isGiant(this)) {
			s.scale.set(com.shatteredpixel.shatteredpixeldungeon.actors.buffs
					.ChallengeGiantMark.SCALE);
		}

		return s;
	}

	/**
	 * END(挑战 138): 随机挑一个 sprite 类名 —— **完全随机**。
	 *
	 * <h3>修订记录</h3>
	 * 早先版本只从"本层怪物轮换表"里挑，理由是"那些贴图与当前层级的怪物
	 * 尺寸/帧数一致，不会越界或错帧"。
	 *
	 * <p>但文档所有者要求的是**完全随机**："不是小鼠变蛇这种" ——
	 * 即不该局限于同层怪物，任何怪物的外观都可能出现。
	 *
	 * <h3>关于帧数</h3>
	 * {@code CharSprite} 按**自己的** frames 渲染，贴图类之间的帧数差异
	 * 不会导致越界（精灵图集是整块载入的）。真正需要避免的只是
	 * 拿到 null 或加载失败的贴图 —— 这里用 try/catch 兜住，拿不到就保持原贴图。
	 */
	private static java.util.ArrayList<Class<? extends Mob>> allMobClasses = null;

	private static void buildAllMobClasses() {
		java.util.ArrayList<Class<? extends Mob>> list = new java.util.ArrayList<>();

		//收集所有关卡用过的怪物：把 1-25 层的轮换表全部并起来。
		//这样既覆盖了全部常规怪物，又不必手工维护一份清单
		//（手工清单容易漏，也会在新增怪物时忘记更新）。
		for (int d = 1; d <= 25; d++) {
			try {
				java.util.ArrayList<Class<? extends Mob>> rot =
						MobSpawner.getMobRotation(d);
				if (rot == null) continue;
				for (Class<? extends Mob> c : rot) {
					if (!list.contains(c)) list.add(c);
				}
			} catch (Throwable ignored) {
				//某一层取不到就跳过，不影响其它层
			}
		}

		//再补上 Boss 与常见召唤物 —— 它们不在普通轮换表里，
		//但外观很有辨识度，纳入随机池更符合"完全随机"的本意。
		addIfMissing(list, Goo.class);
		addIfMissing(list, Tengu.class);
		addIfMissing(list, DM300.class);
		addIfMissing(list, DwarfKing.class);
		addIfMissing(list, YogDzewa.class);
		addIfMissing(list, YogDzewa.Larva.class);
		addIfMissing(list, YogFist.BurningFist.class);
		addIfMissing(list, YogFist.SoiledFist.class);
		addIfMissing(list, YogFist.RottingFist.class);
		addIfMissing(list, YogFist.RustedFist.class);
		addIfMissing(list, YogFist.BrightFist.class);
		addIfMissing(list, YogFist.DarkFist.class);
		addIfMissing(list, Mimic.class);
		addIfMissing(list, GoldenMimic.class);
		addIfMissing(list, CrystalMimic.class);
		addIfMissing(list, Albino.class);
		addIfMissing(list, ArmoredStatue.class);
		addIfMissing(list, Bee.class);

		allMobClasses = list;
	}

	/** 把类加入列表（去重）。 */
	private static void addIfMissing(java.util.ArrayList<Class<? extends Mob>> list,
									 Class<? extends Mob> c) {
		if (c != null && !list.contains(c)) list.add(c);
	}

	private String pickRandomSpriteClassName() {
		try {
			if (allMobClasses == null) buildAllMobClasses();
			if (allMobClasses.isEmpty()) return null;

			//最多试 8 次：有些怪物类没有 spriteClass（抽象类/占位），
			//跳过它们再抽，而不是直接放弃。
			for (int tries = 0; tries < 8; tries++) {
				Class<? extends Mob> alt = allMobClasses.get(
						com.watabou.utils.Random.Int(allMobClasses.size()));
				Mob probe = Reflection.newInstance(alt);
				if (probe != null && probe.spriteClass != null) {
					return probe.spriteClass.getName();
				}
			}
		} catch (Throwable e) {
			//忽略：拿不到就保持原贴图
		}
		return null;
	}
	
	@Override
	protected boolean act() {
		
		super.act();

		//==== END(挑战 18 老龄化): 普通怪物每回合 13% 概率睡眠 1 回合 ====
		//放在 paralysed 判定**之前**：挂上 MagicalSleep 后，
		//本回合就直接消耗掉（与"睡眠 1 回合"的语义一致）。
		//Boss / 精英怪免疫，见 ChallengeEffects.rollAgingSleep。
		if (state != SLEEPING
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.rollAgingSleep(this)) {
			//==== END(修复 18): 改成自己的 AgingSleep ====
			//原先用 Drowsy，但 Drowsy.act() 会挂 MagicalSleep ——
			//于是"睡眠 2 回合"变成了"永久魔法睡眠"，玩家实测就是这个现象。
			//
			//AgingSleep 是真睡眠（外观 Zzz），但会**自然醒**，且绝不挂 MagicalSleep。
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
					this, com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.AgingSleep.class,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.AgingSleep.DURATION);
		}

		boolean justAlerted = alerted;
		alerted = false;
		
		if (justAlerted){
			sprite.showAlert();
		} else {
			sprite.hideAlert();
			sprite.hideLost();
			sprite.hideInvestigate();
		}
		
		if (paralysed > 0) {
			enemySeen = false;
			spend( TICK );
			return true;
		}

		if (buff(Terror.class) != null || buff(Dread.class) != null ){
			state = FLEEING;
		}
		
		enemy = chooseEnemy();
		
		boolean enemyInFOV = enemy != null && enemy.isAlive() && fieldOfView[enemy.pos] && enemy.invisible <= 0;

		//prevents action, but still updates enemy seen status
		if (buff(Feint.AfterImage.FeintConfusion.class) != null){
			enemySeen = enemyInFOV;
			spend( TICK );
			return true;
		}

		boolean result = state.act( enemyInFOV, justAlerted );

		//for updating hero FOV
		if (buff(PowerOfMany.PowerBuff.class) != null){
			Dungeon.level.updateFieldOfView( this, fieldOfView );
			GameScene.updateFog(pos, viewDistance+(int)Math.ceil(speed()));
		}

		return result;
	}
	
	//FIXME this is sort of a band-aid correction for allies needing more intelligent behaviour
	protected boolean intelligentAlly = false;
	
	protected Char chooseEnemy() {

		//==== END(挑战 152 和平地牢): 合约未破时怪物不主动攻击 ====
		//文档所有者说明："所有怪物不会对你有攻击行为，直到你违反了和平合约"。
		//
		//做法：返回 null 表示"没有敌人" —— 怪物不会主动接近、不会攻击，
		//但仍然会正常走动（WANDERING），所以地牢不是空荡荡的。
		//
		//"违反合约"由玩家**主动攻击任何怪物**触发（见 Char.attack），
		//触发后本层恢复原版行为；换层时由 resetPeaceful() 重置。
		//
		//只对 ENEMY 阵营生效 —— 玩家的盟友不该被这条规则影响。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.monstersPassive()
				&& alignment == Alignment.ENEMY) {
			return null;
		}

		Dread dread = buff( Dread.class );
		if (dread != null) {
			Char source = (Char)Actor.findById( dread.object );
			if (source != null) {
				return source;
			}
		}

		Terror terror = buff( Terror.class );
		if (terror != null) {
			Char source = (Char)Actor.findById( terror.object );
			if (source != null) {
				return source;
			}
		}
		
		//if we are an alert enemy, auto-hunt a target that is affected by aggression, even another enemy
		if ((alignment == Alignment.ENEMY || buff(Amok.class) != null ) && state != PASSIVE && state != SLEEPING) {
			if (enemy != null && enemy.buff(StoneOfAggression.Aggression.class) != null){
				state = HUNTING;
				return enemy;
			}
			for (Char ch : Actor.chars()) {
				if (ch != this && fieldOfView[ch.pos] &&
						ch.buff(StoneOfAggression.Aggression.class) != null) {
					state = HUNTING;
					return ch;
				}
			}
		}

		//find a new enemy if..
		boolean newEnemy = false;
		//we have no enemy, or the current one is dead/missing
		if ( enemy == null || !enemy.isAlive() || !Actor.chars().contains(enemy) || state == WANDERING) {
			newEnemy = true;
		//We are amoked and current enemy is the hero
		} else if (buff( Amok.class ) != null && enemy == Dungeon.hero) {
			newEnemy = true;
		//We are charmed and current enemy is what charmed us
		} else if (buff(Charm.class) != null && buff(Charm.class).object == enemy.id()) {
			newEnemy = true;
		}

		//additionally, if we are an ally, find a new enemy if...
		if (!newEnemy && alignment == Alignment.ALLY){
			//current enemy is also an ally
			if (enemy.alignment == Alignment.ALLY){
				newEnemy = true;
			//current enemy is invulnerable
			} else if (enemy.isInvulnerable(getClass())){
				newEnemy = true;
			}
		}

		if ( newEnemy ) {

			HashSet<Char> enemies = new HashSet<>();

			//if we are amoked...
			if ( buff(Amok.class) != null) {
				//try to find an enemy mob to attack first.
				for (Mob mob : Dungeon.level.mobs)
					if (mob.alignment == Alignment.ENEMY && mob != this
							&& fieldOfView[mob.pos] && mob.invisible <= 0) {
						enemies.add(mob);
					}
				
				if (enemies.isEmpty()) {
					//try to find ally mobs to attack second.
					for (Mob mob : Dungeon.level.mobs)
						if (mob.alignment == Alignment.ALLY && mob != this
								&& fieldOfView[mob.pos] && mob.invisible <= 0) {
							enemies.add(mob);
						}
					
					if (enemies.isEmpty()) {
						//try to find the hero third
						if (fieldOfView[Dungeon.hero.pos] && Dungeon.hero.invisible <= 0) {
							enemies.add(Dungeon.hero);
						}
					}
				}
				
			//if we are an ally...
			} else if ( alignment == Alignment.ALLY ) {
				//look for hostile mobs to attack
				for (Mob mob : Dungeon.level.mobs)
					if (mob.alignment == Alignment.ENEMY && fieldOfView[mob.pos]
							&& mob.invisible <= 0 && !mob.isInvulnerable(getClass()))
						//do not target passive mobs
						//intelligent allies also don't target mobs which are wandering or asleep
						if (mob.state != mob.PASSIVE &&
								(!intelligentAlly || (mob.state != mob.SLEEPING && mob.state != mob.WANDERING))) {
							enemies.add(mob);
						}
				
			//if we are an enemy...
			} else if (alignment == Alignment.ENEMY) {
				//look for ally mobs to attack
				for (Mob mob : Dungeon.level.mobs)
					if (mob.alignment == Alignment.ALLY && fieldOfView[mob.pos] && mob.invisible <= 0)
						enemies.add(mob);

				//and look for the hero
				if (fieldOfView[Dungeon.hero.pos] && Dungeon.hero.invisible <= 0) {
					enemies.add(Dungeon.hero);
				}
				
			}

			//do not target anything that's charming us
			Charm charm = buff( Charm.class );
			if (charm != null){
				Char source = (Char)Actor.findById( charm.object );
				if (source != null && enemies.contains(source) && enemies.size() > 1){
					enemies.remove(source);
				}
			}

			//neutral characters in particular do not choose enemies.
			if (enemies.isEmpty()){
				return null;
			} else {
				//go after the closest potential enemy, preferring enemies that can be reached/attacked, and the hero if two are equidistant
				PathFinder.buildDistanceMap(pos, Dungeon.findPassable(this, Dungeon.level.passable, fieldOfView, true));
				Char closest = null;
				int closestDist = Integer.MAX_VALUE;

				for (Char curr : enemies){
					int currDist = Integer.MAX_VALUE;
					//we aren't trying to move into the target, just toward them
					for (int i : PathFinder.NEIGHBOURS8){
						if (PathFinder.distance[curr.pos+i] < currDist){
							currDist = PathFinder.distance[curr.pos+i];
						}
					}
					if (closest == null){
						closest = curr;
						closestDist = currDist;
					} else if (canAttack(closest) && !canAttack(curr)){
						continue;
					} else if ((canAttack(curr) && !canAttack(closest))
							|| (currDist < closestDist)){
						closest = curr;
					} else if ( curr == Dungeon.hero &&
							(currDist == closestDist) || (canAttack(curr) && canAttack(closest))){
						closest = curr;
					}
				}
				//if we were going to target the hero, but an afterimage exists, target that instead
				if (closest == Dungeon.hero){
					for (Char ch : enemies){
						if (ch instanceof Feint.AfterImage){
							closest = ch;
							break;
						}
					}
				}

				return closest;
			}

		} else
			return enemy;
	}
	
	@Override
	public boolean add( Buff buff ) {
		if (super.add( buff )) {
			if (buff instanceof Amok || buff instanceof AllyBuff) {
				state = HUNTING;
			} else if (buff instanceof Terror || buff instanceof Dread) {
				state = FLEEING;
			} else if (buff instanceof Sleep) {
				state = SLEEPING;
				postpone(Sleep.SWS);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean remove( Buff buff ) {
		if (super.remove( buff )) {
			if (state == FLEEING && ((buff instanceof Terror && buff(Dread.class) == null)
					|| (buff instanceof Dread && buff(Terror.class) == null))) {
				if (enemySeen) {
					sprite.showStatus(CharSprite.WARNING, Messages.get(this, "rage"));
					state = HUNTING;
				} else {
					state = WANDERING;
				}
			}
			return true;
		}
		return false;
	}
	
	protected boolean canAttack( Char enemy ) {
		if (Dungeon.level.adjacent( pos, enemy.pos )){
			return true;
		}
		for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
			if (buff.canAttackWithExtraReach( enemy )){
				return true;
			}
		}
		return false;
	}

	private boolean cellIsPathable( int cell ){
		if (!Dungeon.level.passable[cell]){
			if (flying || buff(Amok.class) != null){
				if (!Dungeon.level.avoid[cell]){
					return false;
				}
			} else {
				return false;
			}
		}
		if (Char.hasProp(this, Char.Property.LARGE) && !Dungeon.level.openSpace[cell]){
			return false;
		}
		if (Actor.findChar(cell) != null){
			return false;
		}

		return true;
	}

	protected boolean getCloser( int target ) {
		
		if (rooted || target == pos || !Dungeon.level.insideMap(target)) {
			return false;
		}

		int step = -1;

		if (Dungeon.level.adjacent( pos, target )) {

			path = null;

			if (cellIsPathable(target)) {
				step = target;
			}

		} else {

			boolean newPath = false;
			float longFactor = state == WANDERING ? 2f : 1.33f;
			//scrap the current path if it's empty, no longer connects to the current location
			//or if it's quite inefficient and checking again may result in a much better path
			//mobs are much more tolerant of inefficient paths if wandering
			if (path == null || path.isEmpty()
					|| !Dungeon.level.adjacent(pos, path.getFirst())
					|| path.size() > longFactor*Dungeon.level.distance(pos, target))
				newPath = true;
			else if (path.getLast() != target) {
				//if the new target is adjacent to the end of the path, adjust for that
				//rather than scrapping the whole path.
				if (Dungeon.level.adjacent(target, path.getLast())) {
					int last = path.removeLast();

					if (path.isEmpty()) {

						//shorten for a closer one
						if (Dungeon.level.adjacent(target, pos)) {
							path.add(target);
						//extend the path for a further target
						} else {
							path.add(last);
							path.add(target);
						}

					} else {
						//if the new target is simply 1 earlier in the path shorten the path
						if (path.getLast() == target) {

						//if the new target is closer/same, need to modify end of path
						} else if (Dungeon.level.adjacent(target, path.getLast())) {
							path.add(target);

						//if the new target is further away, need to extend the path
						} else {
							path.add(last);
							path.add(target);
						}
					}

				} else {
					newPath = true;
				}

			}

			//checks if the next cell along the current path can be stepped into
			if (!newPath) {
				int nextCell = path.removeFirst();
				if (!cellIsPathable(nextCell)) {

					newPath = true;
					//If the next cell on the path can't be moved into, see if there is another cell that could replace it
					if (!path.isEmpty()) {
						for (int i : PathFinder.NEIGHBOURS8) {
							if (Dungeon.level.adjacent(pos, nextCell + i) && Dungeon.level.adjacent(nextCell + i, path.getFirst())) {
								if (cellIsPathable(nextCell+i)){
									path.addFirst(nextCell+i);
									newPath = false;
									break;
								}
							}
						}
					}
				} else {
					path.addFirst(nextCell);
				}
			}

			//generate a new path
			if (newPath) {
				//If we aren't hunting, always take a full path
				PathFinder.Path full = Dungeon.findPath(this, target, Dungeon.level.passable, fieldOfView, true);
				if (state != HUNTING){
					path = full;
				} else {
					//otherwise, check if other characters are forcing us to take a very slow route
					// and don't try to go around them yet in response, basically assume their blockage is temporary
					PathFinder.Path ignoreChars = Dungeon.findPath(this, target, Dungeon.level.passable, fieldOfView, false);
					if (ignoreChars != null && (full == null || full.size() > 2*ignoreChars.size())){
						//check if first cell of shorter path is valid. If it is, use new shorter path. Otherwise do nothing and wait.
						path = ignoreChars;
						if (!cellIsPathable(ignoreChars.getFirst())) {
							return false;
						}
					} else {
						path = full;
					}
				}
			}

			if (path != null) {
				step = path.removeFirst();
			} else {
				return false;
			}
		}
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean getFurther( int target ) {
		if (rooted || target == pos) {
			return false;
		}
		
		int step = Dungeon.flee( this, target, Dungeon.level.passable, fieldOfView, true );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void updateSpriteState() {
		super.updateSpriteState();
		if (Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class) != null
				|| Dungeon.hero.buff(Swiftthistle.TimeBubble.class) != null)
			sprite.add( CharSprite.State.PARALYSED );
	}
	
	public float attackDelay() {
		float delay = 1f;
		if ( buff(Adrenaline.class) != null) delay /= 1.5f;
		return delay;
	}
	
	protected boolean doAttack( Char enemy ) {

		//==== END(挑战 164 魔法地牢): 怪物有 13% 概率施放随机魔法 ====
		//文档所有者说明："怪物有 13% 的可以使用随机一种魔法"
		//
		//做法：在**能打到**的前提下，有 13% 概率改为"放一个法杖法术"
		//而不是普通攻击。"魔法"用法杖的法术表示 —— 那是本作里
		//最接近"怪物施法"的现成机制，不必另造一套。
		//
		//判据要求相邻或已在射程内：否则怪物会隔着半张地图放法术，
		//那既不合理也很难躲。
		if (enemy != null && Dungeon.level != null
				&& (Dungeon.level.adjacent(pos, enemy.pos)
						|| Dungeon.level.distance(pos, enemy.pos) <= 4)
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.rollMagicMob(this)
				&& Random.Int(100) < 13) {
			com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand spell =
					com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.rollMobMagic();
			if (spell != null) {
				//等级随层数走（否则低层怪物放出高层法术会过于致命）
				spell.level(Math.max(0, Dungeon.depth / 5));

				//用 CursedWand.cursedZap()：它接受**任意 Char 作为施法者**，
				//正是"怪物放法术"需要的入口。
				//
				//为什么不自己调 wand.fx()/onZap()：
				//那两个依赖 Item.curUser（**static Hero**），
				//怪物根本塞不进去。cursedZap 则显式接收 user 参数。
				com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand
						.cursedZap(spell, this,
								new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(pos, enemy.pos,
										com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.MAGIC_BOLT),
								new com.watabou.utils.Callback() {
									@Override
									public void call() {
										//施法结束，什么都不用做
									}
								});

				if (sprite != null) sprite.zap(enemy.pos);
				Invisibility.dispel(this);
				spend(attackDelay());
				return true;
			}
		}

		//==== END(挑战 76 原始状态): 非远程怪物扔石头 ====
		//原表："非远程怪物可扔石头进行远程攻击"
		//
		//判据：**不相邻**（相邻就走原本的近战路径）+ **不是远程怪**
		//（远程怪本来就能打到，不需要扔石头）。
		//
		//为什么放在 doAttack 而不是 canAttack：
		//canAttack 决定"要不要走过去打"，若在那里返回 true，
		//怪物会站在原地扔石头而不再靠近 —— 那就不是"原始状态"而是"懦夫状态"了。
		//放在这里的效果是：**怪物该靠近就靠近，实在打不到时才扔石头**。
		if (enemy != null && Dungeon.level != null
				&& !Dungeon.level.adjacent(pos, enemy.pos)
				&& com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.canThrowRock(this, enemy)) {
			if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeRockFall.throwRockAt(this, enemy)) {
				Invisibility.dispel(this);
				spend( attackDelay() );
				return true;
			}
		}

		if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
			sprite.attack( enemy.pos );
			return false;
			
		} else {
			attack( enemy );
			Invisibility.dispel(this);
			spend( attackDelay() );
			return true;
		}
	}
	
	@Override
	public void onAttackComplete() {
		attack( enemy );
		Invisibility.dispel(this);
		spend( attackDelay() );
		super.onAttackComplete();
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		if (buff(GuidingLight.Illuminated.class) != null && Dungeon.hero.heroClass == HeroClass.CLERIC){
			//if the attacker is the cleric, they must be using a weapon they have the str for
			if (enemy instanceof Hero){
				Hero h = (Hero) enemy;
				if (!(h.belongings.attackingWeapon() instanceof Weapon)
						|| ((Weapon) h.belongings.attackingWeapon()).STRReq() <= h.STR()){
					return 0;
				}
			} else {
				return 0;
			}
		}

		if ( !surprisedBy(enemy)
				&& paralysed == 0
				&& !(alignment == Alignment.ALLY && enemy == Dungeon.hero)) {
			return this.defenseSkill;
		} else {
			return 0;
		}
	}
	
	@Override
	public int defenseProc( Char enemy, int damage ) {
		
		if (enemy instanceof Hero
				&& ((Hero) enemy).belongings.attackingWeapon() instanceof MissileWeapon){
			Statistics.thrownAttacks++;
			Badges.validateHuntressUnlock();
		}
		
		if (surprisedBy(enemy)) {
			Statistics.sneakAttacks++;
			Badges.validateRogueUnlock();
			//TODO this is somewhat messy, it would be nicer to not have to manually handle delays here
			// playing the strong hit sound might work best as another property of weapon?
			if (Dungeon.hero.belongings.attackingWeapon() instanceof SpiritBow.SpiritArrow
				|| Dungeon.hero.belongings.attackingWeapon() instanceof Dart){
				Sample.INSTANCE.playDelayed(Assets.Sounds.HIT_STRONG, 0.125f);
			} else {
				Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
			}
			if (enemy.buff(Preparation.class) != null) {
				Wound.hit(this);
			} else {
				Surprise.hit(this);
			}
		}

		//if attacked by something else than current target, and that thing is closer, switch targets
		//or if attacked by target, simply update target position
		if (state != FLEEING) {
			if (state != HUNTING) {
				aggro(enemy);
				target = enemy.pos;
			} else {
				recentlyAttackedBy.add(enemy);
			}
		}

		if (buff(SoulMark.class) != null) {
			int restoration = Math.min(damage, HP+shielding());
			
			//physical damage that doesn't come from the hero is less effective
			if (enemy != Dungeon.hero){
				restoration = Math.round(restoration * 0.4f*Dungeon.hero.pointsInTalent(Talent.SOUL_SIPHON)/3f);
			}
			if (restoration > 0) {
				Buff.affect(Dungeon.hero, Hunger.class).affectHunger(restoration*Dungeon.hero.pointsInTalent(Talent.SOUL_EATER)/3f);

				if (Dungeon.hero.HP < Dungeon.hero.HT) {
					int heal = (int)Math.ceil(restoration * 0.4f);
					Dungeon.hero.HP = Math.min(Dungeon.hero.HT, Dungeon.hero.HP + heal);
					Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
				}
			}
		}

		return super.defenseProc(enemy, damage);
	}

	@Override
	public float speed() {
		return super.speed() * AscensionChallenge.enemySpeedModifier(this);
	}

	public final boolean surprisedBy( Char enemy ){
		return surprisedBy( enemy, true);
	}

	public boolean surprisedBy( Char enemy, boolean attacking ){
		return enemy == Dungeon.hero
				&& (enemy.invisible > 0 || !enemySeen || (fieldOfView != null && fieldOfView.length == Dungeon.level.length() && !fieldOfView[enemy.pos]))
				&& (!attacking || enemy.canSurpriseAttack());
	}

	//whether the hero should interact with the mob (true) or attack it (false)
	public boolean heroShouldInteract(){
		return alignment != Alignment.ENEMY && buff(Amok.class) == null;
	}

	public void aggro( Char ch ) {
		enemy = ch;
		if (state != PASSIVE){
			state = HUNTING;
		}
	}

	public void clearEnemy(){
		enemy = null;
		enemySeen = false;
		if (state == HUNTING) state = WANDERING;
	}
	
	public boolean isTargeting( Char ch){
		return enemy == ch;
	}

	@Override
	public void damage( int dmg, Object src ) {

		//END(挑战 9 狂暴): 记录受击前血量，用于判断本次是否真的掉血
		final int hpBefore = HP;

		//==== END(挑战 28 不动如山): 13% 概率完全免疫 ====
		//放在入口处：被免疫的这次伤害不进入任何后续流程
		//（不触发 9 狂暴的标记、不触发流血等）。
		if (dmg > 0 && com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.immovableBlocks(this)) {
			if (sprite != null) {
				sprite.showStatus(com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE,
						com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
								com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
										.ChallengeEffects.class, "immovable_block"));
			}
			return;
		}

		if (!isInvulnerable(src.getClass())) {
			if (state == SLEEPING) {
				state = WANDERING;
			}
			if (!(src instanceof Corruption) && state != FLEEING) {
				if (state != HUNTING) {
					alerted = true;
					//assume the hero is hitting us in these common cases
					if (src instanceof Wand || src instanceof ClericSpell || src instanceof ArmorAbility) {
						aggro(Dungeon.hero);
						target = Dungeon.hero.pos;
					}
				} else {
					if (src instanceof Wand || src instanceof ClericSpell || src instanceof ArmorAbility) {
						recentlyAttackedBy.add(Dungeon.hero);
					}
				}
			}
		}
		
		super.damage( dmg, src );

		//==== END(挑战 9 狂暴): 怪物受击后获得 20% 攻击提升 ====
		//用"实际掉血"判断是否真的受了伤 —— super.damage() 内部可能
		//因无敌/拦截（物极必反等）完全不扣血，那种情况不该触发狂暴。
		//持续 2 回合、不叠加，由 ChallengeBerserkMark 自己维护倒计时。
		if (hpBefore > HP) {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.markBerserk(this);
		}
	}
	
	
	@Override
	public void destroy() {
		
		super.destroy();
		
		Dungeon.level.mobs.remove( this );

		if (Dungeon.hero.buff(MindVision.class) != null){
			Dungeon.observe();
			GameScene.updateFog(pos, 2);
		}

		if (Dungeon.hero.isAlive()) {
			
			if (alignment == Alignment.ENEMY) {
				Statistics.enemiesSlain++;
				Badges.validateMonstersSlain();
				Statistics.qualifiedForNoKilling = false;
				Bestiary.setSeen(getClass());
				Bestiary.countEncounter(getClass());

				AscensionChallenge.processEnemyKill(this);
				
				int exp = Dungeon.hero.lvl <= maxLvl ? EXP : 0;

				//during ascent, under-levelled enemies grant 10 xp each until level 30
				// after this enemy kills which reduce the amulet curse still grant 10 effective xp
				// for the purposes of on-exp effects, see AscensionChallenge.processEnemyKill
				if (Dungeon.hero.buff(AscensionChallenge.class) != null &&
						exp == 0 && maxLvl > 0 && EXP > 0 && Dungeon.hero.lvl < Hero.MAX_LEVEL){
					exp = Math.round(10 * spawningWeight());
				}

				if (exp > 0) {
					Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(exp), FloatingText.EXPERIENCE);
				}
				Dungeon.hero.earnExp(exp, getClass());

				if (Dungeon.hero.subClass == HeroSubClass.MONK){
					Buff.affect(Dungeon.hero, MonkEnergy.class).gainEnergy(this);
				}
			}
		}
	}
	
	/**
	 * END(挑战 73 神秘复苏): 把这只怪"变成幽灵"。
	 *
	 * <h3>为什么不换成另一个实例</h3>
	 * 我们正在 {@code onAdd()} 里 —— 那时 this 已经在 level.mobs 里了，
	 * 新建一个 Wraith 再删掉自己会打乱 Actor 的注册顺序（而且可能递归）。
	 *
	 * <p>所以改为**就地改造**：把外观与关键数值改成幽灵的样子。
	 * 行为上它就已经是一只幽灵了（飞行、1 血、无经验）。
	 *
	 * <h3>改了哪些</h3>
	 * <ul>
	 *   <li>{@code spriteClass} → {@code WraithSprite}（外观）</li>
	 *   <li>HP/HT → 1（幽灵只有 1 点血）</li>
	 *   <li>EXP → 0（打死幽灵不给经验）</li>
	 *   <li>{@code flying} → true</li>
	 *   <li>属性加 UNDEAD / INORGANIC</li>
	 * </ul>
	 */
	private void becomeWraith() {
		try {
			spriteClass = com.shatteredpixel.shatteredpixeldungeon.sprites
					.WraithSprite.class;

			HP = HT = 1;
			EXP = 0;
			flying = true;

			properties.add(Property.UNDEAD);
			properties.add(Property.INORGANIC);
		} catch (Throwable t) {
			//改造失败就保持原样 —— 那只是少一只幽灵
		}
	}

	@Override
	public void die( Object cause ) {

		if (cause == Chasm.class){
			//50% chance to round up, 50% to round down
			if (EXP % 2 == 1) EXP += Random.Int(2);
			EXP /= 2;
		}

		if (alignment == Alignment.ENEMY){
			if (buff(Trap.HazardAssistTracker.class) != null){
				Statistics.hazardAssistedKills++;
				Badges.validateHazardAssists();
			}

			//==== END(挑战 87 盗贼鼠群): 击杀带赃款的怪 → 双倍返还 ====
			//放在 rollToDropLoot 之前：返还是"把偷走的钱还回来"，
			//不是掉落物，所以不走掉落管线（否则会被 35/36/47/48 等
			//掉落倍率规则影响，那就偏离"返还"的语义了）。
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.onThiefKilled(this);

			//==== END(挑战 108 装备觉醒): 武器击杀计数 ====
			//原表："武器击杀 50 后觉醒，获得一条随机附魔词缀"。
			//只统计玩家用武器造成的击杀；法术/陷阱/环境致死不计。
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.onMobKilledForAwakening(this, cause);

			//==== END(挑战 126 格林之心): 杀怪得黑之魂 ====
			//这条与 108 无关，是独立的成长系统（126 关闭了经验，改为攒魂）。
			com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.BlackSoul.onMobKilled(this);

			//==== END(挑战 77 亡灵法师): 死亡后 20% 留下幽灵 ====
			//原表："怪物死亡后 20% 变成幽灵"
			//在原地生成一只真幽灵（而不是像 73 那样就地改造自己 ——
			//那时它已经死了，改造没有意义）。
			if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.rollNecromancer(this)) {
				com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.spawnWraithAt(pos);
			}

			//==== END(挑战 86 复仇之魂): 10% 在下一层复仇 ====
			//原表："被击杀怪物 10% 概率在下一层以幽灵形式复仇"
			//这里只**记账**，真正的生成发生在进入下一层时
			//（Level.create() 调 consumeVengefulSouls）。
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.rollVengefulSoul(this);

			rollToDropLoot();

			//==== END(挑战 34 赏金制度): 击杀精英 / Boss 额外掉落金币 ====
			//只对有"精英 buff"或 Boss/小 Boss 属性的怪物生效，普通怪返回 0。
			//走 level.drop 而不是直接改 Dungeon.gold —— 这样金币会正常堆叠、
			//正常触发拾取流程，也让"宝物猎人/贫瘠"等掉落规则能照常作用于它。
			int bounty = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.bountyGoldFor(this);
			if (bounty > 0 && Dungeon.level != null) {
				Dungeon.level.drop(
						new com.shatteredpixel.shatteredpixeldungeon.items.Gold(bounty),
						pos).sprite.drop();
			}

			if (cause == Dungeon.hero || cause instanceof Weapon || cause instanceof Weapon.Enchantment){
				if (Dungeon.hero.hasTalent(Talent.LETHAL_MOMENTUM)
						&& Random.Float() < 0.34f + 0.33f* Dungeon.hero.pointsInTalent(Talent.LETHAL_MOMENTUM)){
					Buff.affect(Dungeon.hero, Talent.LethalMomentumTracker.class, 0f);
				}
				if (Dungeon.hero.heroClass != HeroClass.DUELIST
						&& Dungeon.hero.hasTalent(Talent.LETHAL_HASTE)
						&& Dungeon.hero.buff(Talent.LethalHasteCooldown.class) == null){
					Buff.affect(Dungeon.hero, Talent.LethalHasteCooldown.class, 100f);
					Buff.affect(Dungeon.hero, GreaterHaste.class).set(2 + 2*Dungeon.hero.pointsInTalent(Talent.LETHAL_HASTE));
				}
			}

		}

		if (Dungeon.hero.isAlive() && !Dungeon.level.heroFOV[pos]) {
			GLog.i( Messages.get(this, "died") );
		}

		boolean soulMarked = buff(SoulMark.class) != null;

		super.die( cause );

		if (!(this instanceof Wraith)
				&& soulMarked
				&& Random.Float() < (0.4f*Dungeon.hero.pointsInTalent(Talent.NECROMANCERS_MINIONS)/3f)){
			Wraith w = Wraith.spawnAt(pos, Wraith.class);
			if (w != null) {
				Buff.affect(w, Corruption.class);
				if (Dungeon.level.heroFOV[pos]) {
					CellEmitter.get(pos).burst(ShadowParticle.CURSE, 6);
					Sample.INSTANCE.play(Assets.Sounds.CURSED);
				}
			}
		}
	}

	public float lootChance(){
		float lootChance = this.lootChance;

		float dropBonus = RingOfWealth.dropChanceMultiplier( Dungeon.hero );

		Talent.BountyHunterTracker bhTracker = Dungeon.hero.buff(Talent.BountyHunterTracker.class);
		if (bhTracker != null){
			Preparation prep = Dungeon.hero.buff(Preparation.class);
			if (prep != null){
				// 2/4/8/16% per prep level, multiplied by talent points
				float bhBonus = 0.02f * (float)Math.pow(2, prep.attackLevel()-1);
				bhBonus *= Dungeon.hero.pointsInTalent(Talent.BOUNTY_HUNTER);
				dropBonus += bhBonus;
			}
		}

		dropBonus += ShardOfOblivion.lootChanceMultiplier()-1f;

		float chance = lootChance * dropBonus;

		//==== END(挑战 64 宝物猎人): 普通怪物掉落 -30% ====
		//只影响**普通怪**：精英（ChampionEnemy）与 Boss/小 Boss 保持原有掉落，
		//否则"宝物猎人"会连带削掉 Boss 的必掉物，与"宝箱内容增加"的补偿不成比例。
		//注意用 buffs()（复数）：Char.buff(Class) 是精确类匹配，
		//而精英实际挂的是 Blazing/Projecting 等**子类**，用 buff() 查不到。
		boolean isSpecial = !buffs(ChampionEnemy.class).isEmpty()
				|| Char.hasProp(this, Char.Property.BOSS)
				|| Char.hasProp(this, Char.Property.MINIBOSS);
		if (!isSpecial) {
			chance *= com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.monsterDropMultiplier();
		}

		return chance;
	}
	
	public void rollToDropLoot(){
		if (Dungeon.hero.lvl > maxLvl + 2) return;

		//==== END(挑战 167 黄金地牢): 怪物不掉落任何物品（只有金币） ====
		//原表（文档所有者说明）："怪物无法掉落任何物品，只掉金币"。
		//
		//做法：跳过"掉落物"与"财富戒指额外掉落"两段，
		//但**保留**已有的金币掉落（挑战 34 赏金制度那段在下文，不受影响）。
		//
		//注意：Boss 的**任务掉落**（天狗面具等）走的是 level.dropToDrop 之外的
		//路径（Boss 自己的 die() 里直接 drop），所以这里拦住不影响主线。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.goldenNoDrops()) {
			return;
		}

		MasterThievesArmband.StolenTracker stolen = buff(MasterThievesArmband.StolenTracker.class);
		if (stolen == null || !stolen.itemWasStolen()) {
			if (Random.Float() < lootChance()) {
				Item loot = createLoot();
				if (loot != null) {
					Dungeon.level.drop(loot, pos).sprite.drop();
				}
			}
		}
		
		//ring of wealth logic
		if (Ring.getBuffedBonus(Dungeon.hero, RingOfWealth.Wealth.class) > 0) {
			int rolls = 1;
			if (properties.contains(Property.BOSS)) rolls = 15;
			else if (properties.contains(Property.MINIBOSS)) rolls = 5;
			ArrayList<Item> bonus = RingOfWealth.tryForBonusDrop(Dungeon.hero, rolls);
			if (bonus != null && !bonus.isEmpty()) {
				for (Item b : bonus) Dungeon.level.drop(b, pos).sprite.drop();
				RingOfWealth.showFlareForBonusDrop(sprite);
			}
		}
		
		//lucky enchant logic
		if (buff(Lucky.LuckProc.class) != null){
			Dungeon.level.drop(buff(Lucky.LuckProc.class).genLoot(), pos).sprite.drop();
			Lucky.showFlare(sprite);
		}

		//soul eater talent
		if (buff(SoulMark.class) != null &&
				Random.Int(10) < Dungeon.hero.pointsInTalent(Talent.SOUL_EATER)){
			Talent.onFoodEaten(Dungeon.hero, 0, null);
		}

	}
	
	protected Object loot = null;
	protected float lootChance = 0;
	
	@SuppressWarnings("unchecked")
	public Item createLoot() {
		Item item;
		if (loot instanceof Generator.Category) {

			item = Generator.randomUsingDefaults( (Generator.Category)loot );

		} else if (loot instanceof Class<?>) {

			if (ExoticPotion.regToExo.containsKey(loot)){
				if (Random.Float() < ExoticCrystals.consumableExoticChance()){
					return Generator.random(ExoticPotion.regToExo.get(loot));
				}
			} else if (ExoticScroll.regToExo.containsKey(loot)){
				if (Random.Float() < ExoticCrystals.consumableExoticChance()){
					return Generator.random(ExoticScroll.regToExo.get(loot));
				}
			}

			item = Generator.random( (Class<? extends Item>)loot );

		} else {

			item = (Item)loot;

		}
		return item;
	}

	//how many mobs this one should count as when determining spawning totals
	public float spawningWeight(){
		return 1;
	}
	
	public boolean reset() {
		return false;
	}
	
	public void beckon( int cell ) {
		
		notice();
		
		if (state != HUNTING && state != FLEEING) {
			state = WANDERING;
		}
		target = cell;
	}
	
	public String description() {
		return Messages.get(this, "desc");
	}

	public String info(){
		String desc = description();

		for (Buff b : buffs(ChampionEnemy.class)){
			desc += "\n\n_" + Messages.titleCase(b.name()) + "_\n" + b.desc();
		}

		return desc;
	}
	
	public void notice() {
		sprite.showAlert();
	}
	
	public void yell( String str ) {
		GLog.newLine();
		GLog.n( "%s: \"%s\" ", Messages.titleCase(name()), str );
	}

	//some mobs have an associated landmark entry, which is added when the hero sees them
	//mobs may also remove this landmark in some cases, such as when a quest is complete or they die
	public Notes.Landmark landmark(){
		return null;
	}

	public interface AiState {
		boolean act( boolean enemyInFOV, boolean justAlerted );
	}

	protected class Sleeping implements AiState {

		public static final String TAG	= "SLEEPING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {

			//debuffs cause mobs to wake as well
			for (Buff b : buffs()){
				if (b.type == Buff.buffType.NEGATIVE){
					awaken(enemyInFOV);
					if (state == SLEEPING){
						spend(TICK); //wait if we can't wake up for some reason
					}
					return true;
				}
			}

			//can be awoken by the least stealthy hostile present, not necessarily just our target
			if (enemyInFOV || (enemy != null && enemy.invisible > 0)) {

				float highestChance = Float.POSITIVE_INFINITY;
				Char closestHostile = null;

				for (Char ch : Actor.chars()){
					if (fieldOfView[ch.pos] && ch.invisible == 0 && ch.alignment != alignment && ch.alignment != Alignment.NEUTRAL){
						float bestChance = detectionChance(ch);
						//silent steps rogue talent, which also applies to rogue's shadow clone
						if ((ch instanceof Hero || ch instanceof ShadowClone.ShadowAlly)
								&& Dungeon.hero.hasTalent(Talent.SILENT_STEPS)){
							if (distance(ch) >= 4 - Dungeon.hero.pointsInTalent(Talent.SILENT_STEPS)) {
								bestChance = Float.POSITIVE_INFINITY;
							}
						}
						//flying characters are naturally stealthy
						if (ch.flying && distance(ch) >= 2){
							bestChance = Float.POSITIVE_INFINITY;
						}
						if (bestChance < highestChance){
							highestChance = bestChance;
							closestHostile = ch;
						}
					}
				}

				if (closestHostile != null && Random.Float() < detectionChance(closestHostile)) {
					awaken(enemyInFOV);
					if (state == SLEEPING){
						spend(TICK); //wait if we can't wake up for some reason
					}
					return true;
				}

			}

			enemySeen = false;
			spend( TICK );

			return true;
		}

		//chance is 1 in (distance + stealth)
		protected float detectionChance( Char enemy ){
			return 1 / (distance( enemy ) + enemy.stealth());
		}

		protected void awaken( boolean enemyInFOV ){
			if (enemyInFOV) {
				enemySeen = true;
				notice();
				state = HUNTING;
				target = enemy.pos;
			} else {
				notice();
				state = WANDERING;
				target = Dungeon.level.randomDestination( Mob.this );
			}

			if (alignment == Alignment.ENEMY && Dungeon.isChallenged(Challenges.SWARM_INTELLIGENCE)) {
				for (Mob mob : Dungeon.level.mobs) {
					if (mob.paralysed <= 0
							&& Dungeon.level.distance(pos, mob.pos) <= 8
							&& mob.state != mob.HUNTING) {
						mob.beckon(target);
					}
				}
			}
			spend(TIME_TO_WAKE_UP);
		}
	}

	protected class Wandering implements AiState {

		public static final String TAG	= "WANDERING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV && (justAlerted || Random.Float() < detectionChance(enemy))) {

				return noticeEnemy();

			} else {

				return continueWandering();

			}
		}

		//chance is 1 in (distance/2 + stealth)
		protected float detectionChance( Char enemy ){
			return 1 / (distance( enemy ) / 2f + enemy.stealth());
		}

		protected boolean noticeEnemy(){
			enemySeen = true;
			
			notice();
			alerted = true;
			state = HUNTING;
			target = enemy.pos;
			
			if (alignment == Alignment.ENEMY && Dungeon.isChallenged( Challenges.SWARM_INTELLIGENCE )) {
				for (Mob mob : Dungeon.level.mobs) {
					if (mob.paralysed <= 0
							&& Dungeon.level.distance(pos, mob.pos) <= 8
							&& mob.state != mob.HUNTING) {
						mob.beckon( target );
					}
				}
			}
			
			return true;
		}
		
		protected boolean continueWandering(){
			enemySeen = false;
			
			int oldPos = pos;
			if (target != -1 && getCloser( target )) {
				spend( 1 / speed() );
				return moveSprite( oldPos, pos );
			} else {
				target = randomDestination();
				spend( TICK );
			}
			
			return true;
		}

		protected int randomDestination(){
			return Dungeon.level.randomDestination( Mob.this );
		}
		
	}

	//we keep a list of characters we were recently hit by, so we can switch targets if needed
	protected ArrayList<Char> recentlyAttackedBy = new ArrayList<>();

	protected class Hunting implements AiState {

		public static final String TAG	= "HUNTING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			if (enemyInFOV && !isCharmedBy( enemy ) && canAttack( enemy )) {

				recentlyAttackedBy.clear();
				target = enemy.pos;
				return doAttack( enemy );

			} else {

				//if we cannot attack our target, but were hit by something else that
				// is visible and attackable or closer, swap targets
				if (handleRecentAttackers()){
					return act( true, justAlerted );
				}

				if (enemyInFOV) {
					target = enemy.pos;
				} else if (enemy == null) {
					sprite.showLost();
					state = WANDERING;
					target = ((Mob.Wandering)WANDERING).randomDestination();
					spend( TICK );
					return true;
				}
				
				int oldPos = pos;
				if (target != -1 && getCloser( target )) {
					
					spend( 1 / speed() );
					return moveSprite( oldPos,  pos );

				} else {

					return handleUnreachableTarget(enemyInFOV, justAlerted);
				}
			}
		}

		protected boolean handleRecentAttackers(){
			boolean swapped = false;
			if (!recentlyAttackedBy.isEmpty()){
				for (Char ch : recentlyAttackedBy){
					if (ch != null && ch.isActive() && Actor.chars().contains(ch) && alignment != ch.alignment && fieldOfView[ch.pos] && ch.invisible == 0 && !isCharmedBy(ch)) {
						if (canAttack(ch) || enemy == null || Dungeon.level.distance(pos, ch.pos) < Dungeon.level.distance(pos, enemy.pos)) {
							enemy = ch;
							target = ch.pos;
							swapped = true;
						}
					}
				}
				recentlyAttackedBy.clear();
			}
			return swapped;
		}

		//prevents rare infinite loop cases
		protected boolean recursing = false;

		//Try to switch targets to another enemy that is closer or reachable
		//unless we have already done that and still can't move toward them, then move on.
		protected boolean handleUnreachableTarget(boolean enemyInFOV, boolean justAlerted){
			if (!recursing) {
				Char oldEnemy = enemy;
				enemy = null;
				enemy = chooseEnemy();
				if (enemy != null && enemy != oldEnemy) {
					recursing = true;
					boolean result = act(enemyInFOV, justAlerted);
					recursing = false;
					return result;
				}
			}

			spend( TICK );
			if (!enemyInFOV) {
				sprite.showLost();
				state = WANDERING;
				target = ((Mob.Wandering)WANDERING).randomDestination();
			}
			return true;
		}
	}

	//essentially a more aggressive version of wandering, where target pos is updated like hunting
	//not currently used directly by mobs outside of the vault, which also add more behaviour here
	protected class Investigating extends Wandering {

		public static final String TAG	= "INVESTIGATING";

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (enemyInFOV){
				target = enemy.pos;
			} else {
				//we lose our target BEFORE reaching their last known position
				if (Dungeon.level.distance(pos, target) <= 1){
					sprite.showLost();
					state = WANDERING;
					target = ((Mob.Wandering)WANDERING).randomDestination();
					spend( TICK );
					return true;
				}
			}
			return super.act(enemyInFOV, justAlerted);
		}

		//same detection chance as wandering

	}

	protected class Fleeing implements AiState {

		public static final String TAG	= "FLEEING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			//triggers escape logic when 0-dist rolls a 6 or greater.
			if (enemy == null || !enemyInFOV && 1 + Random.Int(Dungeon.level.distance(pos, target)) >= 6){
				escaped();
				if (state != FLEEING){
					spend( TICK );
					return true;
				}
			
			//if enemy isn't in FOV, keep running from their previous position.
			} else if (enemyInFOV) {
				target = enemy.pos;
			}

			int oldPos = pos;
			if (target != -1 && getFurther( target )) {

				spend( 1 / speed() );
				return moveSprite( oldPos, pos );

			} else {

				spend( TICK );
				nowhereToRun();

				return true;
			}
		}

		protected void escaped(){
			//does nothing by default, some enemies have special logic for this
		}

		//enemies will turn and fight if they have nowhere to run and aren't affect by terror
		protected void nowhereToRun() {
			if (buff( Terror.class ) == null && buff( Dread.class ) == null) {
				if (enemySeen) {
					sprite.showStatus(CharSprite.WARNING, Messages.get(Mob.class, "rage"));
					state = HUNTING;
				} else {
					state = WANDERING;
				}
			}
		}
	}

	protected class Passive implements AiState {

		public static final String TAG	= "PASSIVE";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			spend( TICK );
			return true;
		}
	}
	
	
	private static ArrayList<Mob> heldAllies = new ArrayList<>();

	public static void holdAllies( Level level ){
		holdAllies(level, Dungeon.hero.pos);
	}

	public static void holdAllies( Level level, int holdFromPos ){
		heldAllies.clear();
		for (Mob mob : level.mobs.toArray( new Mob[0] )) {
			//preserve directable allies or empowered intelligent allies no matter where they are
			if (mob instanceof DirectableAlly
				|| (mob.intelligentAlly && PowerOfMany.getPoweredAlly() == mob)) {
				if (mob instanceof DirectableAlly) {
					((DirectableAlly) mob).clearDefensingPos();
				}
				level.mobs.remove( mob );
				heldAllies.add(mob);
				
			//preserve other intelligent allies if they are near the hero
			} else if (mob.alignment == Alignment.ALLY
					&& mob.intelligentAlly
					&& Dungeon.level.distance(holdFromPos, mob.pos) <= 5){
				level.mobs.remove( mob );
				heldAllies.add(mob);
			}
		}
	}

	public static void restoreAllies( Level level, int pos ){
		restoreAllies(level, pos, -1);
	}

	public static void restoreAllies( Level level, int pos, int gravitatePos ){
		if (!heldAllies.isEmpty()){
			
			ArrayList<Integer> candidatePositions = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8) {
				if (!Dungeon.level.solid[i+pos] && !Dungeon.level.avoid[i+pos] && level.findMob(i+pos) == null){
					candidatePositions.add(i+pos);
				}
			}

			//gravitate pos sets a preferred location for allies to be closer to
			if (gravitatePos == -1) {
				Collections.shuffle(candidatePositions);
			} else {
				Collections.sort(candidatePositions, new Comparator<Integer>() {
					@Override
					public int compare(Integer t1, Integer t2) {
						return Dungeon.level.distance(gravitatePos, t1) -
								Dungeon.level.distance(gravitatePos, t2);
					}
				});
			}

			//can only have one empowered ally at once, prioritize incoming ally
			if (Stasis.getStasisAlly() != null){
				for (Mob mob : level.mobs.toArray( new Mob[0] )) {
					if (mob.buff(PowerOfMany.PowerBuff.class) != null){
						mob.buff(PowerOfMany.PowerBuff.class).detach();
					}
				}
			}
			
			for (Mob ally : heldAllies) {

				//can only have one empowered ally at once, prioritize incoming ally
				if (ally.buff(PowerOfMany.PowerBuff.class) != null){
					for (Mob mob : level.mobs.toArray( new Mob[0] )) {
						if (mob.buff(PowerOfMany.PowerBuff.class) != null){
							mob.buff(PowerOfMany.PowerBuff.class).detach();
						}
					}
				}

				level.mobs.add(ally);
				ally.state = ally.WANDERING;
				
				if (!candidatePositions.isEmpty()){
					ally.pos = candidatePositions.remove(0);
				} else {
					ally.pos = pos;
				}
				if (ally.sprite != null) ally.sprite.place(ally.pos);

				if (ally.fieldOfView == null || ally.fieldOfView.length != level.length()){
					ally.fieldOfView = new boolean[level.length()];
				}
				Dungeon.level.updateFieldOfView( ally, ally.fieldOfView );
				
			}
		}
		heldAllies.clear();
	}
	
	public static void clearHeldAllies(){
		heldAllies.clear();
	}
}

