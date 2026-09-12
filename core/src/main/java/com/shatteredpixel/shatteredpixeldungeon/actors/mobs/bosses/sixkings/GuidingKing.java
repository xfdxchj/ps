package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogFist;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.SummonKingSprite;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  5号 · 召唤王（六古神之拳）
 * ═══════════════════════════════════════════════════════════════
 *
 * 核心机制：
 *   · HP = 1，**永久无敌**，本体不参与战斗
 *   · 开战直接召唤 6 个「古神之拳」
 *   · 玩家无法通过打本体推进战斗，必须清掉六拳
 *
 * END(用户指正·重要): 之前我用自建的 AncientFist + 方舟贴图实现，
 * 但【原版地牢本来就有"古神之拳"】——{@code YogFist}，共 6 种：
 *     燃烧之拳 / 泥土之拳 / 腐烂之拳 / 锈蚀之拳 / 光明之拳 / 黑暗之拳
 * 贴图 {@code sprites/yog_fists.png}，帧布局 TextureFilm(24, 17)。
 * 现在直接召唤这 6 种原版拳，既保真又自带各自的技能与特效。
 *
 * 强化规则：
 *   · 击杀 3 个拳 → 剩余 3 个强化（伤害 +50%）
 *   · 只剩 1 个   → 该拳狂暴（攻速 +100%、受伤 −50%）
 *   · 6 个全灭    → 本体失去无敌，可被一击杀死
 */
public class GuidingKing extends Boss {

    {
        spriteClass = SummonKingSprite.class;

        HT  = 1;            // 本体 1 血
        EXP = 120;
        baseHT = HT;

        baseMin = 0;        // 本体不攻击
        baseMax = 0;
        baseAcc = 0;
        baseEva = 0;
        baseMinDef = 0;
        baseMaxDef = 0;

        properties.add( Property.BOSS );
        properties.add( Property.IMMOVABLE );   // 本体不动
        alignment = Alignment.ENEMY;

        loot = Generator.Category.ARTIFACT;
        lootChance = 1f;
    }

    /** 存活的拳 */
    private final ArrayList<Mob> fists = new ArrayList<>();
    private boolean summoned = false;

    /** 记录每个拳的强化等级 */
    private static final java.util.HashMap<Mob, Integer> empower =
            new java.util.HashMap<>();

    private static final String SUMMONED = "summoned";

    // ═══════════════════════════════════════════════
    @Override
    protected boolean act() {

        // ── 开战：召唤 6 个古神之拳 ──
        if (!summoned) {
            summoned = true;
            summonSixFists();
            spend( TICK );
            return true;
        }

        // 清理已死引用
        fists.removeIf( f -> f == null || !f.isAlive() );

        // 只要还有拳活着，本体维持无敌
        if (!fists.isEmpty()) {
            if (buff(Invulnerability.class) == null) {
                Buff.affect(this, Invulnerability.class);
            }
        } else {
            Buff.detach(this, Invulnerability.class);
        }

        checkEmpower();

        spend( TICK );
        return true;
    }

    /** 召唤原版 6 种古神之拳 */
    private void summonSixFists() {

        yell( Messages.get(this, "summon") );

        // 原版的 6 种拳
        Class<? extends YogFist>[] kinds = new Class[]{
                YogFist.BurningFist.class,   // 燃烧之拳
                YogFist.SoiledFist.class,    // 泥土之拳
                YogFist.RottingFist.class,   // 腐烂之拳
                YogFist.RustedFist.class,    // 锈蚀之拳
                YogFist.BrightFist.class,    // 光明之拳
                YogFist.DarkFist.class       // 黑暗之拳
        };

        int placed = 0;
        for (Class<? extends YogFist> k : kinds) {
            int cell = findSpawnCell( placed );
            if (cell == -1) {
                placed++;
                continue;
            }
            try {
                YogFist fist = k.getDeclaredConstructor().newInstance();
                fist.pos = cell;
                fists.add( fist );
                //END(注意): 这里是 act() 阶段（不是 level.createMobs()），
                //Dungeon.level 已经存在，所以用 GameScene.add 是正确的。
                GameScene.add( fist );

                if (Dungeon.level != null && Dungeon.level.heroFOV[cell]) {
                    CellEmitter.get(cell).burst( ShadowParticle.UP, 12 );
                }
            } catch (Exception e) {
                com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
                        "[六王] 召唤古神之拳失败: " + e);
            }
            placed++;
        }

        Buff.affect(this, Invulnerability.class);
    }

    /** 找环绕本体的空格 */
    private int findSpawnCell(int index) {
        int w = Dungeon.level.width();
        int cx = pos % w, cy = pos / w;

        int[][] offsets = {
                { 0, -2}, { 2, -1}, { 2,  1}, { 0,  2}, {-2,  1}, {-2, -1},
                { 1, -3}, { 3, -1}, { 3,  1}, { 1,  3}, {-1,  3}, {-3,  1},
                {-3, -1}, {-1, -3}
        };

        for (int i = 0; i < offsets.length; i++) {
            int idx = (index + i) % offsets.length;
            int x = cx + offsets[idx][0];
            int y = cy + offsets[idx][1];

            if (x < 1 || y < 1 || x >= w - 1 || y >= Dungeon.level.height() - 1) continue;

            int c = y * w + x;
            if (c < 0 || c >= Dungeon.level.length()) continue;
            if (!Dungeon.level.passable[c]) continue;
            if (Actor.findChar(c) != null) continue;

            return c;
        }
        return -1;
    }

    /** 强化判定 */
    private void checkEmpower() {
        int alive = fists.size();

        if (alive == 3) {
            boolean any = false;
            for (Mob f : fists) {
                if (empower.getOrDefault(f, 0) < 1) {
                    empower.put(f, 1);
                    // 原版 YogFist 的 HP 是 HT，直接加倍血量并提升伤害
                    f.HT = Math.round(f.HT * 1.5f);
                    f.HP = Math.min(f.HP + f.HT / 4, f.HT);
                    any = true;
                }
            }
            if (any) yell( Messages.get(this, "empower1") );

        } else if (alive == 1) {
            Mob last = fists.get(0);
            if (empower.getOrDefault(last, 0) < 2) {
                empower.put(last, 2);
                Buff.prolong( last, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste.class, 999f );
                yell( Messages.get(this, "empower2") );
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  本体：只要还有拳活着就免伤
    // ═══════════════════════════════════════════════
    @Override
    public int defenseProc(Char enemy, int damage) {
        if (!fists.isEmpty()) {
            return 0;
        }
        return super.defenseProc(enemy, damage);
    }

    @Override
    public void die(Object cause) {
        for (Mob f : fists) {
            if (f != null && f.isAlive()) f.die( null );
        }
        fists.clear();
        empower.clear();

        super.die(cause);
        GameScene.bossSlain();
    }

    @Override
    public String name() { return Messages.get(this, "name"); }

    public String title() { return Messages.get(this, "title"); }

    @Override
    public void notice() {
        super.notice();
        yell( Messages.get(this, "notice") );
    }

    // ═══════════════════════════════════════════════
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SUMMONED, summoned);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        summoned = bundle.getBoolean(SUMMONED);
    }
}
