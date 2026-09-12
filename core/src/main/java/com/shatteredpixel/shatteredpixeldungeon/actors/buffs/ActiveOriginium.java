//END(port from Arknights): ActiveOriginium
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class ActiveOriginium
extends Buff {
    protected float level;
    private static final String LEVEL = "level";

    public ActiveOriginium() {
        this.type = Buff.buffType.NEGATIVE;
        this.announced = true;
    }

    public float level() {
        return this.level;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LEVEL, this.level);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.level = bundle.getFloat(LEVEL);
    }

    public void set(float level) {
        this.level = Math.max(this.level, level);
    }

    @Override
    public int icon() {
        return 52;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public boolean act() {
        if (this.target.isAlive()) {
            this.level = Random.NormalFloat(this.level * 0.5f - 1.0f, this.level);
            int dmg = Math.round(this.level * 0.2f + (float)this.target.HP * 0.05f);
            if (dmg < 1) {
                dmg = 1;
            }
            if (this.target == Dungeon.hero) {
                dmg += 1 + Dungeon.hero.lvl / 5;
            }
            dmg = Math.min(30, dmg);
            if (this.level > 0.0f) {
                this.target.damage(dmg, this);
                if (this.target.sprite.visible) {
                    Splash.at(this.target.sprite.center(), -1.5707963f, 0.52359873f, this.target.sprite.blood(), Math.min(10 * dmg / this.target.HT, 10));
                }
                if (this.target == Dungeon.hero && !this.target.isAlive()) {
                    Dungeon.fail(this.getClass());
                    GLog.n(Messages.get(this, "ondeath"));
                }
                this.spend(1.0f);
            } else {
                this.detach();
            }
        } else {
            this.detach();
        }
        return true;
    }

    @Override
    public String heroMessage() {
        return Messages.get(this, "heromsg");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", Math.round(this.level));
    }
}
