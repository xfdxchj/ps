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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSight;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RevealedArea;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.DivineSense;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.EndGem;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfCorrosion;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfCorruption;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfDisintegration;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfLightning;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfLivingEarth;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfPrismaticLight;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfTransfusion;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EvolvedWandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.items.EndGemItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.DeadEndLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.LastLevel;
//END(移植自魔绫·挑战区)
import com.shatteredpixel.shatteredpixeldungeon.levels.HollowExitLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HollowLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect;
import com.watabou.noosa.Game;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

public class Dungeon {

	//enum of items which have limited spawns, records how many have spawned
	//could all be their own separate numbers, but this allows iterating, much nicer for bundling/initializing.
	public static enum LimitedDrops {
		//limited world drops
		STRENGTH_POTIONS,
		UPGRADE_SCROLLS,
		ARCANE_STYLI,
		ENCH_STONE,
		INT_STONE,
		TRINKET_CATA,
		LAB_ROOM, //actually a room, but logic is the same

		//Health potion sources
		//enemies
		SWARM_HP,
		NECRO_HP,
		BAT_HP,
		WARLOCK_HP,
		HUNR_HP, //END(移植自魔绫): 猎人(ShieldHuntsman)生命药水掉落
		ICERAT_HP, //END(port from MagicLing·Galaxy)
		SNIPER_HP, //END(port from Arknights): 汐斯塔狙击手生命药水掉落
		//Demon spawners are already limited in their spawnrate, no need to limit their health drops
		//alchemy
		COOKING_HP,
		BLANDFRUIT_SEED,

		//Other limited enemy drops
		SLIME_WEP,
		SKELE_WEP,
		THEIF_MISC,
		GUARD_ARM,
		SHAMAN_WAND,
		DM200_EQUIP,
		GOLEM_EQUIP,

		//containers
		VELVET_POUCH,
		SCROLL_HOLDER,
		POTION_BANDOLIER,
		MAGICAL_HOLSTER,

		//lore documents
		LORE_SEWERS,
		LORE_PRISON,
		LORE_CAVES,
		LORE_CITY,
		LORE_HALLS;

		public int count = 0;

		//for items which can only be dropped once, should directly access count otherwise.
		public boolean dropped(){
			return count != 0;
		}
		public void drop(){
			count = 1;
		}

		public static void reset(){
			for (LimitedDrops lim : values()){
				lim.count = 0;
			}
		}

		public static void store( Bundle bundle ){
			for (LimitedDrops lim : values()){
				bundle.put(lim.name(), lim.count);
			}
		}

		public static void restore( Bundle bundle ){
			for (LimitedDrops lim : values()){
				if (bundle.contains(lim.name())){
					lim.count = bundle.getInt(lim.name());
				} else {
					lim.count = 0;
				}
				
			}
		}

	}

	public static int challenges;
	public static float mobsToChampion;

	public static Hero hero;
	public static Level level;

	public static QuickSlot quickslot = new QuickSlot();
	
	public static int depth;
	//determines path the hero is on. Current uses:
	// 0 is the default path
	// 1 is for quest sub-floors
	public static int branch;

	//END(方舟兼容桩): 方舟原版用这两个布尔在 31-40F 三选一（海嗣/雨林/海滨，都不选=海滨）。
	// 本 fork 由 ChallengeArea.applySelection() 依据挑战区勾选结果推导，方舟内容类直接读这两个标志。
	public static boolean extrastage_Gavial = false;
	public static boolean extrastage_Sea    = false;

	//keeps track of what levels the game should try to load instead of creating fresh
	public static ArrayList<Integer> generatedLevels = new ArrayList<>();

	public static int gold;
	public static int energy;
	
	public static HashSet<Integer> chapters;

	public static SparseArray<ArrayList<Item>> droppedItems;

	//first variable is only assigned when game is started, second is updated every time game is saved
	public static int initialVersion;
	public static int version;

	public static boolean daily;
	public static boolean dailyReplay;
	public static String customSeedText = "";
	public static long seed;
	public static long lastPlayed;

	//we initialize the seed separately so that things like interlevelscene can access it early
	public static void initSeed(){
		if (daily) {
			//Ensures that daily seeds are not in the range of user-enterable seeds
			seed = SPDSettings.lastDaily() + DungeonSeed.TOTAL_SEEDS;
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			customSeedText = format.format(new Date(SPDSettings.lastDaily()));
		} else if (!SPDSettings.customSeed().isEmpty()){
			customSeedText = SPDSettings.customSeed();
			seed = DungeonSeed.convertFromText(customSeedText);
		} else {
			customSeedText = "";
			seed = DungeonSeed.randomSeed();
		}
	}
	
