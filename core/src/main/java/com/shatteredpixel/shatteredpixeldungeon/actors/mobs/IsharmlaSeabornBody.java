//END(port from Arknights): IsharmlaSeabornBody
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Doom;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.IsharmlaSeabornHead;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.IsharmlaSeabornTail;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Mula_2Sprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossMultiHealthBar;
import com.watabou.utils.Bundle;

public class IsharmlaSeabornBody
extends Mob {
    private boolean isDead;
    private int cooldown;
    int healAmount;
    private static final String IS_DEAD_BODY = "isDeadBody";
    private static final String SHIELD_COOLDOWN = "shieldCooldown";

    public IsharmlaSeabornBody() {
        this.spriteClass = Mula_2Sprite.class;
        this.HT = 1000;
        this.HP = 1000;
        this.defenseSkill = 20;
        this.actPriority = -21;
        this.properties.add(Char.Property.SEA);
        this.properties.add(Char.Property.BOSS);
        this.properties.add(Char.Property.IMMOVABLE);
        this.state = new Hunting();
        this.isDead = false;
        this.cooldown = Dungeon.isChallenged(512) ? 6 : 9;
        this.healAmount = Dungeon.isChallenged(512) ? 50 : 40;
    }

    @Override
    public void notice() {
        BossMultiHealthBar.assignBoss(this);
    }

    @Override
    public int defenseSkill(Char enemy) {
        if (this.isDead) {
            return INFINITE_EVASION;
        }
        if (enemy instanceof Hero && Dungeon.level.map[enemy.pos] == 1) {
            return INFINITE_EVASION;
        }
        return super.defenseSkill(enemy);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return false;
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
            for (Mob mob : Dungeon.level.mobs) {
                if (!(mob instanceof IsharmlaSeabornHead) && !(mob instanceof IsharmlaSeabornBody) && !(mob instanceof IsharmlaSeabornTail) || !mob.isAlive()) continue;
                mob.sprite.emitter().burst(Speck.factory(0), 3);
                mob.HP = Math.min(mob.HT, mob.HP + this.healAmount);
            }
            this.cooldown = Dungeon.isChallenged(512) ? 6 : 9;
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
        bundle.put(IS_DEAD_BODY, this.isDead);
        bundle.put(SHIELD_COOLDOWN, this.cooldown);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.isDead = bundle.getBoolean(IS_DEAD_BODY);
        this.cooldown = bundle.getInt(SHIELD_COOLDOWN);
    }

    protected class Hunting
    implements Mob.AiState {
        protected Hunting() {
        }

        @Override
        public boolean act(boolean enemyInFOV, boolean justAlerted) {
            IsharmlaSeabornBody.this.enemySeen = enemyInFOV;
            if (enemyInFOV && !IsharmlaSeabornBody.this.isCharmedBy(IsharmlaSeabornBody.this.enemy) && IsharmlaSeabornBody.this.canAttack(IsharmlaSeabornBody.this.enemy)) {
                IsharmlaSeabornBody.this.target = IsharmlaSeabornBody.this.enemy.pos;
                return IsharmlaSeabornBody.this.doAttack(IsharmlaSeabornBody.this.enemy);
            }
            IsharmlaSeabornBody.this.spend(1.0f);
            return true;
        }
    }
}
