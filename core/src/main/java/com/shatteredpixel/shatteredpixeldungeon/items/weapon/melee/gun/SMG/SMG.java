/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.SMG;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class SMG
extends Gun {
    public SMG() {
        this.round = this.max_round = 4;
        this.shotPerShoot = 3;
        this.shootingAccuracy = 1.2f;
        this.adjacentShootingAccuracy = 1.5f;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return 2 * (this.tier + 1) + Math.round(0.5f * (float)lvl * (float)(this.tier + 1));
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new SMGBullet();
    }

    public class SMGBullet
    extends Gun.Bullet {
        public SMGBullet() {
            this.image = ItemSpriteSheet.TRIPLE_BULLET;
        }
    }
}

