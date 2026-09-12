//END(port from Arknights): Eunectes
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.TheBigUglyThing;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Jumama_BossSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.BArray;
import com.watabou.noosa.Camera;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Eunectes
extends Mob {
    private boolean isBarrier;
    private static final String BARRIER = "isBarrier";

    public Eunectes() {
        this.spriteClass = Jumama_BossSprite.class;
        this.HP = 800;
        this.HT = 800;
        this.defenseSkill = 25;
        this.state = this.HUNTING;
        this.properties.add(Char.Property.BOSS);
        this.properties.add(Char.Property.INFECTED);
        this.immunities.add(Silence.class);
        this.isBarrier = false;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(48, 60);
    }

    @Override
    public int attackSkill(Char target) {
        return 45;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 24);
    }

    @Override
    public void damage(int dmg, Object src) {
        LockedFloor lock;
        if (dmg > this.HT / 2) {
            dmg = this.HT / 2;
        }
        super.damage(dmg, src);
        if (this.HT / 2 >= this.HP && !this.isBarrier) {
            Buff.affect(this, Barrier.class).setShield(400);
            this.isBarrier = true;
        }
        if ((lock = Dungeon.hero.buff(LockedFloor.class)) != null) {
            lock.addTime((float)dmg * 0.3f);
        }
    }

    @Override
    protected boolean act() {
        if (this.buff(Barrier.class) != null) {
            this.HP = Math.min(this.HP + 8, this.HT);
        }
        return super.act();
    }

    @Override
    public void notice() {
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
            this.yell(Messages.get(this, "notice"));
        }
    }

    @Override
    public void die(Object cause) {
        int thispos = this.pos;
        Buff.affect(Dungeon.hero, TBUTCount.class, 3.0f).setpoint(thispos);
        super.die(cause);
        this.yell(Messages.get(this, "die"));
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(BARRIER, this.isBarrier);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.isBarrier = bundle.getBoolean(BARRIER);
    }

    public static class TBUTCount
    extends FlavourBuff {
        int pos;
        private static final String POS = "pos";

        public void setpoint(int n) {
            this.pos = n;
        }

        @Override
        public void detach() {
            TheBigUglyThing newboss = new TheBigUglyThing();
            newboss.pos = this.pos;
            GameScene.flash(-2130706433);
            Camera.main.shake(2.0f, 2.0f);
            if (Actor.findChar(this.pos) != null) {
                int pushPos = this.pos;
                for (int c : PathFinder.NEIGHBOURS8) {
                    if (Actor.findChar(this.pos + c) != null || !Dungeon.level.passable[this.pos + c] || !Dungeon.level.openSpace[this.pos + c] && Char.hasProp(Actor.findChar(this.pos), Char.Property.LARGE) || !(Dungeon.level.trueDistance(this.pos, this.pos + c) > Dungeon.level.trueDistance(this.pos, pushPos))) continue;
                    pushPos = this.pos + c;
                }
                if (pushPos != this.pos) {
                    Char ch = Actor.findChar(this.pos);
                    Actor.addDelayed(new Pushing(ch, ch.pos, pushPos), -1.0f);
                    ch.pos = pushPos;
                    Dungeon.level.occupyCell(ch);
                }
            }
            GameScene.add(newboss);
            PathFinder.buildDistanceMap(this.pos, BArray.not(Dungeon.level.solid, null), 3);
            for (int i = 0; i < PathFinder.distance.length; ++i) {
                int vol;
                if (PathFinder.distance[i] >= Integer.MAX_VALUE || (vol = Fire.volumeAt(i, Fire.class)) >= 3) continue;
                CellEmitter.get(i).burst(EarthParticle.FALLING, 5);
            }
            super.detach();
        }

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(POS, this.pos);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            this.pos = bundle.getInt(POS);
        }
    }
}
