//END(port from Arknights): TeaRose
package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class TeaRose
extends Item {
    public TeaRose() {
        this.image = ItemSpriteSheet.TEA;
        this.stackable = true;
        this.unique = true;
        this.cursed = false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }
}
