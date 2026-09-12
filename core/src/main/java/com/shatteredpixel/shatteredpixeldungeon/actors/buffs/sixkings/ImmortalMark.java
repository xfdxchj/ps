package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

/**
 * 2号「不灭追猎者」的印记。
 *
 * 每层印记增加玩家的受伤，叠满 50 层触发处决（必定击杀）。
 * 玩家可以通过净化（吃净化药水等）移除。
 */
public class ImmortalMark extends Buff {

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    private int stacks = 0;

    private static final String STACKS = "stacks";
    /** 触发处决的层数 */
    public static final int MAX_STACKS = 50;

    @Override
    public int icon() {
        return BuffIndicator.MARK;
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString(stacks);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", stacks, MAX_STACKS);
    }

    /** 叠加印记。 */
    public void addMark(int amount) {
        stacks += amount;

        if (stacks >= MAX_STACKS) {
            stacks = 0;
            if (target != null && target.isAlive()) {
                //END(设计): 处决 —— 造成足以致命的伤害
                target.damage( target.HP + target.HT, this );
            }
        }
    }

    public int stacks() {
        return stacks;
    }

    /** 每层印记让玩家多受 1% 伤害（可自行调整）。 */
    public float damageMultiplier() {
        return 1f + stacks * 0.01f;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(STACKS, stacks);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        stacks = bundle.getInt(STACKS);
    }
}
