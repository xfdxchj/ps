//END(port from Arknights): LavaSlugSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class LavaSlugSprite
extends MobSprite {
    public LavaSlugSprite() {
        this.texture("sprites/big_bug.png");
        TextureFilm frames = new TextureFilm(this.texture, 36, 24);
        this.idle = new MovieClip.Animation(8, true);
        this.idle.frames(frames, 1, 2, 3, 4, 5, 6);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 1, 2, 3, 4, 5, 6);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(8, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
