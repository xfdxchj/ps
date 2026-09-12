package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ExampleBossSprite;
import com.watabou.utils.Random;

/**
 * ═══════════════════════════════════════════════════════════════
 *  示例 Boss —— 一个能跑的最小实现
 * ═══════════════════════════════════════════════════════════════
 *
 * 这个 Boss 演示了：
 *   - 数值设置
 *   - 每回合随机元素攻击（法术王思路）
 *   - 命中附加状态
 *   - 受伤减伤
 *   - 死亡处理
 *
 * 你可以直接复制它改造成六大天王中的任何一个。
 *
 * 配套文件：
 *   sprites/ExampleBossSprite.java
 *   levels/boss/ExampleBossLevel.java
 *   assets/sprites/sixkings/exampleboss.png
 *
 * 文案键（需要加到 actors.properties）：
 *   actors.mobs.bosses.sixkings.exampleboss.name=示例天王
 *   actors.mobs.bosses.sixkings.exampleboss.desc=这是一个用来演示的 Boss。
 *   actors.mobs.bosses.sixkings.exampleboss.notice=让我看看你的实力。
 */
public class ExampleBoss extends Boss {

    {
        spriteClass = ExampleBossSprite.class;

        // ─────────── 数值 ───────────
        HT         = 300;
        EXP        = 25;
        baseHT     = HT;

        baseMin    = 16;
        baseMax    = 26;
        baseAcc    = 32;
        baseEva    = 16;
        baseMinDef = 6;
        baseMaxDef = 13;

        // ─────────── 属性 ───────────
        properties.add(Property.BOSS);

        // ─────────── 阵营 ───────────
        alignment = Alignment.ENEMY;

        // ─────────── 掉落 ───────────
        loot = Generator.Category.WEP_T5;
        lootChance = 1f;

        // ─────────── 自定义状态 ───────────
        skillCd = 2;
    }

    // ═══════════════════════════════════════════════
    //  元素系统
    // ═══════════════════════════════════════════════
    private enum Elem { FIRE, FROST }

    private Elem curElem = Elem.FIRE;
    private int skillCd = 2;

    // ═══════════════════════════════════════════════
    //  每回合 AI
    // ═══════════════════════════════════════════════
    @Override
    protected boolean act() {

        // 冷却好了就放法术
        if (skillCd-- <= 0 && enemy != null && enemySeen) {
            curElem = Elem.values()[Random.Int(Elem.values().length)];
            castSpell();
            skillCd = Random.IntRange(3, 5);   // 下次冷却
            spend(TICK);
            return true;
        }

        return super.act();   // 默认：靠近并攻击
    }

    /** 施法：伤害 + 元素状态 */
    private void castSpell() {
        Char target = enemy;
        if (target == null) return;

        // 视觉反馈
        sprite.flash();

        // 伤害
        int dmg = Random.NormalIntRange(baseMin, baseMax);
        target.damage(dmg, this);

        // 元素附加效果
        if (curElem == Elem.FIRE) {
            Buff.affect(target, Burning.class).reignite(target);
            yell("烈焰！");
        } else {
            Buff.prolong(target, Chill.class, 4f);
            yell("冰封！");
        }
    }

    // ═══════════════════════════════════════════════
    //  攻击命中（附加效果）
    //  注意：本 fork 的 attack(Char) 是 final，用 attackHook
    // ═══════════════════════════════════════════════
    public boolean attackHook(Char enemy) {
        // 命中回一点血
        if (HP < HT) {
            HP = Math.min(HT, HP + 3);
        }
        return false;
    }

    // ═══════════════════════════════════════════════
    //  受伤（减伤）
    // ═══════════════════════════════════════════════
    @Override
    public int defenseProc(Char enemy, int damage) {
        // 减伤 20%
        damage = Math.round(damage * 0.8f);
        return super.defenseProc(enemy, damage);
    }

    // ═══════════════════════════════════════════════
    //  死亡
    // ═══════════════════════════════════════════════
    @Override
    public void die(Object cause) {
        super.die(cause);          // 基类已自动 unseal
        GameScene.bossSlain();
    }

    // ═══════════════════════════════════════════════
    //  名字 / 称号
    // ═══════════════════════════════════════════════
    @Override
    public String name() {
        return Messages.get(this, "name");
    }

    @Override
    public String title() {
        return "六大天王 · 示例";
    }

    @Override
    public void notice() {
        super.notice();
        yell(Messages.get(this, "notice"));
    }

    // ═══════════════════════════════════════════════
    //  存档
    // ═══════════════════════════════════════════════
    private static final String SKILL_CD = "skillCd";
    private static final String CUR_ELEM = "curElem";

    @Override
    public void storeInBundle(com.watabou.utils.Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SKILL_CD, skillCd);
        bundle.put(CUR_ELEM, curElem.name());
    }

    @Override
    public void restoreFromBundle(com.watabou.utils.Bundle bundle) {
        super.restoreFromBundle(bundle);
        skillCd = bundle.getInt(SKILL_CD);
        try {
            curElem = Elem.valueOf(bundle.getString(CUR_ELEM));
        } catch (Exception e) {
            curElem = Elem.FIRE;
        }
    }
}