	public static void init() {

		initialVersion = version = Game.versionCode;
		challenges = SPDSettings.challenges();
		//END(移植自魔绫·挑战区): 把开局勾选的挑战区域写入 Statistics（Hollow 等）
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea
				.applySelection(SPDSettings.challengeAreas());
		mobsToChampion = 1;

		Actor.clear();
		Actor.resetNextID();

		//offset seed slightly to avoid output patterns
		Random.pushGenerator( seed+1 );

			Scroll.initLabels();
			Potion.initColors();
			Ring.initGems();

			SpecialRoom.initForRun();
			SecretRoom.initForRun();

			Generator.fullReset();

		Random.resetGenerators();
		
		Statistics.reset();
		Notes.reset();

		quickslot.reset();
		QuickSlotButton.reset();
		Toolbar.swappedQuickslots = false;
		
		depth = 1;
		branch = 0;
		generatedLevels.clear();

		gold = 0;
		energy = 0;

		droppedItems = new SparseArray<>();

		LimitedDrops.reset();
		
		chapters = new HashSet<>();
		
		Ghost.Quest.reset();
		Wandmaker.Quest.reset();
		Blacksmith.Quest.reset();
		Imp.Quest.reset();

		hero = new Hero();
		hero.live();
		
		Badges.reset();
		
		GamesInProgress.selectedClass.initHero( hero );

		//END 便利测试挑战:勾选 CONVENIENCE 的开局给整套“新增内容+素材”,便于直接做/用所有新增物:
		//5 种宝石各一、13 把进化法杖各一、30 张升级卷、若干强化符石(炼金蜕变料),并全部 identify+入包,
		//同时把它们登记为“本局已见/图鉴已记录”(日志直接可见)。不影响未勾选的普通对局。
		if (isChallenged( Challenges.CONVENIENCE )){
			gold += 300; //测试资金

			for (EndGem gem : EndGem.values()) {
				EndGemItem g = new EndGemItem(gem);
				g.identify();
				g.collect();
			}

			//END 便利：先给几只储物(子包)容器——它们能收纳对应物、相当于把背包“扩大”，
			//后面那堆法杖/卷轴/药水/种子自然不会散到找不见。
			for (com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag b : new com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag[]{
					new com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster(),   //装法杖
					new com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder(),    //装卷轴/符石
					new com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier(), //装药水/合剂
					new com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch()}){   //装种子/植物
				b.collect();
			}

			for (Class<?> e : new Class<?>[]{EvolvedWandOfMagicMissile.class, EvolvedWandOfFireblast.class, EvolvedWandOfLightning.class,EvolvedWandOfBlastWave.class,EvolvedWandOfCorrosion.class,EvolvedWandOfCorruption.class,EvolvedWandOfDisintegration.class,EvolvedWandOfFrost.class,EvolvedWandOfLivingEarth.class,EvolvedWandOfPrismaticLight.class,EvolvedWandOfRegrowth.class,EvolvedWandOfTransfusion.class,EvolvedWandOfWarding.class}){
				try {
					Item w = (Item)(e.getDeclaredConstructor().newInstance());
					w.identify();
					w.collect();
				} catch (Exception ex){ /*忽略单件失败*/ }
			}

			//END 便利：同堆(而非几十个散件)，屏幕少图标、不易埋在别物后
			com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade su = new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade(); su.quantity(15); su.identify(); su.collect();
			com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation stz = new com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation(); stz.quantity(6); stz.identify(); stz.collect();

			//END 便利：为灵能弓改造准备料——灵能核心现由「强化符石 + 驱邪卷轴」合成(见 SpiritBowCoreRecipe)，
			//故额外给若干驱邪卷轴(强化符石上面已给 6)，并保留液金/核心备用。
			com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal lm = new com.shatteredpixel.shatteredpixeldungeon.items.LiquidMetal(); lm.quantity(150); lm.identify(); lm.collect();
			com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse rc = new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse(); rc.quantity(6); rc.identify(); rc.collect();
			com.shatteredpixel.shatteredpixeldungeon.endcontent.items.SpiritBowCore coreSt = new com.shatteredpixel.shatteredpixeldungeon.endcontent.items.SpiritBowCore(); coreSt.quantity(3); coreSt.identify(); coreSt.collect();

			//END 便利：进阶弓三选一配齐“特殊料” 雷鸣魔药 / 唤魔晶柱（随上面升级卷轴即可锻三把），并配一把原版灵能弓当合成基底
			com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew sbS = new com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.ShockingBrew(); sbS.quantity(3); sbS.identify(); sbS.collect();
			com.shatteredpixel.shatteredpixeldungeon.items.spells.SummonElemental seS = new com.shatteredpixel.shatteredpixeldungeon.items.spells.SummonElemental(); seS.quantity(3); seS.identify(); seS.collect();
			com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow bow0 = new com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow(); bow0.identify(); bow0.collect(); //作铁匠炉基底

			//END 便利：三把进化/进阶弓各送一把(identified)，方便直接试用
			com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EndSpiritBowMight   mBow = new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EndSpiritBowMight();   mBow.identify(); mBow.collect();
			com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EndSpiritBowStorm   sBow = new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EndSpiritBowStorm();   sBow.identify(); sBow.collect();
			com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EndSpiritBowSummon  uBow = new com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EndSpiritBowSummon();  uBow.identify(); uBow.collect();

			//END 便利：破印进阶(随破印贴护甲获得)与刺杀匕首玩法的基础件与素材
			com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard shardN = new com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard();
			shardN.quantity(6); shardN.identify(); shardN.collect();
			try {
				com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.AssassinDagger dag = new com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.AssassinDagger();
				dag.identify(); dag.collect();
			} catch (Exception ignore){ /*单件失败忽略*/ }
			try {
				com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit tk = new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit();
				tk.identify(); tk.collect();      //原版炼金工具箱,便于炼金
			} catch (Exception ignore){ /*单件失败忽略*/ }
			//END 便利：给原料级 破印(炼金基底)与破印进阶三成品各一,便于直接试贴护甲
			try {
				com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal bs = new com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal();
				bs.identify(); bs.collect();
			} catch (Exception ignore){ /*单件失败忽略*/ }
			for (Class<?> c : new Class<?>[]{
					com.shatteredpixel.shatteredpixeldungeon.endcontent.armor.BladeShieldSeal.class,
					com.shatteredpixel.shatteredpixeldungeon.endcontent.armor.BloodRageSeal.class,
					com.shatteredpixel.shatteredpixeldungeon.endcontent.armor.FlyWeaponSeal.class}){
				try {
					com.shatteredpixel.shatteredpixeldungeon.items.Item it =
							(com.shatteredpixel.shatteredpixeldungeon.items.Item)c.getDeclaredConstructor().newInstance();
					it.identify(); it.collect();
				} catch (Exception ignore){ /*单件失败忽略*/ }
			}

			//END 便利：跳关测试道具 + 板甲 + 传说武器
			//★ 注意：背包容量仅 20 格，而本便利块会发放 22+ 组物品 → 后面发的会挤出行外看不见。
			//   因此传送符必须**插到 items 列表最前面**（第 0 位），保证一定可见。
			//深渊传送符：多次使用可逐站前进（25F→26F→31F→32F→33F→38F）。
			try {
				com.shatteredpixel.shatteredpixeldungeon.endcontent.items.EndFloorSkip skip =
						new com.shatteredpixel.shatteredpixeldungeon.endcontent.items.EndFloorSkip();
				skip.identify();
				//直接插到最前，绕过容量检查（测试道具，必须拿到）
				hero.belongings.backpack.items.add(0, skip);
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.p(
						"[便利] 已发放：深渊传送符（背包第 1 格，使用可逐站跳层）");
			} catch (Exception e){
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("[便利] 传送符异常: " + e);
			}

			//板甲(原版 PlateArmor)，已鉴定
			try {
				com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor plate =
						new com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor();
				plate.identify(); plate.collect();
			} catch (Exception ignore){ /*单件失败忽略*/ }

			//传说武器一把(随机从 9 把里取，已鉴定)。若想固定某把，把下行的类替换即可。
			try {
				Class<?>[] legend = new Class<?>[]{
						com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.legend.ClearSword.class,
						com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.legend.DiedCrossBow.class,
						com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.legend.ForestBow.class,
						com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.legend.GoldLongGun.class,
						com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.legend.KingAxe.class,
						com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.legend.MoonDao.class,
						com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.legend.RiceSword.class,
						com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.legend.SaiPlus.class};
				Class<?> pick = legend[com.watabou.utils.Random.Int(legend.length)];
				com.shatteredpixel.shatteredpixeldungeon.items.Item lw =
						(com.shatteredpixel.shatteredpixeldungeon.items.Item)pick.getDeclaredConstructor().newInstance();
				lw.identify(); lw.collect();
			} catch (Exception ignore){ /*单件失败忽略*/ }

			//END 便利：把终焉新造物标记“已见”(Catalog) + 记入本局 discovered(日志)
			markEndContentSeen();
		}
	}

