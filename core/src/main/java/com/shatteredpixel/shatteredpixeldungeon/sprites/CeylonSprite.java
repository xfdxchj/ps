//END(port from Arknights): CeylonSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class CeylonSprite
extends MobSprite {
    public CeylonSprite() {
        this.texture("sprites/ceylon.png");
        TextureFilm frames = new TextureFilm(this.texture, 38, 38);
        this.idle = new MovieClip.Animation(10, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5, 6);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 0);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(16, false);
        this.die.frames(frames, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 38, 38, 38, 38, 39, 40, 41, 42, 43, 44, 0);
        this.play(this.idle);
    }
}
