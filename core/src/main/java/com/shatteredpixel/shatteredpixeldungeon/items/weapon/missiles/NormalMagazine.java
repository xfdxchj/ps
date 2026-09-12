//END(port from Arknights): NormalMagazine
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import java.util.ArrayList;

public class NormalMagazine
extends MissileWeapon {
    public NormalMagazine() {
        this.image = ItemSpriteSheet.AMMO1;
        this.hitSound = "sounds/hit_magic.mp3";
        this.hitSoundPitch = 1.0f;
        this.tier = 3;
        this.baseUses = 1.0f;
    }

    @Override
    public int value() {
        return super.value() / 2;
    }

    public static class Recipe
    extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {
        @Override
        public boolean testIngredients(ArrayList<Item> ingredients) {
            boolean stone = false;
            boolean seed = false;
            for (Item ingredient : ingredients) {
                if (ingredient.quantity() <= 0) continue;
                if (ingredient instanceof Runestone) {
                    stone = true;
                    continue;
                }
                if (!(ingredient instanceof Plant.Seed)) continue;
                seed = true;
            }
            return stone && seed;
        }

        @Override
        public int cost(ArrayList<Item> ingredients) {
            return 2;
        }

        @Override
        public Item brew(ArrayList<Item> ingredients) {
            if (!this.testIngredients(ingredients)) {
                return null;
            }
            for (Item ingredient : ingredients) {
                ingredient.quantity(ingredient.quantity() - 1);
            }
            return this.sampleOutput(null);
        }

        @Override
        public Item sampleOutput(ArrayList<Item> ingredients) {
            return new NormalMagazine();
        }
    }
}
