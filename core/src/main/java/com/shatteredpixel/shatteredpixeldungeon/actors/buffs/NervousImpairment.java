//END(port from Arknights): NervousImpairment
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

public class NervousImpairment
extends Buff {
    float currentDamage = 0.0f;
    float limit = 100.0f;
    private static final String POW = "Power";

    public void sum(float nervousDamage) {
        this.currentDamage = Math.min(100.0f, this.currentDamage + nervousDamage);
        if (this.currentDamage <= 0.0f) {
            this.detach();
        }
        if (this.currentDamage >= this.limit) {
            this.burst();
        }
    }

    void burst() {
        if (Dungeon.extrastage_Sea && Dungeon.depth >= 40) {
            this.target.damage(this.target.HT / 3, this);
        } else {
            this.target.damage(this.target.HT / 4, this);
        }
        Buff.affect(this.target, Slow.class, 2.0f);
        Buff.affect(this.target, Weakness.class, 2.0f);
        this.detach();
    }

    @Override
    public int icon() {
        return 55;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", Float.valueOf(this.currentDamage));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(POW, this.currentDamage);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.currentDamage = bundle.getFloat(POW);
    }
}
