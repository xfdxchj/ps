//END(port from Arknights): Oblivion
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class Oblivion
extends FlavourBuff {
    public static final float DURATION = 6.0f;

    public Oblivion() {
        this.type = Buff.buffType.NEGATIVE;
        this.announced = true;
    }

    @Override
    public void detach() {
        super.detach();
        Dungeon.observe();
    }

    @Override
    public int icon() {
        return 16;
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0.0f, (6.0f - this.visualcooldown()) / 6.0f);
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", this.dispTurns());
    }
}
