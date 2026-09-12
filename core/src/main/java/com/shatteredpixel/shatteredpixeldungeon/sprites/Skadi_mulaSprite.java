//END(port from Arknights): Skadi_mulaSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Skadi_mulaSprite
extends MobSprite {
    public Skadi_mulaSprite() {
        this.texture("sprites/skadi_mula.png");
        TextureFilm frames = new TextureFilm(this.texture, 46, 36);
        this.idle = new MovieClip.Animation(8, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 0);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }

    @Override
    public void attack(int cell) {
        this.play(this.attack);
    }
}
