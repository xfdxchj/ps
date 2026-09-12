//END(port from Arknights): SeaObject
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NervousImpairment;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BelfrySprite;
import com.watabou.utils.PathFinder;

public class SeaObject
extends NPC {
    public SeaObject() {
        this.spriteClass = BelfrySprite.class;
        this.properties.add(Char.Property.IMMOVABLE);
        this.properties.add(Char.Property.MINIBOSS);
        this.properties.add(Char.Property.INORGANIC);
        this.properties.add(Char.Property.STATIC);
        this.immunities.add(Burning.class);
        this.state = this.PASSIVE;
        this.HP = 2000;
        this.HT = 2000;
    }

    @Override
    public void beckon(int cell) {
    }

    @Override
    protected boolean act() {
        this.spend(1.0f);
        return true;
    }

    @Override
    public boolean interact(Char c) {
        return true;
    }

    @Override
    protected void spend(float time) {
        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; ++i) {
            Char ch = SeaObject.findChar(this.pos + PathFinder.NEIGHBOURS8[i]);
            if (ch == null || !ch.isAlive() || ch.alignment != Char.Alignment.ALLY || ch.buff(NervousImpairment.class) == null) continue;
            ch.buff(NervousImpairment.class).sum(-15.0f * time);
        }
        super.spend(time);
    }
}
