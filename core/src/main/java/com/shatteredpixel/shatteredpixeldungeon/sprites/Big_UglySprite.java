//END(port from Arknights): Big_UglySprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Big_UglySprite
extends MobSprite {
    public Big_UglySprite() {
        this.texture("sprites/big_ugly.png");
        TextureFilm frames = new TextureFilm(this.texture, 78, 60);
        this.idle = new MovieClip.Animation(10, true);
        this.idle.frames(frames, 1, 2);
        this.run = new MovieClip.Animation(13, true);
        this.run.frames(frames, 3, 4, 5, 6, 7, 8);
        this.attack = new MovieClip.Animation(13, false);
        this.attack.frames(frames, 9, 10, 11, 12, 13, 14, 15, 16);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
