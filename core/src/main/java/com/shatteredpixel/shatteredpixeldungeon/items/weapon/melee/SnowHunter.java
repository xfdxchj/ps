//END(port from Arknights): SnowHunter
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Door;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class SnowHunter
extends MeleeWeapon {
    public static final String AC_ZAP = "ZAP";
    private boolean swiching;
    private static final String SWICH = "swiching";

    public SnowHunter() {
        this.image = ItemSpriteSheet.CLIFF;
        this.hitSound = "sounds/hit_whip.mp3";
        this.hitSoundPitch = 0.87f;
        this.defaultAction = AC_ZAP;
        this.tier = 4;
        this.RCH = 5;
        this.swiching = false;
    }

    @Override
    public int max(int lvl) {
        return 4 * this.tier + lvl * this.tier;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ZAP);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_ZAP)) {
            this.swiching = !this.swiching;
            SnowHunter.updateQuickslot();
        }
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (!this.swiching) {
            Ballistica trajectory = new Ballistica(attacker.pos, defender.pos, 1);
            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), 7);
            this.moveChar(attacker, trajectory, 1, defender.pos, false, false);
        } else if (this.swiching) {
            Ballistica chain = new Ballistica(defender.pos, attacker.pos, 1);
            if (Actor.findChar(chain.collisionPos) != null) {
                SnowHunter.chainEnemy(chain, defender);
            }
        }
        return super.proc(attacker, defender, damage);
    }

    private void moveChar(final Char ch, Ballistica trajectory, int power, int enemypos, final boolean closeDoors, boolean collideDmg) {
        int dist;
        boolean collided;
        if (ch.properties().contains((Object)Char.Property.BOSS)) {
            power /= 2;
        }
        boolean bl = collided = (dist = Math.min(trajectory.dist, power)) == trajectory.dist;
        if (dist == 0 || ch.properties().contains((Object)Char.Property.IMMOVABLE)) {
            return;
        }
        if (Char.hasProp(ch, Char.Property.LARGE)) {
            for (int i = 1; i <= dist; ++i) {
                if (Dungeon.level.openSpace[trajectory.path.get(i)]) continue;
                dist = i - 1;
                collided = true;
                break;
            }
        }
        if (Actor.findChar(trajectory.path.get(dist)) != null) {
            --dist;
            collided = true;
        }
        if (dist < 0) {
            return;
        }
        final int newPos = trajectory.path.get(dist);
        if (newPos == enemypos) {
            return;
        }
        final int finalDist = dist;
        final boolean finalCollided = collided && collideDmg;
        final int initialpos = ch.pos;
        Actor.addDelayed(new Pushing(ch, ch.pos, newPos, new Callback(){

            @Override
            public void call() {
                if (initialpos != ch.pos) {
                    ch.sprite.place(ch.pos);
                    return;
                }
                int oldPos = ch.pos;
                ch.pos = newPos;
                if (finalCollided && ch.isActive()) {
                    ch.damage(Random.NormalIntRange(finalDist, 2 * finalDist), this);
                    if (ch.isActive()) {
                        Paralysis.prolong(ch, Paralysis.class, 1.0f + (float)finalDist / 2.0f);
                    }
                }
                if (closeDoors && Dungeon.level.map[oldPos] == 6) {
                    Door.leave(oldPos);
                }
                Dungeon.level.occupyCell(ch);
                if (ch == Dungeon.hero) {
                    Dungeon.observe();
                }
            }
        }), -1.0f);
    }

    public static void chainEnemy(Ballistica chain, final Char enemy) {
        if (enemy.properties().contains((Object)Char.Property.IMMOVABLE)) {
            return;
        }
        int bestPos = -1;
        for (int i : chain.subPath(1, chain.dist)) {
            if (Dungeon.level.solid[i] || Actor.findChar(i) != null || Char.hasProp(enemy, Char.Property.LARGE) && !Dungeon.level.openSpace[i]) continue;
            bestPos = i;
            break;
        }
        if (bestPos == -1) {
            return;
        }
        final int pulledPos = bestPos;
        Actor.add(new Pushing(enemy, enemy.pos, pulledPos, new Callback(){

            @Override
            public void call() {
                enemy.sprite.move(enemy.pos, pulledPos);
                enemy.pos = pulledPos;
                Dungeon.level.occupyCell(enemy);
                Dungeon.observe();
                GameScene.updateFog();
            }
        }));
    }

    @Override
    public String desc() {
        if (this.swiching) {
            return Messages.get(this, "desc_mode");
        }
        return Messages.get(this, "desc");
    }

    @Override
    public String status() {
        if (this.isIdentified()) {
            if (this.swiching) {
                return "EX";
            }
            return "NM";
        }
        return null;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SWICH, this.swiching);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.swiching = bundle.getBoolean(SWICH);
    }
}
