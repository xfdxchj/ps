//END(port from Arknights): Mula_1Sprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Mula_1Sprite
extends MobSprite {
    public Mula_1Sprite() {
        this.texture("sprites/mula_1.png");
        TextureFilm frames = new TextureFilm(this.texture, 152, 130);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        this.run = new MovieClip.Animation(10, true);
        this.run.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        this.play(this.idle);
    }

    @Override
    public void attack(int cell) {
        this.play(this.attack);
    }
}
