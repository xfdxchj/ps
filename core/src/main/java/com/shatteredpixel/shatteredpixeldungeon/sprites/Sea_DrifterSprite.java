//END(port from Arknights): Sea_DrifterSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Sea_DrifterSprite
extends MobSprite {
    public Sea_DrifterSprite() {
        this.texture("sprites/sea_drifter.png");
        TextureFilm frames = new TextureFilm(this.texture, 44, 44);
        this.idle = new MovieClip.Animation(7, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 0, 1, 2, 3, 4, 5);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 6, 7, 8, 9, 10, 11);
        this.die = new MovieClip.Animation(20, false);
        this.die.frames(frames, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5);
        this.play(this.idle);
    }
}
