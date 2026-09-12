package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  「炮击」延迟爆炸体 —— 远程王的预警/爆炸机制
 * ═══════════════════════════════════════════════════════════════
 *
 * 机制：
 *   1. 生成时把目标格标红（Terrain.TRAP）+ 粒子预警
 *   2. 经过 warnTurns 回合后爆炸
 *   3. 爆炸对 radius 范围内的角色造成伤害
 *   4. 伤害后还原地形
 *
 * 这是一个独立的 Actor，会随回合推进（可存档）。
 */
public class ArtilleryShell extends Actor {

    /** 目标格 */
    public int cell;
    /** 剩余预警回合 */
    public int warnTurns;
    /** 爆炸半径（0 = 只炸中心格） */
    public int radius;
    /** 伤害下限 / 上限 */
    public int dmgMin, dmgMax;
    /** 中心格伤害倍率 */
    public float centerMult = 2f;
    /** 施法者（用于伤害归属） */
    public Char owner;

    /** 记录被改过的格子，爆炸后还原 */
    private final ArrayList<Integer> claimed = new ArrayList<>();

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

        // 第一回合：标记预警
        if (claimed.isEmpty()) {
            markWarning();
        }

        if (warnTurns-- <= 0) {
            explode();
            return false;      // 移除自己
        }

        spend( TICK );
        return true;
    }

    /** 预警：标红 + 粒子 */
    private void markWarning() {
        for (int c : affectedCells()) {
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;

            // 记录原地形并改成警示地形
            claimed.add( c );
            Level.set( c, Terrain.TRAP );
            GameScene.updateMap( c );

            if (Dungeon.level.heroFOV[c]) {
                CellEmitter.get(c).burst( FlameParticle.FACTORY, 4 );
            }
        }
    }

    /** 爆炸 */
    private void explode() {

        ArrayList<Integer> cells = affectedCells();

        // ── 伤害 ──
        for (int c : cells) {
            if (c < 0 || c >= Dungeon.level.length()) continue;

            Char ch = Actor.findChar( c );
            if (ch == null) continue;
            if (ch == owner) continue;

            int dmg = Random.NormalIntRange( dmgMin, dmgMax );
            if (c == cell) {
                dmg = Math.round( dmg * centerMult );    // 中心格翻倍
            }
            ch.damage( dmg, owner );

            // 击退
            if (ch.isAlive()) {
                Ballistica traj = new Ballistica(cell, ch.pos, Ballistica.PROJECTILE);
                WandOfBlastWave.throwChar(ch, traj, 2, true, true, owner);
            }
        }

        // ── 视觉 ──
        for (int c : cells) {
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (Dungeon.level.heroFOV[c]) {
                CellEmitter.get(c).burst( FlameParticle.FACTORY, 14 );
                CellEmitter.get(c).burst( SmokeParticle.FACTORY, 8 );
            }
        }

        // ── 还原地形 ──
        restoreTerrain();
    }

    /** 还原被标记的格子 */
    private void restoreTerrain() {
        for (int c : claimed) {
            if (c < 0 || c >= Dungeon.level.length()) continue;
            // 只还原"还是 TRAP"的格子（避免覆盖其它效果）
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
    }
}
