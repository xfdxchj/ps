//END(port from Arknights): Sea_LeefSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Sea_LeefSprite
extends MobSprite {
    public Sea_LeefSprite() {
        this.texture("sprites/sea_reef.png");
        TextureFilm frames = new TextureFilm(this.texture, 48, 40);
        this.idle = new MovieClip.Animation(8, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5);
        this.run = new MovieClip.Animation(12, true);
        this.run.frames(frames, 0, 1, 2, 3, 4, 5);
        this.attack = new MovieClip.Animation(12, false);
        this.attack.frames(frames, 6, 7, 8, 9, 10, 11, 12, 13);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 14, 15, 16, 17, 18, 19, 20, 21, 22);
        this.play(this.idle);
    }
}
