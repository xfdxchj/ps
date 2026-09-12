//END(port from Arknights): NetherseaBrandguiderSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class NetherseaBrandguiderSprite
extends MobSprite {
    public NetherseaBrandguiderSprite() {
        this.texture("sprites/sea_brandguider.png");
        TextureFilm frames = new TextureFilm(this.texture, 60, 52);
        this.idle = new MovieClip.Animation(8, true);
        this.idle.frames(frames, 1, 2, 3, 4, 5, 6, 7, 8);
        this.run = new MovieClip.Animation(12, true);
        this.run.frames(frames, 1, 2, 3, 4, 5, 6, 7, 8);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0, 9, 10, 11, 12, 13, 14, 15, 16);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 0, 17, 18, 19, 21, 22, 23, 24, 25, 26, 27, 28);
        this.play(this.idle);
    }
}
