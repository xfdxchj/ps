package com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 狂暴冷却(200回合)。Buff条上显示图标并读秒。 */
public class RageCooldown extends FlavourBuff {
	@Override public int icon(){ return BuffIndicator.RAGE; }
	@Override public String toString(){ return "狂暴冷却"; }
}
