//END(port from Arknights): ErgateSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class ErgateSprite
extends MobSprite {
    public ErgateSprite() {
        this.texture("sprites/ergate.png");
        TextureFilm frames = new TextureFilm(this.texture, 32, 32);
        this.idle = new MovieClip.Animation(8, true);
        this.idle.frames(frames, 0);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 0);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(8, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