	/** 便利测试挑战下：把图鉴登记成“几乎所有会出现物品都见过”，即日志全解锁。 */
	private static void markEndContentSeen(){
		//END: 全解锁——遍历所有 Generator 类别,每个类的实体都标记已见过(方便日志/图鉴直接看全)
		for (Generator.Category c : Generator.Category.values()){
			if (c.classes == null) continue;
			for (Class<?> k : c.classes){
				if (k != null) Catalog.setSeen(k);
			}
		}
		Catalog.setSeen(EndGemItem.class);
		for (Class<?> clz : new Class<?>[]{
				//进化法杖(13)
				EvolvedWandOfMagicMissile.class,
				EvolvedWandOfFireblast.class,
				EvolvedWandOfLightning.class,
				EvolvedWandOfBlastWave.class,
				EvolvedWandOfCorrosion.class,
				EvolvedWandOfCorruption.class,
				EvolvedWandOfDisintegration.class,
				EvolvedWandOfFrost.class,
				EvolvedWandOfLivingEarth.class,
				EvolvedWandOfPrismaticLight.class,
				EvolvedWandOfRegrowth.class,
				EvolvedWandOfTransfusion.class,
				EvolvedWandOfWarding.class
		}){
			Catalog.setSeen(clz);
		}
	}

	public static boolean isChallenged( int mask ) {
		return (challenges & mask) != 0;
	}

	public static boolean levelHasBeenGenerated(int depth, int branch){
		return generatedLevels.contains(depth + 1000*branch);
	}
	
	public static Level newLevel() {
		
		Dungeon.level = null;
		Actor.clear();
		
		Level level;
		if (branch == 0) {
			switch (depth) {
				case 1:
				case 2:
				case 3:
				case 4:
					level = new SewerLevel();
					break;
				case 5:
					level = new SewerBossLevel();
					break;
				case 6:
				case 7:
				case 8:
				case 9:
					level = new PrisonLevel();
					break;
				case 10:
					level = new PrisonBossLevel();
					break;
				case 11:
				case 12:
				case 13:
				case 14:
					level = new CavesLevel();
					break;
				case 15:
					level = new CavesBossLevel();
					break;
				case 16:
				case 17:
				case 18:
				case 19:
					level = new CityLevel();
					break;
				case 20:
					level = new CityBossLevel();
					break;
				case 21:
				case 22:
				case 23:
				case 24:
					level = new HallsLevel();
					break;
				case 25:
					level = new HallsBossLevel();
					break;
				case 26:
					//END(移植自魔绫·挑战区): 26F 起按「选中的区域顺序」动态分配层号。
					// 见 ChallengeArea.floorPlan()：把选中的区域串成一条层号区间，单选也能走通。
					int[] plan = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea
							.areaAtDepth(depth);
					if (plan == null) {
						level = new LastLevel();      //没选任何挑战区 → 正常主线结局
					} else {
						level = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea
								.createAreaLevel(plan[0], plan[1], depth);
					}
					break;
				default:
					//END: 26F 之后的层号全部交给挑战区动态分配（未分配则视为终局）
					int[] plan2 = (depth > 26)
							? com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea.areaAtDepth(depth)
							: null;
					if (plan2 != null) {
						level = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea
								.createAreaLevel(plan2[0], plan2[1], depth);
					} else {
						level = new DeadEndLevel();
					}
			}
		} else if (branch == 1) {
			switch (depth) {
				case 11:
				case 12:
				case 13:
				case 14:
					level = new MiningLevel();
					break;
				case 16:
				case 17:
				case 18:
				case 19:
					level = new VaultLevel();
					break;
				default:
					level = new DeadEndLevel();
			}
		} else {
			level = new DeadEndLevel();
		}

		//dead end levels (and vault levels for now!) get cleared, don't count as generated
		if (!(level instanceof DeadEndLevel || level instanceof VaultLevel)){
			//this assumes that we will never have a depth value outside the range 0 to 999
			// or -500 to 499, etc.
			if (!generatedLevels.contains(depth + 1000*branch)) {
				generatedLevels.add(depth + 1000 * branch);
			}

			if (depth > Statistics.deepestFloor && branch == 0) {
				Statistics.deepestFloor = depth;

				if (Statistics.qualifiedForNoKilling) {
					Statistics.completedWithNoKilling = true;
				} else {
					Statistics.completedWithNoKilling = false;
				}
			}
		}

		Statistics.qualifiedForBossRemainsBadge = false;
		
		level.create();
		
		if (branch == 0) Statistics.qualifiedForNoKilling = !bossLevel();
		Statistics.qualifiedForBossChallengeBadge = false;
		
		return level;
	}
	
