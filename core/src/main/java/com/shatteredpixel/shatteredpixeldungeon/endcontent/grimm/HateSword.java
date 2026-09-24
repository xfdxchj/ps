/*
 * 破碎的地牢 (End fork) — 挑战 125/136「格林之器」的专属武器
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(挑战 136 格林之器3): 怨恨之剑。
 *
 * <h3>原表效果（136 版，强化版）</h3>
 * "怨恨之剑（生命越低伤害越高；血量越低，倍率越接近 2 倍；
 *   但真正进入 1 血时，直接跃升到 3 倍。最大生命为 1 时造成 3 倍伤害）"
 *
 * <h3>公式</h3>
 * 设 {@code hpPct = 当前生命 / 最大生命}（0..1）：
 * <pre>
 *   hpPct = 0（即 1 血）  ->  ×3.0    （跃升，不连续）
 *   0 &lt; hpPct &lt; 1        ->  ×(1 + (1 - hpPct))   ... 线性趋近 2 倍
 *   hpPct = 1（满血）     ->  ×1.0
 * </pre>
 * 即**线性插值**：满血 ×1，接近 0 血时趋向 ×2，
 * 但真正到 1 血时**跳过 2 倍直接给 3 倍** —— 这是原表明确要求的"跃升"。
 *
 * <p>另外："最大生命为 1 时造成 3 倍伤害" —— 那属于 hpPct 恒为 1 的边界，
 * 但它要求 3 倍，所以单独判 {@code HT <= 1}。
 */
public class HateSword extends MeleeWeapon {

	/** 1 血时的跃升倍率。 */
	public static final float BRINK_MULT = 3.0f;
	/** 濒死（但未到 1 血）时的渐近上限。 */
	public static final float NEAR_DEATH_MAX = 2.0f;

	{
		image = ItemSpriteSheet.GRIMM_HATESWORD;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 0.9f;
		tier = 5;
		DLY = 1f;
	}

	@Override public String name(){ return "怨恨之剑"; }

	/**
	 * 基础面板。
	 *
	 * <p>原表没给基础数值，所以按 **T5 单手剑的中等偏上**取值 ——
	 * 它的强度应该来自"低血倍率"，而不是基础面板。
	 */
	@Override public int min(int lvl){ return 4 + lvl; }
	@Override public int max(int lvl){ return 20 + 5*lvl; }

		/**
	 * END(修复): 力量需求随等级递减。
	 *
	 * <p>原先硬编码返回 18 —— 于是"每升 3 级减力量需求"完全没有，
	 * 玩家实测就是这个现象。
	 *
	 * <p>改用原版 Weapon 的通用公式（三角数递减 +1/+3/+6/+10…），
	 * 与其它 T5 武器一致。
	 */
	@Override public int STRReq(int lvl){ return STRReq(5, lvl); }

	@Override
	public String info(){
		//END(修复): 先取父类文本 —— 那里面才有伤害面板/力量需求/等级。
		//原先直接 return 自定义文本，于是物品描述里完全看不到数值。
		return super.info() + "\n\n" +
				"剑身上刻满了不属于你的怨念。它渴望着持剑者的鲜血。\n\n" +
				"- 生命越**低**，造成的伤害越高\n" +
				"- 满血时无加成；血量趋近于零时倍率趋近 **2 倍**\n" +
				"- 一旦真正只剩 **1 点生命**，倍率**跃升到 3 倍**\n" +
				"- 最大生命为 1 时同样享受 3 倍\n\n" +
				"它逼你在死亡边缘作战 —— 也会在边缘把你杀死。" + com.shatteredpixel.shatteredpixeldungeon.endcontent.EndItemStats.block(this);
	}

	/**
	 * END(136): 按当前生命比例计算伤害倍率。
	 *
	 * <p>静态方法是为了可测试性：不依赖实例，能在无图形环境里验证公式。
	 *
	 * @param hp 当前生命
	 * @param ht 最大生命
	 * @return 伤害倍率（1.0 ~ 3.0）
	 */
	public static float hateMultiplier(int hp, int ht) {
		if (ht <= 1) return BRINK_MULT;          //最大生命为 1 → 直接 3 倍
		if (hp <= 1) return BRINK_MULT;          //真正进入 1 血 → 跃升

		float pct = hp / (float) ht;             //(0, 1]
		//线性插值：满血 1.0，趋近 0 血时趋近 NEAR_DEATH_MAX
		return 1f + (NEAR_DEATH_MAX - 1f) * (1f - pct);
	}

	/**
	 * END(136): 攻击时套用倍率。
	 *
	 * <p>放在 {@code damageRoll} 而不是附魔的 {@code proc}：
	 * 这是**武器本身的固有特性**，不是附魔效果，
	 * 用 proc 会被"驱邪卷轴洗掉"之类的逻辑误伤。
	 */
	@Override
	public int damageRoll(Char owner) {
		int dmg = super.damageRoll(owner);
		if (owner == null) return dmg;

		float mult = hateMultiplier(owner.HP, owner.HT);
		return Math.max(1, Math.round(dmg * mult));
	}

	@Override
	public int value(){ return 0; }

	@Override
	public boolean isUpgradable(){ return true; }
}
