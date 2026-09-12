//END(port from Arknights): SanityPotion
package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NervousImpairment;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import java.util.ArrayList;

public class SanityPotion
extends Food {
    public static final String AC_DRINK = "DRINK";

    public SanityPotion() {
        this.image = ItemSpriteSheet.TYLENOL;
        this.defaultAction = AC_DRINK;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.remove("EAT");
        actions.add(AC_DRINK);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_DRINK)) {
            if (hero.buff(NervousImpairment.class) != null) {
                hero.buff(NervousImpairment.class).sum(-50.0f);
            }
            this.detach(SanityPotion.curUser.belongings.backpack);
            Sample.INSTANCE.play("sounds/drink.mp3");
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
}
