//END(port from Arknights): TiacauhShredder
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tiacauh_ShredderSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class TiacauhShredder
extends Mob {
    public TiacauhShredder() {
        this.spriteClass = Tiacauh_ShredderSprite.class;
        this.HT = 150;
        this.HP = 150;
        this.defenseSkill = 20;
        this.EXP = 15;
        this.maxLvl = 35;
        this.loot = Generator.Category.WEAPON;
        this.lootChance = 0.24f;
        this.immunities.add(Silence.class);
    }

    @Override
    protected boolean act() {
        if (this.buff(Burning.class) != null) {
            this.damage(Random.IntRange(36, 48), Burning.class);
            if (!this.isAlive()) {
                return true;
            }
        }
        return super.act();
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(38, 48);
    }

    @Override
    public int attackSkill(Char target) {
        return 40;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 20);
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
        int grassCells = 0;
        for (int i : PathFinder.NEIGHBOURS9) {
            if (Dungeon.level.map[this.pos + i] != 30 && Dungeon.level.map[this.pos + i] != 15) continue;
            ++grassCells;
        }
        if (grassCells > 0) {
            damage = Math.round((float)damage * (1.0f + (float)grassCells * 0.04f));
        }
        return super.attackProc(enemy, damage);
    }
}
