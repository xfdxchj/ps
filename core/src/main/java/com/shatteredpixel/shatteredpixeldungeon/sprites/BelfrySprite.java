//END(port from Arknights): BelfrySprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class BelfrySprite
extends MobSprite {
    public BelfrySprite() {
        this.texture("sprites/belfry.png");
        TextureFilm frames = new TextureFilm(this.texture, 32, 50);
        this.idle = new MovieClip.Animation(10, true);
        this.idle.frames(frames, 0, 1, 2, 3, 2, 1, 0, 1, 2, 3, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 2, 1, 0, 1, 2, 3, 2, 1, 0, 0, 0, 0, 0, 1, 2, 3, 2, 1, 0, 4, 5, 4, 0, 6, 7, 6, 0, 0, 0, 0, 0);
        this.run = new MovieClip.Animation(8, true);
        this.run.frames(frames, 0);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
