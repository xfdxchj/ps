//END(port from Arknights): CustomeSet
package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import java.util.ArrayList;

public class CustomeSet
extends Artifact {
    public static final String AC_SHADOW = "SHADOW";

    public CustomeSet() {
        this.image = ItemSpriteSheet.ARTIFACT_TOOLKIT;
        this.defaultAction = AC_SHADOW;
        this.levelCap = 10;
        this.charge = 100;
        this.partialCharge = 0.0f;
        this.chargeCap = 100;
        this.defaultAction = AC_SHADOW;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (this.isEquipped(hero) && !this.cursed) {
            actions.add(AC_SHADOW);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_SHADOW) && this.activeBuff == null) {
            if (!this.isEquipped(hero)) {
                GLog.i(Messages.get(Artifact.class, "need_to_equip"));
            } else if (this.cursed) {
                GLog.i(Messages.get(this, "cursed"));
            } else if (this.charge < 100) {
                GLog.i(Messages.get(this, "no_charge"));
            } else {
                int mirror = 1 + this.level() / 5;
                new ScrollOfMirrorImage();
                ScrollOfMirrorImage.spawnImages(curUser, mirror);
                this.charge = 0;
                CustomeSet.updateQuickslot();
                Talent.onArtifactUsed(Dungeon.hero);
                if (this.level() < this.levelCap) {
                    this.upgrade();
                }
                curUser.spendAndNext(1.0f);
            }
        }
    }

    @Override
    public void charge(Hero target, float amount) {
        if (this.charge < this.chargeCap) {
            this.charge += Math.round(1.0f * amount);
            if (this.charge >= this.chargeCap) {
                this.charge = this.chargeCap;
                CustomeSet.updateQuickslot();
            }
        }
    }

    @Override
    public String desc() {
        Object desc = super.desc();
        if (this.isEquipped(Dungeon.hero) && this.cursed) {
            desc = (String)desc + "\n\n";
            desc = (String)desc + Messages.get(this, "desc_cursed");
        }
        return desc.toString();
    }

    @Override
    protected Artifact.ArtifactBuff passiveBuff() {
        return new CustomSetBuff();
    }

    public class CustomSetBuff
    extends Artifact.ArtifactBuff {
        @Override
        public boolean act() {
            LockedFloor lock = this.target.buff(LockedFloor.class);
            if (CustomeSet.this.activeBuff == null && (lock == null || lock.regenOn()) && !Dungeon.isInRhodes()) {
                if (CustomeSet.this.charge < CustomeSet.this.chargeCap && !CustomeSet.this.cursed) {
                    float chargeGain = 0.13f;
                    CustomeSet.this.partialCharge += (chargeGain *= RingOfEnergy.artifactChargeMultiplier(this.target));
                    if (CustomeSet.this.partialCharge > 1.0f && CustomeSet.this.charge < CustomeSet.this.chargeCap) {
                        CustomeSet.this.partialCharge -= 1.0f;
                        ++CustomeSet.this.charge;
                        Item.updateQuickslot();
                    }
                }
            } else {
                CustomeSet.this.partialCharge = 0.0f;
            }
            this.spend(1.0f);
            return true;
        }

        @Override
        public void charge(Hero target, float amount) {
            CustomeSet.this.charge += Math.round(1.0f * amount);
            CustomeSet.this.charge = Math.min(CustomeSet.this.charge, CustomeSet.this.chargeCap);
            Item.updateQuickslot();
        }
    }
}
