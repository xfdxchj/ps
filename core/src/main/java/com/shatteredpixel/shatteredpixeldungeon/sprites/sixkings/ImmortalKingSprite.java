package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 不灭追猎者（用战士贴图）
 *
 * END(用户要求): 改用【地牢原版战士贴图】sprites/warrior.png。
 * 帧布局照抄 HeroSprite：TextureFilm(12, 15)
 *   idle   = 0, 0, 0, 1, 0, 0, 1, 1
 *   run    = 2, 3, 4, 5, 6, 7
 *   die    = 8, 9, 10, 11, 12, 11
 *   attack = 13, 14, 15, 0
 */
public class ImmortalKingSprite extends MobSprite {

    public ImmortalKingSprite() {
        super();
        texture( "sprites/warrior.png" );
        TextureFilm frames = new TextureFilm( texture, 12, 15 );

        idle = new MovieClip.Animation( 1, true );
        idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

        run = new MovieClip.Animation( 15, true );
        run.frames( frames, 2, 3, 4, 5, 6, 7 );

        die = new MovieClip.Animation( 20, false );
        die.frames( frames, 8, 9, 10, 11, 12, 11 );

        attack = new MovieClip.Animation( 15, false );
        attack.frames( frames, 13, 14, 15, 0 );

        play( idle );
    }
}
