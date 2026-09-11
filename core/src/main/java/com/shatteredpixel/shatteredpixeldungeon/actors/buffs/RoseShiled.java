package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

/**
 * END(移植自魔绫·挑战区): 玫瑰护盾（视觉以 AURA 光环表现）。
 * 移植调整：魔绫使用 CharSprite.State.ROSESHIELDED（本 fork 无该枚举项），改用既有的 AURA 状态 + 粉色光环。
 */
public class RoseShiled extends FlavourBuff {

    public static final float DURATION	= 10f;

    {
        type = buffType.POSITIVE;
        announced = true;
    }


    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - visualcooldown()) / DURATION);
    }

    @Override
    public void fx(boolean on) {
        if (on) {
            target.sprite.add(CharSprite.State.AURA);
            target.sprite.aura(Window.Pink_COLOR, 2);
        } else {
            target.sprite.remove(CharSprite.State.AURA);
            target.sprite.clearAura();
        }
    }



    @Override
    public int icon() {
        return BuffIndicator.ROSEBARRIER;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.tint(0, 0.5f, 1, 0.5f);
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String heroMessage() {
        return Messages.get(this, "heromsg");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns());
    }
}
