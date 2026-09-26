/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.AR;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class AR
extends Gun {
    public AR() {
        this.round = this.max_round = 4;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return 4 * (this.tier + 1) + lvl * (this.tier + 1);
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new ARBullet();
    }

    public class ARBullet
    extends Gun.Bullet {
        public ARBullet() {
            super();
            this.image = ItemSpriteSheet.SINGLE_BULLET;
        }
    }
}