	public static void resetLevel() {
		
		Actor.clear();
		
		level.reset();
		switchLevel( level, level.entrance() );
	}

	public static long seedCurDepth(){
		return seedForDepth(depth, branch);
	}

	public static long seedForDepth(int depth, int branch){
		int lookAhead = depth;
		lookAhead += 30*branch; //Assumes depth is always 1-30, and branch is always 0 or higher

		Random.pushGenerator( seed );

			for (int i = 0; i < lookAhead; i ++) {
				Random.Long(); //we don't care about these values, just need to go through them
			}
			long result = Random.Long();

		Random.popGenerator();
		return result;
	}
	
	public static boolean shopOnLevel() {
		//END(修复·挑战区): 挑战区不额外生成商店（避免与方舟关卡布局冲突）
		if (depth >= 26) return false;
		return depth == 6 || depth == 11 || depth == 16;
	}
	
	public static boolean bossLevel() {
		return bossLevel( depth );
	}
	
	public static boolean bossLevel( int depth ) {
		//END(修复·挑战区): 挑战区的 Boss 层由 ChallengeArea 决定（区内偏移 4 和 9）。
		//原实现只认 5/10/15/20/25，挑战区永远不会被判定为 Boss 层。
		if (depth >= 26) {
			int[] info = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeArea.areaAtDepth(depth);
			if (info != null && (info[1] == 4 || info[1] == 9)) return true;
		}
		return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth == 25;
	}

	//value used for scaling of damage values and other effects.
	//is usually the dungeon depth, but can be set to 26 when ascending
	public static int scalingDepth(){
		if (Dungeon.hero != null && Dungeon.hero.buff(AscensionChallenge.class) != null){
			return 26;
		} else {
			return depth;
		}
	}

	public static boolean interfloorTeleportAllowed(){
		if (Dungeon.level.locked
				|| Dungeon.level instanceof MiningLevel
				|| (Dungeon.hero != null && Dungeon.hero.belongings.getItem(Amulet.class) != null)){
			return false;
		}
		return true;
	}
	
	public static void switchLevel( final Level level, int pos ) {

		//Position of -2 specifically means trying to place the hero the exit
		if (pos == -2){
			LevelTransition t = level.getTransition(LevelTransition.Type.REGULAR_EXIT);
			if (t != null) pos = t.cell();
		}

		//Place hero at the entrance if they are out of the map (often used for pos = -1)
		// or if they are in invalid terrain terrain (except in the mining level, where that happens normally)
		if (pos < 0 || pos >= level.length() || level.invalidHeroPos(pos)){
			//END(修复·挑战区): 挑战区关卡（方舟 12 关等）可能没有 ENTRANCE 类型的过渡
			//（它们的出口被强制改成了 REGULAR_ENTRANCE 以避免跳层），
			//此时 getTransition(null) 会返回 null → NPE。
			//兜底顺序：任意 entrance → 任意过渡 → 关卡第一个可行走格。
			LevelTransition fallback = level.getTransition(null);
			if (fallback == null && level.transitions != null && !level.transitions.isEmpty()) {
				fallback = level.transitions.get(0);
			}
			if (fallback != null) {
				pos = fallback.cell();
			} else {
				pos = level.randomRespawnCell(null);      // 最后的兜底
				if (pos < 0) pos = 0;
			}
		}
		
		PathFinder.setMapSize(level.width(), level.height());
		
		Dungeon.level = level;
		hero.pos = pos;

		if (hero.buff(AscensionChallenge.class) != null){
			hero.buff(AscensionChallenge.class).onLevelSwitch();
		}

		Mob.restoreAllies( level, pos );

		Actor.init();

		level.addRespawner();
		
		for(Mob m : level.mobs){
			if (m.pos == hero.pos && !Char.hasProp(m, Char.Property.IMMOVABLE)){
				//displace mob
				for(int i : PathFinder.NEIGHBOURS8){
					if (Actor.findChar(m.pos+i) == null && level.passable[m.pos + i]){
						m.pos += i;
						break;
					}
				}
			}
		}
		
		Light light = hero.buff( Light.class );
		hero.viewDistance = light == null ? level.viewDistance : Math.max( Light.DISTANCE, level.viewDistance );
		
		hero.curAction = hero.lastAction = null;

		observe();
		try {
			saveAll();
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException(e);
			/*This only catches IO errors. Yes, this means things can go wrong, and they can go wrong catastrophically.
			But when they do the user will get a nice 'report this issue' dialogue, and I can fix the bug.*/
		}
	}

	public static void dropToChasm( Item item ) {
		int depth = Dungeon.depth + 1;
		ArrayList<Item> dropped = Dungeon.droppedItems.get( depth );
		if (dropped == null) {
			Dungeon.droppedItems.put( depth, dropped = new ArrayList<>() );
		}
		dropped.add( item );
	}

