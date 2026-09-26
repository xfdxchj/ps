/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.MG;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class MG
extends Gun {
    public MG() {
        this.round = this.max_round = 4;
        this.shotPerShoot = 3;
        this.shootingAccuracy = 0.9f;
        this.adjacentShootingAccuracy = 0.3f;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return 2 * (this.tier + 2) + Math.round(0.5f * (float)lvl * (float)(this.tier + 2));
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new MGBullet();
    }

    public class MGBullet
    extends Gun.Bullet {
        public MGBullet() {
            this.image = ItemSpriteSheet.TRIPLE_BULLET;
        }
    }
}

