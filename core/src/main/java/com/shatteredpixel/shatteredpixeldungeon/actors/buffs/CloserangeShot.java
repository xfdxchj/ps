//END(port from Arknights): CloserangeShot
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.BArray;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

public class CloserangeShot
extends Buff {
    private boolean actived = false;
    private static final String ACTIVE = "actived";

    @Override
    public int icon() {
        return 27;
    }

    @Override
    public void tintIcon(Image icon) {
        if (this.actived) {
            icon.hardlight(1.0f, 1.0f, 1.0f);
        } else {
            icon.hardlight(0.5f, 0.5f, 0.0f);
        }
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        if (this.actived) {
            return Messages.get(this, "active");
        }
        return Messages.get(this, "passive");
    }

    @Override
    public boolean act() {
        this.spend(1.0f);
        this.isActived();
        return true;
    }

    public void isActived() {
        int range = 2;
        if (Dungeon.hero.hasTalent(Talent.ZERO_RANGE_SHOT)) {
            --range;
        }
        boolean isactive = false;
        PathFinder.buildDistanceMap(this.target.pos, BArray.not(Dungeon.level.solid, null), range);
        for (int cell = 0; cell < PathFinder.distance.length; ++cell) {
            Char ch;
            if (PathFinder.distance[cell] >= Integer.MAX_VALUE || (ch = Actor.findChar(cell)) == null || ch.alignment != Char.Alignment.ENEMY) continue;
            isactive = true;
        }
        this.actived = isactive;
    }

    public boolean state() {
        return this.actived;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ACTIVE, this.actived);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.actived = bundle.getBoolean(ACTIVE);
    }
}
