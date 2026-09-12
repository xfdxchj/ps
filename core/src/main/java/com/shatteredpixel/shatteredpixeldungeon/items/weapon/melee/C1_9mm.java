//END(port from Arknights): C1_9mm
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.GunWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class C1_9mm
extends GunWeapon {
    public C1_9mm() {
        this.image = ItemSpriteSheet.C1;
        this.hitSound = "sounds/hit_gun.mp3";
        this.hitSoundPitch = 0.9f;
        this.FIRE_DELAY_MULT = 0.66f;
        this.bulletMax = 34;
        this.bullet = Random.Int(this.bulletMax / 2, this.bulletMax + 1);
        this.MIN_RANGE = 1;
        this.MAX_RANGE = 4;
        this.usesTargeting = true;
        this.defaultAction = "ZAP";
        this.tier = 3;
    }

    @Override
    public float getFireAcc(int from, int to) {
        int distance = this.getDistance(from, to);
        if (this.isWithinRange(distance)) {
            return 1.0f;
        }
        if (distance > this.getMaxRange()) {
            return Math.max(0.0f, 1.0f - 0.25f * (float)(distance - this.getMaxRange()));
        }
        return 1.0f;
    }

    @Override
    protected void specialFire(Char ch) {
        Buff.affect(ch, Slow.class, 2.0f);
    }
}
