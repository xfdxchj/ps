//END(port from Arknights): IsharmlaSeabornTail
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.IsharmlaSeabornHead;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Mula_3Sprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossMultiHealthBar;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class IsharmlaSeabornTail
extends Mob {
    private boolean isDead;
    private int cooldown;
    private static final String IS_DEAD_TAIL = "isDeadTail";
    private static final String SING_COOLDOWN = "singCooldown";

    public IsharmlaSeabornTail() {
        this.spriteClass = Mula_3Sprite.class;
        this.HT = 1000;
        this.HP = 1000;
        this.defenseSkill = 20;
        this.actPriority = -21;
        this.properties.add(Char.Property.SEA);
        this.properties.add(Char.Property.BOSS);
        this.properties.add(Char.Property.IMMOVABLE);
        this.properties.add(Char.Property.STATIC);
        this.state = new Hunting();
        this.isDead = false;
        this.cooldown = 3;
    }

    @Override
    public void notice() {
        BossMultiHealthBar.assignBoss(this);
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(25, 45);
    }

    @Override
    public int attackSkill(Char target) {
        return 50;
    }

    @Override
    public int defenseSkill(Char enemy) {
        if (this.isDead) {
            return INFINITE_EVASION;
        }
        if (enemy instanceof Hero && (Dungeon.level.map[enemy.pos] == 1 || Dungeon.level.map[enemy.pos] == 20)) {
            return INFINITE_EVASION;
        }
        return super.defenseSkill(enemy);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return !this.isDead && Dungeon.level.map[enemy.pos] != 1;
    }

    @Override
    protected boolean act() {
        this.sprite.turnTo(this.pos, 999999);
        this.rooted = true;
        if (this.isDead) {
            this.alerted = false;
            return super.act();
        }
        if (this.cooldown > 0) {
            --this.cooldown;
        } else {
            int damage = Dungeon.isChallenged(512) ? 12 : 10;
            Dungeon.hero.damage(damage, this);
            for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                if (mob.alignment != Char.Alignment.ALLY) continue;
                mob.damage(damage, this);
            }
            this.cooldown = Dungeon.isChallenged(512) ? 3 : 4;
        }
        return super.act();
    }

    @Override
    public void damage(int dmg, Object src) {
        if (this.isDead) {
            return;
        }
        int heroTile = Dungeon.level.map[Dungeon.hero.pos];
        if (heroTile == 1 || heroTile == 20) {
            return;
        }
        super.damage(dmg, src);
        if (this.HP < 1) {
            this.isDead = true;
            Buff.affect(this, Doom.class);
            ++Dungeon.mulaCount;
            IsharmlaSeabornHead.triggerAnger();
        }
    }

    @Override
    public void die(Object cause) {
    }

    @Override
    public boolean isAlive() {
        return !this.isDead;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(IS_DEAD_TAIL, this.isDead);
        bundle.put(SING_COOLDOWN, this.cooldown);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.isDead = bundle.getBoolean(IS_DEAD_TAIL);
        this.cooldown = bundle.getInt(SING_COOLDOWN);
    }

    protected class Hunting
    implements Mob.AiState {
        protected Hunting() {
        }

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            IsharmlaSeabornTail.this.enemySeen = enemyInFOV;
            if (enemyInFOV && !IsharmlaSeabornTail.this.isCharmedBy(IsharmlaSeabornTail.this.enemy) && IsharmlaSeabornTail.this.canAttack(IsharmlaSeabornTail.this.enemy)) {
                IsharmlaSeabornTail.this.target = IsharmlaSeabornTail.this.enemy.pos;
                return IsharmlaSeabornTail.this.doAttack(IsharmlaSeabornTail.this.enemy);
            }
            IsharmlaSeabornTail.this.spend(1.0f);
            return true;
        }
    }
}
