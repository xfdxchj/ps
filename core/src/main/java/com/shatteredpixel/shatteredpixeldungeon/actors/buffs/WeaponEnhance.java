/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class WeaponEnhance
extends Buff {
    private int hit;
    private int maxHit;
    private int level;
    private static final String HIT = "hit";
    private static final String MAX_HIT = "maxHit";
    private static final String LEVEL = "level";

    public WeaponEnhance() {
        this.type = Buff.buffType.POSITIVE;
        this.hit = 0;
        this.maxHit = 0;
        this.level = 0;
    }

    public void set(int level, int hit) {
        this.hit = this.maxHit = hit;
        this.level = level;
        Item.updateQuickslot();
    }

    @Override
    public void detach() {
        super.detach();
        Item.updateQuickslot();
    }

    @Override
    public float iconFadePercent() {
        return Math.max((float)(this.maxHit - this.hit) / (float)this.maxHit, 0.0f);
    }

    public void attackProc() {
        --this.hit;
        if (this.hit <= 0) {
            this.detach();
        }
        BuffIndicator.refreshHero();
    }

    public int weaponLevel(int weaponLevel) {
        return weaponLevel += this.level;
    }

    @Override
    public int icon() {
        return BuffIndicator.NONE;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(1.0f, 0.0f, 0.0f);
    }

    public String toString() {
        return Messages.get(this, "name", new Object[0]);
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", this.level, this.hit);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(HIT, this.hit);
        bundle.put(MAX_HIT, this.maxHit);
        bundle.put(LEVEL, this.level);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.hit = bundle.getInt(HIT);
        this.maxHit = bundle.getInt(MAX_HIT);
        this.level = bundle.getInt(LEVEL);
    }
}

