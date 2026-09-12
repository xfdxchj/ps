//END(port from Arknights): RingOfMistress
package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import java.text.DecimalFormat;

public class RingOfMistress
extends Ring {
    public RingOfMistress() {
        this.icon = ItemSpriteSheet.Icons.RING_ACCURACY;
    }

    @Override
    public String statsInfo() {
        if (this.isIdentified()) {
            return Messages.get(this, "stats", new DecimalFormat("#.##").format(100.0 * (Math.pow(1.15f, this.soloBuffedBonus()) - 1.0)));
        }
        return Messages.get(this, "typical_stats", new DecimalFormat("#.##").format(15.0));
    }

    @Override
    protected Ring.RingBuff buff() {
        return new WeaponChargeUp();
    }

    public static float SPMultiplier(Char t) {
        return (float)Math.pow(1.15, RingOfMistress.getBuffedBonus(t, WeaponChargeUp.class));
    }

    public class WeaponChargeUp
    extends Ring.RingBuff {
    }
}
