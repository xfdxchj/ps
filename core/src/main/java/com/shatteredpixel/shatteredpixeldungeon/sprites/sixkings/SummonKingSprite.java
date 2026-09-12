package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 召唤王（方舟·汐斯塔特工）
 *
 * 帧布局照抄方舟原版：TextureFilm(32, 32)
 */
public class SummonKingSprite extends MobSprite {

    public SummonKingSprite() {
        super();
        texture( "sprites/sixkings/siesta_agent.png" );
        TextureFilm frames = new TextureFilm( texture, 32, 32 );

        idle   = new MovieClip.Animation( 10, true );
        idle.frames( frames, 0 );

        run    = new MovieClip.Animation( 12, true );
        run.frames( frames, 1, 2, 3, 4, 5, 6, 7, 8 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 0 );

        die    = new MovieClip.Animation( 12, false );
        die.frames( frames, 0 );

        play( idle );
    }
}
