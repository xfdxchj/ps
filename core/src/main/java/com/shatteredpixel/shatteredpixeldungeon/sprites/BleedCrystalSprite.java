package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

//END(移植自魔绫·挑战区): 凝血结晶精灵
public class BleedCrystalSprite extends MobSprite {

    public BleedCrystalSprite() {
        texture(Assets.Sprites.BLEED_SENTRY);

        idle = new Animation(1, true);
        idle.frames(texture.uvRect(0, 0, 8, 15));

        run = idle.clone();
        attack = idle.clone();
        die = idle.clone();
        zap = idle.clone();

        play(idle);
    }
}
