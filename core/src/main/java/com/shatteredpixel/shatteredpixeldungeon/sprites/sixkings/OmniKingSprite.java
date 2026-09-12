package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 全能王精灵（32x32 占位图：sprites/sixkings/omniking.png） */
public class OmniKingSprite extends MobSprite {
    public OmniKingSprite() {
        super();
        texture( "sprites/sixkings/omniking.png" );
        TextureFilm frames = new TextureFilm( texture, 32, 32 );

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
