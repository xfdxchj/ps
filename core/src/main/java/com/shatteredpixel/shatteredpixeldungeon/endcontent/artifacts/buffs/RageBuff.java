package com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;

/**
 * 狂暴临时增益(自身 10 回合,快速/强攻的乘数需要攻击端挂在 Hero 上——
 * 本库标记存在;乘数接入请见各端另行(WIP)。触发时由 execute 附加。
 */
public class RageBuff extends FlavourBuff {
	public static final float DURATION = 10f;
	@Override public String toString(){ return "狂暴(增伤/提速)"; }
}
