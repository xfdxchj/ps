//END(port from Arknights): First_talkSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class First_talkSprite
extends MobSprite {
    public First_talkSprite() {
        this.texture("sprites/first_talk.png");
        TextureFilm frames = new TextureFilm(this.texture, 58, 56);
        this.idle = new MovieClip.Animation(10, true);
        this.idle.frames(frames, 0);
        this.run = new MovieClip.Animation(12, true);
        this.run.frames(frames, 1, 2, 3, 4, 5, 6, 7, 8);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
