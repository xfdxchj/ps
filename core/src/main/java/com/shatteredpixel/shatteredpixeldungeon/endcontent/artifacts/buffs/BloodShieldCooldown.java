package com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 血盾冷却(一次);技能键保留,冷却中仅提示。Buff条上显示图标并读秒。 */
public class BloodShieldCooldown extends FlavourBuff {
	@Override public int icon(){ return BuffIndicator.ARMOR; }
	@Override public String toString(){ return "血盾冷却"; }
}
