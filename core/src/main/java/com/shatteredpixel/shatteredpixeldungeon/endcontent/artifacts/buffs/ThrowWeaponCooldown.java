package com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 飞行武器冷却(20回合)。Buff条上显示图标并读秒。 */
public class ThrowWeaponCooldown extends FlavourBuff {
	@Override public int icon(){ return BuffIndicator.THROWN_WEP; }
	@Override public String toString(){ return "飞武冷却"; }
}
