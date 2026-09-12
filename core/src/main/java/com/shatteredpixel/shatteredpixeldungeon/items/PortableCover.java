//END(port from Arknights): PortableCover
package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Mushroomslices;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import java.util.ArrayList;

public class PortableCover
extends Item {
    private static String AC_USE = "USE";

    public PortableCover() {
        this.image = ItemSpriteSheet.BARRI;
        this.stackable = true;
        this.defaultAction = AC_USE;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_USE);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_USE)) {
            Buff.append(curUser, CoverBuff.class, 3.0f);
            this.detach(PortableCover.curUser.belongings.backpack);
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
        return 30 * this.quantity;
    }

    public static class CoverBuff
    extends FlavourBuff {
    }

    //END(移植调整): 原方舟写作 `class Recipe extends Recipe.SimpleRecipe`，
    //但内部类名 Recipe 会遮蔽外部同名类，导致循环继承。这里改用全限定名。
    public static class CoverRecipe
    extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
        public CoverRecipe() {
            this.inputs = new Class[]{Mushroomslices.class};
            this.inQuantity = new int[]{2};
            this.cost = 0;
            this.output = PortableCover.class;
            this.outQuantity = 1;
        }
    }
}
