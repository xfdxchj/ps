package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.sixkings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NTNPC;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.sixkings.GuideSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  六王 · 引路人（NPC，位于第 1 层）
 * ═══════════════════════════════════════════════════════════════
 *
 * 剧情：六人本是挚友，却因各自的执念分崩离析。
 *
 * 对话分两段：
 *   · 首次对话：讲述往事（六人如何相遇、如何决裂）
 *   · 再次对话：给玩家的嘱托
 *
 * 玩家打完全部六王后回到这里会有第三段对话（可选）。
 */
public class SixKingsGuide extends NTNPC {

    {
        spriteClass = GuideSprite.class;
        alignment = Alignment.NEUTRAL;

        // NPC 不参与战斗
        HT = 1;
    }

    /** 是否已经讲过故事 */
    private boolean storyTold = false;

    private static final String STORY_TOLD = "storyTold";

    // ═══════════════════════════════════════════════
    @Override
    public boolean interact(Char c) {

        if (c != Dungeon.hero) return true;

        sprite.turnTo( pos, c.pos );

        if (!storyTold) {
            storyTold = true;
            showStory();
        } else {
            showReminder();
        }

        return true;
    }

    /** 首次：讲述六人的故事（多段） */
    private void showStory() {
        ArrayList<String> lines = new ArrayList<>();

        lines.add( Messages.get(this, "story1") );
        lines.add( Messages.get(this, "story2") );
        lines.add( Messages.get(this, "story3") );
        lines.add( Messages.get(this, "story4") );
        lines.add( Messages.get(this, "story5") );

        GameScene.show( new WndQuest( this, lines, 0 ) );
    }

    /** 之后：嘱托 */
    private void showReminder() {
        GameScene.show( new WndQuest( this, Messages.get(this, "reminder") ) );
    }

    // ═══════════════════════════════════════════════
    @Override
    public String name() {
        return Messages.get(this, "name");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(STORY_TOLD, storyTold);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        storyTold = bundle.getBoolean(STORY_TOLD);
    }
}
