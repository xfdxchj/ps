package com.shatteredpixel.shatteredpixeldungeon.endcontent.artifacts.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/** 狂暴冷却(200回合)。Buff条上显示图标并读秒。 */
public class RageCooldown extends FlavourBuff {
	@Override public int icon(){ return BuffIndicator.FURY; } //18, <32 确保大片/小片两套图集都有帧
	@Override public String name(){ return "狂暴冷却"; }
	@Override public String toString(){ return name(); }
	/**
	 * END(修复·冷却只撑 1 回合): 每 tick 续命，到点了才消失。
	 *
	 * <h3>根因</h3>
	 * 本类继承 {@code FlavourBuff} —— 那是 {@code Buff.affect(Char, Class, float)}
	 * 的硬性要求（该重载只接受它的子类）。但：
	 * <pre>
	 *   // FlavourBuff.act()
	 *   public boolean act() { detach(); return true; }   // 第一个 tick 就消失
	 * </pre>
	 * 它**完全不递减 cooldown** —— 于是 {@code affect(hero, XxxCooldown.class, 20f)}
	 * 设的 20 回合实际只撑了 **1 个 tick**，冷却形同虚设。
	 *
	 * <h3>正确写法（照原版 AdrenalineSurge 那类计时 buff）</h3>
	 * <pre>
	 *   public boolean act() {
	 *       if (cooldown() > 1f) spend(TICK);   // 还没到点 → 再等一回合
	 *       else                 detach();      // 到点 → 消失
	 *       return true;
	 *   }
	 * </pre>
	 * 关键是**每次都 spend(TICK) 续命** —— 这样它才会一直留在调度队列里，
	 * 冷却条也会真正一格一格地走。1 tick = 1 回合，
	 * 与"战士纹章"那类 CD 的语义一致。
	 */
	@Override
	public boolean act(){
		if (cooldown() > 1f){
			spend( TICK );
		} else {
			detach();
		}
		return true;
	}
}
