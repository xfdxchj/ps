/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.RL;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RL
extends Gun {
    public RL() {
        this.round = this.max_round = 2;
        this.explode = true;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return 6 * (this.tier + 2) + lvl * (this.tier + 2);
    }

    @Override
    public int bulletUse() {
        return Math.max(0, (this.maxRound() - this.round) * 3);
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new RLBullet();
    }

    public class RLBullet
    extends Gun.Bullet {
        public RLBullet() {
            this.image = ItemSpriteSheet.SHRAPNEL_BOMB;
        }
    }
}

