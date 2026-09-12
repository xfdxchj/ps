//END(port from Arknights): SchwarzSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class SchwarzSprite
extends MobSprite {
    public SchwarzSprite() {
        this.texture("sprites/schwarz.png");
        TextureFilm frames = new TextureFilm(this.texture, 42, 32);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 9);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 10, 11, 12, 13, 14, 15, 16, 17);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
