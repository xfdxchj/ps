//END(port from Arknights): Tomimi_BossSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Tomimi_BossSprite
extends MobSprite {
    public Tomimi_BossSprite() {
        this.texture("sprites/tomimi.png");
        TextureFilm frames = new TextureFilm(this.texture, 36, 36);
        this.idle = new MovieClip.Animation(7, true);
        this.idle.frames(frames, 1, 2, 3, 4, 5, 6);
        this.run = new MovieClip.Animation(13, true);
        this.run.frames(frames, 7, 8, 9, 10, 11, 12, 13, 14);
        this.attack = new MovieClip.Animation(18, false);
        this.attack.frames(frames, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 27, 28, 29);
        this.play(this.idle);
    }
}
