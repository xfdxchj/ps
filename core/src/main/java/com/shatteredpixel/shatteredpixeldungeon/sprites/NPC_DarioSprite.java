//END(port from Arknights): NPC_DarioSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class NPC_DarioSprite
extends MobSprite {
    public NPC_DarioSprite() {
        this.texture("sprites/npc_dario.png");
        TextureFilm frames = new TextureFilm(this.texture, 38, 34);
        this.idle = new MovieClip.Animation(5, true);
        this.idle.frames(frames, 0, 1, 2, 3, 4, 5);
        this.run = new MovieClip.Animation(5, true);
        this.run.frames(frames, 0, 1, 2, 3, 4, 5);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 28, 28);
        this.play(this.idle);
    }
}
