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

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SacrificialFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.SmokeScreen;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WellWater;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.PinCushion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Shadows;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.DivineSense;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Stasis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Piranha;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Sheep;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SacrificialParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.WindParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Stylus;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.DimensionalSundial;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.EyeOfNewt;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.MossyClump;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrapMechanism;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.HeavyBoomerang;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.HighGrass;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.watabou.utils.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public abstract class Level implements Bundlable {
	
	public static enum Feeling {
		NONE,
		CHASM,
		WATER,
		GRASS,
		DARK,
		LARGE,
		TRAPS,
		SECRETS;

		public String title(){
			return Messages.get(this, name()+"_title");
		}

		public String desc() {
			return Messages.get(this, name()+"_desc");
		}
	}

	protected int width;
	protected int height;
	protected int length;
	
	protected static final float TIME_TO_RESPAWN	= 50;

	public int version;
	
	public int[] map;
	public boolean[] visited;
	public boolean[] mapped;
	public boolean[] discoverable;

	public int viewDistance = Dungeon.isChallenged( Challenges.DARKNESS ) ? 2 : 8;
	
	public boolean[] heroFOV;
	
	public boolean[] passable;
	public boolean[] losBlocking;
	public boolean[] flamable;
	public boolean[] secret;
	public boolean[] solid;
	public boolean[] avoid;
	public boolean[] water;
	public boolean[] pit;

	public boolean[] openSpace;
	
	public Feeling feeling = Feeling.NONE;
	
	public int entrance;
	public int exit;

	public ArrayList<LevelTransition> transitions;

	//END(port from Arknights): 平台系统（潮汐/海面平台）
	public com.watabou.utils.SparseArray<com.shatteredpixel.shatteredpixeldungeon.levels.features.Platform> platforms
			= new com.watabou.utils.SparseArray<>();
	//END(port from Arknights): 海怪（海嗣 Boss 场地的触手/暗礁）
	public com.watabou.utils.SparseArray<com.shatteredpixel.shatteredpixeldungeon.levels.features.SeaTerror> seaTerrors
			= new com.watabou.utils.SparseArray<>();

	//when a boss level has become locked.
	public boolean locked = false;
	
	public HashSet<Mob> mobs;
	public SparseArray<Heap> heaps;
	public HashMap<Class<? extends Blob>,Blob> blobs;
	public SparseArray<Plant> plants;
	public SparseArray<Trap> traps;
	public ArrayList<CustomTilemap> customTiles;
	public ArrayList<CustomTilemap> customWalls;
	
	protected ArrayList<Item> itemsToSpawn = new ArrayList<>();

	protected Group visuals;
	protected Group wallVisuals;
	
	public int color1 = 0x004400;
	public int color2 = 0x88CC44;

	//END(移植自魔绫): 是否绘制额外的玻璃/高光层；部分挑战区关卡(如 HollowLevel)会关掉它。
	public boolean extraGlass = true;

	private static final String VERSION     = "version";
	private static final String WIDTH       = "width";
	private static final String HEIGHT      = "height";
	private static final String MAP			= "map";
	private static final String VISITED		= "visited";
	private static final String MAPPED		= "mapped";
	private static final String TRANSITIONS	= "transitions";
	private static final String LOCKED      = "locked";
	private static final String HEAPS		= "heaps";
	private static final String PLANTS		= "plants";
	private static final String TRAPS       = "traps";
	private static final String CUSTOM_TILES= "customTiles";
	private static final String CUSTOM_WALLS= "customWalls";
	private static final String MOBS		= "mobs";
	private static final String BLOBS		= "blobs";
	private static final String FEELING		= "feeling";

	public void create() {

		Random.pushGenerator( Dungeon.seedCurDepth() );

		//TODO maybe just make this part of RegularLevel?
		if (!Dungeon.bossLevel() && Dungeon.branch == 0) {

			addItemToSpawn(Generator.random(Generator.Category.FOOD));

			if (Dungeon.posNeeded()) {
				Dungeon.LimitedDrops.STRENGTH_POTIONS.count++;
				addItemToSpawn( new PotionOfStrength() );
			}
			if (Dungeon.souNeeded()) {
				Dungeon.LimitedDrops.UPGRADE_SCROLLS.count++;
				//every 2nd scroll of upgrade is removed with forbidden runes challenge on
				//TODO while this does significantly reduce this challenge's levelgen impact, it doesn't quite remove it
				//for 0 levelgen impact, we need to do something like give the player all SOU, but nerf them
				//or give a random scroll (from a separate RNG) instead of every 2nd SOU
				if (!Dungeon.isChallenged(Challenges.NO_SCROLLS) || Dungeon.LimitedDrops.UPGRADE_SCROLLS.count%2 != 0){
					addItemToSpawn(new ScrollOfUpgrade());
				}
			}
			if (Dungeon.asNeeded()) {
				Dungeon.LimitedDrops.ARCANE_STYLI.count++;
				addItemToSpawn( new Stylus() );
			}
			if ( Dungeon.enchStoneNeeded() ){
				Dungeon.LimitedDrops.ENCH_STONE.drop();
				addItemToSpawn( new StoneOfEnchantment() );
			}
			if ( Dungeon.intStoneNeeded() ){
				Dungeon.LimitedDrops.INT_STONE.drop();
				addItemToSpawn( new StoneOfIntuition() );
			}
			if ( Dungeon.trinketCataNeeded() ){
				Dungeon.LimitedDrops.TRINKET_CATA.drop();
				addItemToSpawn( new TrinketCatalyst());
			}
			
			if (Dungeon.depth > 1) {
				//50% chance of getting a level feeling
				//~7.15% chance for each feeling
				switch (Random.Int( 14 )) {
					case 0:
						feeling = Feeling.CHASM;
						break;
					case 1:
						feeling = Feeling.WATER;
						break;
					case 2:
						feeling = Feeling.GRASS;
						break;
					case 3:
						feeling = Feeling.DARK;
						viewDistance = Math.round(5*viewDistance/8f);
						break;
					case 4:
						feeling = Feeling.LARGE;
						addItemToSpawn(Generator.random(Generator.Category.FOOD));
						break;
					case 5:
						feeling = Feeling.TRAPS;
						break;
					case 6:
						feeling = Feeling.SECRETS;
						break;
					default:
						//if-else statements are fine here as only one chance can be above 0 at a time
						if (Random.Float() < MossyClump.overrideNormalLevelChance()){
							feeling = MossyClump.getNextFeeling();
						} else if (Random.Float() < TrapMechanism.overrideNormalLevelChance()) {
							feeling = TrapMechanism.getNextFeeling();
						} else {
							feeling = Feeling.NONE;
						}
				}
			}
		}

		//==== END(挑战 150 淹没地牢 / 154 废弃地牢): 强制整层生态 ====
		//做法照搬原版 MossyClump（苔藓丛簇）——那个饰品就是靠返回
		//Level.Feeling.GRASS / WATER 让**整层**变成草地或水域。
		//各个关卡类的 painter() 里都有形如
		//    .setWater(feeling == Feeling.WATER ? 0.98f : 0.38f, 4)
		//    .setGrass(feeling == Feeling.GRASS ? 0.80f : 0.20f, 3)
		//的写法，所以只要把 feeling 设对，地形生成自然就是"淹没"/"草木"。
		//
		//这样做的好处：**完全不碰地形，也不换关卡类** ——
		//因此与 1 牢地碎破（换关卡类）、6 完整地牢（改层数）都不冲突。
		//
		//放在 feeling 抽签**之后**：强制覆盖，不受随机结果影响。
		//150 与 154 互斥（框架已声明），所以这里只需按顺序判断。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.floodedEnabled()) {
			feeling = Feeling.WATER;
		} else if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.abandonedEnabled()) {
			feeling = Feeling.GRASS;
		}
		
		do {
			width = height = length = 0;

			transitions = new ArrayList<>();

			mobs = new HashSet<>();
			heaps = new SparseArray<>();
			blobs = new HashMap<>();
			plants = new SparseArray<>();
			traps = new SparseArray<>();
			customTiles = new ArrayList<>();
			customWalls = new ArrayList<>();
			
		} while (!build());
		
		buildFlagMaps();
		cleanWalls();
		
		createMobs();
		createItems();

		Random.popGenerator();

		//==== END(挑战 62 芙莉莲 / 64 宝物猎人): 宝箱后处理 ====
		//必须放在 Random.popGenerator() **之后** ——
		//本方法内所有随机数都取自关卡种子序列，若在 pop 之前追加随机调用，
		//会改变后续关卡的 RNG 序列，导致种子与关卡内容错位。
		applyChestChallenges();

		//==== END(挑战 54 我爱花花): 草 13% 替换成随机花 ====
		//同样放在 popGenerator 之后，理由同上。
		applyFlowerChallenge();

		//==== END(挑战 49 切尔诺贝利): 全图毒气 ====
		//也是确定性铺设（不掷骰），放在 popGenerator 之后不影响关卡生成。
		applyChernobyl();

		//END(修复 49): 毒气每回合 -1 点，30 回合就会散光 ——
		//所以这里额外挂一个维持器，每 3 回合把毒气补回来。
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChernobylKeeper.ensureRunning();

		//==== END(挑战 86 复仇之魂): 生成上一层的"复仇幽灵" ====
		//原表："被击杀怪物 10% 概率在下一层以幽灵形式复仇"
		//上一层死亡时记下的数量在这里取出来生成。
		//consumeVengefulSouls() 会同时清空计数，所以每层只生成一次。
		int vengeful = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.consumeVengefulSouls();
		for (int i = 0; i < vengeful; i++) {
			int cell = randomRespawnCell(null);
			if (cell == -1) break;
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.spawnWraithAt(cell);
		}

		//==== END(挑战 129 心爱的少女): 童话残片 ====
		//每 2 层刷一枚，每层最多一枚（文档所有者定稿）。
		//放在最后：它需要用到已经铺好的地形找落点。
		spawnFairyFragmentIfDue();
	}

	/**
	 * END(挑战 129): 按楼层节奏刷新一枚童话残片。
	 *
	 * <p>只在**每 2 层**刷，且每层最多一枚。
	 * 抽的种类会**优先选玩家还没有的**，否则玩家会攒一堆重复的，
	 * 而合成要求"9 枚不同的残片"。
	 */
	private void spawnFairyFragmentIfDue() {
		if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.belovedGirlEnabled()) {
			return;
		}
		if (Dungeon.branch != 0) return;             //挑战支线不刷
		if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
				.FairyFragment.shouldSpawnOnFloor(Dungeon.depth)) {
			return;
		}

		//找一块可以放东西的空地
		int cell = com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
				.FairyFragment.pickDropCell(this);
		if (cell < 0) return;

		com.shatteredpixel.shatteredpixeldungeon.items.Item f =
				com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
						.FairyFragment.rollMissingKind();
		if (f == null) return;                        //已集齐

		drop(f, cell).sprite.drop(cell);
	}

	/**
	 * END(挑战 49 切尔诺贝利): 全图铺毒气，玩家受影响、怪物免疫。
	 *
	 * <p>用法照搬 {@code ToxicGasRoom} 的现成逻辑（那是本 fork 已验证的毒气铺法）：
	 * <ul>
	 *   <li><b>只铺 {@code Terrain.EMPTY}</b> —— 原版毒气室就是这么判的。
	 *       不能用 {@code passable[]}：那里包含水、草、门，
	 *       在这些格子上铺毒气语义不对（水里冒毒气、门上冒毒气）。</li>
	 *   <li><b>每个格子给 30 的量</b> —— 原版注释写"as if gas has been
	 *       spreading in the room for a while"，即模拟毒气已扩散一会儿的状态。
	 *       给得太多会让整层毒气浓度过高，反而失真。</li>
	 * </ul>
	 *
	 * <p>"怪物免疫"通过给每只怪挂 {@code BlobImmunity} 实现，
	 * 而不是改 {@code ToxicGas} 本身 —— 后者会连带影响玩家丢出的毒气瓶。
	 */
	private static final int CHERNOBYL_SEED = 30;

	private void applyChernobyl() {

		if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.chernobylEnabled()) {
			return;   //未勾选 49，直接跳过（省掉整图遍历与 Blob 分配）
		}

		//---- 全图铺毒气（判据与 ToxicGasRoom 一致：只铺 EMPTY）----
		for (int i = 0; i < length(); i++) {
			if (map[i] != Terrain.EMPTY) continue;
			com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob.seed(
					i, CHERNOBYL_SEED,
					com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas.class,
					this);
		}

		//---- 怪物免疫（原表："玩家受影响，怪物免疫"）----
		for (com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m : mobs) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
					m,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.BlobImmunity.class,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs
							.BlobImmunity.DURATION);
		}
	}

	/**
	 * END(挑战 54 我爱花花): 把 13% 的草地替换为随机植物。
	 *
	 * <p>机制：在 GRASS 格子上种一个随机种子（{@code Level.plant} 会把它
	 * 变成带植物的草地）。纯趣味，不影响数值。
	 *
	 * <p>只处理普通 GRASS，不碰 HIGH_GRASS / FURROWED_GRASS ——
	 * 那些是"高草"（有遮蔽效果），改了会影响潜行机制。
	 */
	private void applyFlowerChallenge() {

		if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.flowerEnabled()) {
			return;   //未勾选 54，直接跳过（省掉整图遍历）
		}

		for (int i = 0; i < length(); i++) {
			if (map[i] != Terrain.GRASS) continue;
			if (heaps.get(i) != null) continue;              // 有物品的格子不动
			if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.rollFlower()) {
				continue;
			}

			//随机选一种种子种下去
			Plant.Seed seed = (Plant.Seed)
					com.shatteredpixel.shatteredpixeldungeon.items.Generator
							.random(com.shatteredpixel.shatteredpixeldungeon.items.Generator
									.Category.SEED);
			if (seed == null) continue;

			try {
				plant(seed, i);
			} catch (Exception e) {
				//某些格子不适合种植（地形/已有植物），跳过即可，不该让整个关卡生成失败
			}
		}
	}

	/**
	 * END(挑战 62/64): 对已生成的宝箱做数量与内容的调整。
	 *
	 * <ul>
	 *   <li><b>62 芙莉莲</b> — 每个宝箱按概率追加一个（等效"+20% 数量"）</li>
	 *   <li><b>64 宝物猎人</b> — 每个宝箱额外塞入 1 件物品</li>
	 * </ul>
	 *
	 * <p>为什么不改各房间类的生成代码：宝箱分散在 20+ 个房间类里
	 * （TreasuryRoom / PoolRoom / SentryRoom / Vault* …），逐个改既易漏、
	 * 又会在这些类里引入挑战耦合。统一在关卡生成后处理更可靠。
	 */
	private void applyChestChallenges() {

		boolean wantExtra = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.chestCountMultiplier() != 1f;
		int contentBonus = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.chestContentBonus();

		if (!wantExtra && contentBonus <= 0) return;

		//先收集，避免边遍历边改 heaps（SparseArray 结构会被 drop 修改）
		ArrayList<Heap> chests = new ArrayList<>();
		for (Heap h : heaps.valueList()) {
			if (h != null && (h.type == Heap.Type.CHEST
					|| h.type == Heap.Type.LOCKED_CHEST
					|| h.type == Heap.Type.CRYSTAL_CHEST)) {
				chests.add(h);
			}
		}
		if (chests.isEmpty()) return;

		for (Heap chest : chests) {

			//---- 64 宝物猎人：宝箱内容 +1 件 ----
			if (contentBonus > 0) {
				for (int i = 0; i < contentBonus; i++) {
					Item extra = Generator.random();
					if (extra != null) {
						chest.drop(extra);
					}
				}
			}

			//---- 62 芙莉莲：按概率再生成一个宝箱 ----
			//用"概率追加"表达 +20%：长期期望数量正好是 1.2 倍。
			if (wantExtra) {
				float mult = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.chestCountMultiplier();
				if (Random.Float() < (mult - 1f) / mult) {
					int pos = randomRespawnCell(null);
					if (pos != -1) {
						Heap newChest = drop(Generator.random(), pos);
						if (newChest != null) newChest.type = Heap.Type.CHEST;
					}
				}
			}
		}
	}
	
	public void setSize(int w, int h){
		
		width = w;
		height = h;
		length = w * h;
		
		map = new int[length];
		Arrays.fill( map, feeling == Level.Feeling.CHASM ? Terrain.CHASM : Terrain.WALL );
		
		visited     = new boolean[length];
		mapped      = new boolean[length];
		
		heroFOV     = new boolean[length];
		
		passable	= new boolean[length];
		losBlocking	= new boolean[length];
		flamable	= new boolean[length];
		secret		= new boolean[length];
		solid		= new boolean[length];
		avoid		= new boolean[length];
		water		= new boolean[length];
		pit			= new boolean[length];

		openSpace   = new boolean[length];
		
		PathFinder.setMapSize(w, h);
	}
	
	public void reset() {
		
		for (Mob mob : mobs.toArray( new Mob[0] )) {
			if (!mob.reset()) {
				mobs.remove( mob );
			}
		}
		createMobs();
	}

	public void playLevelMusic(){
		//do nothing by default
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {

		//version key may be absent for local saves written without manifest/version; treat as current
		version = bundle.contains(VERSION) ? bundle.getInt( VERSION ) : ShatteredPixelDungeon.v3_3_0;

		//saves from before v2.5.4 are not supported
		if (version < ShatteredPixelDungeon.v2_5_4){
			throw new RuntimeException("old save");
		}

		setSize( bundle.getInt(WIDTH), bundle.getInt(HEIGHT));
		
		mobs = new HashSet<>();
		heaps = new SparseArray<>();
		blobs = new HashMap<>();
		plants = new SparseArray<>();
		traps = new SparseArray<>();
		customTiles = new ArrayList<>();
		customWalls = new ArrayList<>();
		
		map		= bundle.getIntArray( MAP );

		visited	= bundle.getBooleanArray( VISITED );
		mapped	= bundle.getBooleanArray( MAPPED );

		transitions = new ArrayList<>();
		for (Bundlable b : bundle.getCollection( TRANSITIONS )){
			transitions.add((LevelTransition) b);
		}

		locked      = bundle.getBoolean( LOCKED );
		
		Collection<Bundlable> collection = bundle.getCollection( HEAPS );
		for (Bundlable h : collection) {
			Heap heap = (Heap)h;
			if (!heap.isEmpty())
				heaps.put( heap.pos, heap );
		}
		
		collection = bundle.getCollection( PLANTS );
		for (Bundlable p : collection) {
			Plant plant = (Plant)p;
			plants.put( plant.pos, plant );
		}

		collection = bundle.getCollection( TRAPS );
		for (Bundlable p : collection) {
			Trap trap = (Trap)p;
			traps.put( trap.pos, trap );
		}

		collection = bundle.getCollection( CUSTOM_TILES );
		for (Bundlable p : collection) {
			CustomTilemap vis = (CustomTilemap)p;
			customTiles.add(vis);
		}

		collection = bundle.getCollection( CUSTOM_WALLS );
		for (Bundlable p : collection) {
			CustomTilemap vis = (CustomTilemap)p;
			customWalls.add(vis);
		}
		
		collection = bundle.getCollection( MOBS );
		for (Bundlable m : collection) {
			Mob mob = (Mob)m;
			if (mob != null) {
				mobs.add( mob );
			}
		}
		
		collection = bundle.getCollection( BLOBS );
		for (Bundlable b : collection) {
			Blob blob = (Blob)b;
			blobs.put( blob.getClass(), blob );
		}

		feeling = bundle.getEnum( FEELING, Feeling.class );
		if (feeling == Feeling.DARK) {
			viewDistance = Math.round(5 * viewDistance / 8f);
		}

		if (bundle.contains( "mobs_to_spawn" )) {
			for (Class<? extends Mob> mob : bundle.getClassArray("mobs_to_spawn")) {
				if (mob != null) mobsToSpawn.add(mob);
			}
		}

		if (bundle.contains( "respawner" )){
			respawner = (MobSpawner) bundle.get("respawner");
		}

		buildFlagMaps();
		cleanWalls();

	}
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		//桌面未打包直达 = 无 Implementation-Version → Game.versionCode 会退成 -1/0；
		//不能把 -1 落盘,否则读档 Level.restore 会把它当 <v2_5_4 “old save” 而崩。
		//这里兜底:以当前最低可支持版本为止下限,让开发态存档也可正常回读。
		bundle.put( VERSION, Math.max( Game.versionCode, ShatteredPixelDungeon.v2_5_4 ) );
		bundle.put( WIDTH, width );
		bundle.put( HEIGHT, height );
		bundle.put( MAP, map );
		bundle.put( VISITED, visited );
		bundle.put( MAPPED, mapped );
		bundle.put( TRANSITIONS, transitions );
		bundle.put( LOCKED, locked );
		bundle.put( HEAPS, heaps.valueList() );
		bundle.put( PLANTS, plants.valueList() );
		bundle.put( TRAPS, traps.valueList() );
		bundle.put( CUSTOM_TILES, customTiles );
		bundle.put( CUSTOM_WALLS, customWalls );
		bundle.put( MOBS, mobs );
		bundle.put( BLOBS, blobs.values() );
		bundle.put( FEELING, feeling );
		bundle.put( "mobs_to_spawn", mobsToSpawn.toArray(new Class[0]));
		bundle.put( "respawner", respawner );
	}
	
	public int tunnelTile() {
		return feeling == Feeling.CHASM ? Terrain.EMPTY_SP : Terrain.EMPTY;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public int length() {
		return length;
	}
	
	public String tilesTex() {
		return null;
	}
	
	public String waterTex() {
		return null;
	}
	
	abstract protected boolean build();
	
	private ArrayList<Class<?extends Mob>> mobsToSpawn = new ArrayList<>();
	
	public Mob createMob() {
		if (mobsToSpawn == null || mobsToSpawn.isEmpty()) {
			mobsToSpawn = MobSpawner.getMobRotation(Dungeon.depth);
		}

		Mob m = Reflection.newInstance(mobsToSpawn.remove(0));
		ChampionEnemy.rollForChampion(m);

		//==== END(挑战 1 牢地碎破): 配置表数值覆写（HP 部分见 Mob.onAdd）====
		//注意：HP/HT 的覆写**不在**这里，而在 Mob.onAdd()。
		//原因：createMob() 只覆盖"关卡刷出的普通怪"，而 Boss 召唤物
		//（古神的拳头/幼虫等）与 60 多处直接 level.mobs.add(...) 都绕过它。
		//onAdd() 由 Actor.add() 调用，覆盖全部入场路径。
		//
		//命中/闪避/护甲/伤害是**方法**，在 Char.hit / Char.attack 里覆写，
		//与创建路径无关，因此不受此影响。

		//5 区刷原 1 区怪物时的"移速×2 + 永久祝福"（见配置表说明）
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.crumblingSwiftBlessed(m)) {
			//移速 ×2：用 Adrenaline 不是"速度"，这里改用 Haste（本 fork 的加速 buff）
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					m, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste.class);
			//永久祝福：用一个极长时长的 Blessing
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(
					m,
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless.class,
					9999f);
		}

		//==== END(挑战 119 怪物浪潮): 怪物数值 ×0.2 ====
		//这里是全游戏**怪物实例化的唯一出口**，一处生效即覆盖全部。
		//只改 HP/HT —— 伤害/命中/闪避是方法计算出来的，改字段无效，
		//它们的削弱由 ChallengeEffects 在计算侧处理（见 mobDamageMultiplier）。
		//Boss/小 Boss 不削弱（已定稿）。
		float statMult = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.mobStatMultiplier(m);
		if (statMult != 1f) {
			m.HT = Math.max(1, Math.round(m.HT * statMult));
			m.HP = m.HT;
		}

		//==== END(挑战 10 巨型化): 13% 怪物 HP ×1.5 + 体型放大 ====
		//放在 119 的削弱**之后**：两者共存时先削到 20% 再 ×1.5，
		//最终为原值的 30%，顺序符合"先应用挑战的数值修正、再叠加体型"的直觉。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.rollGiant(m)) {
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.applyGiant(m);
		}

		return m;
	}

	//END(port from Arknights): 方舟用 nMobs() 让关卡额外指定刷怪数量（默认0）。
	//本 fork 的刷怪由 MobSpawner 按层号决定，此方法仅用于让方舟关卡编译/覆写。
	public int nMobs() {
		return 0;
	}
	abstract protected void createMobs();

	abstract protected void createItems();

	public int entrance(){
		LevelTransition l = getTransition(null);
		if (l != null){
			return l.cell();
		}
		return 0;
	}

	public int exit(){
		LevelTransition l = getTransition(LevelTransition.Type.REGULAR_EXIT);
		if (l != null){
			return l.cell();
		}
		return 0;
	}

	public LevelTransition getTransition(LevelTransition.Type type){
		if (transitions.isEmpty()){
			return null;
		}
		for (LevelTransition transition : transitions){
			//if we don't specify a type, prefer to return any entrance
			if (type == null &&
					(transition.type == LevelTransition.Type.REGULAR_ENTRANCE
							|| transition.type == LevelTransition.Type.BRANCH_ENTRANCE
							|| transition.type == LevelTransition.Type.SURFACE)){
				return transition;
			} else if (transition.type == type){
				return transition;
			}
		}
		return type != null ? getTransition(null) : transitions.get(0);
	}

	public LevelTransition getTransition(int cell){
		for (LevelTransition transition : transitions){
			if (transition.inside(cell)){
				return transition;
			}
		}
		return null;
	}

	//returns true if we immediately transition, false otherwise
	public boolean activateTransition(Hero hero, LevelTransition transition){
		if (locked){
			return false;
		}

		beforeTransition();
		InterlevelScene.curTransition = transition;
		if (transition.type == LevelTransition.Type.REGULAR_EXIT
				|| transition.type == LevelTransition.Type.BRANCH_EXIT) {
			InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
		} else {
			InterlevelScene.mode = InterlevelScene.Mode.ASCEND;
		}
		Game.switchScene(InterlevelScene.class);
		return true;
	}

	//some buff effects have special logic or are cancelled from the hero before transitioning levels
	public static void beforeTransition(){

		//time freeze effects need to resolve their pressed cells before transitioning
		TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
		if (timeFreeze != null) timeFreeze.disarmPresses();
		Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
		if (timeBubble != null) timeBubble.disarmPresses();

		//iron stomach and challenge arena do not persist between floors
		Talent.WarriorFoodImmunity foodImmune = Dungeon.hero.buff(Talent.WarriorFoodImmunity.class);
		if (foodImmune != null) foodImmune.detach();
		ScrollOfChallenge.ChallengeArena arena = Dungeon.hero.buff(ScrollOfChallenge.ChallengeArena.class);
		if (arena != null) arena.detach();
		//awareness also doesn't, honestly it's weird that it's a buff
		Awareness awareness = Dungeon.hero.buff(Awareness.class);
		if (awareness != null) awareness.detach();

		Char ally = Stasis.getStasisAlly();
		if (Char.hasProp(ally, Char.Property.IMMOVABLE)){
			Dungeon.hero.buff(Stasis.StasisBuff.class).act();
			GLog.w(Messages.get(Stasis.StasisBuff.class, "left_behind"));
		}

		//spend the hero's partial turns,  so the hero cannot take partial turns between floors
		Dungeon.hero.spendToWhole();
		for (Actor a : Actor.all()){
			//also adjust any other actors that are now ahead of the hero due to this
			if (a.cooldown() < Dungeon.hero.cooldown()){
				a.spendToWhole();
			}
		}
	}

	public void seal(){
		if (!locked) {
			locked = true;
			Buff.affect(Dungeon.hero, LockedFloor.class);
		}
	}

	public void unseal(){
		if (locked) {
			locked = false;
			if (Dungeon.hero.buff(LockedFloor.class) != null){
				Dungeon.hero.buff(LockedFloor.class).detach();
			}
		}
	}

	public ArrayList<Item> getItemsToPreserveFromSealedResurrect(){
		ArrayList<Item> items = new ArrayList<>();
		for (Heap h : heaps.valueList()){
			if (h.type == Heap.Type.HEAP) {
				for (Item i : h.items){
					if (i instanceof Bomb){
						((Bomb) i).fuse = null;
					}
					items.add(i);
				}
			}
		}
		for (Mob m : mobs){
			for (PinCushion b : m.buffs(PinCushion.class)){
				items.addAll(b.getStuckItems());
			}
		}
		for (HeavyBoomerang.CircleBack b : Dungeon.hero.buffs(HeavyBoomerang.CircleBack.class)){
			if (b.activeDepth() == Dungeon.depth) items.add(b.cancel());
		}
		return items;
	}

	public Group addVisuals() {
		if (visuals == null || visuals.parent == null){
			visuals = new Group();
		} else {
			visuals.clear();
			visuals.camera = null;
		}
		for (int i=0; i < length(); i++) {
			if (pit[i]) {
				visuals.add( new WindParticle.Wind( i ) );
				if (i >= width() && water[i-width()]) {
					visuals.add( new FlowParticle.Flow( i - width() ) );
				}
			}
		}
		return visuals;
	}

	//for visual effects that should render above wall overhang tiles
	public Group addWallVisuals(){
		if (wallVisuals == null || wallVisuals.parent == null){
			wallVisuals = new Group();
		} else {
			wallVisuals.clear();
			wallVisuals.camera = null;
		}
		return wallVisuals;
	}

	
	public int mobLimit() {
		return 0;
	}

	public int mobCount(){
		float count = 0;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
			if (mob.alignment == Char.Alignment.ENEMY && !mob.properties().contains(Char.Property.MINIBOSS)) {
				count += mob.spawningWeight();
			}
		}
		return Math.round(count);
	}

	public Mob findMob( int pos ){
		for (Mob mob : mobs){
			if (mob.pos == pos){
				return mob;
			}
		}
		return null;
	}

	private MobSpawner respawner;

	public Actor addRespawner() {
		if (respawner == null){
			respawner = new MobSpawner();
			Actor.addDelayed(respawner, respawnCooldown());
		} else {
			Actor.add(respawner);
			if (respawner.cooldown() > respawnCooldown()){
				respawner.resetCooldown();
			}
		}
		return respawner;
	}

	public float respawnCooldown(){
		float cooldown;
		if (Statistics.amuletObtained){
			if (Dungeon.depth == 1){
				//very fast spawns on floor 1! 0/2/4/6/8/10/12, etc.
				cooldown = (Dungeon.level.mobCount()) * (TIME_TO_RESPAWN / 25f);
			} else {
				//respawn time is 5/5/10/15/20/25/25, etc.
				cooldown = Math.round(GameMath.gate( TIME_TO_RESPAWN/10f, Dungeon.level.mobCount() * (TIME_TO_RESPAWN / 10f), TIME_TO_RESPAWN / 2f));
			}
		} else if (Dungeon.level.feeling == Feeling.DARK){
			cooldown = 2*TIME_TO_RESPAWN/3f;
		} else {
			cooldown = TIME_TO_RESPAWN;
		}
		return cooldown / DimensionalSundial.spawnMultiplierAtCurrentTime();
	}

	public boolean spawnMob(int disLimit){
		PathFinder.buildDistanceMap(Dungeon.hero.pos, BArray.or(passable, avoid, null));

		Mob mob = createMob();
		if (mob.state != mob.PASSIVE) {
			mob.state = mob.WANDERING;
		}
		int tries = 30;
		do {
			mob.pos = randomRespawnCell(mob);
			tries--;
		} while ((mob.pos == -1 || PathFinder.distance[mob.pos] < disLimit) && tries > 0);

		if (Dungeon.hero.isAlive() && mob.pos != -1 && PathFinder.distance[mob.pos] >= disLimit) {
			GameScene.add( mob );
			if (!mob.buffs(ChampionEnemy.class).isEmpty()){
				GLog.w(Messages.get(ChampionEnemy.class, "warn"));
			}
			return true;
		} else {
			return false;
		}
	}
	
	public int randomRespawnCell( Char ch ) {
		int cell;
		int count = 0;
		do {

			if (++count > 30) {
				return -1;
			}

			cell = Random.Int( length() );

		} while ((Dungeon.level == this && heroFOV[cell])
				|| !passable[cell]
				|| (Char.hasProp(ch, Char.Property.LARGE) && !openSpace[cell])
				|| Actor.findChar( cell ) != null);
		return cell;
	}
	
	public int randomDestination( Char ch ) {
		int cell;
		do {
			cell = Random.Int( length() );
		} while (!passable[cell]
				|| (Char.hasProp(ch, Char.Property.LARGE) && !openSpace[cell]));
		return cell;
	}
	
	public void addItemToSpawn( Item item ) {
		if (item != null) {
			itemsToSpawn.add( item );
		}
	}

	public Item findPrizeItem(){ return findPrizeItem(null); }

	public Item findPrizeItem(Class<?extends Item> match){
		if (itemsToSpawn.size() == 0)
			return null;

		if (match == null){
			//if we have a trinket catalyst, always return that first
			for (Item item : itemsToSpawn){
				if (item instanceof TrinketCatalyst){
					itemsToSpawn.remove(item);
					return item;
				}
			}

			Item item = Random.element(itemsToSpawn);
			itemsToSpawn.remove(item);
			return item;
		}

		for (Item item : itemsToSpawn){
			if (match.isInstance(item)){
				itemsToSpawn.remove( item );
				return item;
			}
		}

		return null;
	}

	public void buildFlagMaps() {
		
		for (int i=0; i < length(); i++) {
			int flags = Terrain.flags[map[i]];
			passable[i]     = (flags & Terrain.PASSABLE) != 0;
			losBlocking[i]  = (flags & Terrain.LOS_BLOCKING) != 0;
			flamable[i]     = (flags & Terrain.FLAMABLE) != 0;
			secret[i]       = (flags & Terrain.SECRET) != 0;
			solid[i]        = (flags & Terrain.SOLID) != 0;
			avoid[i]        = (flags & Terrain.AVOID) != 0;
			water[i]        = (flags & Terrain.LIQUID) != 0;
			pit[i]          = (flags & Terrain.PIT) != 0;
		}

		for (Blob b : blobs.values()){
			b.onBuildFlagMaps(this);
		}
		
		int lastRow = length() - width();
		for (int i=0; i < width(); i++) {
			passable[i] = avoid[i] = false;
			losBlocking[i] = solid[i] = true;
			passable[lastRow + i] = avoid[lastRow + i] = false;
			losBlocking[lastRow + i] = solid[lastRow + i] = true;
		}
		for (int i=width(); i < lastRow; i += width()) {
			passable[i] = avoid[i] = false;
			losBlocking[i] = solid[i] = true;
			passable[i + width()-1] = avoid[i + width()-1] = false;
			losBlocking[i + width()-1] = solid[i + width()-1] = true;
		}

		//an open space is large enough to fit large mobs. A space is open when it is not solid
		// and there is an open corner with both adjacent cells opens
		for (int i=0; i < length(); i++) {
			if (solid[i]){
				openSpace[i] = false;
			} else {
				for (int j = 1; j < PathFinder.CIRCLE8.length; j += 2){
					if (solid[i+PathFinder.CIRCLE8[j]]) {
						openSpace[i] = false;
					} else if (!solid[i+PathFinder.CIRCLE8[(j+1)%8]]
							&& !solid[i+PathFinder.CIRCLE8[(j+2)%8]]){
						openSpace[i] = true;
						break;
					}
				}
			}
		}

	}

	//updates open space both on the cell itself and adjacent cells
	public void updateOpenSpace(int cell){
		for (int i : PathFinder.NEIGHBOURS9) {
			if (solid[cell+i]){
				openSpace[cell+i] = false;
			} else {
				for (int j = 1; j < PathFinder.CIRCLE8.length; j += 2){
					if (solid[cell+i+PathFinder.CIRCLE8[j]]) {
						openSpace[cell+i] = false;
					} else if (!solid[cell+i+PathFinder.CIRCLE8[(j+1)%8]]
							&& !solid[cell+i+PathFinder.CIRCLE8[(j+2)%8]]){
						openSpace[cell+i] = true;
						break;
					}
				}
			}
		}
	}

	public void destroy( int pos ) {
		//if raw tile type is flammable or empty
		int terr = map[pos];
		if (terr == Terrain.EMPTY || terr == Terrain.EMPTY_DECO
				|| (Terrain.flags[map[pos]] & Terrain.FLAMABLE) != 0) {
			set(pos, Terrain.EMBERS);
		}
		Blob web = blobs.get(Web.class);
		if (web != null){
			web.clear(pos);
		}
	}

	public void cleanWalls() {
		if (discoverable == null || discoverable.length != length) {
			discoverable = new boolean[length()];
		}

		for (int i=0; i < length(); i++) {
			
			boolean d = false;
			
			for (int j=0; j < PathFinder.NEIGHBOURS9.length; j++) {
				int n = i + PathFinder.NEIGHBOURS9[j];
				if (n >= 0 && n < length() && map[n] != Terrain.WALL && map[n] != Terrain.WALL_DECO) {
					d = true;
					break;
				}
			}
			
			discoverable[i] = d;
		}
	}
	
	public static void set( int cell, int terrain ){
		set( cell, terrain, Dungeon.level );
	}
	
	public static void set( int cell, int terrain, Level level ) {
		Painter.set(level, cell, terrain);

		if (terrain != Terrain.TRAP && terrain != Terrain.SECRET_TRAP && terrain != Terrain.INACTIVE_TRAP) {
			level.traps.remove(cell);
		}

		level.updateCellFlags(cell);
	}

	public void updateCellFlags( int cell ){
		int terrain = map[cell];

		int flags = Terrain.flags[terrain];
		passable[cell]      = (flags & Terrain.PASSABLE) != 0;
		losBlocking[cell]   = (flags & Terrain.LOS_BLOCKING) != 0;
		flamable[cell]      = (flags & Terrain.FLAMABLE) != 0;
		secret[cell]        = (flags & Terrain.SECRET) != 0;
		solid[cell]         = (flags & Terrain.SOLID) != 0;
		avoid[cell]         = (flags & Terrain.AVOID) != 0;
		pit[cell]           = (flags & Terrain.PIT) != 0;
		water[cell]         = terrain == Terrain.WATER;

		if (this instanceof SewerLevel){
			if (map[cell] == Terrain.REGION_DECO || map[cell] == Terrain.REGION_DECO_ALT){
				flamable[cell] = true;
			}
		}

		for (Blob b : blobs.values()){
			b.onUpdateCellFlags(this, cell);
		}

		updateOpenSpace(cell);
	}
	
	public Heap drop( Item item, int cell ) {

		if (item == null || Challenges.isItemBlocked(item)){

			//create a dummy heap, give it a dummy sprite, don't add it to the game, and return it.
			//effectively nullifies whatever the logic calling this wants to do, including dropping items.
			Heap heap = new Heap();
			ItemSprite sprite = heap.sprite = new ItemSprite();
			sprite.link(heap);
			return heap;

		}

		//==== END(挑战 167 黄金地牢): 地面不刷物品，只刷金币 ====
		//原表（文档所有者说明）："地面不刷新物品，只刷新金币"。
		//
		//做法：把要掉的物品**换算成等值金币**再掉 ——
		//直接丢弃会让"金币地牢"变成"什么都没有的地牢"，
		//而换算成金币才符合"只刷金币"的字面意思。
		//
		//**任务/剧情物品不换算**（天狗面具等），否则主线会断。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.shouldConvertDropToGold(item)) {
			int value = Math.max(1, item.value() * Math.max(1, item.quantity()));
			//金币的价值换算：原版 1 金币 ≈ 1 价值单位，
			//但直接按原价会太慷慨（物品原价通常远高于玩家实际能卖到的钱），
			//所以打 4 折 —— 与"卖店"的手感接近。
			value = Math.max(1, value * 2 / 5);
			item = new com.shatteredpixel.shatteredpixeldungeon.items.Gold().random();
			((com.shatteredpixel.shatteredpixeldungeon.items.Gold) item).quantity(value);
		}

		//==== END(挑战 47/48/35/36): 掉落增减 ====
		//放在最前：被"减少"规则丢弃的物品直接走 dummy heap 分支，
		//不进入任何后续逻辑（不生成 Heap、不进 FOV 记录）。
		//用概率表达倍率 —— Level.drop() 每次只处理一个物品，没有"数量"可乘。
		float keepChance = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.dropKeepChance(item);
		if (keepChance < 1f && Random.Float() >= keepChance) {
			Heap heap = new Heap();
			ItemSprite sprite = heap.sprite = new ItemSprite();
			sprite.link(heap);
			return heap;
		}
		//倍率 > 1 时追加一份（不会递归调用本方法，避免死循环）
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.extraDropCopy(item)) {
			Item extra = item.duplicate();
			if (extra != null && extra != item) {
				//把追加的那份放到同一格（稍后 heap.drop 会合并数量）
				dropNoChallenge(extra, cell);
			}
		}

		return dropNoChallenge(item, cell);
	}

	/** END(挑战): 实际的掉落逻辑 —— 与原本的 drop() 完全一致，只是不再经过挑战过滤。 */
	private Heap dropNoChallenge( Item item, int cell ) {

		Heap heap = heaps.get( cell );
		if (heap == null) {
			
			heap = new Heap();
			heap.seen = Dungeon.level == this && heroFOV[cell];
			heap.pos = cell;
			heap.drop(item);
			if (map[cell] == Terrain.CHASM || (Dungeon.level != null && pit[cell])) {
				Dungeon.dropToChasm( item );
				GameScene.discard( heap );
			} else {
				heaps.put( cell, heap );
				GameScene.add( heap );
			}
			
		} else if (heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST) {
			
			int n;
			do {
				n = cell + PathFinder.NEIGHBOURS8[Random.Int( 8 )];
			} while (!passable[n] && !avoid[n]);
			return drop( item, n );
			
		} else {
			heap.drop(item);
		}
		
		if (Dungeon.level != null && ShatteredPixelDungeon.scene() instanceof GameScene) {
			pressCell( cell );
		}
		
		return heap;
	}
	
	public Plant plant( Plant.Seed seed, int pos ) {

		Plant plant = plants.get( pos );
		if (plant != null) {
			plant.wither();
		}

		if (map[pos] == Terrain.HIGH_GRASS ||
				map[pos] == Terrain.FURROWED_GRASS ||
				map[pos] == Terrain.EMPTY ||
				map[pos] == Terrain.EMBERS ||
				map[pos] == Terrain.EMPTY_DECO) {
			set(pos, Terrain.GRASS, this);
			GameScene.updateMap(pos);
		}

		//we have to get this far as grass placement has RNG implications in levelgen
		if (Dungeon.isChallenged(Challenges.NO_HERBALISM)){
			return null;
		}
		
		plant = seed.couch( pos, this );
		plants.put( pos, plant );
		
		GameScene.plantSeed( pos );

		for (Char ch : Actor.chars()){
			if (ch instanceof WandOfRegrowth.Lotus
					&& ((WandOfRegrowth.Lotus) ch).inRange(pos)
					&& Actor.findChar(pos) != null){
				plant.trigger();
				return null;
			}
		}
		
		return plant;
	}
	
	public void uproot( int pos ) {
		plants.remove(pos);
		GameScene.updateMap( pos );
	}

	public Trap setTrap( Trap trap, int pos ){
		Trap existingTrap = traps.get(pos);
		if (existingTrap != null){
			traps.remove( pos );
		}
		trap.set( pos );
		traps.put( pos, trap );
		GameScene.updateMap( pos );
		return trap;
	}

	public void disarmTrap( int pos ) {
		set(pos, Terrain.INACTIVE_TRAP);
		GameScene.updateMap(pos);
	}

	public void discover( int cell ) {
		set( cell, Terrain.discover( map[cell] ) );
		Trap trap = traps.get( cell );
		if (trap != null)
			trap.reveal();
		GameScene.updateMap( cell );
	}

	public boolean setCellToWater( boolean includeTraps, int cell ){
		Point p = cellToPoint(cell);

		//if a custom tilemap is over that cell, don't put water there
		for (CustomTilemap cust : customTiles){
			Point custPoint = new Point(p);
			custPoint.x -= cust.tileX;
			custPoint.y -= cust.tileY;
			if (custPoint.x >= 0 && custPoint.y >= 0
					&& custPoint.x < cust.tileW && custPoint.y < cust.tileH){
				if (cust.image(custPoint.x, custPoint.y) != null){
					return false;
				}
			}
		}

		int terr = map[cell];
		if (terr == Terrain.EMPTY || terr == Terrain.GRASS ||
				terr == Terrain.EMBERS || terr == Terrain.EMPTY_SP ||
				terr == Terrain.HIGH_GRASS || terr == Terrain.FURROWED_GRASS
				|| terr == Terrain.EMPTY_DECO){
			set(cell, Terrain.WATER);
			GameScene.updateMap(cell);
			return true;
		} else if (includeTraps && (terr == Terrain.SECRET_TRAP ||
				terr == Terrain.TRAP || terr == Terrain.INACTIVE_TRAP)){
			set(cell, Terrain.WATER);
			Dungeon.level.traps.remove(cell);
			GameScene.updateMap(cell);
			return true;
		}

		return false;
	}
	
	public int fallCell( boolean fallIntoPit ) {
		int result;
		do {
			result = randomRespawnCell( null );
			if (result == -1) return -1;
		} while (traps.get(result) != null
				|| findMob(result) != null);
		return result;
	}
	
	public void occupyCell( Char ch ){
		if (!ch.isImmune(Web.class) && Blob.volumeAt(ch.pos, Web.class) > 0){
			blobs.get(Web.class).clear(ch.pos);
			Web.affectChar( ch );
		}

		if (Blob.volumeAt(ch.pos, SacrificialFire.class) > 0 && ch.buff( SacrificialFire.Marked.class ) == null){
			if (Dungeon.level.heroFOV[ch.pos]) {
				CellEmitter.get(ch.pos).burst( SacrificialParticle.FACTORY, 5 );
			}
			Buff.prolong( ch, SacrificialFire.Marked.class, SacrificialFire.Marked.DURATION );
		}

		if (!ch.flying){

			//we call act here instead of detach in case the debuffs haven't managed to deal dmg once yet
			if (map[ch.pos] == Terrain.WATER){
				if (ch.buff(Burning.class) != null){
					ch.buff(Burning.class).act();
				}
				if (ch.buff(Ooze.class) != null){
					ch.buff(Ooze.class).act();
				}
			}

			if ( (map[ch.pos] == Terrain.GRASS || map[ch.pos] == Terrain.EMBERS)
					&& ch == Dungeon.hero && Dungeon.hero.hasTalent(Talent.REJUVENATING_STEPS)
					&& ch.buff(Talent.RejuvenatingStepsCooldown.class) == null){

				if (!Regeneration.regenOn()){
					set(ch.pos, Terrain.FURROWED_GRASS);
				} else if (ch.buff(Talent.RejuvenatingStepsFurrow.class) != null && ch.buff(Talent.RejuvenatingStepsFurrow.class).count() >= 200) {
					set(ch.pos, Terrain.FURROWED_GRASS);
				} else {
					set(ch.pos, Terrain.HIGH_GRASS);
					Buff.count(ch, Talent.RejuvenatingStepsFurrow.class, 3 - Dungeon.hero.pointsInTalent(Talent.REJUVENATING_STEPS));
				}
				GameScene.updateMap(ch.pos);
				Buff.affect(ch, Talent.RejuvenatingStepsCooldown.class, 15f - 5f*Dungeon.hero.pointsInTalent(Talent.REJUVENATING_STEPS));
			}
			
			if (pit[ch.pos]){
				if (ch == Dungeon.hero) {
					Chasm.heroFall(ch.pos);
				} else if (ch instanceof Mob) {
					Chasm.mobFall( (Mob)ch );
				}
				return;
			}
			
			//characters which are not the hero or a sheep 'soft' press cells
			pressCell( ch.pos, ch instanceof Hero || ch instanceof Sheep);
		} else {
			if (map[ch.pos] == Terrain.DOOR){
				Door.enter( ch.pos );
			}
		}

		if (ch.isAlive() && ch instanceof Piranha && !water[ch.pos]){
			((Piranha) ch).dieOnLand();
		}
	}
	
	//public method for forcing the hard press of a cell. e.g. when an item lands on it
	public void pressCell( int cell ){
		pressCell( cell, true );
	}
	
	//a 'soft' press ignores hidden traps
	//a 'hard' press triggers all things
	private void pressCell( int cell, boolean hard ) {

		Trap trap = null;
		
		switch (map[cell]) {
		
		case Terrain.SECRET_TRAP:
			if (hard) {
				trap = traps.get( cell );
				GLog.i(Messages.get(Level.class, "hidden_trap", trap.name()));
			}
			break;
			
		case Terrain.TRAP:
			trap = traps.get( cell );
			break;
			
		case Terrain.HIGH_GRASS:
		case Terrain.FURROWED_GRASS:
			HighGrass.trample( this, cell);
			break;
			
		case Terrain.WELL:
			WellWater.affectCell( cell );
			break;
			
		case Terrain.DOOR:
			Door.enter( cell );
			break;
		}

		TimekeepersHourglass.timeFreeze timeFreeze =
				Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);

		Swiftthistle.TimeBubble bubble =
				Dungeon.hero.buff(Swiftthistle.TimeBubble.class);

		if (trap != null) {
			if (bubble != null){
				Sample.INSTANCE.play(Assets.Sounds.TRAP);
				discover(cell);
				bubble.setDelayedPress(cell);
				
			} else if (timeFreeze != null){
				Sample.INSTANCE.play(Assets.Sounds.TRAP);
				discover(cell);
				timeFreeze.setDelayedPress(cell);
				
			} else {
				if (Dungeon.hero.pos == cell) {
					Dungeon.hero.interrupt();
				}
				trap.trigger();

			}
		}
		
		Plant plant = plants.get( cell );
		if (plant != null) {
			if (bubble != null){
				Sample.INSTANCE.play(Assets.Sounds.TRAMPLE, 1, Random.Float( 0.96f, 1.05f ) );
				bubble.setDelayedPress(cell);

			} else if (timeFreeze != null){
				Sample.INSTANCE.play(Assets.Sounds.TRAMPLE, 1, Random.Float( 0.96f, 1.05f ) );
				timeFreeze.setDelayedPress(cell);

			} else {
				plant.trigger();

			}
		}

		if (hard && Blob.volumeAt(cell, Web.class) > 0){
			blobs.get(Web.class).clear(cell);
		}
	}

	private static boolean[] heroMindFov;

	private static boolean[] modifiableBlocking;

	public void updateFieldOfView( Char c, boolean[] fieldOfView ) {

		int cx = c.pos % width();
		int cy = c.pos / width();
		
		boolean sighted = c.buff( Blindness.class ) == null && c.buff( Shadows.class ) == null
						&& c.isAlive();
		if (sighted) {
			boolean[] blocking = null;

			if (modifiableBlocking == null || modifiableBlocking.length != Dungeon.level.losBlocking.length){
				modifiableBlocking = new boolean[Dungeon.level.losBlocking.length];
			}

			//grass is see-through by some specific entities, but not during the fungi quest
			if (!(Dungeon.level instanceof  MiningLevel) || Blacksmith.Quest.Type() != Blacksmith.Quest.FUNGI){
				if ((c instanceof Hero && ((Hero) c).subClass == HeroSubClass.WARDEN)
						|| c instanceof YogFist.SoiledFist || c instanceof GnollGeomancer) {
					if (blocking == null) {
						System.arraycopy(Dungeon.level.losBlocking, 0, modifiableBlocking, 0, modifiableBlocking.length);
						blocking = modifiableBlocking;
					}
					for (int i = 0; i < blocking.length; i++) {
						if (blocking[i] && (Dungeon.level.map[i] == Terrain.HIGH_GRASS || Dungeon.level.map[i] == Terrain.FURROWED_GRASS)) {
							blocking[i] = false;
						}
					}
				}
			}

			//allies and specific enemies can see through shrouding fog
			if ((c.alignment != Char.Alignment.ALLY && !(c instanceof GnollGeomancer))
					&& Dungeon.level.blobs.containsKey(SmokeScreen.class)
					&& Dungeon.level.blobs.get(SmokeScreen.class).volume > 0) {
				if (blocking == null) {
					System.arraycopy(Dungeon.level.losBlocking, 0, modifiableBlocking, 0, modifiableBlocking.length);
					blocking = modifiableBlocking;
				}
				Blob s = Dungeon.level.blobs.get(SmokeScreen.class);
				for (int i = 0; i < blocking.length; i++){
					if (!blocking[i] && s.cur[i] > 0){
						blocking[i] = true;
					}
				}
			}

			if (blocking == null){
				blocking = Dungeon.level.losBlocking;
			}

			float viewDist = c.viewDistance;
			if (c instanceof Hero){
				viewDist *= 1f + 0.25f*((Hero) c).pointsInTalent(Talent.FARSIGHT);
				viewDist *= EyeOfNewt.visionRangeMultiplier();
			}
			
			ShadowCaster.castShadow( cx, cy, width(), fieldOfView, blocking, Math.round(viewDist) );
		} else {
			BArray.setFalse(fieldOfView);
		}
		
		int sense = 1;
		//Currently only the hero can get mind vision
		if (c.isAlive() && c == Dungeon.hero) {
			for (Buff b : c.buffs( MindVision.class )) {
				sense = Math.max( ((MindVision)b).distance, sense );
			}
			if (c.buff(MagicalSight.class) != null){
				sense = Math.max( MagicalSight.DISTANCE, sense );
			}
		}
		
		//uses rounding
		if (!sighted || sense > 1) {
			
			int[][] rounding = ShadowCaster.rounding;
			
			int left, right;
			int pos;
			for (int y = Math.max(0, cy - sense); y <= Math.min(height()-1, cy + sense); y++) {
				if (rounding[sense][Math.abs(cy - y)] < Math.abs(cy - y)) {
					left = cx - rounding[sense][Math.abs(cy - y)];
				} else {
					left = sense;
					while (rounding[sense][left] < rounding[sense][Math.abs(cy - y)]){
						left--;
					}
					left = cx - left;
				}
				right = Math.min(width()-1, cx + cx - left);
				left = Math.max(0, left);
				pos = left + y * width();
				System.arraycopy(discoverable, pos, fieldOfView, pos, right - left + 1);
			}
		}

		if (c instanceof SpiritHawk.HawkAlly && Dungeon.hero.pointsInTalent(Talent.EAGLE_EYE) >= 3){
			int range = 1+(Dungeon.hero.pointsInTalent(Talent.EAGLE_EYE)-2);
			for (Mob mob : mobs) {
				int p = mob.pos;
				if (!fieldOfView[p] && distance(c.pos, p) <= range) {
					for (int i : PathFinder.NEIGHBOURS9) {
						fieldOfView[mob.pos + i] = true;
					}
				}
			}
		}

		//Currently only the hero can get mind vision or awareness
		if (c.isAlive() && c == Dungeon.hero) {

			if (heroMindFov == null || heroMindFov.length != length()){
				heroMindFov = new boolean[length];
			} else {
				BArray.setFalse(heroMindFov);
			}

			Dungeon.hero.mindVisionEnemies.clear();
			if (c.buff( MindVision.class ) != null) {
				for (Mob mob : mobs) {
					if (mob instanceof Mimic && mob.alignment == Char.Alignment.NEUTRAL&& ((Mimic) mob).stealthy()){
						continue;
					}
					for (int i : PathFinder.NEIGHBOURS9) {
						heroMindFov[mob.pos + i] = true;
					}
				}
			} else {

				int mindVisRange = 0;
				if (((Hero) c).hasTalent(Talent.HEIGHTENED_SENSES)){
					mindVisRange = 1+((Hero) c).pointsInTalent(Talent.HEIGHTENED_SENSES);
				}
				if (c.buff(DivineSense.DivineSenseTracker.class) != null){
					if (((Hero) c).heroClass == HeroClass.CLERIC){
						mindVisRange = 4+4*((Hero) c).pointsInTalent(Talent.DIVINE_SENSE);
					} else {
						mindVisRange = 1+2*((Hero) c).pointsInTalent(Talent.DIVINE_SENSE);
					}
				}
				mindVisRange = Math.max(mindVisRange, EyeOfNewt.mindVisionRange());

				//power of many's life link spell allows allies to get divine sense
				Char ally = PowerOfMany.getPoweredAlly();
				if (ally != null && ally.buff(DivineSense.DivineSenseTracker.class) == null){
					ally = null;
				}

				if (mindVisRange >= 1) {
					for (Mob mob : mobs) {
						if (mob instanceof Mimic && mob.alignment == Char.Alignment.NEUTRAL && ((Mimic) mob).stealthy()){
							continue;
						}
						int p = mob.pos;
						if (!fieldOfView[p] && (distance(c.pos, p) <= mindVisRange || (ally != null && distance(ally.pos, p) <= mindVisRange))) {
							for (int i : PathFinder.NEIGHBOURS9) {
								heroMindFov[mob.pos + i] = true;
							}
						}
					}
				}
			}
			
			if (c.buff( Awareness.class ) != null) {
				for (Heap heap : heaps.valueList()) {
					int p = heap.pos;
					for (int i : PathFinder.NEIGHBOURS9) heroMindFov[p+i] = true;
				}
			}

			for (TalismanOfForesight.CharAwareness a : c.buffs(TalismanOfForesight.CharAwareness.class)){
				Char ch = (Char) Actor.findById(a.charID);
				if (ch == null || !ch.isAlive()) {
					continue;
				}
				int p = ch.pos;
				for (int i : PathFinder.NEIGHBOURS9) heroMindFov[p+i] = true;
			}

			for (TalismanOfForesight.HeapAwareness h : c.buffs(TalismanOfForesight.HeapAwareness.class)){
				if (Dungeon.depth != h.depth || Dungeon.branch != h.branch) continue;
				for (int i : PathFinder.NEIGHBOURS9) heroMindFov[h.pos+i] = true;
			}

			for (Mob m : mobs){
				if (m instanceof WandOfWarding.Ward
						|| m instanceof WandOfRegrowth.Lotus
						|| m instanceof SpiritHawk.HawkAlly
						|| m.buff(PowerOfMany.PowerBuff.class) != null){
					if (m.fieldOfView == null || m.fieldOfView.length != length()){
						m.fieldOfView = new boolean[length()];
						Dungeon.level.updateFieldOfView( m, m.fieldOfView );
					}
					BArray.or(heroMindFov, m.fieldOfView, heroMindFov);
				}
			}

			for (RevealedArea a : c.buffs(RevealedArea.class)){
				if (Dungeon.depth != a.depth || Dungeon.branch != a.branch) continue;
				for (int i : PathFinder.NEIGHBOURS9) heroMindFov[a.pos+i] = true;
			}

			//set mind vision chars
			for (Mob mob : mobs) {
				if (heroMindFov[mob.pos] && !fieldOfView[mob.pos]){
					Dungeon.hero.mindVisionEnemies.add(mob);
				}
			}

			BArray.or(heroMindFov, fieldOfView, fieldOfView);

		}

		if (c == Dungeon.hero) {
			for (Heap heap : heaps.valueList())
				if (!heap.seen && fieldOfView[heap.pos])
					heap.seen = true;
		}

	}

	public float levelExplorePercent( int depth ){
		return 0;
	}
	
	public int distance( int a, int b ) {
		int ax = a % width();
		int ay = a / width();
		int bx = b % width();
		int by = b / width();
		return Math.max( Math.abs( ax - bx ), Math.abs( ay - by ) );
	}
	
	public boolean adjacent( int a, int b ) {
		return distance( a, b ) == 1;
	}
	
	//uses pythagorean theorum for true distance, as if there was no movement grid
	public float trueDistance(int a, int b){
		int ax = a % width();
		int ay = a / width();
		int bx = b % width();
		int by = b / width();
		return (float)Math.sqrt(Math.pow(Math.abs( ax - bx ), 2) + Math.pow(Math.abs( ay - by ), 2));
	}

	//usually just if a cell is solid, but other cases exist too
	public boolean invalidHeroPos( int tile ){
		return !passable[tile] && !avoid[tile];
	}

	//returns true if the input is a valid tile within the level
	public boolean insideMap( int tile ){
				//top and bottom row and beyond
		return !((tile < width || tile >= length - width) ||
				//left and right column
				(tile % width == 0 || tile % width == width-1));
	}

	public Point cellToPoint( int cell ){
		return new Point(cell % width(), cell / width());
	}

	public int pointToCell( Point p ){
		return p.x + p.y*width();
	}
	
	public String tileName( int tile ) {
		
		switch (tile) {
			case Terrain.CHASM:
				return Messages.get(Level.class, "chasm_name");
			case Terrain.EMPTY:
			case Terrain.EMPTY_SP:
			case Terrain.EMPTY_DECO:
			case Terrain.CUSTOM_DECO_EMPTY:
			case Terrain.SECRET_TRAP:
				return Messages.get(Level.class, "floor_name");
			case Terrain.GRASS:
				return Messages.get(Level.class, "grass_name");
			case Terrain.WATER:
				return Messages.get(Level.class, "water_name");
			case Terrain.WALL:
			case Terrain.WALL_DECO:
			case Terrain.SECRET_DOOR:
				return Messages.get(Level.class, "wall_name");
			case Terrain.DOOR:
				return Messages.get(Level.class, "closed_door_name");
			case Terrain.OPEN_DOOR:
				return Messages.get(Level.class, "open_door_name");
			case Terrain.ENTRANCE:
			case Terrain.ENTRANCE_SP:
				return Messages.get(Level.class, "entrace_name");
			case Terrain.EXIT:
				return Messages.get(Level.class, "exit_name");
			case Terrain.EMBERS:
				return Messages.get(Level.class, "embers_name");
			case Terrain.FURROWED_GRASS:
				return Messages.get(Level.class, "furrowed_grass_name");
			case Terrain.LOCKED_DOOR:
			case Terrain.HERO_LKD_DR:
				return Messages.get(Level.class, "locked_door_name");
			case Terrain.CRYSTAL_DOOR:
				return Messages.get(Level.class, "crystal_door_name");
			case Terrain.PEDESTAL:
				return Messages.get(Level.class, "pedestal_name");
			case Terrain.BARRICADE:
				return Messages.get(Level.class, "barricade_name");
			case Terrain.HIGH_GRASS:
				return Messages.get(Level.class, "high_grass_name");
			case Terrain.LOCKED_EXIT:
				return Messages.get(Level.class, "locked_exit_name");
			case Terrain.UNLOCKED_EXIT:
				return Messages.get(Level.class, "unlocked_exit_name");
			case Terrain.WELL:
				return Messages.get(Level.class, "well_name");
			case Terrain.EMPTY_WELL:
				return Messages.get(Level.class, "empty_well_name");
			case Terrain.STATUE:
			case Terrain.STATUE_SP:
				return Messages.get(Level.class, "statue_name");
			case Terrain.INACTIVE_TRAP:
				return Messages.get(Level.class, "inactive_trap_name");
			case Terrain.BOOKSHELF:
				return Messages.get(Level.class, "bookshelf_name");
			case Terrain.ALCHEMY:
				return Messages.get(Level.class, "alchemy_name");
			default:
				return Messages.get(Level.class, "default_name");
		}
	}
	
	public String tileDesc( int tile ) {
		
		switch (tile) {
			case Terrain.CHASM:
				return Messages.get(Level.class, "chasm_desc");
			case Terrain.WATER:
				return Messages.get(Level.class, "water_desc");
			case Terrain.ENTRANCE:
			case Terrain.ENTRANCE_SP:
				return Messages.get(Level.class, "entrance_desc");
			case Terrain.EXIT:
			case Terrain.UNLOCKED_EXIT:
				return Messages.get(Level.class, "exit_desc");
			case Terrain.EMBERS:
				return Messages.get(Level.class, "embers_desc");
			case Terrain.HIGH_GRASS:
			case Terrain.FURROWED_GRASS:
				return Messages.get(Level.class, "high_grass_desc");
			case Terrain.LOCKED_DOOR:
			case Terrain.HERO_LKD_DR:
				return Messages.get(Level.class, "locked_door_desc");
			case Terrain.CRYSTAL_DOOR:
				return Messages.get(Level.class, "crystal_door_desc");
			case Terrain.LOCKED_EXIT:
				return Messages.get(Level.class, "locked_exit_desc");
			case Terrain.BARRICADE:
				return Messages.get(Level.class, "barricade_desc");
			case Terrain.INACTIVE_TRAP:
				return Messages.get(Level.class, "inactive_trap_desc");
			case Terrain.STATUE:
			case Terrain.STATUE_SP:
				return Messages.get(Level.class, "statue_desc");
			case Terrain.ALCHEMY:
				return Messages.get(Level.class, "alchemy_desc");
			case Terrain.EMPTY_WELL:
				return Messages.get(Level.class, "empty_well_desc");
			default:
				return "";
		}
	}
	//END(port from Arknights): 平台 / 海怪 API
	public java.util.List<com.shatteredpixel.shatteredpixeldungeon.levels.features.Platform> createPlatform(
			com.shatteredpixel.shatteredpixeldungeon.levels.features.Platform.Generator gen, int pos) {
		java.util.List<com.shatteredpixel.shatteredpixeldungeon.levels.features.Platform> made = gen.generate(pos, this);
		for (com.shatteredpixel.shatteredpixeldungeon.levels.features.Platform p : made) {
			platforms.put(p.pos, p);
			com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.createPlatform(p.pos);
		}
		return made;
	}

	public void destroyPlatform(int pos) {
		platforms.remove(pos);
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap(pos);
	}

	public com.shatteredpixel.shatteredpixeldungeon.levels.features.SeaTerror addSeaTerror(int pos) {
		com.shatteredpixel.shatteredpixeldungeon.levels.features.SeaTerror t =
				new com.shatteredpixel.shatteredpixeldungeon.levels.features.SeaTerror();
		t.pos = pos;
		seaTerrors.put(pos, t);
		return t;
	}

	public void destroySeaTerror(int pos) {
		seaTerrors.remove(pos);
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap(pos);
	}

}
