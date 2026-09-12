//END(port from Arknights): Tiacauh_BraveSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Tiacauh_BraveSprite
extends MobSprite {
    public Tiacauh_BraveSprite() {
        this.texture("sprites/tiacauh_brave.png");
        TextureFilm frames = new TextureFilm(this.texture, 56, 46);
        this.idle = new MovieClip.Animation(5, true);
        this.idle.frames(frames, 0, 1, 2, 3, 2, 1, 0);
        this.run = new MovieClip.Animation(6, true);
        this.run.frames(frames, 4, 5, 6, 7, 8, 9, 10, 11);
        this.attack = new MovieClip.Animation(20, false);
        this.attack.frames(frames, 4, 5, 6, 7, 8, 9, 10, 11);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
