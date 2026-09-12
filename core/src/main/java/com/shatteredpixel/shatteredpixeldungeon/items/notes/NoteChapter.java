package com.shatteredpixel.shatteredpixeldungeon.items.notes;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStory;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  笔记章节（3 个）—— 由 3 张残页合成
 * ═══════════════════════════════════════════════════════════════
 *
 *   第 1 章：相遇        （残页 1-3）
 *   第 2 章：裂痕        （残页 4-6）
 *   第 3 章：诀别        （残页 7-9）
 */
public class NoteChapter extends Item {

    public static final String AC_READ = "READ";

    /** 章节号 1-3 */
    private int index = 1;

    {
        stackable = true;
        defaultAction = AC_READ;
        image = ItemSpriteSheet.ALCH_PAGE;
    }

    public NoteChapter() { }

    public NoteChapter(int index) {
        this.index = Math.max(1, Math.min(3, index));
    }

    public int index() { return index; }

    // ═══════════════════════════════════════════════
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add( AC_READ );
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_READ)) {
            GameScene.show( new WndStory( Messages.get(this, "text" + index) ) );
        }
    }

    @Override
    public String name() { return Messages.get(this, "name", index); }

    @Override
    public String desc() { return Messages.get(this, "desc"); }

    @Override
    public int value() { return 100 * index; }

    // ═══════════════════════════════════════════════
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("index", index);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        index = bundle.getInt("index");
    }
}
