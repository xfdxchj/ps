//END(port from Arknights): Tiacauh_fanaticSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Tiacauh_fanaticSprite
extends MobSprite {
    public Tiacauh_fanaticSprite() {
        this.texture("sprites/tiacauhfanatic.png");
        TextureFilm frames = new TextureFilm(this.texture, 56, 46);
        this.idle = new MovieClip.Animation(5, true);
        this.idle.frames(frames, 0, 1, 2, 1, 0);
        this.run = new MovieClip.Animation(8, true);
        this.run.frames(frames, 3, 4, 5, 6, 7, 8, 9, 10);
        this.attack = new MovieClip.Animation(20, false);
        this.attack.frames(frames, 3, 4, 5, 6, 7, 8, 9, 10);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
