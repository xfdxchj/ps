package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 全能王（方舟·海嗣掠食者）
 *
 * 帧布局照抄方舟原版：TextureFilm(34, 34)
 */
public class OmniKingSprite extends MobSprite {

    public OmniKingSprite() {
        super();
        texture( "sprites/sixkings/sea_predator.png" );
        TextureFilm frames = new TextureFilm( texture, 34, 34 );

        idle   = new MovieClip.Animation( 10, true );
        idle.frames( frames, 0 );

        run    = new MovieClip.Animation( 12, true );
        run.frames( frames, 1, 2, 3, 4, 5, 6 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 7, 8, 9, 10, 11, 12, 13 );

        die    = new MovieClip.Animation( 12, false );
        die.frames( frames, 14, 15, 16, 17, 18, 19, 20 );

        play( idle );
    }
}
