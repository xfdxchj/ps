package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ElementalBuff.DamageBuff;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ElementalBuff.ElementalBaseBuff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * END(移植自魔绫·挑战区): 凋零——每回合造成少量伤害的持续减益。
 * 原为魔绫 `DwarfGeneral.Wither` 内嵌类，此处抽为独立类以便复用（`TimeReset.MobsWither` 亦继承它）。
 */
public class Wither extends ElementalBaseBuff {

    {
        type = buffType.NEUTRAL;
        announced = true;
    }

    public static final float DURATION = 30f;
    private float damageInc = 0;

    @Override
    public boolean act() {
        if (target.isAlive()) {

            damageInc = Random.Int(2, 5);
            target.damage((int) damageInc, this);
            damageInc -= (int) damageInc;

            spend(1f);
            if (--level <= 0) {
                detach();
            }
            if (target == hero && !target.isAlive()) {
                GLog.n(Messages.get(this, "on_kill"));
            }

        } else {
            detach();
        }

        return true;
    }

    @Override
    public int icon() {
        return BuffIndicator.POISON;
    }

    public static final String DAMAGE = "damage_inc";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(DAMAGE, damageInc);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        damageInc = bundle.getFloat(DAMAGE);
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(1f, 0f, 0f);
    }
}
