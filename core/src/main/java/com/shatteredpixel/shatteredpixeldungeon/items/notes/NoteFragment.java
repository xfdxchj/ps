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
 *  笔记残页（9 张）
 * ═══════════════════════════════════════════════════════════════
 *
 * 每张残页记录六人往事的一个片段。
 *
 * 流程：
 *   9 张残页  --(3 合 1)-->  3 个章节  --(3 合 1)-->  1 本完整手记
 *                                                      ↓
 *                                               解锁六王区域入口
 *
 * 使用残页：阅读（弹出故事）
 */
public class NoteFragment extends Item {

    public static final String AC_READ = "READ";

    /** 残页编号 1-9 */
    private int index = 1;

    {
        stackable = true;
        defaultAction = AC_READ;
        //END(移植调整): 用现有的书页图标
        image = ItemSpriteSheet.GUIDE_PAGE;
    }

    public NoteFragment() { }

    public NoteFragment(int index) {
        this.index = Math.max(1, Math.min(9, index));
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
            read( hero );
        }
    }

    /** 阅读残页：弹出故事 */
    private void read(Hero hero) {
        String text = Messages.get( this, "text" + index );
        GameScene.show( new WndStory( text ) );

        // 读过的登记（可选）
        Notes_seen( index );
    }

    private void Notes_seen(int i) {
        //END(可选): 如需记录阅读进度，可在此写入 Statistics
    }

    // ═══════════════════════════════════════════════
    @Override
    public String name() {
        return Messages.get( this, "name", index );
    }

    @Override
    public String desc() {
        return Messages.get( this, "desc" );
    }

    @Override
    public int value() { return 20 * index; }

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
