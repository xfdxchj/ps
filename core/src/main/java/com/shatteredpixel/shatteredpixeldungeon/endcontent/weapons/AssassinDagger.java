package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 刺杀匕首 · 基底（MissileWeapon，可投掷、无限耐久）。
 * <p>数值：min 2~6、力量 10、durabilityPerUse()=0（无限）。
 * 作为“炼金进化”的基础件：在炼金锅中与 1 枚邪能碎片(MetalShard)合成，
 * 可将其进化为三支独立成品之一（见 dagger 炼金配方与各 DaggerXxx 成品类）。
 * 本类不再携带形态枚举 / 右键切换——形态即独立成品物。
 */
public class AssassinDagger extends MissileWeapon {

	{
		image = ItemSpriteSheet.THROWING_KNIFE;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.1f;
		stackable = false;
		bones = false;
		tier = 1;
		baseUses = 5;
	}

	@Override public String name(){ return "刺杀匕首"; }

	//数值 2~6 (min2 +lvl↑, max6 +2lvl↑)，逼近清单“2~6、力量10、无限耐久”
	@Override public int min(int lvl){ return 2 + lvl; }
	@Override public int max(int lvl){ return 6 + 2*lvl; }
	@Override public int STRReq(int lvl){ return 10; }

	//无限耐久：抛投后永不耗损。返回 0 == MissileWeapon 里“lasting forever”语义
	@Override public float durabilityPerUse(int lvl){ return 0f; }

	@Override public String info(){
		return "专为背刺打造的基础匕首。投掷命中可靠,\n且能在炼金锅中消耗 1 枚邪能碎片," +
				"进化为独立成品:三叉戟 / 传送 / 处决型。";
	}
}
