package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;

import java.util.ArrayList;

/**
 * END(移植自魔绫·挑战区): “不可击杀、只对话”的 NPC 基类。
 */
public class NTNPC extends NPC {

    {
        properties.add(Property.IMMOVABLE);
    }

    protected ArrayList<String> chat = new ArrayList<>();
    protected ArrayList<String> endChat = new ArrayList<>();

    protected ArrayList<String> sChat = new ArrayList<>();

    @Override
    protected boolean act() {
        throwItem();
        return super.act();
    }

    @Override
    public int defenseSkill( Char enemy ) {
        return 1000;
    }

    @Override
    protected Char chooseEnemy() {
        return null;
    }

    @Override
    public void damage(int dmg, Object src) {
    }

    @Override
    public boolean add(Buff buff ) {
        return false;
    }

    @Override
    public boolean reset() {
        return true;
    }

    @Override
    public boolean interact(Char c) {
        sprite.turnTo( pos, Dungeon.hero.pos );
        if(Statistics.amuletObtained && !endChat.isEmpty()){
            WndQuest.chating(this,endChat);
        }else {
            WndQuest.chating(this, chat);
        }
        return true;
    }
}
