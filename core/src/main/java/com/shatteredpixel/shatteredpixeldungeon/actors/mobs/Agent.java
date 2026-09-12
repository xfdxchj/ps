//END(port from Arknights): Agent
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ceylon;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Siesta_AgentSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class Agent
extends Mob {
    public Agent() {
        this.spriteClass = Siesta_AgentSprite.class;
        this.HT = 125;
        this.HP = 125;
        this.defenseSkill = 26;
        this.EXP = 16;
        this.maxLvl = 31;
        this.loot = Generator.Category.WEAPON;
        this.lootChance = 0.1f;
        this.immunities.add(Charm.class);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (this.buff(Silence.class) == null && Random.Int(3) < 1) {
            float time = 5.0f;
            if (Dungeon.isChallenged(1024)) {
                time = 10.0f;
            }
            Buff.affect(enemy, Hex.class, time);
            Buff.affect(enemy, Vulnerable.class, time);
        }
        return super.attackProc(enemy, damage);
    }

    //END(移植调整): 本 fork 的 Char.attack(Char) 是 final，无法覆写。
    //原方舟在攻击里插入特殊逻辑，这里改为辅助方法 attackHook，由子类逻辑自行调用。
    public boolean attackHook(Char enemy) {
        boolean visibleFight;
        if (enemy == null) {
            return false;
        }
        boolean bl = visibleFight = Dungeon.level.heroFOV[this.pos] || Dungeon.level.heroFOV[enemy.pos];
        if (enemy.isInvulnerable(this.getClass())) {
            if (visibleFight) {
                enemy.sprite.showStatus(65280, Messages.get(this, "invulnerable"));
                Sample.INSTANCE.play("sounds/hit_parry.mp3", 1.0f, Random.Float(0.96f, 1.05f));
            }
            return false;
        }
        if (Agent.hit(this, enemy, true)) {
            int dmg = this.damageRoll();
            int effectiveDamage = enemy.defenseProc(this, dmg);
            if (enemy.buff(Vulnerable.class) != null) {
                effectiveDamage = Math.round((float)effectiveDamage * 1.33f);
            }
            effectiveDamage = this.attackProc(enemy, effectiveDamage);
            if (visibleFight && (effectiveDamage > 0 || !enemy.blockSound(Random.Float(0.96f, 1.05f)))) {
                this.hitSound(Random.Float(0.87f, 1.15f));
            }
            if (!enemy.isAlive()) {
                return true;
            }
            enemy.damage(effectiveDamage, this);
            if (enemy.sprite != null) {
                enemy.sprite.bloodBurstA(this.sprite.center(), effectiveDamage);
                enemy.sprite.flash();
            }
            if (!enemy.isAlive() && visibleFight && enemy == Dungeon.hero) {
                Dungeon.fail(this.getClass());
                GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", this.name())));
            }
            return true;
        }
        if (visibleFight) {
            String defense = enemy.defenseVerb();
            enemy.sprite.showStatus(0xFFFF00, defense);
            Sample.INSTANCE.play("sounds/miss.mp3");
        }
        return false;
    }

    @Override
    public int damageRoll() {
        if (this.buff(Silence.class) != null) {
            return Random.NormalIntRange(14, 26);
        }
        return Random.NormalIntRange(22, 31);
    }

    @Override
    public int attackSkill(Char target) {
        return 42;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 20);
    }

    @Override
    public void rollToDropLoot() {
        Ceylon.Quest.process(this);
        super.rollToDropLoot();
    }
}
