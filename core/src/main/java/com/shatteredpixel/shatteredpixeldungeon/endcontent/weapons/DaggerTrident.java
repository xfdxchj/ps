package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.HeavyBoomerang;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 刺杀·三叉戟(回返型成品)。
 * <p>沿用真·回旋机制: 继承 HeavyBoomerang 以获得“掷出后自动飞回手中”的完整行为
 * (含飞回飞行 / 再次自动投掷等逻辑, 见其内部 CircleBack 状态)。
 * 数值做成一把更有分量的三叉戟: 力量需求 12, 射程与命中依托回旋, 耐久无限(不会在飞行中断损)。
 */
public class DaggerTrident extends HeavyBoomerang {

	{
		image = ItemSpriteSheet.TRIDENT;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;
		tier = 3;
		//回返品固有的贴点旗标沿用 (sticky 已在 HeavyBoomerang 关掉)
		stackable = false;
		bones = false;
		baseUses = 5;
	}

	@Override public String name(){ return "刺杀·三叉戟"; }

	//更强投掷面板: min 2*tier+lv, max 5*tier + tier*lv(三叉戟比基底匕首重且狠)
	@Override public int min(int lvl){ return 2*tier + lvl; }
	@Override public int max(int lvl){ return 5*tier + tier*lvl; }
	@Override public int STRReq(int lvl){ return 12; }

	//无限耐久: 飞行完仍旧完好, 每掷都会飞回手中, 不会就此损坏
	@Override public float durabilityPerUse(int lvl){ return 0f; }

	@Override public String info(){
		return "铸造精悍的三叉戟: 命中可靠、出手厚重。击出后会像回旋镖一样自动飞回你的手中," +
				"\n而飞回途中穿过敌军时仍能再次削砍——适合需要回收白刃的贴身猎杀者。";
	}
}
