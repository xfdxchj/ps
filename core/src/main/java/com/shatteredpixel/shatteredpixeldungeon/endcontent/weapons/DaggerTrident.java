package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * 刺杀·三叉戟（高数值成品）。
 * <p>投掷型三叉戟：出手厚重、数值明显强于基底匕首，但**不是回旋镖**——掷出后不会自己飞回，
 * 也不附带传送/处决。属于“数值型”进化成品。无限耐久。
 */
public class DaggerTrident extends MissileWeapon {

	{
		image = ItemSpriteSheet.TRIDENT;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;
		stackable = false;
		bones = false;
		tier = 3;
		baseUses = 5;
	}

	@Override public String name(){ return "刺杀·三叉戟"; }

	//更强投掷面板: min 2*tier+lv, max 5*tier + tier*lv(三叉戟比基底匕首重且狠)
	@Override public int min(int lvl){ return 2 + lvl; }
	@Override public int max(int lvl){ return 14 + 4*lvl; }
	@Override public int STRReq(int lvl){ return 12; }

	//无限耐久
	@Override public float durabilityPerUse(int lvl){ return 0f; }

	@Override public String info(){
		return "铸造精悍的三叉戟:命中可靠、出手厚重(高额投掷伤害)。" +
				"\n一击掷出即落地,不会飞回,也不附带传送/处决——纯粹以力道取胜。";
	}
}
