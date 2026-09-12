//END(port from Arknights): Wraith_donut
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CustomeSet;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Wraith_donutSprite;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Wraith_donut
extends Mob {
    private static final float SPAWN_DELAY = 2.0f;
    private int level;
    private static final String LEVEL = "level";

    public Wraith_donut() {
        this.spriteClass = Wraith_donutSprite.class;
        this.HT = 1;
        this.HP = 1;
        this.EXP = 0;
        this.maxLvl = -2;
        this.flying = false;
        this.immunities.add(Silence.class);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LEVEL, this.level);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.level = bundle.getInt(LEVEL);
        this.adjustStats(this.level);
    }

    @Override
    public int damageRoll() {
        CustomeSet.CustomSetBuff setBuff = Dungeon.hero.buff(CustomeSet.CustomSetBuff.class);
        int itembuff = 0;
        if (setBuff != null) {
            itembuff = setBuff.itemLevel();
        }
        return Random.NormalIntRange(1 + this.level / 2, 2 + this.level + itembuff);
    }

    @Override
    public int attackSkill(Char target) {
        CustomeSet.CustomSetBuff setBuff = Dungeon.hero.buff(CustomeSet.CustomSetBuff.class);
        int itembuff = 0;
        if (setBuff != null) {
            itembuff = setBuff.itemLevel();
        }
        return 10 + this.level + itembuff;
    }

    public void adjustStats(int level) {
        this.level = level;
        this.defenseSkill = this.attackSkill(null) * 5;
        this.enemySeen = true;
    }

    @Override
    public float spawningWeight() {
        return 0.0f;
    }

    @Override
    public boolean reset() {
        this.state = this.WANDERING;
        return true;
    }

    public static void spawnAround(int pos) {
        for (int n : PathFinder.NEIGHBOURS4) {
            Wraith_donut.spawnAt(pos + n);
        }
    }

    public static Wraith_donut spawnAt(int pos) {
        if (!Dungeon.level.solid[pos] && Actor.findChar(pos) == null) {
            Wraith_donut w = new Wraith_donut();
            w.adjustStats(Dungeon.depth);
            w.pos = pos;
            w.state = w.HUNTING;
            GameScene.add(w, 2.0f);
            w.sprite.alpha(0.0f);
            w.sprite.parent.add(new AlphaTweener(w.sprite, 1.0f, 0.5f));
            w.sprite.emitter().burst(ShadowParticle.CURSE, 5);
            return w;
        }
        return null;
    }
}
