package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;

/**
 * ═══════════════════════════════════════════════════════════════
 *  Boss 精灵模板 —— 复制本文件并改名
 * ═══════════════════════════════════════════════════════════════
 *
 * 改名前全局替换：
 *   SorcererKingSprite -> 你的精灵类名
 *   sprites/sixkings/sorcererking.png -> 你的贴图路径
 *
 * 贴图规格（详见 01-贴图规格.md）：
 *   256×16   = 16 格 × 16px，单行 10 帧（标准）
 *   512×32   = 16 格 × 32px，大 Boss
 *
 * ⚠️ TextureFilm 的格子大小必须与图片一致，否则 Boss 只显示一角或崩溃。
 */
public class SorcererKingSprite extends MobSprite {

    public SorcererKingSprite() {
        super();

        // ─────────── 贴图 ───────────
        // 注意：路径区分大小写！
        texture( "sprites/sixkings/sorcererking.png" );

        // ─────────── 切分格子 ───────────
        // 16px 格 -> (16, 16)
        // 32px 格 -> (32, 32)   ← 大 Boss 改这里
        TextureFilm frames = new TextureFilm( texture, 16, 16 );

        // ─────────── 动画 ───────────
        // new Animation(速度, 是否循环)
        //   速度：帧间隔，**数字越大越慢**
        //   循环：true = 循环播放，false = 播一次就停

        // 站立（循环）
        idle = new Animation( 8, true );
        idle.frames( frames, 0, 1 );

        // 移动（循环）
        run = new Animation( 12, true );
        run.frames( frames, 2, 3 );

        // 攻击（播一次）
        attack = new Animation( 12, false );
        attack.frames( frames, 4, 5, 6 );

        // 死亡（播一次）
        die = new Animation( 10, false );
        die.frames( frames, 7, 8, 9 );

        // 初始状态
        play( idle );
    }

    // ═══════════════════════════════════════════════
    //  可选：只有 1 帧静态图时的写法
    // ═══════════════════════════════════════════════
    //
    // public SorcererKingSprite() {
    //     super();
    //     texture( "sprites/sixkings/sorcererking.png" );
    //     TextureFilm frames = new TextureFilm( texture, 16, 16 );
    //
    //     idle   = new Animation( 1, true );   idle.frames( frames, 0 );
    //     run    = new Animation( 1, true );   run.frames( frames, 0 );
    //     attack = new Animation( 1, false );  attack.frames( frames, 0 );
    //     die    = new Animation( 1, false );  die.frames( frames, 0 );
    //
    //     play( idle );
    // }

    // ═══════════════════════════════════════════════
    //  可选：让 Boss 有特殊外观效果
    // ═══════════════════════════════════════════════

    /** 例：给 Boss 加发光描边（火焰王）。 */
    // @Override
    // public void link( Char ch ) {
    //     super.link( ch );
    //     add( State.BURNING );
    // }

    /** 例：用 tint 给 Boss 染成红色（省美术）。 */
    // @Override
    // public void draw() {
    //     super.draw();
    //     hardlight( 0xFF6644 );    // 叠加红色
    // }
}
