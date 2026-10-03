/*
 * 破碎的地牢 (End fork) — 咒术回战系列音效触发。
 *
 * 甲方之后会把 OG G 素材放到 assets/audio/jujutsu/ 下（文件名见 KEYS）。
 * 素材到位前用现有原版音效兜底，不会因缺文件报错。
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.audio.Sample;

public final class JujutsuSfx {

	private JujutsuSfx() {}

	/** 自定义素材目录。 */
	private static final String DIR = "audio/jujutsu/";

	//==== 技能音效（key -> 自定义文件 / 原版兜底）====
	public static final String CANG    = "cang";
	public static final String HE      = "he";
	public static final String ZI      = "zi";
	public static final String DOMAIN  = "domain";
	public static final String SUKUNA_INTRO = "sukuna_intro";
	public static final String SUKUNA_PHASE = "sukuna_phase";
	public static final String KAI     = "kai";     // 解
	public static final String BACHI   = "batsu";   // 捌
	public static final String FIRE    = "open";    // 开
	public static final String SHRINE  = "shrine";  // 伏魔御厨子
	public static final String SPACE   = "space";   // 空间斩
	public static final String FUGA    = "fuga";    // 灶开

	/** 已知缺素材时不再重试。 */
	private static final java.util.HashSet<String> failed = new java.util.HashSet<>();

	public static void play(String key){
		String fallback = fallbackFor(key);
		//只有领域音效有素材；其余 10 条已放弃，直接用原版兜底，避免刷 File not found
		if (DOMAIN.equals(key) || SHRINE.equals(key)){
			String custom = DIR + key + ".ogg";
			if (!failed.contains(custom)){
				try {
					Sample.INSTANCE.load(custom);
					Sample.INSTANCE.play(custom);
					return;
				} catch (Throwable t){
					failed.add(custom);
				}
			}
		}
		if (fallback != null) Sample.INSTANCE.play(fallback);
	}

	private static String fallbackFor(String key){
		switch (key){
			case CANG:   return Assets.Sounds.ZAP;
			case HE:     return Assets.Sounds.BLAST;
			case ZI:     return Assets.Sounds.RAY;
			case DOMAIN: return Assets.Sounds.EVOKE;
			case SUKUNA_INTRO: return Assets.Sounds.BOSS;
			case SUKUNA_PHASE: return Assets.Sounds.ALERT;
			case KAI:    return Assets.Sounds.HIT_SLASH;
			case BACHI:  return Assets.Sounds.HIT_STRONG;
			case FIRE:   return Assets.Sounds.BURNING;
			case SHRINE: return Assets.Sounds.EVOKE;
			case SPACE:  return Assets.Sounds.SHATTER;
			case FUGA:   return Assets.Sounds.BURNING;
			default:     return Assets.Sounds.CLICK;
		}
	}
}
