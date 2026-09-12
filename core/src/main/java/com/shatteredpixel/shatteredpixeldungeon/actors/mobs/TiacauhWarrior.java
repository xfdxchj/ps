//END(port from Arknights): TiacauhWarrior
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Tiacauh_warriorSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class TiacauhWarrior
extends Mob {
    public TiacauhWarrior() {
        this.spriteClass = Tiacauh_warriorSprite.class;
        this.HT = 80;
        this.HP = 80;
        this.defenseSkill = 16;
        this.EXP = 14;
        this.maxLvl = 29;
        this.loot = new MysteryMeat();
        this.lootChance = 0.137f;
        this.immunities.add(Silence.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(28, 40);
    }

    @Override
    public int attackSkill(Char target) {
        return 38;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 16);
    }

    @Override
    public void damage(int dmg, Object src) {
        int grassCells = 0;
        for (int i : PathFinder.NEIGHBOURS9) {
            if (Dungeon.level.map[this.pos + i] != 30 && Dungeon.level.map[this.pos + i] != 15) continue;
            ++grassCells;
        }
        if (grassCells > 0) {
            dmg = Math.round((float)dmg * (1.0f - (float)grassCells * 0.04f));
        }
        if (Dungeon.isChallenged(1024)) {
            dmg = (int)((float)dmg * 0.8f);
        }
        if (src == Burning.class) {
            dmg *= 2;
        }
        super.damage(dmg, src);
    }
}
