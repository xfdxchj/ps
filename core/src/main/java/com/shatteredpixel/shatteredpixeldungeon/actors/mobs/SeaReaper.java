//END(port from Arknights): SeaReaper
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NervousImpairment;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Dario;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SanityPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Sea_ReaperSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class SeaReaper
extends Mob {
    boolean awake;
    boolean firstHit;
    private static final String AWAKE = "awake";
    private static final String FIRST_HIT = "firstHit";

    public SeaReaper() {
        this.spriteClass = Sea_ReaperSprite.class;
        this.HT = 140;
        this.HP = 140;
        this.defenseSkill = 20;
        this.EXP = 15;
        this.maxLvl = 31;
        this.loot = new SanityPotion();
        this.lootChance = 0.1f;
        this.properties.add(Char.Property.SEA);
        this.awake = false;
        this.firstHit = false;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(26, 40);
    }

    @Override
    public int attackSkill(Char target) {
        return 35;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 16);
    }

    @Override
    public float speed() {
        if (this.awake) {
            return super.speed() * 2.0f;
        }
        return super.speed();
    }

    @Override
    public int defenseProc(Char enemy, int damage) {
        if (!this.awake) {
            this.awake = true;
            ((Sea_ReaperSprite)this.sprite).updateChargeState(this.awake);
        }
        return super.defenseProc(enemy, damage);
    }

    @Override
    protected boolean act() {
        boolean isCorrupted;
        if (!this.firstHit) {
            ((Sea_ReaperSprite)this.sprite).updateChargeState(this.awake);
            this.firstHit = true;
        }
        boolean bl = isCorrupted = this.buff(Corruption.class) != null;
        if (this.awake && !isCorrupted) {
            for (int i = 0; i < PathFinder.NEIGHBOURS8.length; ++i) {
                Char ch = SeaReaper.findChar(this.pos + PathFinder.NEIGHBOURS8[i]);
                if (ch == null || !ch.isAlive() || ch.alignment != Char.Alignment.ALLY) continue;
                Buff.affect(ch, NervousImpairment.class).sum(16.0f);
            }
        }
        return super.act();
    }

    @Override
    public void rollToDropLoot() {
        float healChance = 0.17f * RingOfWealth.dropChanceMultiplier(Dungeon.hero);
        if (Dungeon.hero.lvl <= this.maxLvl + 2 && Random.Float() < healChance) {
            Dungeon.level.drop((Item)new PotionOfHealing(), (int)this.pos).sprite.drop();
        }
        super.rollToDropLoot();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(AWAKE, this.awake);
        bundle.put(FIRST_HIT, this.firstHit);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.awake = bundle.getBoolean(AWAKE);
        this.firstHit = bundle.getBoolean(FIRST_HIT);
    }

    @Override
    public void die(Object cause) {
        super.die(cause);
        Dario.Quest.process();
    }

}
