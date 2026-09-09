package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 传送冷却。Buff条上显示图标并读秒。 */
public class TeleportCooldown extends FlavourBuff {
	@Override public int icon(){ return BuffIndicator.INVISIBLE; }
	@Override public String toString(){ return "传送冷却"; }
}
