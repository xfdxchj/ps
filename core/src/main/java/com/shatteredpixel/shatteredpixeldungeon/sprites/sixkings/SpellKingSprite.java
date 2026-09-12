package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 法术王（方舟·萨卡兹术师 Schwarz）
 *
 * 帧布局照抄方舟原版：TextureFilm(42, 32)
 */
public class SpellKingSprite extends MobSprite {

    public SpellKingSprite() {
        super();
        texture( "sprites/sixkings/schwarz.png" );
        TextureFilm frames = new TextureFilm( texture, 42, 32 );

        idle   = new MovieClip.Animation( 10, true );
        idle.frames( frames, 9 );

        run    = new MovieClip.Animation( 12, true );
        run.frames( frames, 10, 11, 12, 13, 14, 15, 16, 17 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 1, 2, 3, 4, 5, 6, 7, 8, 9 );

        die    = new MovieClip.Animation( 12, false );
        die.frames( frames, 0 );

        play( idle );
    }
}
