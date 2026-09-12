//END(port from Arknights): SiestaBoss
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Drowsy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Agent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Schwarz;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith_donut;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.NewGameItem.Certificate;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CroninSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class SiestaBoss
extends Mob {
    private static final String[] LINE_KEYS = new String[]{"skill1", "skill2"};
    private int phase;
    private int Life;
    private int TelType;
    private BossAgent Agent1;
    private BossAgent Agent2;
    private Schwarz mySchwarz;
    private int Agent1Id;
    private int Agent2Id;
    private int SchwarzId;
    public static int[] TelPos = new int[]{35, 49, 71, 81, 93, 124, 130, 174, 182, 226, 232, 263, 275, 285, 307, 321};
    private static final String PHASE = "phase";
    private static final String LIFE = "Life";
    private static final String TTYPE = "TelType";
    private static final String AGENT1 = "Agent1";
    private static final String AGENT2 = "Agent2";
    private static final String SCHWARZ = "mySchwarz";

    public SiestaBoss() {
        this.spriteClass = CroninSprite.class;
        this.HT = 1200;
        this.HP = 1200;
        this.defenseSkill = 0;
        this.EXP = 40;
        this.state = this.HUNTING;
        this.baseSpeed = 0.0f;
        this.properties.add(Char.Property.BOSS);
        this.properties.add(Char.Property.IMMOVABLE);
        this.immunities.add(Amok.class);
        this.immunities.add(Terror.class);
        this.immunities.add(Silence.class);
        this.immunities.add(TalismanOfForesight.CharAwareness.class);
        this.phase = 0;
        this.Life = 5;
        this.TelType = 0;
    }

    @Override
    protected Char chooseEnemy() {
        return null;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 10);
    }

    @Override
    protected boolean act() {
        TalismanOfForesight.CharAwareness b;
        if (Dungeon.hero != null && (b = Dungeon.hero.buff(TalismanOfForesight.CharAwareness.class)) != null && b.charID == this.id()) {
            Buff.detach(Dungeon.hero, b.getClass());
        }
        if (this.phase == 0) {
            if (Dungeon.hero.viewDistance >= Dungeon.level.distance(this.pos, Dungeon.hero.pos)) {
                Dungeon.observe();
            }
            if (Dungeon.level.heroFOV[this.pos]) {
                this.Agent1Id = -1;
                this.Agent2Id = -1;
                this.SchwarzId = -1;
                this.notice();
            }
        }
        if (this.phase == 0) {
            this.mySchwarz = new Schwarz();
            this.SchwarzId = this.mySchwarz.id();
            GameScene.add(this.mySchwarz);
            this.damage(1, this);
            this.spend(1.0f);
            ++this.phase;
            return true;
        }
        return super.act();
    }

    @Override
    public void move(int step, boolean travelling) {
    }

    @Override
    public void damage(int dmg, Object src) {
        LockedFloor lock;
        int hpBracket = 200;
        int beforeHitHP = this.HP;
        super.damage(dmg, src);
        dmg = beforeHitHP - this.HP;
        if (beforeHitHP / hpBracket - this.HP / hpBracket >= 2) {
            this.HP = hpBracket * (beforeHitHP / hpBracket - 1) + 1;
        }
        if (this.phase == 1 && this.HP <= this.HT / 2) {
            this.yell(Messages.get(this, "phase2"));
            ++this.phase;
        }
        if (this.isAlive() && beforeHitHP / hpBracket != this.HP / hpBracket) {
            this.Skill();
            --this.Life;
        }
        if ((lock = Dungeon.hero.buff(LockedFloor.class)) != null) {
            lock.addTime((float)dmg * 0.3f);
        }
    }

    @Override
    public boolean isAlive() {
        return this.HP > 0 || this.Life > 0;
    }

    @Override
    public void notice() {
        if (this.phase == 0) {
            this.yell(Messages.get(this, "notice"));
        }
        if (!BossHealthBar.isAssigned()) {
            BossHealthBar.assignBoss(this);
        }
    }

    @Override
    public void die(Object cause) {
        Bestiary.skipCountingEncounters = true;
        for (Mob mob : new java.util.ArrayList<Mob>(Dungeon.level.mobs)) {
            if (mob instanceof BossAgent || mob instanceof Schwarz) {
                mob.die(cause);
            }
            Badges.validatesiesta1();
        }
        Bestiary.skipCountingEncounters = false;
        this.yell(Messages.get(this, "defeated"));
        Dungeon.level.drop((Item)new Certificate((int)40), (int)this.pos).sprite.drop(this.pos);
        GameScene.bossSlain();
        Dungeon.level.unseal();
        super.die(cause);
    }

    private void Skill() {
        int A1pos;
        int A2pos;
        int SwPos;
        int ThisPos;
        for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
            if (!(m instanceof MirrorImage) && !(m instanceof Wraith_donut)) continue;
            m.die(null);
        }
        switch (this.TelType) {
            default: {
                ThisPos = TelPos[Random.IntRange(0, 3)];
                SwPos = TelPos[Random.IntRange(4, 7)];
                A2pos = TelPos[Random.IntRange(8, 11)];
                A1pos = TelPos[Random.IntRange(12, 15)];
                break;
            }
            case 1: {
                A2pos = TelPos[Random.IntRange(0, 3)];
                A1pos = TelPos[Random.IntRange(4, 7)];
                ThisPos = TelPos[Random.IntRange(8, 11)];
                SwPos = TelPos[Random.IntRange(12, 15)];
                break;
            }
            case 2: {
                A1pos = TelPos[Random.IntRange(0, 3)];
                A2pos = TelPos[Random.IntRange(4, 7)];
                SwPos = TelPos[Random.IntRange(8, 11)];
                ThisPos = TelPos[Random.IntRange(12, 15)];
                break;
            }
            case 3: {
                SwPos = TelPos[Random.IntRange(0, 3)];
                ThisPos = TelPos[Random.IntRange(4, 7)];
                A1pos = TelPos[Random.IntRange(8, 11)];
                A2pos = TelPos[Random.IntRange(12, 15)];
            }
        }
        if (!(this.Agent1 == null || this.Agent1.isAlive() && Dungeon.level.mobs.contains(this.Agent1) && this.Agent1.alignment == this.alignment)) {
            this.Agent1 = null;
            this.Agent1Id = -1;
        }
        if (!(this.Agent2 == null || this.Agent2.isAlive() && Dungeon.level.mobs.contains(this.Agent2) && this.Agent2.alignment == this.alignment)) {
            this.Agent2 = null;
            this.Agent2Id = -1;
        }
        if (this.Agent1 == null) {
            if (this.Agent1Id != -1) {
                Actor ch = Actor.findById(this.Agent1Id);
                if (ch instanceof BossAgent) {
                    this.Agent1 = (BossAgent)ch;
                }
                if (ch != null) {
                    this.Agent1.pos = A1pos;
                    this.Agent1.sprite.move(this.pos, A1pos);
                } else {
                    this.Agent1 = new BossAgent();
                    this.Agent1.pos = A1pos;
                    this.Agent1Id = this.Agent1.id();
                    GameScene.add(this.Agent1);
                }
            } else {
                this.Agent1 = new BossAgent();
                this.Agent1.pos = A1pos;
                this.Agent1Id = this.Agent1.id();
                GameScene.add(this.Agent1);
            }
        } else {
            this.Agent1.pos = A1pos;
            this.Agent1.sprite.move(this.pos, A1pos);
        }
        if (this.Agent2 == null) {
            if (this.Agent2Id != -1) {
                Actor ch2 = Actor.findById(this.Agent2Id);
                if (ch2 instanceof BossAgent) {
                    this.Agent2 = (BossAgent)ch2;
                }
                if (ch2 != null) {
                    this.Agent2.pos = A2pos;
                    this.Agent2.sprite.move(this.pos, A2pos);
                } else {
                    this.Agent2 = new BossAgent();
                    this.Agent2.pos = A2pos;
                    this.Agent2Id = this.Agent2.id();
                    GameScene.add(this.Agent2);
                }
            } else {
                this.Agent2 = new BossAgent();
                this.Agent2.pos = A2pos;
                this.Agent2Id = this.Agent2.id();
                GameScene.add(this.Agent2);
            }
        } else {
            this.Agent2.pos = A2pos;
            this.Agent2.sprite.move(this.pos, A2pos);
        }
        Actor Sch = Actor.findById(this.SchwarzId);
        if (Sch instanceof Schwarz) {
            this.mySchwarz = (Schwarz)Sch;
        }
        this.mySchwarz.pos = SwPos;
        this.mySchwarz.sprite.move(this.pos, SwPos);
        if (this.phase == 2) {
            Buff.affect(this.Agent1, Stamina.class, 30.0f);
            Buff.affect(this.Agent2, Stamina.class, 30.0f);
            if (this.mySchwarz != null && this.mySchwarz.Phase != 2) {
                this.mySchwarz.Phase = 2;
            }
        }
        if (Dungeon.siesta1_bosspower > 3) {
            Buff.affect(Dungeon.hero, Paralysis.class, 2.0f);
        }
        if (Dungeon.siesta1_bosspower > 2) {
            Buff.affect(Dungeon.hero, Slow.class, 4.0f);
        }
        if (Dungeon.siesta1_bosspower > 1) {
            Buff.affect(Dungeon.hero, Blindness.class, 5.0f);
        }
        if (Dungeon.siesta1_bosspower > 0) {
            Buff.affect(Dungeon.hero, Burning.class).reignite(Dungeon.hero);
        }
        this.pos = ThisPos;
        this.sprite.place(this.pos);
        this.yell(Messages.get(this, Random.element(LINE_KEYS)));
        GameScene.flash(-2130706433);
        Dungeon.observe();
        GameScene.updateFog();
        this.TelType = this.TelType < 3 ? ++this.TelType : Random.IntRange(0, 4);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, this.phase);
        bundle.put(LIFE, this.Life);
        bundle.put(TTYPE, this.TelType);
        if (this.Agent1Id != -1) {
            bundle.put(AGENT1, this.Agent1Id);
        } else {
            bundle.put(AGENT1, -1);
        }
        if (this.Agent2Id != -1) {
            bundle.put(AGENT2, this.Agent2Id);
        } else {
            bundle.put(AGENT2, -1);
        }
        bundle.put(SCHWARZ, this.SchwarzId);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.phase = bundle.getInt(PHASE);
        this.Life = bundle.getInt(LIFE);
        this.TelType = bundle.getInt(TTYPE);
        if (this.phase != 0) {
            BossHealthBar.assignBoss(this);
        }
        if (bundle.contains(AGENT1)) {
            this.Agent1Id = bundle.getInt(AGENT1);
        }
        if (bundle.contains(AGENT2)) {
            this.Agent2Id = bundle.getInt(AGENT2);
        }
        if (bundle.contains(SCHWARZ)) {
            this.SchwarzId = bundle.getInt(SCHWARZ);
        }
    }

    public static class BossAgent
    extends Agent {
        public BossAgent() {
            this.HP = 70;
            this.HT = 70;
            this.state = this.HUNTING;
            this.immunities.add(Drowsy.class);
            this.immunities.add(MagicalSleep.class);
            this.immunities.add(AllyBuff.class);
            this.maxLvl = -5;
        }

        @Override
        public int damageRoll() {
            if (this.buff(Silence.class) != null) {
                return Random.NormalIntRange(11, 21);
            }
            return Random.NormalIntRange(18, 26);
        }
    }
}
