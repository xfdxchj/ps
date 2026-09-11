package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ElementalBuff.Immunities;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ElementalBuff.BaseBuff.ScaryBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ElementalBuff.ElementalFABuff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

/**
 * END(移植自魔绫·挑战区): 恐惧免疫（短时间内不再被叠加恐惧）。
 */
public class ScaryImmunitiesBuff extends ElementalFABuff {

    {
        immunities.add( ScaryBuff.class );
    }

    {
        type = buffType.POSITIVE;
        announced = true;
    }

    public void damgeScary() {
        DURATION = DURATION/2;
    }

    public static float DURATION	= Random.NormalIntRange(90,145);

    @Override
    public int icon() {
        return BuffIndicator.IMELSAZE;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(Window.Pink_COLOR);
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - visualcooldown()) / DURATION);
    }

}
