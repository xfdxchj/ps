/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.SR;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class SR
extends Gun {
    public SR() {
        this.round = this.max_round = 2;
        this.reload_time = 3.0f;
        this.shootingAccuracy = 2.0f;
        this.adjacentShootingAccuracy = 0.3f;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return 4 * (this.tier + 2) + lvl * (this.tier + 2);
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new SRBullet();
    }

    public class SRBullet
    extends Gun.Bullet {
        public SRBullet() {
            this.image = ItemSpriteSheet.SNIPER_BULLET;
        }
    }
}

