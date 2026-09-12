//END(port from Arknights): Hallucination
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Hallucination
extends Buff {
    public static final float DURATION = 10.0f;
    protected float left;
    private static final String LEFT = "left";

    public Hallucination() {
        this.type = Buff.buffType.NEGATIVE;
        this.announced = true;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LEFT, this.left);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.left = bundle.getFloat(LEFT);
    }

    public void set(float duration) {
        this.left = duration;
    }

    @Override
    public boolean act() {
        this.spend(1.0f);
        this.left -= 1.0f;
        if (this.left <= 0.0f) {
            this.detach();
        }
        return true;
    }

    public void proc() {
        if (Random.Int(4) == 0) {
            this.target.damage(this.target.damageRoll() / 3, this.target);
        }
        if (this.target instanceof Hero && !this.target.isAlive()) {
            Dungeon.fail(this.getClass());
            GLog.n(Messages.get(this, "ondeath"));
        }
    }

    @Override
    public int icon() {
        return 33;
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0.0f, (10.0f - this.left + 1.0f) / 10.0f);
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }
}
