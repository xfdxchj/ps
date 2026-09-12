//END(port from Arknights): Schwarz
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Drowsy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HandclapSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SchwarzSprite;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

public class Schwarz
extends Mob {
    private static final String[] LINE_KEYS = new String[]{"snipe1", "snipe2", "snipe3"};
    public int Phase;
    private int CoolDown;
    private int LastPos;
    private static final String PHASE = "Phase";
    private static final String CD = "CoolDown";
    private static final String SKILLPOS = "LastPos";

    public Schwarz() {
        this.spriteClass = SchwarzSprite.class;
        this.HT = 1000;
        this.HP = 1000;
        this.defenseSkill = 100;
        this.state = this.HUNTING;
        this.maxLvl = 45;
        this.EXP = -1;
        this.properties.add(Char.Property.BOSS);
        this.immunities.add(Drowsy.class);
        this.immunities.add(MagicalSleep.class);
        this.immunities.add(Terror.class);
        this.immunities.add(Silence.class);
        this.Phase = 1;
        this.CoolDown = 8;
        this.LastPos = -1;
    }

    @Override
    public int damageRoll() {
        if (this.Phase == 2) {
            return Random.NormalIntRange(65, 80);
        }
        return Random.NormalIntRange(50, 70);
    }

    @Override
    public int attackSkill(Char target) {
        return 40;
    }

    @Override
    public void damage(int dmg, Object src) {
        Sample.INSTANCE.play("sounds/hit_parry.mp3", 1.0f, Random.Float(0.96f, 1.05f));
        this.sprite.showStatus(65280, Messages.get(this, "parried"));
    }

    @Override
    protected boolean act() {
        if (this.CoolDown == 0) {
            if (this.LastPos == -1) {
                this.LastPos = Dungeon.hero.pos;
                this.sprite.parent.addToBack(new TargetedCell(this.LastPos, 0xFF0000));
                this.yell(Messages.get(this, Random.element(LINE_KEYS)));
                this.spend(GameMath.gate(1.0f, Dungeon.hero.cooldown(), 3.0f));
                Dungeon.hero.interrupt();
                return true;
            }
            if (this.LastPos == Dungeon.hero.pos) {
                Dungeon.hero.damage(this.damageRoll(), this);
                Dungeon.hero.sprite.burst(0xFF0000, 10);
                CellEmitter.center(Dungeon.hero.pos).burst(HandclapSprite.GooParticle.FACTORY, 60);
                Camera.main.shake(5.0f, 0.5f);
                Sample.INSTANCE.play("sounds/skill_crossbow.mp3");
                this.CoolDown = this.Phase == 1 ? 8 : 5;
                this.LastPos = -1;
                this.spend(1.0f);
                return true;
            }
            CellEmitter.center(this.LastPos).burst(HandclapSprite.GooParticle.FACTORY, 60);
            Camera.main.shake(5.0f, 0.5f);
            Sample.INSTANCE.play("sounds/skill_crossbow.mp3");
            this.CoolDown = this.Phase == 1 ? 8 : 5;
            this.LastPos = -1;
        } else {
            --this.CoolDown;
        }
        return super.act();
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(PHASE, this.Phase);
        bundle.put(CD, this.CoolDown);
        bundle.put(SKILLPOS, this.LastPos);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.Phase = bundle.getInt(PHASE);
        this.CoolDown = bundle.getInt(CD);
        this.LastPos = bundle.getInt(SKILLPOS);
    }
}
