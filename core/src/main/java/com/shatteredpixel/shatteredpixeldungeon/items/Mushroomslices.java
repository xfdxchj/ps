//END(port from Arknights): Mushroomslices
package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Mushroomslices
extends Item {
    public Mushroomslices() {
        this.image = ItemSpriteSheet.MUSH;
        this.stackable = true;
        this.bones = false;
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
