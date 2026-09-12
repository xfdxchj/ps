//END(port from Arknights): Siesta_InfantrySprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Siesta_InfantrySprite
extends MobSprite {
    public Siesta_InfantrySprite() {
        this.texture("sprites/siesta_infantry.png");
        TextureFilm frames = new TextureFilm(this.texture, 32, 32);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0);
        this.run = new MovieClip.Animation(12, true);
        this.run.frames(frames, 1, 2, 3, 4, 5, 6, 7, 8);
        this.attack = new MovieClip.Animation(12, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
