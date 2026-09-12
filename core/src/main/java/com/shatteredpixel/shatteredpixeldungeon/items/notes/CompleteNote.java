package com.shatteredpixel.shatteredpixeldungeon.items.notes;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndStory;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  完整手记 —— 由 3 个章节合成，是开启六王区域的「钥匙」
 * ═══════════════════════════════════════════════════════════════
 *
 * 用途：
 *   · 阅读：弹出完整故事（六人的结局）
 *   · 持有：解锁六王区域（见 ChallengeArea / 入口判定）
 */
public class CompleteNote extends Item {

    public static final String AC_READ = "READ";

    {
        stackable = false;
        unique = true;
        defaultAction = AC_READ;
        image = ItemSpriteSheet.HALLS_PAGE;   //END: 用现有的"大厅"书页图标
    }

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
            GameScene.show( new WndStory( Messages.get(this, "text") ) );
        }
    }

    @Override
    public boolean isUpgradable()  { return false; }
    @Override
    public boolean isIdentified()  { return true; }

    @Override
    public String name() { return Messages.get(this, "name"); }
    @Override
    public String desc() { return Messages.get(this, "desc"); }

    @Override
    public int value() { return 500; }
}
