/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.HG;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class HG
extends Gun {
    public HG() {
        this.round = this.max_round = 4;
        this.shootingSpeed = 0.5f;
        this.reload_time = 1.0f;
        this.adjacentShootingAccuracy = 2.0f;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return 2 * (this.tier + 1) + lvl * (this.tier + 1);
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new HGBullet();
    }

    public class HGBullet
    extends Gun.Bullet {
        public HGBullet() {
            this.image = ItemSpriteSheet.SINGLE_BULLET;
        }
    }
}

