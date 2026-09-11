package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.hollow;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NTNPC;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DeathRongSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;

/**
 * END(移植自魔绫·挑战区): 空洞遗迹 NPC·死亡之荣。
 * 移植调整：魔绫原版交互打开 `WndDialog(BoatPlot/BoatPlot_End)`（依赖未搬的对话/剧情节系统），
 * 此处改为等价的 `WndQuest` 文本对话。
 * TODO(待搬)：搬入 plot 与 `WndDialog` 后可恢复原剧情演出。
 */
public class DeathRong extends NTNPC {

    {
        spriteClass = DeathRongSprite.class;
        properties.add(Property.IMMOVABLE);
        flying = true;
    }

    public boolean first=true;
    public boolean secnod=true;
    public boolean rd = true;

    private static final String FIRST = "first";
    private static final String SECNOD = "secnod";
    private static final String RD = "rd";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(FIRST, first);
        bundle.put(SECNOD, secnod);
        bundle.put(RD, rd);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        first = bundle.getBoolean(FIRST);
        secnod = bundle.getBoolean(SECNOD);
        rd = bundle.getBoolean(RD);
    }

    @Override
    protected boolean act() {

        throwItem();

        sprite.turnTo( pos, Dungeon.hero.pos );
        spend( TICK );
        return true;
    }

    @Override
    public int defenseSkill( Char enemy ) {
        return INFINITE_EVASION;
    }

    @Override
    public boolean interact(Char c) {

        sprite.turnTo(pos, Dungeon.hero.pos);

        if(first){
            Game.runOnRenderThread(() -> GameScene.show(new WndQuest(this,
                    Messages.get(DeathRong.class, "hello"))));
        } else if(Statistics.defalult_deaddog) {
            Game.runOnRenderThread(() -> GameScene.show(new WndQuest(this,
                    Messages.get(DeathRong.class, "hello_end"))));
        }
        Bestiary.setSeen(DeathRong.class);

        return true;
    }

    public static void tell(String text) {
        Game.runOnRenderThread(() -> GameScene.show(new WndQuest(new DeathRong(), text)));
    }

}
