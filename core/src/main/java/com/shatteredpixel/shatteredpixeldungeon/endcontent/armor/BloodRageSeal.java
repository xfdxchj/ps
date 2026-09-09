package com.shatteredpixel.shatteredpixeldungeon.endcontent.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EndRageAttack;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.RageCooldown;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;

/**
 * 破印·进阶 「狂暴」。
 * 贴到护甲并穿戴后，护甲右键出现“狂暴”技能键：进入狂暴、攻击伤害 +100%(EndRageAttack，经 Char.attack ×2，不改攻速)。
 * 以当前生命 30% 作为代价，随后进入较长冷却(RageCooldown)。
 */
public class BloodRageSeal extends BrokenSeal {

	public static final String KEY = "BLOOD_RAGE";
	public static final float COOLDOWN = 200f;

	@Override public String name(){ return "破印·狂暴"; }

	@Override public String armorSkillKey(){ return KEY; }

	@Override public boolean armorSkillUsable(Hero hero){
		return hero != null && hero.buff(RageCooldown.class) == null;
	}

	@Override public void armorSkillEffect(Hero hero){
		if (hero == null) return;
		hero.HP = Math.max(1, hero.HP - Math.round(hero.HP * 0.30f));
		Buff.affect(hero, EndRageAttack.class, EndRageAttack.DURATION);
		Buff.affect(hero, RageCooldown.class, COOLDOWN);
	}

	@Override public String info(){
		return "战意决堤：以当前生命 30% 为代价,进入狂暴——攻击伤害提高 100% (±不改变攻速)。" +
				"\n贴附到护甲并穿戴，即可在护甲上使用此技能。";
	}
}
