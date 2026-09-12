package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;

//END(STUB): Arknights Ceylon placeholder (terrain stage).
//Provides only the inner API the ported levels call.
public class Ceylon extends NPC {
    { spriteClass = null; }

    //END(STUB): 关卡会调用 Ceylon.Quest.spawn(level) / isSpawnd()
    public static class Quest {
        private static boolean spawned = false;
        public static void spawn(com.shatteredpixel.shatteredpixeldungeon.levels.Level level) {
            spawned = true;
        }
        public static boolean isSpawnd() {
            return spawned;
        }
    }
}
