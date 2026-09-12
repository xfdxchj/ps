//END(port from Arknights): Bonk
package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import java.util.ArrayList;

public class Bonk
extends Item {
    public static final String AC_DRINK = "DRINK";

    public Bonk() {
        this.image = ItemSpriteSheet.BONK;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_DRINK);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_DRINK)) {
            Buff.append(curUser, BonkBuff.class, 10.0f);
            this.detach(Bonk.curUser.belongings.backpack);
            curUser.spendAndNext(1.0f);
        }
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public int value() {
        return 40;
    }

    public static class BonkBuff
    extends FlavourBuff {
        public BonkBuff() {
            this.type = Buff.buffType.POSITIVE;
            this.announced = true;
        }

        @Override
        public int icon() {
            return 21;
        }

        @Override
        public String toString() {
            return Messages.get(this, "name");
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", this.dispTurns());
        }
    }
}
