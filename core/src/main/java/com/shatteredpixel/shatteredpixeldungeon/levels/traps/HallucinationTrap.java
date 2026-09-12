//END(port from Arknights): HallucinationTrap
package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hallucination;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.noosa.audio.Sample;

public class HallucinationTrap
extends Trap {
    public HallucinationTrap() {
        this.color = 1;
        this.shape = 8;
        this.canBeHidden = true;
    }

    @Override
    public void activate() {
        Char c = Actor.findChar(this.pos);
        if (c != null && !c.flying) {
            Buff.affect(c, Hallucination.class).set(10.0f);
        }
        if (Dungeon.level.heroFOV[this.pos]) {
            Sample.INSTANCE.play("sounds/trap.mp3");
        }
    }
}
