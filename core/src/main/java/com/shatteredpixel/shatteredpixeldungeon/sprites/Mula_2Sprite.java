//END(port from Arknights): Mula_2Sprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Mula_2Sprite
extends MobSprite {
    public Mula_2Sprite() {
        this.texture("sprites/mula_2.png");
        TextureFilm frames = new TextureFilm(this.texture, 92, 60);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 0);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
