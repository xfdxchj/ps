package com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/** 古神之拳（召唤王的 6 个召唤物）
 *
 * END(用户要求): 用方舟原版贴图 sprites/sixkings/emperor_blade.png。
 * 帧布局照抄方舟 FistSprite：TextureFilm(72, 46)
 *   idle   = 0, 1, 2, 1, 0
 *   run    = 7, 8, 9, 10, 11, 12, 13, 14
 *   attack = 3, 4, 5, 6
 *   die    = 15
 */
public class FistSprite extends MobSprite {

    public FistSprite() {
        super();
        texture( "sprites/sixkings/emperor_blade.png" );
        TextureFilm frames = new TextureFilm( texture, 72, 46 );

        idle = new MovieClip.Animation( 10, true );
        idle.frames( frames, 0, 1, 2, 1, 0 );

        run = new MovieClip.Animation( 12, true );
        run.frames( frames, 7, 8, 9, 10, 11, 12, 13, 14 );

        attack = new MovieClip.Animation( 12, false );
        attack.frames( frames, 3, 4, 5, 6 );

        die = new MovieClip.Animation( 12, false );
        die.frames( frames, 15 );

        play( idle );
    }
}
