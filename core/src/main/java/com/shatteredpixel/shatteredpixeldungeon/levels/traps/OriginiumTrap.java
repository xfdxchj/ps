//END(port from Arknights): OriginiumTrap
package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ActiveOriginium;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;

public class OriginiumTrap
extends Trap {
    public OriginiumTrap() {
        this.color = 0;
        this.shape = 8;
        this.canBeHidden = false;
    }

    @Override
    public void trigger() {
        if (Dungeon.level.heroFOV[this.pos]) {
            Sample.INSTANCE.play("sounds/trap.mp3");
        }
        this.reveal();
        Level.set(this.pos, 18);
        this.activate();
    }

    @Override
    public void activate() {
        Char c = Actor.findChar(this.pos);
        if (c != null) {
            Buff.affect(c, ActiveOriginium.class).set((float)c.HT * 0.1f);
            if (c instanceof Mob) {
                if (((Mob)c).state == ((Mob)c).HUNTING) {
                    ((Mob)c).state = ((Mob)c).WANDERING;
                }
                ((Mob)c).beckon(Dungeon.level.randomDestination(c));
            }
        }
        if (Dungeon.level.heroFOV[this.pos]) {
            GameScene.flash(-2130771968);
            Sample.INSTANCE.play("sounds/blast.mp3");
        }
    }
}
