package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 古神之拳（方舟·皇帝之刃）
 *
 * 帧布局照抄方舟原版：TextureFilm(72, 46)
 */
public class FistSprite extends MobSprite {

    public FistSprite() {
        super();
        texture( "sprites/sixkings/emperor_blade.png" );
        TextureFilm frames = new TextureFilm( texture, 72, 46 );

        idle   = new MovieClip.Animation( 10, true );
        idle.frames( frames, 0, 1, 2, 1, 0 );

        run    = new MovieClip.Animation( 12, true );
        run.frames( frames, 7, 8, 9, 10, 11, 12, 13, 14 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 3, 4, 5, 6 );

        die    = new MovieClip.Animation( 12, false );
        die.frames( frames, 15 );

        play( idle );
    }
}
