package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

/**
 * 刺杀·潜袭匕首(“基础背刺”成品 · 普通投掷)。
 * <p>靠“更高基准的偷袭面”表现背刺: 相比基底匕首拥有更高的基础伤害, 攻其不备、背刺取敌。
 * 保持简单可靠的纯数值实现(不依赖目标是否“察觉”之类的脆弱 Char 状态断言)。
 * 非堆叠、非骨、力量 10、无限耐久。
 * 未命中判定等均由 MissileWeapon 负责, 本类仅替换了背刺感的数值面板。
 */
public class AssassinateDagger extends MissileWeapon {

	{
		image = ItemSpriteSheet.THROWING_KNIFE;
		hitSound = com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.05f;
		stackable = false;
		bones = false;
		tier = 2;
		baseUses = 5;
	}

	@Override public String name(){ return "基础背刺"; }

	//高出普通匕首一档的攻击面, 数值上让它“藏得住一击必杀的刃”——3+lv ~ 10+2lv
	@Override public int min(int lvl){ return 3 + lvl; }
	@Override public int max(int lvl){ return 10 + 2*lvl; }
	@Override public int STRReq(int lvl){ return 10; }

	@Override public float durabilityPerUse(int lvl){ return 0f; }

	@Override public String info(){
		return "专为背刺与偷袭打磨的潜行匕首: 更高的基准攻击面兼作“先发制人”的一步。" +
				"\n命中可靠、无限耐久。配合隐匿/背对打出的伤害尤其凶险。";
	}
}
