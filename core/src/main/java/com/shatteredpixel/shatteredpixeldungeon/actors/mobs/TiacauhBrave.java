//END(port from Arknights): TiacauhBrave
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tiacauh_BraveSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class TiacauhBrave
extends Mob {
    private boolean isAttack;
    private static final String ATTACK = "isAttack";

    public TiacauhBrave() {
        this.spriteClass = Tiacauh_BraveSprite.class;
        this.HT = 145;
        this.HP = 145;
        this.defenseSkill = 16;
        this.EXP = 20;
        this.maxLvl = 37;
        this.loot = Generator.Category.SCROLL;
        this.lootChance = 0.4f;
        this.immunities.add(Silence.class);
        this.immunities.add(Terror.class);
        this.isAttack = false;
    }

    @Override
    public int damageRoll() {
        if (Dungeon.isChallenged(1024)) {
            return Random.NormalIntRange(44, 57);
        }
        return Random.NormalIntRange(35, 57);
    }

    @Override
    public int attackSkill(Char target) {
        return 47;
    }

    @Override
    public int drRoll() {
        if (!this.isAttack) {
            return Random.NormalIntRange(20, 50);
        }
        return Random.NormalIntRange(0, 18);
    }

    @Override
    public void damage(int dmg, Object src) {
        if (src == Burning.class) {
            dmg *= 2;
        }
        super.damage(dmg, src);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (!this.isAttack) {
            this.isAttack = true;
            if (Dungeon.isChallenged(1024)) {
                Buff.affect(enemy, Hex.class, 5.0f);
            }
            Buff.affect(enemy, Vulnerable.class, 3.0f);
        }
        return super.attackProc(enemy, damage);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ATTACK, this.isAttack);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.isAttack = bundle.getBoolean(ATTACK);
    }
}
