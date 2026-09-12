package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  「炮击」延迟爆炸体 —— 远程王/全能王的预警 + 爆炸
 * ═══════════════════════════════════════════════════════════════
 *
 * END(修复·预警不可见): 玩家反馈"区域(5*5)的贴图效果没有，感觉就是直接命中"。
 *
 * 原因：原实现只用 {@code CellEmitter} 撒几个粒子，粒子一瞬间就散了，
 * 玩家根本看不出"哪里将要爆炸"。
 *
 * 现在的预警手段（三重，肉眼可见）：
 *   1. {@link TargetedCell} —— 本 fork 自带的红色预警框（SuperNovaTracker 同款），
 *      会持续闪烁，是最明显的提示
 *   2. 把地面改成 {@code Terrain.TRAP}（视觉上变色）
 *   3. 每回合在预警格上持续喷粒子（不是只喷一次）
 *
 * 爆炸时：
 *   - {@code WandOfBlastWave.BlastWave.blast(cell, radius)} 真实的扩散波纹
 *   - 大量火焰 + 烟雾粒子
 *   - 屏幕震动
 *   - 对范围内角色造成伤害并击退
 */
public class ArtilleryShell extends Actor {

    /** 目标格 */
    public int cell;
    /** 剩余预警回合 */
    public int warnTurns;
    /** 爆炸半径（0 = 只炸中心格；2 = 5x5） */
    public int radius;
    /** 伤害下限 / 上限 */
    public int dmgMin, dmgMax;
    /** 中心格伤害倍率 */
    public float centerMult = 2f;
    /** 施法者（伤害归属） */
    public Char owner;

    /** 预警用的红色框（视觉） */
    private transient ArrayList<TargetedCell> markers = new ArrayList<>();

    /** 记录被改过的格子，爆炸后还原 */
    private final ArrayList<Integer> claimed = new ArrayList<>();

    /** 已经初始化过预警 */
    private boolean warned = false;

    public ArtilleryShell() { }

    public ArtilleryShell(int cell, int warnTurns, int radius,
                          int dmgMin, int dmgMax, Char owner) {
        this.cell = cell;
        this.warnTurns = warnTurns;
        this.radius = radius;
        this.dmgMin = dmgMin;
        this.dmgMax = dmgMax;
        this.owner = owner;
    }

    // ═══════════════════════════════════════════════
    @Override
    protected boolean act() {

        // 第一回合：建立预警
        if (!warned) {
            warned = true;
            markWarning();
        }

        // 每回合刷新预警（粒子持续喷 + 红色框重新加，防止被清理）
        if (warnTurns > 0) {
            refreshWarning();
        }

        if (warnTurns-- <= 0) {
            explode();
            clearMarkers();
            return false;      // 移除自己
        }

        spend( TICK );
        return true;
    }

    // ═══════════════════════════════════════════════
    //  预警
    // ═══════════════════════════════════════════════
    private void markWarning() {

        // ── 1) 地面变色 + 记录原状 ──
        for (int c : affectedCells()) {
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;

            claimed.add( c );
            Level.set( c, Terrain.TRAP );
            GameScene.updateMap( c );
        }

        // ── 2) 红色预警框（最明显的视觉提示）──
        addMarkers();
    }

    /** 加红色预警框 */
    private void addMarkers() {
        if (Dungeon.hero == null || Dungeon.hero.sprite == null) return;
        if (Dungeon.hero.sprite.parent == null) return;

        for (int c : affectedCells()) {
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.heroFOV[c]) continue;
            try {
                TargetedCell t = new TargetedCell( c, 0xFF3300 );
                Dungeon.hero.sprite.parent.add( t );
                markers.add( t );
            } catch (Throwable ignored) { }
        }
    }

    /** 每回合刷新：粒子持续喷 */
    private void refreshWarning() {
        for (int c : affectedCells()) {
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.heroFOV[c]) continue;

            // 边缘格子喷更多，让范围看得出来
            boolean isEdge = (c == cell + radius) || (c == cell - radius)
                    || (c == cell + radius * Dungeon.level.width())
                    || (c == cell - radius * Dungeon.level.width());

            CellEmitter.get( c ).burst( FlameParticle.FACTORY, isEdge ? 3 : 1 );
        }
    }

    /** 清掉红色框 */
    private void clearMarkers() {
        for (TargetedCell t : markers) {
            try {
                if (t != null && t.parent != null) t.killAndErase();
            } catch (Throwable ignored) { }
        }
        markers.clear();
    }

    // ═══════════════════════════════════════════════
    //  爆炸
    // ═══════════════════════════════════════════════
    private void explode() {

        ArrayList<Integer> cells = affectedCells();

        // ── 1) 视觉：真实的扩散波纹 ──
        try {
            WandOfBlastWave.BlastWave.blast( cell, radius + 1f );
        } catch (Throwable ignored) { }
        try {
            Camera.main.shake( 4, 0.25f );
        } catch (Throwable ignored) { }

        // ── 2) 粒子 ──
        for (int c : cells) {
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (Dungeon.level.heroFOV[c]) {
                CellEmitter.get(c).burst( FlameParticle.FACTORY, 12 );
                CellEmitter.get(c).burst( SmokeParticle.FACTORY, 6 );
            }
        }

        // ── 3) 伤害 ──
        for (int c : cells) {
            if (c < 0 || c >= Dungeon.level.length()) continue;

            Char ch = Actor.findChar( c );
            if (ch == null) continue;
            if (ch == owner) continue;

            int dmg = Random.NormalIntRange( dmgMin, dmgMax );
            if (c == cell) {
                dmg = Math.round( dmg * centerMult );    // 中心翻倍
            }
            ch.damage( dmg, owner );

            // 击退
            if (ch.isAlive()) {
                try {
                    Ballistica traj = new Ballistica( cell, ch.pos, Ballistica.PROJECTILE );
                    WandOfBlastWave.throwChar( ch, traj, 2, true, true, owner );
                } catch (Throwable ignored) { }
            }
        }

        // ── 4) 还原地形 ──
        restoreTerrain();
    }

    /** 还原被标记的格子 */
    private void restoreTerrain() {
        for (int c : claimed) {
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (Dungeon.level.map[c] == Terrain.TRAP) {
                Level.set( c, Terrain.EMPTY );
                GameScene.updateMap( c );
            }
        }
        claimed.clear();
    }

    /** 受影响的格子（方形范围） */
    private ArrayList<Integer> affectedCells() {
        ArrayList<Integer> out = new ArrayList<>();
        int w = Dungeon.level.width();

        if (radius <= 0) {
            out.add( cell );
            return out;
        }

        int cx = cell % w, cy = cell / w;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int x = cx + dx, y = cy + dy;
                if (x < 0 || y < 0 || x >= w || y >= Dungeon.level.height()) continue;
                out.add( y * w + x );
            }
        }
        return out;
    }

    // ═══════════════════════════════════════════════
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("cell", cell);
        bundle.put("warnTurns", warnTurns);
        bundle.put("radius", radius);
        bundle.put("dmgMin", dmgMin);
        bundle.put("dmgMax", dmgMax);
        bundle.put("centerMult", centerMult);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        cell       = bundle.getInt("cell");
        warnTurns  = bundle.getInt("warnTurns");
        radius     = bundle.getInt("radius");
        dmgMin     = bundle.getInt("dmgMin");
        dmgMax     = bundle.getInt("dmgMax");
        centerMult = bundle.getFloat("centerMult");
        warned     = true;     // 读档后不再重复初始化预警
    }
}
