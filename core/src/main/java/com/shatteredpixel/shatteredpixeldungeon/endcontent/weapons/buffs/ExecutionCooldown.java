package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 处决后冷却。Buff条上显示图标并读秒。 */
public class ExecutionCooldown extends FlavourBuff {
	@Override public int icon(){ return BuffIndicator.CRIPPLE; } //23, <32 确保两套图集都有帧
	@Override public String toString(){ return "处决冷却"; }
}
