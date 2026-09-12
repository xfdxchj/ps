//END(port from Arknights): Piersailor
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PiersailorSprite;
import com.watabou.utils.Random;

public class Piersailor
extends Mob {
    public Piersailor() {
        this.spriteClass = PiersailorSprite.class;
        this.HT = 180;
        this.HP = 180;
        this.defenseSkill = 0;
        this.maxLvl = 30;
        this.EXP = 15;
        this.immunities.add(Silence.class);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(36, 48);
    }

    @Override
    public int drRoll() {
        if (Dungeon.isChallenged(1024)) {
            return Random.NormalIntRange(4, 24);
        }
        return Random.NormalIntRange(2, 20);
    }

    @Override
    public int attackSkill(Char target) {
        return 35;
    }

    @Override
    protected boolean act() {
        if (Dungeon.level.map[this.pos] == 29 && this.state == this.HUNTING) {
            if (Dungeon.isChallenged(1024)) {
                this.damage(this.HT / 40, this);
            } else {
                this.damage(this.HT / 20, this);
            }
            if (!this.isAlive()) {
                return true;
            }
        }
        return super.act();
    }

    public static Piersailor spawnAt(int pos) {
        if (!Dungeon.level.solid[pos] && Actor.findChar(pos) == null) {
            Piersailor w = new Piersailor();
            w.HP = w.HT / 3;
            w.pos = pos;
            w.state = w.HUNTING;
            GameScene.add(w, 1.0f);
            CellEmitter.get(pos).burst(Speck.factory(7), 4);
            if (Dungeon.level.map[w.pos] == 0) {
                Chasm.mobFall(w);
                ++Statistics.enemiesSlain;
                Badges.validateMonstersSlain();
                Statistics.qualifiedForNoKilling = false;
                if (w.EXP > 0 && Dungeon.hero.lvl <= w.maxLvl) {
                    Dungeon.hero.sprite.showStatus(65280, Messages.get(w, "exp", w.EXP));
                    Dungeon.hero.earnExp(w.EXP, w.getClass());
                } else {
                    Dungeon.hero.earnExp(0, w.getClass());
                }
            }
            return w;
        }
        return null;
    }
}
