//END(port from Arknights): Wraith_donutSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Wraith_donutSprite
extends MobSprite {
    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 34;

    public Wraith_donutSprite() {
        this.texture("sprites/Wraith_donut.png");
        TextureFilm frames = new TextureFilm(this.texture, 32, 32);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0, 0, 0);
        this.run = new MovieClip.Animation(15, true);
        this.run.frames(frames, 1, 2, 3, 4, 5, 6, 7, 8);
        this.attack = new MovieClip.Animation(17, false);
        this.attack.frames(frames, 9, 10, 11, 12, 13, 14, 15, 16);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27);
        this.play(this.idle);
    }
}