	public static boolean posNeeded() {
		//2 POS each floor set
		int posLeftThisSet = 2 - (LimitedDrops.STRENGTH_POTIONS.count - (depth / 5) * 2);
		if (posLeftThisSet <= 0) return false;

		int floorThisSet = (depth % 5);

		//pos drops every two floors, (numbers 1-2, and 3-4) with a 50% chance for the earlier one each time.
		int targetPOSLeft = 2 - floorThisSet/2;
		if (floorThisSet % 2 == 1 && Random.Int(2) == 0) targetPOSLeft --;

		if (targetPOSLeft < posLeftThisSet) return true;
		else return false;

	}
	
	public static boolean souNeeded() {
		int souLeftThisSet;
		//3 SOU each floor set
		souLeftThisSet = 3 - (LimitedDrops.UPGRADE_SCROLLS.count - (depth / 5) * 3);
		if (souLeftThisSet <= 0) return false;

		int floorThisSet = (depth % 5);
		//chance is floors left / scrolls left
		return Random.Int(5 - floorThisSet) < souLeftThisSet;
	}
	
	public static boolean asNeeded() {
		//1 AS each floor set
		int asLeftThisSet = 1 - (LimitedDrops.ARCANE_STYLI.count - (depth / 5));
		if (asLeftThisSet <= 0) return false;

		int floorThisSet = (depth % 5);
		//chance is floors left / scrolls left
		return Random.Int(5 - floorThisSet) < asLeftThisSet;
	}

	public static boolean enchStoneNeeded(){
		//1 enchantment stone, spawns on chapter 2 or 3
		if (!LimitedDrops.ENCH_STONE.dropped()){
			int region = 1+depth/5;
			if (region > 1){
				int floorsVisited = depth - 5;
				if (floorsVisited > 4) floorsVisited--; //skip floor 10
				return Random.Int(9-floorsVisited) == 0; //1/8 chance each floor
			}
		}
		return false;
	}

	public static boolean intStoneNeeded(){
		//one stone on floors 1-3
		return depth < 5 && !LimitedDrops.INT_STONE.dropped() && Random.Int(4-depth) == 0;
	}

	public static boolean trinketCataNeeded(){
		//one trinket catalyst on floors 1-3
		return depth < 5 && !LimitedDrops.TRINKET_CATA.dropped() && Random.Int(4-depth) == 0;
	}

	public static boolean labRoomNeeded(){
		//END(修复·挑战区·关键): 挑战区（26F+）是定制关卡，**不该插原版实验室房**。
		//原实现 region = 1+depth/5，挑战区 depth=26..45 → region=6..10，
		//而 LAB_ROOM.count 最多到 5 → 条件恒真 → **每层都插实验室房**，
		//挤占标准房间 → 实测症状："常规层只有一个小房间、没有下楼楼梯"。
		//即使是正常打到 26F（count=5），region=6 仍然 > 5，同样每层触发。
		if (depth >= 26) return false;

		//one laboratory each floor set, in floor 3 or 4, 1/2 chance each floor
		int region = 1+depth/5;
		if (region > LimitedDrops.LAB_ROOM.count){
			int floorThisRegion = depth%5;
			if (floorThisRegion >= 4 || (floorThisRegion == 3 && Random.Int(2) == 0)){
				return true;
			}
		}
		return false;
	}

	private static final String INIT_VER	= "init_ver";
	public  static final String VERSION		= "version";
	private static final String SEED		= "seed";
	private static final String CUSTOM_SEED	= "custom_seed";
	private static final String DAILY	    = "daily";
	private static final String DAILY_REPLAY= "daily_replay";
	private static final String LAST_PLAYED = "last_played";
	private static final String CHALLENGES	= "challenges";
	private static final String MOBS_TO_CHAMPION	= "mobs_to_champion";
	private static final String HERO		= "hero";
	private static final String DEPTH		= "depth";
	private static final String BRANCH		= "branch";
	private static final String GENERATED_LEVELS    = "generated_levels";
	private static final String GOLD		= "gold";
	private static final String ENERGY		= "energy";
	private static final String DROPPED     = "dropped%d";
	private static final String PORTED      = "ported%d";
	private static final String LEVEL		= "level";
	private static final String LIMDROPS    = "limited_drops";
	private static final String CHAPTERS	= "chapters";
	private static final String QUESTS		= "quests";
	private static final String BADGES		= "badges";
	
