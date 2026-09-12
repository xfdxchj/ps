//END(port from Arknights): LavaSlug
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.LavaSlugSprite;
import com.watabou.utils.Random;

public class LavaSlug
extends Mob {
    public LavaSlug() {
        this.spriteClass = LavaSlugSprite.class;
        this.HT = 155;
        this.HP = 155;
        this.defenseSkill = 27;
        this.maxLvl = 34;
        this.EXP = 19;
        this.immunities.add(Silence.class);
        this.immunities.add(Burning.class);
        this.immunities.add(WandOfFireblast.class);
        this.properties.add(Char.Property.INFECTED);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(31, 45);
    }

    @Override
    public int attackSkill(Char target) {
        return 40;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 12);
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (Random.Int(3) == 0) {
            enemy.damage(this.damageRoll() / 3, this);
            Buff.affect(enemy, Burning.class).reignite(enemy, 3.0f);
        }
        if (Dungeon.level.map[enemy.pos] == 29) {
            Level.set(enemy.pos, 1);
            GameScene.updateMap(enemy.pos);
            CellEmitter.get(enemy.pos).burst(Speck.factory(13), 10);
        }
        return super.attackProc(enemy, damage);
    }
}
