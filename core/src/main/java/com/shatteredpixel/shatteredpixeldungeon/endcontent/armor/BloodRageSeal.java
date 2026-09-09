package com.shatteredpixel.shatteredpixeldungeon.endcontent.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EndRageAttack;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs.RageCooldown;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

/**
 * 破印·进阶 「狂暴」。
 * 贴到护甲并穿戴后，护甲右键出现“狂暴”技能键（键不因冷却消失；冷却中点击仅提示剩余回合,不扣血）。
 * 可用时：以当前生命 30% 为代价,进入 {@link EndRageAttack} 10 回合狂暴（攻击伤害 +100%,经 Char.attack ×2,不改攻速）；
 * 随后进入较长冷却({@link RageCooldown})。
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
		FlavourBuff cd = hero.buff(RageCooldown.class);
		if (cd != null){
			GLog.w("狂暴仍在冷却，" + Math.round(cd.visualcooldown()) + " 回合后才可再使用。");
			return;
		}
		hero.HP = Math.max(1, hero.HP - Math.round(hero.HP * 0.30f));
		Buff.affect(hero, EndRageAttack.class, EndRageAttack.DURATION);
		Buff.affect(hero, RageCooldown.class, COOLDOWN);
	}

	@Override public String info(){
		return "战意决堤：以当前生命 30% 为代价,进入狂暴——攻击伤害提高 100% (±不改变攻速),持续 " +
				Math.round(EndRageAttack.DURATION) + " 回合。" +
				"\n技能冷却 200 回合;冷却中技能键仍可见,仅提示剩余回合。" +
				"\n贴附到护甲并穿戴，即可在护甲上使用此技能。";
	}
}
