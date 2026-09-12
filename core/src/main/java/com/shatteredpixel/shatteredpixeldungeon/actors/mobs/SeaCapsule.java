//END(port from Arknights): SeaCapsule
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NervousImpairment;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Dario;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SanityPotion;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Sea_CrawlerSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class SeaCapsule
extends Mob {
    public SeaCapsule() {
        this.spriteClass = Sea_CrawlerSprite.class;
        this.HT = 180;
        this.HP = 180;
        this.EXP = 17;
        this.maxLvl = 32;
        this.defenseSkill = 10;
        this.loot = new SanityPotion();
        this.lootChance = 1.0f;
        this.properties.add(Char.Property.SEA);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(12, 24);
    }

    @Override
    public int attackSkill(Char target) {
        return 30;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(10, 20);
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; ++i) {
            Char ch = SeaCapsule.findChar(this.pos + PathFinder.NEIGHBOURS8[i]);
            if (ch == null || !ch.isAlive() || ch.alignment != Char.Alignment.ALLY) continue;
            Buff.affect(ch, NervousImpairment.class).sum(20.0f);
        }
        return super.defenseProc(enemy, damage);
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        Dario.Quest.process();
    }

}
