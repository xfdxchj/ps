//END(port from Arknights): Sea_SpewerSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Sea_SpewerSprite
extends MobSprite {
    public Sea_SpewerSprite() {
        this.texture("sprites/sea_spewer.png");
        TextureFilm frames = new TextureFilm(this.texture, 34, 34);
        this.idle = new MovieClip.Animation(8, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7);
        this.run = new MovieClip.Animation(12, true);
        this.run.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0, 8, 9, 10, 11, 12, 13);
        this.die = new MovieClip.Animation(20, false);
        this.die.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7);
        this.play(this.idle);
    }
}