	public static void saveGame( int save ) {
		try {
			Bundle bundle = new Bundle();

			bundle.put( INIT_VER, initialVersion );
			//与 Level.store 一致:开发态(未打包)桌面 Game.versionCode 可能为 -1,禁止写盘以免
			//读档被 Level/Dungeon 版本门(需 >=v2_5_4)判成 “old save”。下限 v2_5_4。
			bundle.put( VERSION, version = Math.max( Game.versionCode, ShatteredPixelDungeon.v2_5_4 ) );
			bundle.put( SEED, seed );
			bundle.put( CUSTOM_SEED, customSeedText );
			bundle.put( DAILY, daily );
			bundle.put( DAILY_REPLAY, dailyReplay );
			bundle.put( LAST_PLAYED, lastPlayed = Game.realTime);
			bundle.put( CHALLENGES, challenges );
			bundle.put( MOBS_TO_CHAMPION, mobsToChampion );
			bundle.put( HERO, hero );
			bundle.put( DEPTH, depth );
			bundle.put( BRANCH, branch );

			bundle.put( GOLD, gold );
			bundle.put( ENERGY, energy );

			for (int d : droppedItems.keyArray()) {
				bundle.put(Messages.format(DROPPED, d), droppedItems.get(d));
			}

			quickslot.storePlaceholders( bundle );

			Bundle limDrops = new Bundle();
			LimitedDrops.store( limDrops );
			bundle.put ( LIMDROPS, limDrops );
			
			int count = 0;
			int ids[] = new int[chapters.size()];
			for (Integer id : chapters) {
				ids[count++] = id;
			}
			bundle.put( CHAPTERS, ids );
			
			Bundle quests = new Bundle();
			Ghost		.Quest.storeInBundle( quests );
			Wandmaker	.Quest.storeInBundle( quests );
			Blacksmith	.Quest.storeInBundle( quests );
			Imp			.Quest.storeInBundle( quests );
			bundle.put( QUESTS, quests );
			
			SpecialRoom.storeRoomsInBundle( bundle );
			SecretRoom.storeRoomsInBundle( bundle );
			
			Statistics.storeInBundle( bundle );
			Notes.storeInBundle( bundle );
			Generator.storeInBundle( bundle );

			int[] bundleArr = new int[generatedLevels.size()];
			for (int i = 0; i < generatedLevels.size(); i++){
				bundleArr[i] = generatedLevels.get(i);
			}
			bundle.put( GENERATED_LEVELS, bundleArr);
			
			Scroll.save( bundle );
			Potion.save( bundle );
			Ring.save( bundle );

			Actor.storeNextID( bundle );
			
			Bundle badges = new Bundle();
			Badges.saveLocal( badges );
			bundle.put( BADGES, badges );
			
			FileUtils.bundleToFile( GamesInProgress.gameFile(save), bundle);
			
		} catch (IOException e) {
			GamesInProgress.setUnknown( save );
			ShatteredPixelDungeon.reportException(e);
		}
	}
	
	public static void saveLevel( int save ) throws IOException {
		Bundle bundle = new Bundle();
		bundle.put( LEVEL, level );
		
		FileUtils.bundleToFile(GamesInProgress.depthFile( save, depth, branch ), bundle);
	}
	
	public static void saveAll() throws IOException {
		if (hero != null && (hero.isAlive() || WndResurrect.instance != null)) {
			
			Actor.fixTime();
			updateLevelExplored();
			saveGame( GamesInProgress.curSlot );
			saveLevel( GamesInProgress.curSlot );

			GamesInProgress.set( GamesInProgress.curSlot );

		}
	}
	
	public static void loadGame( int save ) throws IOException {
		loadGame( save, true );
	}
	
	public static void loadGame( int save, boolean fullLoad ) throws IOException {
		
		Bundle bundle = FileUtils.bundleFromFile( GamesInProgress.gameFile( save ) );

		initialVersion = bundle.getInt( INIT_VER );
		version = bundle.getInt( VERSION );

		seed = bundle.contains( SEED ) ? bundle.getLong( SEED ) : DungeonSeed.randomSeed();
		customSeedText = bundle.getString( CUSTOM_SEED );
		daily = bundle.getBoolean( DAILY );
		dailyReplay = bundle.getBoolean( DAILY_REPLAY );

		Actor.clear();
		Actor.restoreNextID( bundle );

		quickslot.reset();
		QuickSlotButton.reset();
		Toolbar.swappedQuickslots = false;

		Dungeon.challenges = bundle.getInt( CHALLENGES );
		Dungeon.mobsToChampion = bundle.getFloat( MOBS_TO_CHAMPION );
		
		Dungeon.level = null;
		Dungeon.depth = -1;
		
		Scroll.restore( bundle );
		Potion.restore( bundle );
		Ring.restore( bundle );

		quickslot.restorePlaceholders( bundle );
		
		if (fullLoad) {
			
			LimitedDrops.restore( bundle.getBundle(LIMDROPS) );

			chapters = new HashSet<>();
			int ids[] = bundle.getIntArray( CHAPTERS );
			if (ids != null) {
				for (int id : ids) {
					chapters.add( id );
				}
			}
			
			Bundle quests = bundle.getBundle( QUESTS );
			if (!quests.isNull()) {
				Ghost.Quest.restoreFromBundle( quests );
				Wandmaker.Quest.restoreFromBundle( quests );
				Blacksmith.Quest.restoreFromBundle( quests );
				Imp.Quest.restoreFromBundle( quests );
			} else {
				Ghost.Quest.reset();
				Wandmaker.Quest.reset();
				Blacksmith.Quest.reset();
				Imp.Quest.reset();
			}
			
			SpecialRoom.restoreRoomsFromBundle(bundle);
			SecretRoom.restoreRoomsFromBundle(bundle);

			generatedLevels.clear();
			for (int i : bundle.getIntArray(GENERATED_LEVELS)){
				generatedLevels.add(i);
			}

			droppedItems = new SparseArray<>();
			for (int i=1; i <= 26; i++) {

				//dropped items
				ArrayList<Item> items = new ArrayList<>();
				if (bundle.contains(Messages.format( DROPPED, i )))
					for (Bundlable b : bundle.getCollection( Messages.format( DROPPED, i ) ) ) {
						items.add( (Item)b );
					}
				if (!items.isEmpty()) {
					droppedItems.put( i, items );
				}

			}
		}
		
		Bundle badges = bundle.getBundle(BADGES);
		if (!badges.isNull()) {
			Badges.loadLocal( badges );
		} else {
			Badges.reset();
		}
		
		Notes.restoreFromBundle( bundle );
		
		hero = null;
		hero = (Hero)bundle.get( HERO );
		
		depth = bundle.getInt( DEPTH );
		branch = bundle.getInt( BRANCH );

		gold = bundle.getInt( GOLD );
		energy = bundle.getInt( ENERGY );

		Statistics.restoreFromBundle( bundle );
		Generator.restoreFromBundle( bundle );

	}
	
