package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 不灭追猎者（方舟·海嗣收割者）
 *
 * 帧布局照抄方舟原版：TextureFilm(60, 46)
 */
public class ImmortalKingSprite extends MobSprite {

    public ImmortalKingSprite() {
        super();
        texture( "sprites/sixkings/sea_reaper.png" );
        TextureFilm frames = new TextureFilm( texture, 60, 46 );

        idle   = new MovieClip.Animation( 10, true );
        idle.frames( frames, 2, 3, 4, 5, 6, 7, 8 );

        run    = new MovieClip.Animation( 12, true );
        run.frames( frames, 2, 3, 4, 5, 6, 7, 8 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 0 );

        die    = new MovieClip.Animation( 12, false );
        die.frames( frames, 0 );

        play( idle );
    }
}
