//END(port from Arknights): Mon3terSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Mon3terSprite
extends MobSprite {
    public Mon3terSprite() {
        this.texture("sprites/mon3ter.png");
        TextureFilm frames = new TextureFilm(this.texture, 64, 54);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0, 1, 2, 1);
        this.run = new MovieClip.Animation(6, true);
        this.run.frames(frames, 0, 1, 2, 1);
        this.attack = new MovieClip.Animation(18, false);
        this.attack.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 10, 9, 8);
        this.play(this.idle);
    }
}
