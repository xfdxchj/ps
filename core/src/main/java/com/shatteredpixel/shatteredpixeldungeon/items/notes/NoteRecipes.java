package com.shatteredpixel.shatteredpixeldungeon.items.notes;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.notes.NoteChapter;
import com.shatteredpixel.shatteredpixeldungeon.items.notes.NoteFragment;

import java.util.ArrayList;

/**
 * ═══════════════════════════════════════════════════════════════
 *  笔记合成规则
 * ═══════════════════════════════════════════════════════════════
 *
 *   3 张同章残页 → 1 个章节
 *     残页 1,2,3 → 第 1 章「相遇」
 *     残页 4,5,6 → 第 2 章「裂痕」
 *     残页 7,8,9 → 第 3 章「诀别」
 *
 *   3 个章节 → 1 本完整手记
 *
 * 使用方式：
 *   在炼金釜（Alchemy）里合成；也可由代码直接调用 craft()。
 */
public class NoteRecipes {

    // ═══════════════════════════════════════════════
    //  残页 → 章节
    // ═══════════════════════════════════════════════
    public static class Chapter1 extends Recipe.SimpleRecipe {
        public Chapter1() {
            inputs = new Class[]{ NoteFragment.class, NoteFragment.class, NoteFragment.class };
            inQuantity = new int[]{ 1, 1, 1 };
            cost = 0;
            output = NoteChapter.class;
            outQuantity = 1;
        }
    }

    public static class Chapter2 extends Recipe.SimpleRecipe {
        public Chapter2() {
            inputs = new Class[]{ NoteFragment.class, NoteFragment.class, NoteFragment.class };
            inQuantity = new int[]{ 1, 1, 1 };
            cost = 0;
            output = NoteChapter.class;
            outQuantity = 1;
        }
    }

    public static class Chapter3 extends Recipe.SimpleRecipe {
        public Chapter3() {
            inputs = new Class[]{ NoteFragment.class, NoteFragment.class, NoteFragment.class };
            inQuantity = new int[]{ 1, 1, 1 };
            cost = 0;
            output = NoteChapter.class;
            outQuantity = 1;
        }
    }

    // ═══════════════════════════════════════════════
    //  章节 → 完整手记
    // ═══════════════════════════════════════════════
    public static class Complete extends Recipe.SimpleRecipe {
        public Complete() {
            inputs = new Class[]{ NoteChapter.class, NoteChapter.class, NoteChapter.class };
            inQuantity = new int[]{ 1, 1, 1 };
            cost = 0;
            output = CompleteNote.class;
            outQuantity = 1;
        }
    }

    // ═══════════════════════════════════════════════
    //  工具方法：直接合成（不走炼金釜）
    // ═══════════════════════════════════════════════

    /** 把背包里的 3 张同章残页合成 1 个章节；成功返回 true */
    public static boolean tryCombineFragments(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero,
                                              int chapter) {

        int lo = (chapter - 1) * 3 + 1;      // 本章第一张残页编号
        int need = 3;

        // 统计拥有的数量
        int have = 0;
        for (Item it : hero.belongings.backpack.items) {
            if (it instanceof NoteFragment
                    && ((NoteFragment) it).index() >= lo
                    && ((NoteFragment) it).index() < lo + 3) {
                have += it.quantity();
            }
        }
        if (have < need) return false;

        // 扣除
        int left = need;
        ArrayList<Item> toRemove = new ArrayList<>();
        for (Item it : hero.belongings.backpack.items) {
            if (left <= 0) break;
            if (it instanceof NoteFragment
                    && ((NoteFragment) it).index() >= lo
                    && ((NoteFragment) it).index() < lo + 3) {
                int take = Math.min(left, it.quantity());
                it.quantity(it.quantity() - take);
                left -= take;
                if (it.quantity() <= 0) toRemove.add(it);
            }
        }
        for (Item it : toRemove) hero.belongings.backpack.items.remove(it);

        // 给出章节
        NoteChapter chapterItem = new NoteChapter( chapter );
        if (!chapterItem.collect()) {
            hero.belongings.backpack.items.add( chapterItem );
        }
        return true;
    }

    /** 把 3 个章节合成完整手记 */
    public static boolean tryCombineChapters(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero) {

        int have = 0;
        for (Item it : hero.belongings.backpack.items) {
            if (it instanceof NoteChapter) have += it.quantity();
        }
        if (have < 3) return false;

        int left = 3;
        ArrayList<Item> toRemove = new ArrayList<>();
        for (Item it : hero.belongings.backpack.items) {
            if (left <= 0) break;
            if (it instanceof NoteChapter) {
                int take = Math.min(left, it.quantity());
                it.quantity(it.quantity() - take);
                left -= take;
                if (it.quantity() <= 0) toRemove.add(it);
            }
        }
        for (Item it : toRemove) hero.belongings.backpack.items.remove(it);

        CompleteNote note = new CompleteNote();
        if (!note.collect()) {
            hero.belongings.backpack.items.add( note );
        }
        return true;
    }
}
