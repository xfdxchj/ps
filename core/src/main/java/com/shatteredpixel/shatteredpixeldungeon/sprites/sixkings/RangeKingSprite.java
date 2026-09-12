package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 远程王（方舟·萨卡兹狙击手）
 *
 * 帧布局照抄方舟原版：TextureFilm(38, 34)
 */
public class RangeKingSprite extends MobSprite {

    public RangeKingSprite() {
        super();
        texture( "sprites/sixkings/Sarkaz_Sniper.png" );
        TextureFilm frames = new TextureFilm( texture, 38, 34 );

        idle   = new MovieClip.Animation( 10, true );
        idle.frames( frames, 0 );

        run    = new MovieClip.Animation( 12, true );
        run.frames( frames, 1, 2, 3, 4, 5, 6, 7, 8 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 9, 10, 11, 12, 13 );

        die    = new MovieClip.Animation( 12, false );
        die.frames( frames, 0 );

        play( idle );
    }
}
