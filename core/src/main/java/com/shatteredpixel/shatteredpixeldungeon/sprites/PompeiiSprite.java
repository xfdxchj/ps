//END(port from Arknights): PompeiiSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class PompeiiSprite
extends MobSprite {
    public PompeiiSprite() {
        this.texture("sprites/pompeii.png");
        TextureFilm frames = new TextureFilm(this.texture, 58, 38);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7);
        this.run = new MovieClip.Animation(5, true);
        this.run.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7);
        this.attack = new MovieClip.Animation(5, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
