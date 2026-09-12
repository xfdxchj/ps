//END(port from Arknights): Tomimi_towerSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Tomimi_towerSprite
extends MobSprite {
    public Tomimi_towerSprite() {
        this.texture("sprites/pillar.png");
        TextureFilm frames = new TextureFilm(this.texture, 42, 84);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0, 0, 0);
        this.run = new MovieClip.Animation(18, true);
        this.run.frames(frames, 0);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
