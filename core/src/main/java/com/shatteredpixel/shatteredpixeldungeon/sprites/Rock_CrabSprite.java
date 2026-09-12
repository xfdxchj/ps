//END(port from Arknights): Rock_CrabSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Rock_CrabSprite
extends MobSprite {
    public Rock_CrabSprite() {
        this.texture("sprites/rock_crab.png");
        TextureFilm frames = new TextureFilm(this.texture, 16, 16);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0, 0, 0);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 0);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(20, false);
        this.die.frames(frames, 0, 1, 2, 3);
        this.play(this.idle);
    }
}
