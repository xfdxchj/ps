/*
 * 破碎的地牢 (End fork) — 刺杀匕首传送的冷却
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.weapons.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * END(刺杀匕首): 传送冷却。
 *
 * <h3>修复记录：buff 名字显示成 "nofound"</h3>
 * 原来这个类只覆写了 {@code toString()}：
 * <pre>
 *   @Override public String toString(){ return "传送冷却"; }   // 错
 * </pre>
 * 而原版取 buff 名字走的是 {@code Messages.get(buff, "name")} ——
 * 那对应 {@code name()} 方法，不是 {@code toString()}。
 * 于是 {@code Messages} 找不到 key（本类没有对应的 properties 条目），
 * 返回占位串（"nofound" / "!!!key!!!"）。
 *
 * <p>修法：覆写 {@code name()}。这样不用加 properties 也能正确显示，
 * 而且比加 properties 更稳（不依赖打包时是否包含那个 key）。
 */
public class TeleportCooldown extends FlavourBuff {

	/**
	 * END: buff 名 —— **必须覆写 name()**，不是 toString()。
	 *
	 * <p>这是本类最重要的修复点：原版 buff 的标题一律取自这里。
	 */
	@Override
	public String name(){
		return "传送冷却";
	}

	/** END: 顺带把 toString 也保持一致（日志/调试里会用到）。 */
	@Override
	public String toString(){
		return name();
	}

	@Override public int icon(){ return BuffIndicator.INVISIBLE; }

	@Override
	public String desc(){
		return "空间还没有重新稳定下来。\n\n再等 "
				+ (int)Math.ceil(visualcooldown()) + " 回合才能再次瞬移。";
	}
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
