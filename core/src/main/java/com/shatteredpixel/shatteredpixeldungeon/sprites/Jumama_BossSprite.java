//END(port from Arknights): Jumama_BossSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Jumama_BossSprite
extends MobSprite {
    public Jumama_BossSprite() {
        this.texture("sprites/jumama.png");
        TextureFilm frames = new TextureFilm(this.texture, 36, 50);
        this.idle = new MovieClip.Animation(7, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 6, 7, 5);
        this.run = new MovieClip.Animation(13, true);
        this.run.frames(frames, 0);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0, 1, 2);
        this.die = new MovieClip.Animation(15, false);
        this.die.frames(frames, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        this.play(this.idle);
    }
}
