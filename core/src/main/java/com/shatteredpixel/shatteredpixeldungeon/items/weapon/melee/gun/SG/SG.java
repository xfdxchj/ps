/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.SG;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class SG
extends Gun {
    public SG() {
        this.round = this.max_round = 2;
        this.shotPerShoot = 5;
        this.shootingAccuracy = 1.0f;
        this.adjacentShootingAccuracy = 3.0f;
        this.spread = true;
    }

    @Override
    public int bulletUse() {
        return this.maxRound() - this.round;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return this.tier + 1 + Math.round(0.5f * (float)lvl * (float)(this.tier + 1));
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new SGBullet();
    }

    public class SGBullet
    extends Gun.Bullet {
        public SGBullet() {
            this.image = ItemSpriteSheet.TRIPLE_BULLET;
        }

        @Override
        protected float adjacentAccFactor(Char owner, Char target) {
            return super.adjacentAccFactor(owner, target) * 3.0f;
        }
    }
}

