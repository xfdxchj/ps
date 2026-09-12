//END(port from Arknights): Camouflage
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class Camouflage
extends Invisibility {
    public Camouflage() {
        this.announced = false;
    }

    @Override
    public boolean attachTo(Char target) {
        return super.attachTo(target);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", this.dispTurns());
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public void detach() {
        if (this.target.invisible > 0) {
            --this.target.invisible;
        }
        super.detach();
    }

    public static void dispelCamouflage() {
        if (Dungeon.level != null) {
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                if (mob.buff(Camouflage.class) == null) continue;
                Buff.detach(mob, Camouflage.class);
            }
        }
    }

    public static void dispelCamouflage(Char c) {
        if (c.buff(Camouflage.class) != null) {
            Buff.detach(c, Camouflage.class);
        }
    }

    @Override
    public boolean act() {
        if (!(this.target instanceof Hero) && Camouflage.heroDetects(this.target)) {
            Buff.detach(this.target, Camouflage.class);
        }
        return super.act();
    }

    public static boolean CamoFlageEnemy(Char mob) {
        return mob.buff(Camouflage.class) == null && Dungeon.level.distance(mob.pos, Dungeon.hero.pos) != 1 && !Camouflage.heroDetects(mob);
    }

    public static boolean heroDetects(Char mob) {
        if (Dungeon.hero.buff(Light.class) != null) {
            return true;
        }
        if (Dungeon.hero.buff(MindVision.class) != null) {
            return true;
        }
        for (TalismanOfForesight.CharAwareness b : Dungeon.hero.buffs(TalismanOfForesight.CharAwareness.class)) {
            if (b.charID != mob.id()) continue;
            return true;
        }
        return false;
    }
}
