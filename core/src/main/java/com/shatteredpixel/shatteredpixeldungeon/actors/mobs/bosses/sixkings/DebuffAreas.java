package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ConfusionGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

/**
 * ═══════════════════════════════════════════════════════════════
 *  Debuff 王的「区域」工具类
 * ═══════════════════════════════════════════════════════════════
 *
 * END(用户反馈): "debuff 的也没有（特效），随机区域是 3*3 的，类似药水的效果"。
 *
 * 之前的实现只用 CellEmitter 撒粒子 + 改地形，玩家看不出效果。
 * 现在完全照抄【药水】的做法：
 *
 *   药水（PotionOfToxicGas.shatter）：
 *       splash(cell);                                   // 水花动画
 *       Sample.INSTANCE.play(Assets.Sounds.SHATTER);    // 碎裂音
 *       GameScene.add(Blob.seed(cell, 1000, ToxicGas.class));  // 真正的毒气
 *
 * 我们照做，只是把 amount 调小以控制范围：
 *       Blob 的 evolve() 是 value = (sum/count) - 1，
 *       即每回合从中心向外扩散 1 格、同时整体衰减 1。
 *       所以 amount 决定"能扩散多远"：
 *           amount ≈ 30  -> 约 3x3
 *           amount ≈ 80  -> 约 5x5
 *
 * 这样玩家看到的就是【真正的地面气体】—— 和药水一模一样的效果。
 */
public class DebuffAreas {

    /** 区域类型 */
    public enum Kind {
        TOXIC,       // 毒气（绿）
        CORROSIVE,   // 腐蚀气体（黄绿）
        PARALYTIC,   // 麻痹气体（黄）
        CONFUSION,   // 混乱气体（紫）
        FIRE,        // 火焰（红）
        ACID,        // 酸蚀（地面伤害）
        BLEED,       // 流血（地面伤害）
        SUMMON,      // 召唤（踩到出小怪）
        DOOM         // 定命（三阶段专用）
    }

    /**
     * 生成一个区域 —— 完全模仿药水的做法。
     *
     * @param cell   中心格
     * @param kind   类型
     * @param radius 半径（1 = 3x3，2 = 5x5）
     */
    public static void spawn( int cell, Kind kind, int radius, Char owner ) {

        if (Dungeon.level == null) return;
        if (cell < 0 || cell >= Dungeon.level.length()) return;

        // 气体能扩散多远：amount 越大扩散越远
        int amount = amountForRadius( radius );

        switch (kind) {
            case TOXIC:
                addGas( cell, amount, ToxicGas.class );
                fx( cell, 0x66CC33 );
                break;

            case CORROSIVE:
                addGas( cell, amount, CorrosiveGas.class );
                fx( cell, 0x99CC00 );
                break;

            case PARALYTIC:
                addGas( cell, amount, ParalyticGas.class );
                fx( cell, 0xFFFF66 );
                break;

            case CONFUSION:
                addGas( cell, amount, ConfusionGas.class );
                fx( cell, 0xCC66FF );
                break;

            case FIRE:
                addGas( cell, Math.max(4, radius * 2), Fire.class );
                fx( cell, 0xFF6622 );
                break;

            case ACID:
            case BLEED:
            case SUMMON:
            case DOOM:
                // 这几类是"触发式"的，不产生气体；由外部每回合检查
                fx( cell, kind == Kind.ACID ? 0xCCFF00 : 0xFF3333 );
                break;
        }
    }

    /** 半径 -> 气体量（Blob 每回合衰减 1，所以 amount ≈ 半径 * 步长） */
    public static int amountForRadius( int radius ) {
        // 半径 1(3x3) ≈ 20, 半径 2(5x5) ≈ 60, 半径 3(7x7) ≈ 120
        return Math.max( 12, radius * radius * 20 );
    }

    /** 加气体（和药水一样的调用方式） */
    private static void addGas( int cell, int amount, Class<? extends Blob> type ) {
        try {
            GameScene.add( Blob.seed( cell, amount, type ) );
        } catch (Throwable t) {
            com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
                    "[六王] 生成气体失败: " + t);
        }
    }

    /** 视觉 + 音效（模仿药水碎裂） */
    private static void fx( int cell, int color ) {
        try {
            if (Dungeon.level.heroFOV[cell]) {
                Splash.at( cell, color, 8 );
                Sample.INSTANCE.play( com.shatteredpixel.shatteredpixeldungeon.Assets.Sounds.SHATTER );
            }
            CellEmitter.get( cell ).burst( SmokeParticle.FACTORY, 10 );
        } catch (Throwable ignored) { }
    }

    /**
     * 区域对站在里面的角色的【触发式】效果
     * （气体类由 Blob 自己处理，这里只处理酸蚀/流血/召唤/定命）
     */
    public static void applyTrigger( Char ch, Kind kind, Char owner ) {
        if (ch == null) return;

        switch (kind) {
            case ACID:
                ch.damage( Random.IntRange(3, 6), owner );
                Buff.affect( ch, Corrosion.class ).set( 5f, 3 );
                break;

            case BLEED:
                ch.damage( Random.IntRange(4, 8), owner );
                Buff.affect( ch, Bleeding.class ).set( 8f );
                break;

            case SUMMON:
                if (Random.Int(3) == 0) {
                    summonMinion( ch.pos, owner );
                }
                break;

            case DOOM:
                Buff.affect(ch, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom.class );
                break;

            default:
                break;   // 气体类不用管
        }
    }

    /** 召唤一个小怪（在目标附近） */
    private static void summonMinion( int near, Char owner ) {
        try {
            //END(修复): 本 fork 用 MobSpawner.getMobRotation(depth) 取本层怪种，
            //再用 Reflection.newInstance 实例化（参考 MyCoreHeart / DistortionTrap）。
            java.util.ArrayList<Class<? extends com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob>>
                    rotation = com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner
                            .getMobRotation( Dungeon.depth );
            if (rotation == null || rotation.isEmpty()) return;

            Class<? extends com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob> cls =
                    rotation.get( Random.Int( rotation.size() ) );

            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m =
                    com.watabou.utils.Reflection.newInstance( cls );
            if (m == null) return;

            for (int n : com.watabou.utils.PathFinder.NEIGHBOURS8) {
                int c = near + n;
                if (!Dungeon.level.insideMap(c)) continue;
                if (!Dungeon.level.passable[c]) continue;
                if (Actor.findChar(c) != null) continue;

                m.pos = c;
                GameScene.add( m );
                m.state = m.HUNTING;
                return;
            }
        } catch (Throwable t) {
            com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w("[六王] 召唤失败: " + t);
        }
    }
}
