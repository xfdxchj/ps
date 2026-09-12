package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/**
 * 六王精灵基类（占位图）。
 *
 * Animation 是 MovieClip 的内部类，所以要用 MovieClip.Animation。
 * 贴图规格：256x16（16px 格，10 帧）
 *   帧 0-1 idle / 2-3 run / 4-6 attack / 7-9 die
 */
public class SixKingSprite extends MobSprite {

    public SixKingSprite( String tex ) {
        super();
        texture( tex );
        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        idle   = new MovieClip.Animation( 8,  true );
        idle.frames( frames, 0, 1 );

        run    = new MovieClip.Animation( 12, true );
        run.frames( frames, 2, 3 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 4, 5, 6 );

        die    = new MovieClip.Animation( 10, false );
        die.frames( frames, 7, 8, 9 );

        play( idle );
    }
}