	public static Level loadLevel( int save ) throws IOException {
		
		Dungeon.level = null;
		Actor.clear();

		Bundle bundle = FileUtils.bundleFromFile( GamesInProgress.depthFile( save, depth, branch ));

		Level level = (Level)bundle.get( LEVEL );

		if (level == null){
			throw new IOException();
		} else {
			return level;
		}
	}
	
	public static void deleteGame( int save, boolean deleteLevels ) {

		if (deleteLevels) {
			String folder = GamesInProgress.gameFolder(save);
			for (String file : FileUtils.filesInDir(folder)){
				if (file.contains("depth")){
					FileUtils.deleteFile(folder + "/" + file);
				}
			}
		}

		FileUtils.overwriteFile(GamesInProgress.gameFile(save), 1);
		
		GamesInProgress.delete( save );
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.depth = bundle.getInt( DEPTH );
		info.version = bundle.getInt( VERSION );
		info.challenges = bundle.getInt( CHALLENGES );
		info.seed = bundle.getLong( SEED );
		info.customSeed = bundle.getString( CUSTOM_SEED );
		info.daily = bundle.getBoolean( DAILY );
		info.dailyReplay = bundle.getBoolean( DAILY_REPLAY );
		info.lastPlayed = bundle.getLong( LAST_PLAYED );

		Hero.preview( info, bundle.getBundle( HERO ) );
		Statistics.preview( info, bundle );
	}
	
	public static void fail( Object cause ) {
		if (WndResurrect.instance == null) {
			updateLevelExplored();
			Statistics.gameWon = false;
			Rankings.INSTANCE.submit( false, cause );
		}
	}
	
	public static void win( Object cause ) {

		updateLevelExplored();
		Statistics.gameWon = true;

		hero.belongings.identify();

		Rankings.INSTANCE.submit( true, cause );
	}

	public static void updateLevelExplored(){
		if (branch == 0 && level instanceof RegularLevel && !Dungeon.bossLevel()){
			Statistics.floorsExplored.put( depth, level.levelExplorePercent(depth));
		}
	}

	//default to recomputing based on max hero vision, in case vision just shrank/grew
	public static void observe(){
		int dist = Math.max(Dungeon.hero.viewDistance, 8);
		dist *= 1f + 0.25f*Dungeon.hero.pointsInTalent(Talent.FARSIGHT);

		if (Dungeon.hero.buff(MagicalSight.class) != null){
			dist = Math.max( dist, MagicalSight.DISTANCE );
		}

		observe( dist+1 );
	}
	
	public static void observe( int dist ) {

		if (level == null) {
			return;
		}
		
		level.updateFieldOfView(hero, level.heroFOV);

		int x = hero.pos % level.width();
		int y = hero.pos / level.width();
	
		//left, right, top, bottom
		int l = Math.max( 0, x - dist );
		int r = Math.min( x + dist, level.width() - 1 );
		int t = Math.max( 0, y - dist );
		int b = Math.min( y + dist, level.height() - 1 );
	
		int width = r - l + 1;
		int height = b - t + 1;
		
		int pos = l + t * level.width();
	
		for (int i = t; i <= b; i++) {
			BArray.or( level.visited, level.heroFOV, pos, width, level.visited );
			pos+=level.width();
		}

		//always visit adjacent tiles, even if they aren't seen
		for (int i : PathFinder.NEIGHBOURS9){
			level.visited[hero.pos+i] = true;
		}
	
		GameScene.updateFog(l, t, width, height);

		if (hero.buff(MindVision.class) != null || hero.buff(DivineSense.DivineSenseTracker.class) != null){
			for (Mob m : level.mobs.toArray(new Mob[0])){
				if (m instanceof Mimic && m.alignment == Char.Alignment.NEUTRAL && ((Mimic) m).stealthy()){
					continue;
				}

				BArray.or( level.visited, level.heroFOV, m.pos - 1 - level.width(), 3, level.visited );
				BArray.or( level.visited, level.heroFOV, m.pos - 1, 3, level.visited );
				BArray.or( level.visited, level.heroFOV, m.pos - 1 + level.width(), 3, level.visited );
				//updates adjacent cells too
				GameScene.updateFog(m.pos, 2);
			}
		}

		if (hero.buff(Awareness.class) != null){
			for (Heap h : level.heaps.valueList()){
				BArray.or( level.visited, level.heroFOV, h.pos - 1 - level.width(), 3, level.visited );
				BArray.or( level.visited, level.heroFOV, h.pos - 1, 3, level.visited );
				BArray.or( level.visited, level.heroFOV, h.pos - 1 + level.width(), 3, level.visited );
				GameScene.updateFog(h.pos, 2);
			}
		}

		for (TalismanOfForesight.CharAwareness c : hero.buffs(TalismanOfForesight.CharAwareness.class)){
			Char ch = (Char) Actor.findById(c.charID);
			if (ch == null || !ch.isAlive()) continue;
			BArray.or( level.visited, level.heroFOV, ch.pos - 1 - level.width(), 3, level.visited );
			BArray.or( level.visited, level.heroFOV, ch.pos - 1, 3, level.visited );
			BArray.or( level.visited, level.heroFOV, ch.pos - 1 + level.width(), 3, level.visited );
			GameScene.updateFog(ch.pos, 2);
		}

		for (TalismanOfForesight.HeapAwareness h : hero.buffs(TalismanOfForesight.HeapAwareness.class)){
			if (Dungeon.depth != h.depth || Dungeon.branch != h.branch) continue;
			BArray.or( level.visited, level.heroFOV, h.pos - 1 - level.width(), 3, level.visited );
			BArray.or( level.visited, level.heroFOV, h.pos - 1, 3, level.visited );
			BArray.or( level.visited, level.heroFOV, h.pos - 1 + level.width(), 3, level.visited );
			GameScene.updateFog(h.pos, 2);
		}

		for (RevealedArea a : hero.buffs(RevealedArea.class)){
			if (Dungeon.depth != a.depth || Dungeon.branch != a.branch) continue;
			BArray.or( level.visited, level.heroFOV, a.pos - 1 - level.width(), 3, level.visited );
			BArray.or( level.visited, level.heroFOV, a.pos - 1, 3, level.visited );
			BArray.or( level.visited, level.heroFOV, a.pos - 1 + level.width(), 3, level.visited );
			GameScene.updateFog(a.pos, 2);
		}

		for (Char ch : Actor.chars()){
			if (ch instanceof WandOfWarding.Ward
					|| ch instanceof WandOfRegrowth.Lotus
					|| ch instanceof SpiritHawk.HawkAlly
					|| ch.buff(PowerOfMany.PowerBuff.class) != null){
				x = ch.pos % level.width();
				y = ch.pos / level.width();

				//left, right, top, bottom
				dist = ch.viewDistance+1;
				l = Math.max( 0, x - dist );
				r = Math.min( x + dist, level.width() - 1 );
				t = Math.max( 0, y - dist );
				b = Math.min( y + dist, level.height() - 1 );

				width = r - l + 1;
				height = b - t + 1;

				pos = l + t * level.width();

				for (int i = t; i <= b; i++) {
					BArray.or( level.visited, level.heroFOV, pos, width, level.visited );
					pos+=level.width();
				}
				GameScene.updateFog(ch.pos, dist);
			}
		}

		GameScene.afterObserve();
	}

