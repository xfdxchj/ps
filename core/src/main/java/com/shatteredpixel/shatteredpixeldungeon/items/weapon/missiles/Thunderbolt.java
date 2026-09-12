//END(port from Arknights): Thunderbolt
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Thunderbolt
extends MissileWeapon {
    public Thunderbolt() {
        this.image = ItemSpriteSheet.LISKARM_DOLL;
        this.hitSound = "sounds/lightning.mp3";
        this.hitSoundPitch = 0.8f;
        this.tier = 3;
        this.baseUses = 12.0f;
        this.sticky = false;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        damage = (int)((float)damage * (2.0f * (float)(defender.HP / defender.HT)));
        return super.proc(attacker, defender, damage);
    }
}
