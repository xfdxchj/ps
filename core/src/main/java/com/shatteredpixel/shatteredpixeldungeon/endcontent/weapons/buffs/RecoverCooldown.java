package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;

/** 回收后冷却。 */
public class RecoverCooldown extends FlavourBuff {
	@Override public String name(){ return "回收冷却"; }
	@Override public String toString(){ return name(); }
	@Override public String desc(){ return "回收技能正在冷却，还需 " + dispTurns() + " 回合。"; }
}
