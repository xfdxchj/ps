//END(port from Arknights): UpMagazine
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.NormalMagazine;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class UpMagazine
extends MissileWeapon {
    public UpMagazine() {
        this.image = ItemSpriteSheet.AMMO2;
        this.hitSound = "sounds/hit_magic.mp3";
        this.hitSoundPitch = 1.0f;
        this.tier = 4;
        this.baseUses = 1.0f;
    }

    @Override
    public int value() {
        return super.value() / 2;
    }

    public static class UpMagazineRecipe
    extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
        public UpMagazineRecipe() {
            this.inputs = new Class[]{NormalMagazine.class, MetalShard.class};
            this.inQuantity = new int[]{1, 1};
            this.cost = 4;
            this.output = UpMagazine.class;
            this.outQuantity = 1;
        }
    }
}
