//END(port from Arknights): WndCeylon
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ceylon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Bottle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.TeaRose;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;

public class WndCeylon
extends Window {
    private static final int WIDTH = 120;
    private static final int BTN_HEIGHT = 20;
    private static final int GAP = 2;

    public WndCeylon(final Ceylon ceylon, final TeaRose tokens, final Bottle bottle) {
        IconTitle titlebar = new IconTitle();
        titlebar.icon(new ItemSprite(tokens.image(), null));
        titlebar.label(Messages.titleCase(tokens.name()));
        titlebar.setRect(0.0f, 0.0f, 120.0f, 0.0f);
        this.add(titlebar);
        RenderedTextBlock message = PixelScene.renderTextBlock(Messages.get(this, "message"), 6);
        message.maxWidth(120);
        message.setPos(0.0f, titlebar.bottom() + 2.0f);
        this.add(message);
        RedButton btnReward = new RedButton(Messages.get(this, "reward")){

            @Override
            protected void onClick() {
                WndCeylon.this.takeReward(ceylon, tokens, bottle, Ceylon.Quest.reward);
            }
        };
        btnReward.setRect(0.0f, message.top() + message.height() + 2.0f, 120.0f, 20.0f);
        this.add(btnReward);
        this.resize(120, (int)btnReward.bottom());
    }

    private void takeReward(Ceylon cylon, TeaRose tokens, Bottle bottle, Item reward) {
        this.hide();
        bottle.detachAll(Dungeon.hero.belongings.backpack);
        tokens.detachAll(Dungeon.hero.belongings.backpack);
        if (reward == null) {
            return;
        }
        reward.identify();
        if (reward.doPickUp(Dungeon.hero)) {
            GLog.i(Messages.get(Dungeon.hero, "you_now_have", reward.name()));
        } else {
            Dungeon.level.drop((Item)reward, (int)cylon.pos).sprite.drop();
        }
        cylon.flee();
        Ceylon.Quest.complete();
    }
}
