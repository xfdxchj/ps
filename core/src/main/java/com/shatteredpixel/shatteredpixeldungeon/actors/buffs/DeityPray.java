/*
 * 破碎的地牢 (End fork) — 挑战 151「圣明神明」的祷告状态
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;

/**
 * END(挑战 151 圣明神明): 「停下来祷告」。
 *
 * <h3>为什么不用原版的 Paralysis</h3>
 * 原版 {@code Paralysis} 挂上时会打印 {@code "你被麻痹了！"} ——
 * 那是**中毒/陷阱**语义的提示。
 *
 * <p>151 的效果是"圣光要求你停下来祈祷"，用"麻痹"字样完全不对；
 * 而且文档所有者明确要求显示 **"你停下来祷告"**。
 *
 * <h3>做法</h3>
 * 完全照抄 {@code Paralysis} 的机制（把 {@code paralysed} 计数器加满，
 * 让 {@code Char.act()} 跳过本回合），但：
 * <ul>
 *   <li>用自己的 {@code name/desc/heromsg} 消息</li>
 *   <li>不显示"麻痹"图标（用 {@code NONE}）</li>
 * </ul>
 *
 * <p>这样玩家看到的提示就是"你停下来祷告。"，而行为与麻痹一致
 * （本回合无法行动）。
 */
public class DeityPray extends FlavourBuff {

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	/** 祷告时长（回合）。1 回合 = 只跳过当前这一回合。 */
	public static final float DURATION = 1f;

	@Override public int icon(){ return BuffIndicator.NONE; }

	@Override public String name(){
		return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get(
				this, "name");
	}

	@Override
	public boolean attachTo(Char target) {
		if (!super.attachTo(target)) return false;

		//与 Paralysis 同样的机制：把 paralysed 计数加满，
		//让 Char.act() 跳过本回合的行动。
		target.paralysed++;
		return true;
	}

	@Override
	public void detach() {
		//归还那一次计数（Paralysis 也是这么做的）
		if (target != null && target.paralysed > 0) {
			target.paralysed--;
		}
		super.detach();
	}
}
