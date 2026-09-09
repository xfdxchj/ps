package com.shatteredpixel.shatteredpixeldungeon.endcontent.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.BloodShieldCooldown;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 破印·进阶 「血盾」。
 * 贴到护甲并穿戴后，护甲右键出现“血盾”技能键（冷却中键仍保留,点击仅提示剩余回合,不扣血）：
 * 可用时消耗 20% 当前 HP，获得 30% 最大 HP 的临时护盾(ShieldBuff/Barrier)；冷却 Buff 200 回合。
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
		FlavourBuff cd = hero.buff(BloodShieldCooldown.class);
		if (cd != null){
			GLog.w("血盾仍在冷却，" + Math.round(cd.visualcooldown()) + " 回合后才可再使用。");
			return;
		}
		hero.HP = Math.max(1, hero.HP - Math.round(hero.HP * 0.20f));
		Buff.affect(hero, Barrier.class).setShield(Math.round(hero.HT * 0.30f));
		Buff.affect(hero, BloodShieldCooldown.class, COOLDOWN);
		BuffIndicator.refreshHero();
	}

	@Override public String info(){
		return "血纹流转：消耗 20% 当前生命，化为 30% 最大生命的临时护盾。" +
				"\n冷却 200 回合;冷却中技能键仍可见,仅提示剩余回合。" +
				"\n贴附到护甲并穿戴，即可在护甲上使用此技能。";
	}
}
