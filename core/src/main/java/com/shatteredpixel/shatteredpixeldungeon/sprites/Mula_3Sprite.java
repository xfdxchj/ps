//END(port from Arknights): Mula_3Sprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Mula_3Sprite
extends MobSprite {
    public Mula_3Sprite() {
        this.texture("sprites/mula_3.png");
        TextureFilm frames = new TextureFilm(this.texture, 72, 50);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 0, 1, 2, 3, 4, 5);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0, 1, 2, 3, 4, 5);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 3);
        this.play(this.idle);
    }
}
