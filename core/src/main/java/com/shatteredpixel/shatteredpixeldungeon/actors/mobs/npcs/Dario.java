//END(port from Arknights): Dario
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.FloatingSeaDrifter;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SeaCapsule;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.SeaReaper;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.SanityPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SeaPlatform;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NPC_DarioSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDario;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.HashSet;

public class Dario
extends NPC {
    private int initialPos;
    private boolean seenBefore;
    private boolean encouraged;
    private boolean completed;
    private static final String INITIAL_POS = "initialPos";

    public Dario() {
        this.spriteClass = NPC_DarioSprite.class;
        this.properties.add(Char.Property.NPC);
        this.intelligentAlly = true;
        this.HT = 1000;
        this.HP = 1000;
        this.alignment = Char.Alignment.ALLY;
        this.WANDERING = new Wandering();
        this.state = this.PASSIVE;
        this.actPriority = -19;
        this.initialPos = -1;
        this.seenBefore = false;
        this.encouraged = false;
        this.completed = false;
    }

    @Override
    public int defenseSkill(Char enemy) {
        return Quest.given && Quest.prepared ? 15 : INFINITE_EVASION;
    }

    @Override
    public boolean canAttack(Char enemy) {
        return Quest.given && Quest.prepared && super.canAttack(enemy);
    }

    @Override
    public int attackSkill(Char target) {
        return 40;
    }

    @Override
    public int attackProc(Char enemy, int damage) {
        if (enemy instanceof Mob) {
            ((Mob)enemy).aggro(this);
        }
        return super.attackProc(enemy, damage);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(20, 47);
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(6, 14);
    }

    @Override
    protected boolean act() {
        if (!Quest.given || !Quest.prepared) {
            if (!this.seenBefore) {
                this.yell(Messages.get(this, "announce"));
            }
            this.seenBefore = true;
        } else {
            this.seenBefore = false;
            if (!this.encouraged && Quest.killCount >= 4) {
                this.yell(Messages.get(this, "almost"));
                this.encouraged = true;
            } else if (this.encouraged && !this.completed && !this.isEnemyInFOV() && Quest.isQuestComplete()) {
                this.yell(Messages.get(this, "complete"));
                this.completed = true;
            }
        }
        return super.act();
    }

    @Override
    public boolean interact(Char c) {
        this.sprite.turnTo(this.pos, c.pos);
        if (c != Dungeon.hero) {
            return true;
        }
        if (Quest.isQuestComplete()) {
            final String msg = Messages.get(this, "thank");
            Game.runOnRenderThread(new Callback(){

                @Override
                public void call() {
                    GameScene.show(new WndDario(Dario.this, msg));
                }
            });
            for (Mob mob : Dungeon.level.mobs) {
                mob.beckon(mob.pos);
            }
        } else if (Quest.given && Quest.prepared) {
            final String msg = Messages.get(this, "reminder");
            Quest.pullMobs(this.pos);
            Game.runOnRenderThread(new Callback(){

                @Override
                public void call() {
                    GameScene.show(new WndQuest(Dario.this, msg));
                }
            });
            super.interact(c);
        } else if (Quest.given) {
            this.yell(Messages.get(this, "start"));
            Quest.prepared = true;
            Quest.startQuest(this.pos);
            Buff.detach(this, Invisibility.class);
            this.state = this.WANDERING;
            this.initialPos = this.pos;
        } else {
            Object msg = "";
            switch (Dungeon.hero.heroClass) {
                case WARRIOR: {
                    msg = (String)msg + Messages.get(this, "intro_warrior");
                    break;
                }
                case ROGUE: {
                    msg = (String)msg + Messages.get(this, "intro_rogue");
                    break;
                }
                case MAGE: {
                    msg = (String)msg + Messages.get(this, "intro_mage", Dungeon.hero.heroClass.title());
                    break;
                }
                case HUNTRESS: {
                    msg = (String)msg + Messages.get(this, "intro_huntress");
                    break;
                }
                //END(移植调整): 原方舟另有三个自定义职业分支（rosecat/nearl/chen），
                //本 fork 的 HeroClass 只有 5 个原版职业，故移除这些分支。
            }
            Object msgFinal = msg = (String)msg + Messages.get(this, "intro");
            Game.runOnRenderThread(new Callback(){
                final /* synthetic */ String val$msgFinal;
                {
                    this.val$msgFinal = " ";
                }

                @Override
                public void call() {
                    GameScene.show(new WndQuest(Dario.this, this.val$msgFinal));
                }
            });
            Quest.given = true;
        }
        return true;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(INITIAL_POS, this.initialPos);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.initialPos = bundle.getInt(INITIAL_POS);
    }

    @Override
    public void die(Object cause) {
        this.yell(Messages.get(this, "die"));
        super.die(cause);
    }

    private boolean isEnemyInFOV() {
        for (Mob mob : Dungeon.level.mobs) {
            if (mob == null || mob.alignment != Char.Alignment.ENEMY || !mob.isAlive() || !this.fieldOfView[mob.pos] || mob.invisible > 0) continue;
            return true;
        }
        return false;
    }

    public void flee() {
        GLog.p(Messages.get(this, "success"));
        this.destroy();
        this.sprite.die();
    }

    private class Wandering
    extends Mob.Wandering {
        private Wandering() {
        }

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            if (enemyInFOV) {
                Dario.this.enemySeen = true;
                Dario.this.notice();
                Dario.this.alerted = true;
                Dario.this.state = Dario.this.HUNTING;
                Dario.this.target = ((Dario)Dario.this).enemy.pos;
            } else {
                Dario.this.enemySeen = false;
                int oldPos = Dario.this.pos;
                Dario.this.target = Dario.this.initialPos;
                if (Dario.this.getCloser(Dario.this.target)) {
                    Dario.this.spend(1.0f / Dario.this.speed());
                    return Dario.this.moveSprite(oldPos, Dario.this.pos);
                }
                Dario.this.spend(1.0f);
            }
            return true;
        }
    }

    public static class Quest {
        private static boolean given;
        private static boolean prepared;
        private static int killCount;
        private static final int KILL_COUNT_GOAL = 7;
        private static final String NODE = "dario";
        private static final String GIVEN = "given";
        private static final String PREPARED = "prepared";
        private static final String KILL_COUNT = "killCount";

        public static void reset() {
            given = false;
            prepared = false;
            killCount = 0;
        }

        public static void storeInBundle(Bundle bundle) {
            Bundle node = new Bundle();
            node.put(GIVEN, given);
            node.put(PREPARED, prepared);
            node.put(KILL_COUNT, killCount);
            bundle.put(NODE, node);
        }

        public static void restoreFromBundle(Bundle bundle) {
            Bundle node = bundle.getBundle(NODE);
            if (!node.isNull()) {
                given = node.getBoolean(GIVEN);
                prepared = node.getBoolean(PREPARED);
                killCount = node.getInt(KILL_COUNT);
            } else {
                Quest.reset();
            }
        }

        public static void startQuest(int pos) {
            Quest.spawnAdditionalMobs(pos);
            killCount = 0;
        }

        public static void pullMobs(int pos) {
            int count = 0;
            for (Mob mob : Dungeon.level.mobs) {
                if (count >= 7 - killCount) break;
                if (mob.isAlive() && (mob.state != mob.SLEEPING || mob.state != mob.FLEEING)) {
                    mob.beckon(pos);
                }
                ++count;
            }
        }

        public static boolean isQuestComplete() {
            if (!given || !prepared) {
                return false;
            }
            return killCount >= 7;
        }

        public static void process() {
            if (given && prepared && killCount < 8) {
                ++killCount;
            }
        }

        public static void spawnDario(Level level, int pos) {
            Dario dario = new Dario();
            dario.pos = pos;
            dario.initialPos = pos;
            dario.seenBefore = false;
            dario.encouraged = false;
            dario.completed = false;
            Buff.append(dario, Invisibility.class, 999.0f);
            level.mobs.add(dario);
        }

        public static void dropReward(Dario dario) {
            ArrayList<Item> rewards = new ArrayList<Item>();
            rewards.add(new PotionOfStrength().quantity(1));
            rewards.add(new ScrollOfUpgrade().quantity(1));
            rewards.add(new SanityPotion().quantity(5));
            rewards.add(new SeaPlatform.LittleHandy().quantity(10));
            for (Item item : rewards) {
                if (item.doPickUp(Dungeon.hero)) {
                    GLog.i(Messages.get(Dungeon.hero, "you_now_have", item.name()));
                    continue;
                }
                Dungeon.level.drop((Item)item, (int)dario.pos).sprite.drop();
            }
        }

        private static void spawnAdditionalMobs(int pos) {
            HashSet<Mob> mobs = new HashSet<Mob>();
            mobs.add(new FloatingSeaDrifter());
            mobs.add(new FloatingSeaDrifter());
            mobs.add(new SeaReaper());
            mobs.add(new SeaReaper());
            mobs.add(new SeaReaper());
            mobs.add(new SeaCapsule());
            mobs.add(new SeaCapsule());
            for (Mob mob : mobs) {
                do {
                    mob.pos = Dungeon.level.randomRespawnCell(mob);
                } while (Dungeon.level.heroFOV[mob.pos]);
                mob.state = mob.WANDERING;
                GameScene.add(mob, Random.Int(1, 10));
                mob.beckon(pos);
            }
        }
    }
}
