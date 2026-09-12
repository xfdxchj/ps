package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;

/**
 * ═══════════════════════════════════════════════════════════════
 *  示例 Boss 精灵
 * ═══════════════════════════════════════════════════════════════
 *
 * 贴图：assets/sprites/sixkings/exampleboss.png（256×16，10 帧）
 * 可以用 tools/gen_template_sprite.py 生成占位图：
 *   python gen_template_sprite.py --name exampleboss --frames 10 --size 16
 *
 * 帧布局：
 *   0-1  idle     站立
 *   2-3  run      移动
 *   4-6  attack   攻击
 *   7-9  die      死亡
 */
public class ExampleBossSprite extends MobSprite {

    public ExampleBossSprite() {
        super();

        // 贴图路径（区分大小写！）
        texture( "sprites/sixkings/exampleboss.png" );

        // 16px 格子；如果是 32px 大 Boss 改成 (32, 32)
        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        // new Animation(帧间隔, 是否循环) —— 帧间隔越大越慢

        idle = new Animation( 8, true );
        idle.frames( frames, 0, 1 );

        run = new Animation( 12, true );
        run.frames( frames, 2, 3 );

        attack = new Animation( 12, false );
        attack.frames( frames, 4, 5, 6 );

        die = new Animation( 10, false );
        die.frames( frames, 7, 8, 9 );

        play( idle );
    }
}
