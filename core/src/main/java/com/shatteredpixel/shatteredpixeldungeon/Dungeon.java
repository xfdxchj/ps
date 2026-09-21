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
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
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

	/**
	 * END(挑战框架): 完整挑战掩码（位号 = 表 ID，可容纳全部 118 条规则）。
	 *
	 * <p>{@link #challenges}（int）是它的**低 12 位投影**，供 143 处老的
	 * {@code isChallenged(int)} 调用点继续工作。两者通过
	 * {@link #setChallenges} / {@link #setChallengeMask} 保持同步，
	 * 不要单独改其中一个。
	 */
	public static com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask
			challengeMask =
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask.empty();

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
		//END(挑战框架): 优先读完整掩码（高位规则也在里面）；
		//完整掩码为空时 SPDSettings.challengeMask() 会自行退化到旧 int。
		setChallengeMask( SPDSettings.challengeMask() );

		//END(挑战·音频): 只加载已勾选挑战用到的音频（31 个文件不全量预载）。
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeSfx.init();

		//END(便利挑战): 激活便利挑战时解锁全部炼金配方页
		if (isChallenged(Challenges.CONVENIENCE)) {
			Document.unlockAllAlchemyPages();
		}

		//END(移植自魔绫·挑战区): 把开局勾选的挑战区域写入 Statistics（Hollow 等）
		//
		//==== END(改造·挑战区并入挑战列表): 改从 ChallengeMask 读 ====
		//原先这里读的是 SPDSettings.challengeAreas()（挑战区**独立**的存档键）。
		//现在 6 个区已经是挑战规则表里的条目，勾选状态存在 challengeMask 里，
		//所以优先从那里推导；旧键作为**回退**保留，让老存档仍然能进挑战区。
		//
		//优先顺序：
		//  1) 存档里已有的 Statistics.challengeMask（本局已定，不能被外部设置污染）
		//  2) 从当前 challengeMask 推导出的区域掩码（新入口）
		//  3) SPDSettings.challengeAreas()（旧键，兼容老存档）
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea
				.applySelection( com.shatteredpixel.shatteredpixeldungeon.endcontent
						.challenge.ChallengeArea.areasFromChallengeMask(
								Statistics.challengeMask != 0
										? Statistics.challengeMask
										: (Dungeon.challengeMask != null
												? Dungeon.challengeMask.areaBits()
												: 0),
								SPDSettings.challengeAreas() ) );
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

		//END(无尽轮回): 新的一局从"尚未轮回"开始
		com.shatteredpixel.shatteredpixeldungeon.endcontent.Reincarnation.reset();

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

		//==== END(挑战 65 及时雨): 每局重置"第一次必保"标记 ====
		//该标记是静态的，若不重置会跨局残留 —— 上一局用掉了，
		//这一局就不会触发，玩家会以为规则坏了。
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
				.resetTimelyRain();

		//==== END(挑战 155 家传戒指 / 156 家传铠甲): 开局额外装备 ====
		//必须在 initHero 之后 —— 那时 hero.belongings 才建好，能收纳物品。		//==== END(挑战 151 圣明神明): 生命/命中/闪避 +50%，攻击 +30% ====
		//文档所有者说明："玩家生命，命中，闪避提升 50%，攻击提升 30%，
		//但是每 1 回合要停止并祷告"
		//
		//数值部分在这里一次性结算（与 68 极端状态同一位置）。
		//"每回合停止祷告"由 Hero.act() 处理。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.deityBlessing(hero)) {
			hero.grimmScaleMaxHP(com.shatteredpixel.shatteredpixeldungeon.endcontent
					.challenge.ChallengeEffects.DEITY_DEF_MULT);
			hero.grimmScaleAccuracyAndEvasion(com.shatteredpixel.shatteredpixeldungeon
					.endcontent.challenge.ChallengeEffects.DEITY_DEF_MULT);
			//攻击 +30%：加在 damageRoll 的乘算里（见 Hero.damageRoll）
		}

		//==== END(挑战 68 极端状态): 开局压低生命、翻倍命中 ====
		//放在 initHero 之后、发放装备之前 ——
		//必须在这里算，因为它改的是 HT（最大生命）；
		//若放到"每次升级时"，后续升级会把 HT 抬回去，破坏"最低 10"。
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.applyExtremeState(hero);

		//用 collect() 而不是直接塞背包：collect 会走正常的入包流程
		//（处理堆叠、容量、图鉴登记），比手工操作 belongings 可靠。
		for (Item gear : com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.startingGear()) {
			if (gear != null) {
				gear.identify();
				gear.collect();
			}
		}

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

			//END(便利): 完整手记 —— 直接给，省去收集 9 张残页的过程。
			//（正式流程是：9 张残页 --3合1--> 3 章节 --3合1--> 完整手记）
			try {
				com.shatteredpixel.shatteredpixeldungeon.items.notes.CompleteNote note =
						new com.shatteredpixel.shatteredpixeldungeon.items.notes.CompleteNote();
				note.identify();
				hero.belongings.backpack.items.add(0, note);
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.p(
						"[便利] 已发放：完整手记（可直接阅读六人的往事）");
			} catch (Exception e){
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("[便利] 完整手记异常: " + e);
			}

			//END(用户要求): 枪械已全部删除，不再发放。
			//END(便利): 9 张笔记残页各一份（用于测试 3 合 1）
			try {
				for (int i = 1; i <= 9; i++) {
					com.shatteredpixel.shatteredpixeldungeon.items.notes.NoteFragment frag =
							new com.shatteredpixel.shatteredpixeldungeon.items.notes.NoteFragment(i);
					frag.identify();
					if (!frag.collect()) {
						hero.belongings.backpack.items.add( frag );
					}
				}
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.p(
						"[便利] 已发放：笔记残页 ×9（可合成 3 章节）");
			} catch (Exception e){
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("[便利] 残页异常: " + e);
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

	//==== END(挑战框架): 新旧掩码的同步入口 ====

	/**
	 * 用旧 int 掩码设置挑战（读旧存档 / 老 UI 用）。
	 * <p>同时把它翻译成新掩码，保证新框架也能看到这些规则。
	 */
	public static void setChallenges( int legacy ) {
		challenges = legacy;
		challengeMask =
				com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRegistry
						.fromLegacyInt( legacy );
	}

	/**
	 * 用完整掩码设置挑战（新 UI 用）。
	 * <p>同步导出低 12 位投影到 {@link #challenges}，
	 * 让所有老的 {@code isChallenged(int)} 判断继续正确。
	 */
	public static void setChallengeMask(
			com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask mask ) {
		challengeMask = (mask == null)
				? com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask.empty()
				: mask;
		challenges =
				com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRegistry
						.toLegacyInt( challengeMask );
	}

	public static boolean levelHasBeenGenerated(int depth, int branch){
		return generatedLevels.contains(depth + 1000*branch);
	}
	
	public static Level newLevel() {
		
		Dungeon.level = null;
		Actor.clear();

		//==== END(挑战 43 一贫如洗 + 79 高级附魔台): 进入新区域时结算 ====
		//判据沿用原版的区域划分 depth % 5（每 5 层一组，"完整地牢"下也一样）。
		//第 1 层是起点，不触发。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.isRegionStart(depth) && hero != null) {

			//---- 43 一贫如洗：金币 -20% ----
			float goldMult = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.goldOnNewRegionMultiplier();
			if (goldMult != 1f) {
				int before = gold;
				gold = (int) Math.floor(gold * goldMult);
				int lost = before - gold;
				if (lost > 0) {
					com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
							com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
									com.shatteredpixel.shatteredpixeldungeon.endcontent
											.challenge.ChallengeEffects.class,
									"destitute_lost", lost));
				}
			}

			//---- 79 高级附魔台：每区域 1 个附魔秘卷 ----
			//"附魔秘卷"即 StoneOfEnchantment（原版强化装备的道具）。
			int scrolls = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.enchantScrollsOnNewRegion();
			if (scrolls > 0 && !bossLevel()) {
				for (int i = 0; i < scrolls; i++) {
					//直接进背包而不是丢地上：这是"区域奖励"，掉在出生点容易被漏掉
					new com.shatteredpixel.shatteredpixeldungeon.items.stones
							.StoneOfEnchantment().collect();
				}
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.i(
						com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
								com.shatteredpixel.shatteredpixeldungeon.endcontent
										.challenge.ChallengeEffects.class,
								"enchant_region_gain", scrolls));
			}
		}

		//==== END(挑战 49 切尔诺贝利): 每层额外给净化药水 ====
		//注意这是**每层**（不是每区域），所以放在上面 depth%5==1 的块**外面**。
		//原表："全图毒气…开局给净化药水，每层额外给" —— 没有解药这层就没法玩。
		int purify = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.chernobylPurifyPerFloor();
		if (purify > 0 && hero != null) {
			for (int i = 0; i < purify; i++) {
				//直接进背包：丢在出生点容易被毒气盖住而看不见
				new com.shatteredpixel.shatteredpixeldungeon.items.potions
						.PotionOfPurity().collect();
			}
		}
		
		Level level;

		//==== END(挑战 129 心爱的少女): 999 层「爱丽丝领域」====
		//放在所有其它分支**之前**：999 是一个特殊层号，
		//不属于任何一个 branch，也不该走"区域映射 / 关卡类选择"那一套。
		//
		//它只由《心爱的少女》这一个物品进入，进入方式见 BelovedGirl.execute()。
		if (depth == com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
				.AliceRealm.DEPTH) {
			level = new com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm
					.AliceRealm();
			//直接返回，跳过下方的所有常规生成逻辑（Boss 判定、挑战区调度等）
			return level;
		}

		if (branch == 0) {

			//==== END(挑战 1 牢地碎破): 贴图/环境也倒置 ====
			//原表描述只提了"区域交叉"，但文档所有者要求**贴图一并倒置**：
			//1 区用 5 区（恶魔大厅）的地板墙壁，5 区用 1 区（下水道）。
			//
			//做法：复用同一个映射函数选**关卡类**（关卡类决定了 painter → 贴图与地形风格）。
			//Boss 层也一并倒置（5F 的 Boss 层改用 25F 的 HallsBossLevel）。
			//
			//注意与"怪物数值查表"分开：
			//  · 贴图  = 用映射后的 depth 选关卡类（这里）
			//  · 数值  = 用**实际** depth 查配置表（ChallengeEffects.crumblingRegion）
			//两者用不同的基准，因为配置表是按"玩家实际所处的区域"配的。
			//
			//未勾选 1 时原样返回 depth，本段等价于不存在。
			//
			//==== END(适配 6 完整地牢): 两级映射串联 ====
			//顺序必须是"先 6 再 1"：
			//  第 1 步：6 把"实际 1-50 层"折算成"等价的原版 1-25 层"
			//          （资源表都是按 1-25 设计的）
			//  第 2 步：1 在那个 25 层制上做区域倒置
			//反过来（先倒置再折算）会让 1 拿到 26-50 这样的越界深度，
			//落进 switch 的 default 分支。
			//
			//未勾选 6 时 fullDungeonMappedDepth 原样返回 depth。
			int resourceDepth = com.shatteredpixel.shatteredpixeldungeon.endcontent
					.challenge.ChallengeEffects.fullDungeonMappedDepth(depth);

			int levelDepth = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.crumblingCrossDepth(resourceDepth);

			switch (levelDepth) {
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
						//==== END(无尽轮回): 没选挑战区 → 进入新一轮，而不是结局 ====
						//文档所有者定稿："没有原版的 26 层，26 层直接变成原版一层的，
						//计入一次轮回，施加一个全局 buff，每次一个，可以叠加。"
						//
						//所以 26 层不再是"结局层"，而是**新一轮的第 1 层**：
						//  · 地图用原版第 1 层（下水道）
						//  · 楼层号**仍然显示 26**（玩家看到的是"第 26 层"）
						//  · 每进一个新循环，轮回 +1 层
						//
						//**前提**：勾选了挑战 210「永无止境」。
						//文档所有者定稿："开启靠永无止境挑战" ——
						//没勾选时整个轮回系统等于不存在，26 层走原版结局。
						if (com.shatteredpixel.shatteredpixeldungeon.endcontent
								.Reincarnation.enabled()){
							level = createReincarnatedLevel(depth);
						} else {
							level = new LastLevel();      //没开 210 → 原版结局
						}
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
						//==== END(无尽轮回): 26+ 之后继续循环，直到九轮走完 ====
						//文档所有者定稿："9 次后到达原版 26 层，结束。"
						//
						//分三种：
						//  · 没勾选 210「永无止境」 → 原版死路（轮回系统不存在）
						//  · 还在九轮之内           → 生成循环层（怪物吃对应诅咒）
						//  · 九轮走完               → 交给原版结局（LastLevel）
						if (!com.shatteredpixel.shatteredpixeldungeon.endcontent
								.Reincarnation.enabled()){
							level = new DeadEndLevel();        //没开 210 → 原版死路
						} else if (com.shatteredpixel.shatteredpixeldungeon.endcontent
								.Reincarnation.shouldEnd(depth)){
							level = new LastLevel();           //九轮走完 → 正常结局
						} else {
							level = createReincarnatedLevel(depth);
						}
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

		//==== END(挑战 61 炸弹狂魔): 每层额外掉落 1 个炸弹 ====
		//放在 level.create() 之后、level 正式启用之前 ——
		//此时地图与既有掉落都已就位，再补一个炸弹不会打乱关卡的 RNG 序列。
		//Boss 层也生效（原表未排除）。
		int bonusBombs = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.bonusBombsPerLevel();
		for (int i = 0; i < bonusBombs; i++) {
			int cell = level.randomRespawnCell(null);
			if (cell != -1) {
				level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb(), cell);
			}
		}

		if (branch == 0) Statistics.qualifiedForNoKilling = !bossLevel();

		//==== END(挑战 15 首领护卫): Boss 层额外生成 3 个精英护卫 ====
		//放在 level.create() 之后：
		//  · 此时地形与既有怪物都已就位，能找到合法的生成格
		//  · 且还没进入"玩家已经开始行动"的阶段
		//
		//为什么不用 Level.seal()（那是"进入 Boss 战"的统一点）：
		//玩家可能用传送/位移绕过 Boss 房再回来，seal 会被多次触发；
		//而 newLevel 每层只跑一次，语义清晰。
		//==== END(挑战 152 和平地牢): 换层重置合约 ====
		//文档所有者说明："每下一层重置" —— 上一层的违约不带下去。
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.resetPeaceful();


		//（挑战 120 404 的接线在 Hero.act()：每回合 0.5% 概率）


		//==== END(挑战 151/166 圣明神明): 重置祷告节律计数 ====
		//天使形态（166）会把"每 2 回合停一次"放宽到"每 4 回合"，
		//而 angelForm() 依赖当前掩码 —— 换局时计数器应该从头开始，
		//否则新的一局会从上局的位置接着数。
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.resetDeityPrays();

		//==== END(挑战 88 拍卖行): 换层时重新定价 + 掷抬价 ====
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.rollAuctionBidUp();

		if (bossLevel()) {
			int guards = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
					.ChallengeEffects.bossGuardCount();
			if (guards > 0) {
				com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects.spawnBossGuards(level, guards);
			}
		}

		Statistics.qualifiedForBossChallengeBadge = false;
		
		return level;
	}

	//==================================================================
	//END(无尽轮回): 26 层起的循环层
	//==================================================================

	/**
	 * END(无尽轮回): 生成"循环层"。
	 *
	 * <h3>文档所有者定稿</h3>
	 * "没有原版的 26 层，26 层直接变成原版一层的，计入一次轮回，
	 *  施加一个全局 buff，每次一个，可以叠加"、"最大 9 次轮回"。
	 *
	 * <h3>做法</h3>
	 * <ol>
	 *   <li>把实际楼层映射回原版的 1..25（见 {@code Reincarnation.mappedDepth}）</li>
	 *   <li>用映射后的楼层**生成关卡** —— 第 26 层 = 原版第 1 层的地图与怪物</li>
	 *   <li>但 {@code Dungeon.depth} **保持原值** —— 玩家看到的是"第 26 层"</li>
	 *   <li>每进入一个新循环（mappedDepth == 1 且 depth > 25），轮回 +1</li>
	 * </ol>
	 *
	 * <h3>为什么用映射值生成、而不是改 Dungeon.depth</h3>
	 * 改 {@code depth} 会让 HUD、存档、以及各处"第 N 层"的判定全部错乱。
	 * 只把映射值**传给生成器**，显示层号就不受影响。
	 */
	private static Level createReincarnatedLevel(int depth){
		int mapped = com.shatteredpixel.shatteredpixeldungeon.endcontent
				.Reincarnation.mappedDepth(depth);

		//每进入一个新循环的第一个层 → 轮回 +1
		int cycle = com.shatteredpixel.shatteredpixeldungeon.endcontent
				.Reincarnation.cycleOf(depth);
		if (cycle > com.shatteredpixel.shatteredpixeldungeon.endcontent
				.Reincarnation.cycles()){
			com.shatteredpixel.shatteredpixeldungeon.endcontent
					.Reincarnation.setCycles(cycle);

			com.shatteredpixel.shatteredpixeldungeon.endcontent.ReincarnationCurse cz =
					com.shatteredpixel.shatteredpixeldungeon.endcontent
							.ReincarnationCurse.forCycle(cycle - 1);
			if (cz != null){
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.newLine();
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog
						.w("【轮回 " + cycle + "·" + cz.title + "】" + cz.flavor);
				com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("  " + cz.effect);
			}
		}

		com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.log(
				com.shatteredpixel.shatteredpixeldungeon.endcontent.Dbg.LEVEL,
				"轮回层：显示=" + depth + "  映射=" + mapped
						+ "  轮回=" + com.shatteredpixel.shatteredpixeldungeon.endcontent
								.Reincarnation.cycles()
						+ "  " + com.shatteredpixel.shatteredpixeldungeon.endcontent
								.Reincarnation.summary());

		return createLevelByMappedDepth(mapped);
	}

	/**
	 * END(无尽轮回): 按**原版楼层号**创建关卡。
	 *
	 * <p>这里复刻了 {@code newLevel()} 里 branch==0 那套 1..25 的选择逻辑，
	 * 但用映射后的楼层号 —— 于是第 26/27/28 层就有了原版
	 * 第 1/2/3 层的地图、房间布局与怪物表。
	 */
	private static Level createLevelByMappedDepth(int depth){
		switch (depth){
			case 1:
			case 2:
			case 3:
			case 4:  return new com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel();
			case 5:  return new com.shatteredpixel.shatteredpixeldungeon.levels.SewerBossLevel();
			case 6:
			case 7:
			case 8:
			case 9:  return new com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel();
			case 10: return new com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel();
			case 11:
			case 12:
			case 13:
			case 14: return new com.shatteredpixel.shatteredpixeldungeon.levels.CavesLevel();
			case 15: return new com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel();
			case 16:
			case 17:
			case 18:
			case 19: return new com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel();
			case 20: return new com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel();
			case 21:
			case 22:
			case 23:
			case 24: return new com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel();
			case 25: return new com.shatteredpixel.shatteredpixeldungeon.levels.HallsBossLevel();
			default: return new com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel();
		}
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
		//==== END(适配 6 完整地牢): Boss 层判定不再是写死的 25 层制 ====
		//原版是每 5 层一个 Boss（5/10/15/20/25）。
		//勾选 6 完整地牢后主线变成 50 层，Boss 层相应变成 10/20/30/40/50。
		//
		//不修的话后果很严重：勾了 6 之后 10/20/30/40/50 都不被认作 Boss 层，
		//于是 Boss 不生成、楼梯不上锁、玩家直接走下去 —— 整局失去节奏。
		//
		//用 bossInterval() 统一取间隔，它未勾选 6 时返回 5（等价原版）。
		int bossInterval = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.bossInterval();
		return depth > 0 && depth <= com.shatteredpixel.shatteredpixeldungeon.endcontent
				.challenge.ChallengeEffects.maxMainDepth()
				&& depth % bossInterval == 0;
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

		//==== END(修复·49 切尔诺贝利毒气消失): 在这里挂毒气维持器 ====
		//文档所有者反馈"毒气几回合就消失" —— 维持器**挂载点错了**：
		//  ① 原来挂在 Level.create() 里，那时 Dungeon.level 还是 null
		//     （第 998 行才赋值），ensureRunning() 直接 return 了
		//  ② 即使挂上，紧随其后的 Actor.init() 也会清空所有 Actor
		//
		//现在放在 Actor.init() 与 addRespawner() **之后** ——
		//此时 Actor 系统已就绪，且不会再被清空。
		com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChernobylKeeper.ensureRunning();
		
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
		int posLeftThisSet = 2 - (LimitedDrops.STRENGTH_POTIONS.count - com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.resourceSegment(depth) * 2);
		if (posLeftThisSet <= 0) return false;

		int floorThisSet = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.resourceFloorInSegment(depth);

		//pos drops every two floors, (numbers 1-2, and 3-4) with a 50% chance for the earlier one each time.
		int targetPOSLeft = 2 - floorThisSet/2;
		if (floorThisSet % 2 == 1 && Random.Int(2) == 0) targetPOSLeft --;

		if (targetPOSLeft < posLeftThisSet) return true;
		else return false;

	}
	
	/** 便捷引用，避免在 depth/5 相关处反复写全限定名。 */
	private static com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
			.ChallengeEffects CE() {
		return null;   //占位：见下方替换说明
	}

	public static boolean souNeeded() {

		//==== END(挑战 81 搏杀赌徒): 不再主动刷新升级卷轴 ====
		//原表："不再主动刷新升级卷轴" —— 整局的升级来源改为
		//"财富戒指产出"，因此这里直接判否，让常规投放完全停止。
		//
		//注意放在**掷骰之前**返回：下方的 Random.Int() 会消耗关卡 RNG，
		//提前返回可以让"勾了 81"时的随机序列与"没勾"时保持一致，
		//不至于因为这条规则而改变同种子的关卡布局。
		if (com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.gamblerNoUpgradeScrolls()) {
			return false;
		}

		int souLeftThisSet;
		//3 SOU each floor set
		souLeftThisSet = 3 - (LimitedDrops.UPGRADE_SCROLLS.count - com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.resourceSegment(depth) * 3);
		if (souLeftThisSet <= 0) return false;

		int floorThisSet = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.resourceFloorInSegment(depth);
		//chance is floors left / scrolls left
		return Random.Int(5 - floorThisSet) < souLeftThisSet;
	}
	
	public static boolean asNeeded() {
		//1 AS each floor set
		int asLeftThisSet = 1 - (LimitedDrops.ARCANE_STYLI.count - com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.resourceSegment(depth));
		if (asLeftThisSet <= 0) return false;

		int floorThisSet = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.resourceFloorInSegment(depth);
		//chance is floors left / scrolls left
		return Random.Int(5 - floorThisSet) < asLeftThisSet;
	}

	public static boolean enchStoneNeeded(){
		//1 enchantment stone, spawns on chapter 2 or 3
		if (!LimitedDrops.ENCH_STONE.dropped()){
			int region = 1 + com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.resourceSegment(depth);
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
		int region = 1 + com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.resourceSegment(depth);
		if (region > LimitedDrops.LAB_ROOM.count){
			int floorThisRegion = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects.resourceFloorInSegment(depth);
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
	//END(挑战框架): 完整掩码（位号 = 表 ID），旧 int 装不下高位的规则。
	private static final String CHALLENGE_MASK = "challenge_mask";
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
			//END(挑战框架): 完整掩码另存 long[]。
			//ChallengeMask 用 3 个 long（位号 = 表 ID，最大 138 → ceil(139/64)=3），
			//Bundle 原生支持 long[]，一次存完，不做手工分段（分段容易漏第三段）。
			bundle.put( CHALLENGE_MASK, challengeMask.toLongArray() );
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

			//END(无尽轮回): 轮回次数也要存 —— 否则读档后九种诅咒全没了
			com.shatteredpixel.shatteredpixeldungeon.endcontent
					.Reincarnation.storeInBundle( bundle );

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
		//END(挑战框架): 优先用完整掩码；没有（旧存档）则从 int 翻译。
		if (bundle.contains( CHALLENGE_MASK )) {
			Dungeon.challengeMask =
					com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask
							.of( bundle.getLongArray( CHALLENGE_MASK ) );
		} else {
			Dungeon.challengeMask =
					com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRegistry
							.fromLegacyInt( Dungeon.challenges );
		}
		Dungeon.mobsToChampion = bundle.getFloat( MOBS_TO_CHAMPION );

		//END(便利挑战): 激活便利挑战时解锁全部炼金配方页
		if (isChallenged(Challenges.CONVENIENCE)) {
			Document.unlockAllAlchemyPages();
		}
		
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

		//END(无尽轮回): 读回轮回次数
		com.shatteredpixel.shatteredpixeldungeon.endcontent
				.Reincarnation.restoreFromBundle( bundle );

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
		//END(挑战框架): 带上完整掩码，供存档列表正确显示新规则。
		if (bundle.contains( CHALLENGE_MASK )) {
			info.challengeMask =
					com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask
							.of( bundle.getLongArray( CHALLENGE_MASK ) );
		} else {
			info.challengeMask =
					com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRegistry
							.fromLegacyInt( info.challenges );
		}
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
