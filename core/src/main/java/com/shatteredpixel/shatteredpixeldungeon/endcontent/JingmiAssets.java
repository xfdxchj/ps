/*
 * 破碎的地牢 (End fork) — 挑战 228「我的世界」的资源替换（静谧花园 4.0）
 *
 * 本文件为框架新增。资源由 _extract/静谧花园4.0_资源 导入，
 * 放在 core/src/main/assets/jingmi/ 下（保持与原路径同构）。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent;

public final class JingmiAssets {

	private JingmiAssets() {}

	/** 替换的图片资源（相对 assets 的路径，与 jingmi/ 下同构）。 */
				public static final String[] IMAGES = {
			"effects/effects.png",
			"effects/fireball-short.png",
			"effects/fireball-tall.png",
			"effects/specks.png",
			"effects/spell_icons.png",
			"effects/text_icons.png",
			"environment/custom_tiles/caves_boss.png",
			"environment/custom_tiles/city_boss.png",
			"environment/custom_tiles/prison_exit.png",
			"environment/custom_tiles/weak_floor.png",
			"environment/tiles_caves.png",
			"environment/tiles_caves_crystal.png",
			"environment/tiles_caves_gnoll.png",
			"environment/tiles_city.png",
			"environment/tiles_halls.png",
			"environment/tiles_prison.png",
			"environment/tiles_sewers.png",
			"sprites/avatars.png",
			"sprites/brute.png",
			"sprites/cleric.png",
			"sprites/crab.png",
			"sprites/crystal_guardian.png",
			"sprites/crystal_wisp.png",
			"sprites/dm200.png",
			"sprites/duelist.png",
			"sprites/fungal_core.png",
			"sprites/fungal_sentry.png",
			"sprites/fungal_spinner.png",
			"sprites/gnoll.png",
			"sprites/gnoll_geomancer.png",
			"sprites/gnoll_guard.png",
			"sprites/gnoll_sapper.png",
			"sprites/item_icons.png",
			"sprites/piranha.png",
			"sprites/rat.png",
			"sprites/ratking.png",
			"sprites/scorpio.png",
			"sprites/shaman.png",
			"sprites/wraith.png",
		};

	/** 替换的音乐（BGM）。 */
		public static final String[] MUSIC = {
			"music/caves_1.ogg",
			"music/caves_2.ogg",
			"music/caves_3.ogg",
			"music/caves_boss.ogg",
			"music/caves_boss_finale.ogg",
			"music/caves_tense.ogg",
			"music/city_1.ogg",
			"music/city_2.ogg",
			"music/city_3.ogg",
			"music/city_boss.ogg",
			"music/city_boss_finale.ogg",
			"music/city_tense.ogg",
			"music/halls_1.ogg",
			"music/halls_2.ogg",
			"music/halls_3.ogg",
			"music/halls_boss.ogg",
			"music/halls_boss_finale.ogg",
			"music/halls_tense.ogg",
			"music/prison_1.ogg",
			"music/prison_2.ogg",
			"music/prison_3.ogg",
			"music/prison_boss.ogg",
			"music/prison_tense.ogg",
			"music/sewers_1.ogg",
			"music/sewers_2.ogg",
			"music/sewers_3.ogg",
			"music/sewers_boss.ogg",
			"music/sewers_tense.ogg",
			"music/theme_1.ogg",
			"music/theme_2.ogg",
			"music/theme_finale.ogg",
		};

	/** 替换的音效。 */
		public static final String[] SOUNDS = {
			"sounds/alert.mp3",
			"sounds/atk_crossbow.mp3",
			"sounds/atk_spiritbow.mp3",
			"sounds/badge.mp3",
			"sounds/beacon.mp3",
			"sounds/bee.mp3",
			"sounds/blast.mp3",
			"sounds/bones.mp3",
			"sounds/boss.mp3",
			"sounds/burning.mp3",
			"sounds/chains.mp3",
			"sounds/challenge.mp3",
			"sounds/chargeup.mp3",
			"sounds/charms.mp3",
			"sounds/click.mp3",
			"sounds/cursed.mp3",
			"sounds/death.mp3",
			"sounds/debuff.mp3",
			"sounds/degrade.mp3",
			"sounds/descend.mp3",
			"sounds/dewdrop.mp3",
			"sounds/door_open.mp3",
			"sounds/drink.mp3",
			"sounds/eat.mp3",
			"sounds/evoke.mp3",
			"sounds/falling.mp3",
			"sounds/gas.mp3",
			"sounds/ghost.mp3",
			"sounds/gold.mp3",
			"sounds/grass.mp3",
			"sounds/health_critical.mp3",
			"sounds/health_warn.mp3",
			"sounds/hit.mp3",
			"sounds/hit_arrow.mp3",
			"sounds/hit_crush.mp3",
			"sounds/hit_magic.mp3",
			"sounds/hit_parry.mp3",
			"sounds/hit_slash.mp3",
			"sounds/hit_stab.mp3",
			"sounds/hit_strong.mp3",
			"sounds/item.mp3",
			"sounds/levelup.mp3",
			"sounds/lightning.mp3",
			"sounds/lullaby.mp3",
			"sounds/mastery.mp3",
			"sounds/meld.mp3",
			"sounds/mimic.mp3",
			"sounds/mine.mp3",
			"sounds/miss.mp3",
			"sounds/plant.mp3",
			"sounds/puff.mp3",
			"sounds/ray.mp3",
			"sounds/read.mp3",
			"sounds/rocks.mp3",
			"sounds/scan.mp3",
			"sounds/secret.mp3",
			"sounds/shatter.mp3",
			"sounds/sheep.mp3",
			"sounds/step.mp3",
			"sounds/sturdy.mp3",
			"sounds/teleport.mp3",
			"sounds/tomb.mp3",
			"sounds/trample.mp3",
			"sounds/trap.mp3",
			"sounds/unlock.mp3",
			"sounds/water.mp3",
			"sounds/zap.mp3",
		};

	/** END(228): 这个音效是否要换成 jingmi 版本；没有则返回 null（保持原音）。 */
	public static String soundFor(String original){
		if (original == null) return null;
		if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
			.jingmiEnabled()) return null;
		for (String p : SOUNDS){
			if (original.equals(p)) return "jingmi/" + p;
		}
		return null;
	}

	/** END(228): 这首 BGM 是否要换成 jingmi 版本；没有替换则返回 null。 */
	public static String trackFor(String original){
		if (original == null) return null;
		if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
			.jingmiEnabled()) return null;
		for (String p : MUSIC){
			if (original.equals(p)) return "jingmi/" + p;
		}
		return null;
	}

	/**
	 * END(228 我的世界): 把全部图片资源"别名"到 jingmi/ 下的同名文件。
	 *
	 * <p>做法：往 {@code TextureCache} 里按**原路径**注册替换贴图 ——
	 * 之后所有 {@code TextureCache.get(原路径)} 都会拿到替换图，
	 * 不需要改动任何一处业务代码。
	 */
	public static void apply(){
		//==== END(修复·没勾挑战也换贴图) ====
		//文档所有者反馈："为什么没有开启我的世界挑战也会错乱？"
		//根因：这里原来**没有判开关** —— Dungeon.init() 一调就无条件把全部图集
		//别名到 jingmi/，于是没勾 228 也整个换皮。
		if (!com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeEffects
			.jingmiEnabled()) {
			applied = false;
			return;
		}
		if (applied) return;
		applied = true;
		for (String p : IMAGES){
			com.watabou.gltextures.TextureCache.alias(p, "jingmi/" + p);
		}
	}

	/** END(228): 新开一局时允许重新注册替换贴图。 */
	public static void reset(){
		applied = false;
		//撤掉上一局注册的别名 —— 否则关了 228 还在用替换图
		for (String p : IMAGES){
			com.watabou.gltextures.TextureCache.unalias(p);
		}
	}

	private static boolean applied = false;
}

