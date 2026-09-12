//END(port from Arknights): TiacauhAddict
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hallucination;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tiacauh_DrugsSprite;
import com.watabou.utils.Random;

public class TiacauhAddict
extends Mob {
    public TiacauhAddict() {
        this.spriteClass = Tiacauh_DrugsSprite.class;
        this.baseSpeed = 2.0f;
        this.HT = 100;
        this.HP = 100;
        this.defenseSkill = 20;
        this.EXP = 14;
        this.maxLvl = 32;
        this.loot = Generator.Category.WEAPON;
        this.lootChance = 0.1f;
        this.immunities.add(Silence.class);
        this.immunities.add(ToxicGas.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(36, 46);
    }

    @Override
    public int attackSkill(Char target) {
        return 42;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 14);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (Random.Int(2) == 0) {
            Buff.affect(enemy, Weakness.class, 4.0f);
            Buff.affect(enemy, Vulnerable.class, 4.0f);
        }
        if (Dungeon.isChallenged(1024) && Random.Int(5) == 0) {
            Buff.affect(enemy, Hallucination.class).set(3.0f);
        }
        return super.attackProc(enemy, damage);
    }

    @Override
    public void damage(int dmg, Object src) {
        if (src == Burning.class) {
            dmg *= 2;
        }
        super.damage(dmg, src);
    }
}
