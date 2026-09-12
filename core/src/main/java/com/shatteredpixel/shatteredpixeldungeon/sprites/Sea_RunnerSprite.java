//END(port from Arknights): Sea_RunnerSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Sea_RunnerSprite
extends MobSprite {
    public Sea_RunnerSprite() {
        this.texture("sprites/sea_runner.png");
        TextureFilm frames = new TextureFilm(this.texture, 46, 28);
        this.idle = new MovieClip.Animation(10, true);
        this.idle.frames(frames, 0);
        this.run = new MovieClip.Animation(12, true);
        this.run.frames(frames, 1, 2, 3, 4, 5, 6);
        this.attack = new MovieClip.Animation(12, false);
        this.attack.frames(frames, 7, 8, 9, 10, 11, 12, 13);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 14, 15, 16, 17, 18, 19, 20, 21, 22);
        this.play(this.idle);
    }
}
