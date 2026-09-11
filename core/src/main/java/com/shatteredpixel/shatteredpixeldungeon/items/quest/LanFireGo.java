package com.shatteredpixel.shatteredpixeldungeon.items.quest;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

/**
 * END(移植自魔绫·挑战区): 无形投掷物（仅用于冥犬 Boss 连击的飞射动画）。
 * 移植调整：魔绫用 ItemSpriteSheet.EMPTY，本 fork 无该常量，改用 SOMETHING（同样的占位图）。
 */
public class LanFireGo extends Item {

    {
        image = ItemSpriteSheet.SOMETHING;
        stackable = true;
        unique = true;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String desc() {
        return "";
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }
}
