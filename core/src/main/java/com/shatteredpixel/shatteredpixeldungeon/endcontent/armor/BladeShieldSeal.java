package com.shatteredpixel.shatteredpixeldungeon.endcontent.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.BloodShieldCooldown;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;

/**
 * 破印·进阶 「血盾」。
 * 贴到护甲并穿戴后，护甲右键出现“血盾”技能键：
 * 消耗 20% 当前 HP，获得 30% 最大 HP 的临时护盾(ShieldBuff/Barrier)；冷却 Buff 200 回合。
 */
public class BladeShieldSeal extends BrokenSeal {

	public static final String KEY = "BLADE_SHIELD";
	public static final float COOLDOWN = 200f;

	@Override public String name(){ return "破印·血盾"; }

	@Override public String armorSkillKey(){ return KEY; }

	@Override public boolean armorSkillUsable(Hero hero){
		return hero != null && hero.buff(BloodShieldCooldown.class) == null;
	}

	@Override public void armorSkillEffect(Hero hero){
		if (hero == null) return;
		hero.HP = Math.max(1, hero.HP - Math.round(hero.HP * 0.20f));
		Buff.affect(hero, Barrier.class).setShield(Math.round(hero.HT * 0.30f));
		Buff.affect(hero, BloodShieldCooldown.class, COOLDOWN);
	}

	@Override public String info(){
		return "血纹流转：消耗 20% 当前生命，化为 30% 最大生命的临时护盾。" +
				"\n贴附到护甲并穿戴，即可在护甲上使用此技能。";
	}
}
