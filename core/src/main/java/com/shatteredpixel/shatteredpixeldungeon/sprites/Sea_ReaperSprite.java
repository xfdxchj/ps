//END(port from Arknights): Sea_ReaperSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Sea_ReaperSprite
extends MobSprite {
    public Sea_ReaperSprite() {
        this.texture("sprites/sea_reaper.png");
        this.updateChargeState(false);
    }

    public void updateChargeState(boolean charge) {
        int c = charge ? 9 : 0;
        TextureFilm frames = new TextureFilm(this.texture, 60, 46);
        this.idle = new MovieClip.Animation(5, true);
        this.idle.frames(frames, c + 2, c + 3, c + 4, c + 5, c + 6, c + 7, c + 8);
        this.run = new MovieClip.Animation(20, true);
        this.run.frames(frames, c + 2, c + 3, c + 4, c + 5, c + 6, c + 7, c + 8);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0);
        this.play(this.idle);
    }
}
