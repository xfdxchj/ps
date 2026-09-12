package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.bosses.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Boss;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SorcererKingSprite;
import com.watabou.utils.Random;

/**
 * ═══════════════════════════════════════════════════════════════
 *  Boss 模板 —— 复制本文件并改名即可开始设计
 * ═══════════════════════════════════════════════════════════════
 *
 * 改名前请全局替换：
 *   SorcererKing  -> 你的 Boss 类名
 *   SorcererKingSprite -> 你的精灵类名
 *   sorcererking  -> 你的贴图/文案键名（全小写）
 *
 * 文案键（写进 assets/messages/actors/actors.properties）:
 *   actors.mobs.bosses.sixkings.sorcererking.name   = 显示名
 *   actors.mobs.bosses.sixkings.sorcererking.desc   = 图鉴描述
 *   actors.mobs.bosses.sixkings.sorcererking.notice = 登场台词
 *
 * ⚠️ 键名必须是【完整包路径 + 类名小写】，否则显示 NO TEXT FOUND
 */
public class SorcererKing extends Boss {

    {
        spriteClass = SorcererKingSprite.class;

        // ─────────── 数值（按需调整）───────────
        HT          = 300;      // 生命上限
        EXP         = 30;       // 击杀经验
        baseHT      = HT;       // 内部基准（不要改）

        baseMin     = 18;       // 伤害下限
        baseMax     = 28;       // 伤害上限
        baseAcc     = 32;       // 命中
        baseEva     = 18;       // 闪避
        baseMinDef  = 6;        // 防御下限
        baseMaxDef  = 14;       // 防御上限

        // ─────────── 属性 ───────────
        properties.add(Property.BOSS);      // 必加：Boss 血条 + 免疫即死
        properties.add(Property.DEMONIC);   // 可选，见 README 的属性表
        // 其他常用：LARGE / IMMOVABLE / STATIC / UNDEAD / INORGANIC
        //          FIERY / ICY / ACIDIC / ELECTRIC / DRONE / SEA / INFECTED

        // ─────────── 阵营 ───────────
        alignment = Alignment.ENEMY;

        // ─────────── 掉落 ───────────
        loot = Generator.Category.WEP_T5;   // 也可写具体类，如 ScrollOfUpgrade.class
        lootChance = 1f;                    // 100% 掉落

        // ─────────── 自定义字段 ───────────
        // 例：技能冷却计数器
        skillCooldown = 3;
    }

    /** 示例：技能冷却 */
    private int skillCooldown = 3;

    // ═══════════════════════════════════════════════
    //  每回合的 AI 逻辑
    // ═══════════════════════════════════════════════
    //
    //  返回 true  = 本回合已消耗（不再走默认逻辑）
    //  返回 false = 继续走父类默认逻辑（靠近/攻击）
    //
    @Override
    protected boolean act() {

        // 示例：冷却好了就放技能
        if (skillCooldown-- <= 0 && enemy != null && enemySeen) {
            castSpell();
            skillCooldown = Random.IntRange(3, 6);   // 下次冷却
            spend(TICK);                             // 消耗一回合
            return true;
        }

        return super.act();   // 默认：靠近并攻击
    }

    /** 示例技能：对一个目标造成伤害 + 附加状态 */
    private void castSpell() {
        Char target = enemy;
        if (target == null) return;

        // 1) 台词 / 视觉提示
        //    yell(...) 会在头顶弹字
        //    sprite.flash() 闪白

        // 2) 造成伤害
        int dmg = Random.NormalIntRange(15, 30);
        target.damage(dmg, this);

        // 3) 附加 buff（可选）
        // Buff.affect(target, Burning.class).reignite(target);
        // Buff.prolong(target, Weakness.class, 5f);      // 注意：只对 FlavourBuff 有效

        // 4) 特效（可选）
        // CellEmitter.get(target.pos).burst(FlameParticle.FACTORY, 10);
    }

    // ═══════════════════════════════════════════════
    //  攻击命中时（附加效果）
    // ═══════════════════════════════════════════════
    //  ⚠️ 本 fork 的 Char.attack() 是 final，**不能覆写**！
    //     所以用 attackHook()。
    public boolean attackHook(Char enemy) {
        // 例：命中后吸血
        // int heal = Math.min(enemy.HP, 5);
        // HP = Math.min(HT, HP + heal);

        // 例：命中后减速
        // Buff.prolong(enemy, Slow.class, 3f);

        return false;
    }

    // ═══════════════════════════════════════════════
    //  受到伤害时（减伤 / 反伤 / 变身）
    // ═══════════════════════════════════════════════
    @Override
    public int defenseProc(Char enemy, int damage) {

        // 例 1：减伤 30%
        // damage = Math.round(damage * 0.7f);

        // 例 2：反伤
        // if (enemy != null) enemy.damage(Random.NormalIntRange(2, 5), this);

        // 例 3：三阶段变身（全能王用）
        // int newPhase = (HP * 3) / HT + 1;
        // if (newPhase != phase) { phase = newPhase; onPhaseChange(); }

        return super.defenseProc(enemy, damage);
    }

    // ═══════════════════════════════════════════════
    //  死亡
    // ═══════════════════════════════════════════════
    @Override
    public void die(Object cause) {
        // 基类 Boss.die() 已自动调用 Dungeon.level.unseal()，
        // 不要重复 unseal，否则可能出问题。
        super.die(cause);

        GameScene.bossSlain();      // Boss 血条消失 + 提示
        // 例：额外掉落
        // Dungeon.level.drop(new ScrollOfUpgrade(), pos).sprite.drop();
    }

    // ═══════════════════════════════════════════════
    //  称号（Boss 血条上显示的名字）
    // ═══════════════════════════════════════════════
    @Override
    public String name() {
        return Messages.get(this, "name");
    }

    /** Boss 血条上方的小字（可返回 null 表示不显示）。 */
    @Override
    public String title() {
        return null;
        // 例：return "六大天王 · 法术";
    }

    // ═══════════════════════════════════════════════
    //  登场提示（玩家第一次看到时打印）
    // ═══════════════════════════════════════════════
    @Override
    public void notice() {
        super.notice();
        // yell(Messages.get(this, "notice"));
    }

    // ═══════════════════════════════════════════════
    //  存档（如果加了自定义字段，必须实现这两个）
    // ═══════════════════════════════════════════════
    // private static final String SKILL_CD = "skillCooldown";
    //
    // @Override
    // public void storeInBundle(com.watabou.utils.Bundle bundle) {
    //     super.storeInBundle(bundle);
    //     bundle.put(SKILL_CD, skillCooldown);
    // }
    //
    // @Override
    // public void restoreFromBundle(com.watabou.utils.Bundle bundle) {
    //     super.restoreFromBundle(bundle);
    //     skillCooldown = bundle.getInt(SKILL_CD);
    // }
}
