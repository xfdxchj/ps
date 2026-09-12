//END(port from Arknights): OblivionTrap
package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Oblivion;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.noosa.audio.Sample;

public class OblivionTrap
extends Trap {
    public OblivionTrap() {
        this.color = 2;
        this.shape = 8;
        this.canBeHidden = true;
    }

    @Override
    public void activate() {
        Char c = Actor.findChar(this.pos);
        if (c != null && !c.flying) {
            Buff.affect(c, Oblivion.class, 6.0f);
        }
        if (Dungeon.level.heroFOV[this.pos]) {
            Sample.INSTANCE.play("sounds/trap.mp3");
        }
    }
}
