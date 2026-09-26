/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.GL;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class GL
extends Gun {
    public GL() {
        this.round = this.max_round = 2;
        this.explode = true;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return 6 * (this.tier + 1) + lvl * (this.tier + 1);
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new GLBullet();
    }

    public class GLBullet
    extends Gun.Bullet {
        public GLBullet() {
            super();
            this.image = ItemSpriteSheet.FIRE_BOMB;
        }
    }
}

