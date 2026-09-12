//END(port from Arknights): Accessories
package com.shatteredpixel.shatteredpixeldungeon.items.Gunaccessories;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.GunWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import java.util.ArrayList;

public class Accessories
extends Item {
    public static final String AC_AFFIX = "AFFIX";
    protected float ACCcorrectionvalue;
    protected float DLYcorrectionvalue;
    protected float DMGcorrectionvalue;
    protected int SavingChancevalue;
    protected float CONEcorrectionvalue;
    private final WndBag.Listener itemSelector;

    public Accessories() {
        this.image = ItemSpriteSheet.BOMB;
        this.stackable = false;
        this.bones = false;
        this.ACCcorrectionvalue = 1.0f;
        this.DLYcorrectionvalue = 1.0f;
        this.DMGcorrectionvalue = 1.0f;
        this.SavingChancevalue = 0;
        this.CONEcorrectionvalue = 1.0f;
        this.itemSelector = new WndBag.Listener(){

            @Override
            public void onSelect(Item item) {
                if (item != null) {
                    if (item instanceof GunWeapon && !((GunWeapon)item).affixAccessories(Accessories.this.Affix())) {
                        Accessories.this.Affix().collect();
                    }
                } else {
                    Accessories.this.Affix().collect();
                }
            }
        };
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_AFFIX);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_AFFIX)) {
            GameScene.selectItem(this.itemSelector, WndBag.Mode.MISSILEWEAPON, Messages.get(this, "prompt"));
            this.detach(hero.belongings.backpack);
            hero.spendAndNext(1.0f);
        }
    }

    private Accessories Affix() {
        return this;
    }

    public float GetACCcorrectionvalue() {
        return this.ACCcorrectionvalue;
    }

    public float GetDLYcorrectionvalue() {
        return this.DLYcorrectionvalue;
    }

    public float GetDMGcorrectionvalue() {
        return this.DMGcorrectionvalue;
    }

    public float GetCONEcorrectionvalue() {
        return this.CONEcorrectionvalue;
    }

    public int GetSavingChance() {
        return this.SavingChancevalue;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public int value() {
        return 40;
    }
}