	//we store this to avoid having to re-allocate the array with each pathfind
	private static boolean[] passable;

	private static void setupPassable(){
		if (passable == null || passable.length != Dungeon.level.length())
			passable = new boolean[Dungeon.level.length()];
		else
			BArray.setFalse(passable);
	}

	public static boolean[] findPassable(Char ch, boolean[] pass, boolean[] vis, boolean chars){
		return findPassable(ch, pass, vis, chars, chars);
	}

	public static boolean[] findPassable(Char ch, boolean[] pass, boolean[] vis, boolean chars, boolean considerLarge){
		setupPassable();
		if (ch.flying || ch.buff( Amok.class ) != null) {
			BArray.or( pass, Dungeon.level.avoid, passable );
		} else {
			System.arraycopy( pass, 0, passable, 0, Dungeon.level.length() );
		}

		if (considerLarge && Char.hasProp(ch, Char.Property.LARGE)){
			BArray.and( passable, Dungeon.level.openSpace, passable );
		}

		ch.modifyPassable(passable);

		if (chars) {
			for (Char c : Actor.chars()) {
				if (vis[c.pos]) {
					passable[c.pos] = false;
				}
			}
		}

		return passable;
	}

	public static PathFinder.Path findPath(Char ch, int to, boolean[] pass, boolean[] vis, boolean chars) {

		return PathFinder.find( ch.pos, to, findPassable(ch, pass, vis, chars) );

	}
	
	public static int findStep(Char ch, int to, boolean[] pass, boolean[] visible, boolean chars ) {

		if (Dungeon.level.adjacent( ch.pos, to )) {
			return Actor.findChar( to ) == null && pass[to] ? to : -1;
		}

		return PathFinder.getStep( ch.pos, to, findPassable(ch, pass, visible, chars) );

	}

	public static int flee( Char ch, int from, boolean[] pass, boolean[] visible, boolean chars ) {
		boolean[] passable = findPassable(ch, pass, visible, false, true);
		passable[ch.pos] = true;

		//chars affected by terror have a shorter lookahead and can't approach the fear source
		boolean canApproachFromPos = ch.buff(Terror.class) == null && ch.buff(Dread.class) == null;
		int step = PathFinder.getStepBack( ch.pos, from, canApproachFromPos ? 8 : 4, passable, canApproachFromPos );

		//only consider chars impassable if our retreat step runs into them
		while (step != -1 && Actor.findChar(step) != null && chars){
			passable[step] = false;
			step = PathFinder.getStepBack( ch.pos, from, canApproachFromPos ? 8 : 4, passable, canApproachFromPos );
		}
		return step;

	}


	//END(修复·挑战区): 挑战区落在主线 26F 之后的连续层（26..45+）。
	//但物品档位、商店定价、稀有度等系统都按 `depth/5` 分段的"主线层段"设计，
	//直接用 26+ 会落到 gate 边界（最深一段）或越界，刷出与玩家装备水平不匹配的东西。
	//
	//这里给出**等效主线层号**：
	//   主线 1..25      -> 原样返回
	//   挑战区 26..30   -> 21..25（第 5 段，最深主线区间的装备/难度）
	//   挑战区 31..35   -> 21..25（循环，保持在同一档）
	//   ...
	//即：挑战区的物品分布默认按 **20~24F** 这一档来算。
	public static int effectiveDepth() {
		return effectiveDepth(depth);
	}

	public static int effectiveDepth(int d) {
		final int MAIN_MAX = 25;          //主线终点
		final int CHALLENGE_START = 26;   //挑战区起点
		final int BAND_START = 21;        //挑战区等效起点（20~24F 这一档）
		final int BAND_SIZE = 5;          //每档 5 层

		if (d < CHALLENGE_START) {
			return d;                     //主线：不变
		}
		int offset = d - CHALLENGE_START; //挑战区内的偏移
		return BAND_START + (offset % BAND_SIZE);
	}

	//END(port from Arknights): mulaCount
	public static int mulaCount;

	//END(port from Arknights): siesta1_bosspower
	public static int siesta1_bosspower;

	//END(port from Arknights): eazymode
	public static int eazymode;

	//END(port from Arknights): isInRhodes
	public static boolean isInRhodes() {
        return depth == 0 && branch >= 1 && branch <= 4;
    }
}
