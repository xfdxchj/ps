//END(port from Arknights): ChenShooterBuff
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.SnowHunter;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class ChenShooterBuff
extends FlavourBuff
implements ActionIndicator.Action {
    @Override
    public String actionName() {
        return "shooting";
    }

    @Override
    public int indicatorColor() {
        return 0xFFAA33;
    }

    public int targetid = 0;
    private static final String TARGET = "targetid";

    public ChenShooterBuff() {
        this.type = Buff.buffType.POSITIVE;
    }

    @Override
    public boolean attachTo(Char target) {
        ActionIndicator.setAction(this);
        return super.attachTo(target);
    }

    @Override
    public void detach() {
        super.detach();
        ActionIndicator.clearAction(this);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(TARGET, this.targetid);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.targetid = bundle.getInt(TARGET);
        bundle.put(TARGET, this.targetid);
    }

    //END(移植调整): 方舟用 getIcon() 返回自定义图标；本 fork 的 Buff 用 icon()（返回 int）。
    //这里改为用已有的 BuffIndicator 图标，颜色由 tintIcon 处理。
    @Override
    public int icon() {
        return 27;   //END: BuffIndicator.MARK（本 fork 该常量存在但未 import）
    }

    @Override
    public void tintIcon(com.watabou.noosa.Image icon) {
        icon.hardlight(0x99992E);
    }

    public void set(int id) {
        this.targetid = id;
    }

    @Override
    public void doAction() {
        Hero hero = Dungeon.hero;
        if (hero == null) {
            return;
        }
        Char ch = (Char)Actor.findById(this.targetid);
        if (ch == null) {
            return;
        }
        Ballistica chain = new Ballistica(hero.pos, ch.pos, 1);
        SnowHunter.chainEnemy(chain, hero);
        CellEmitter.get(hero.pos).burst(Speck.factory(7), 6);
        hero.spendAndNext(0.0f);
        if (hero.hasTalent(Talent.TAC_DEF)) {
            Buff.affect(hero, Barrier.class).incShield(hero.pointsInTalent(Talent.TAC_DEF) * 2);
        }
        if (hero.hasTalent(Talent.TAC_SHOT)) {
            Buff.affect(hero, TACMove_tacshot.class);
        }
        float CD = 20.0f;
        if (hero.hasTalent(Talent.GORGEOUS_VACATION)) {
            CD -= (float)(hero.pointsInTalent(Talent.GORGEOUS_VACATION) * 4);
        }
        if (hero.hasTalent(Talent.TECHNICAL) && Random.Int(4) < hero.pointsInTalent(Talent.TECHNICAL)) {
            CD -= 5.0f;
        }
        Buff.affect(hero, TACMoveCooldown.class, CD);
        Dungeon.level.occupyCell(hero);
        Dungeon.observe();
        this.detach();
    }

    public static class TACMove_tacshot
    extends Buff {
    }

    public static class TACMoveCooldown
    extends FlavourBuff {
        @Override
        public int icon() {
            return 54;
        }

        @Override
        public String toString() {
            return Messages.get(this, "name");
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", this.dispTurns());
        }
    }
}
