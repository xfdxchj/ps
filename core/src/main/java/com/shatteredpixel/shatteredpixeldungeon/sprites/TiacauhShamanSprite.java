//END(port from Arknights): TiacauhShamanSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TiacauhShaman;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class TiacauhShamanSprite
extends MobSprite {
    public TiacauhShamanSprite() {
        this.texture("sprites/tiacauh_shaman.png");
        TextureFilm frames = new TextureFilm(this.texture, 56, 46);
        this.idle = new MovieClip.Animation(2, true);
        this.idle.frames(frames, 0);
        this.run = new MovieClip.Animation(12, true);
        this.run.frames(frames, 0);
        this.attack = new MovieClip.Animation(25, false);
        this.attack.frames(frames, 0);
        this.zap = this.attack.clone();
        this.die = new MovieClip.Animation(12, false);
        this.die.frames(frames, 0, 0, 0);
        this.play(this.idle);
    }

    @Override
    public void zap(int cell) {
        this.turnTo(this.ch.pos, cell);
        this.play(this.zap);
        MagicMissile.boltFromChar(this.parent, 12, this, cell, new Callback(){

            @Override
            public void call() {
                ((TiacauhShaman)TiacauhShamanSprite.this.ch).onZapComplete();
            }
        });
        Sample.INSTANCE.play("sounds/zap.mp3");
    }
}
