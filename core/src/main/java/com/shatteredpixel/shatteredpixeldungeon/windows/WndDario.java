//END(port from Arknights): WndDario
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Dario;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;

public class WndDario
extends Window {
    protected static final int WIDTH_MIN = 120;
    protected static final int WIDTH_MAX = 220;
    private static final int BTN_HEIGHT = 20;
    private static final int GAP = 4;

    public WndDario(final Dario dario, String message) {
        int width = 120;
        IconTitle titlebar = new IconTitle(dario.sprite(), Messages.titleCase(dario.name()));
        titlebar.setRect(0.0f, 0.0f, width, 0.0f);
        this.add(titlebar);
        RenderedTextBlock text = PixelScene.renderTextBlock(6);
        text.text(message, width);
        text.setPos(titlebar.left(), titlebar.bottom() + 8.0f);
        this.add(text);
        while (PixelScene.landscape() && text.bottom() > 150.0f && width < 220) {
            text.maxWidth(width += 20);
        }
        RedButton btnReward = new RedButton(Messages.get(dario, "reward")){

            @Override
            protected void onClick() {
                WndDario.this.completeQuest(dario);
            }
        };
        btnReward.setRect(0.0f, text.top() + text.height() + 4.0f, width, 20.0f);
        this.add(btnReward);
        this.resize(width, (int)btnReward.bottom());
    }

    private void completeQuest(Dario dario) {
        this.hide();
        Dario.Quest.dropReward(dario);
        dario.flee();
    }
}
