//END(port from Arknights): TiacauhRitualist
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TiacauhRitualistSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.utils.Random;

public class TiacauhRitualist
extends Mob {
    private static final float TIME_TO_ZAP = 1.0f;

    public TiacauhRitualist() {
        this.spriteClass = TiacauhRitualistSprite.class;
        this.HT = 95;
        this.HP = 95;
        this.defenseSkill = 18;
        this.EXP = 17;
        this.maxLvl = 36;
        this.loot = Generator.Category.STONE;
        this.lootChance = 0.5f;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(38, 48);
    }

    @Override
    public int attackSkill(Char target) {
        return 40;
    }

    @Override
    public int drRoll() {
        return Random.NormalIntRange(0, 16);
    }

    @Override
    public void damage(int dmg, Object src) {
        if (src == Burning.class) {
            dmg *= 2;
        }
        super.damage(dmg, src);
    }

    @Override
    protected boolean canAttack(Char enemy) {
        return new Ballistica((int)this.pos, (int)enemy.pos, (int)6).collisionPos == enemy.pos;
    }

    @Override
    protected boolean doAttack(Char enemy) {
        if (Dungeon.level.distance(this.pos, enemy.pos) <= 1) {
            return super.doAttack(enemy);
        }
        if (this.buff(Silence.class) == null) {
            this.spend(1.0f);
            if (TiacauhRitualist.hit(this, enemy, true)) {
                int dmg = Random.NormalIntRange(12, 18);
                enemy.damage(dmg, new TiacauhBolat());
                if (Dungeon.isChallenged(1024) && Random.Int(2) == 0) {
                    Buff.affect(enemy, Blindness.class, 1.0f);
                }
                if (enemy.sprite.visible) {
                    enemy.sprite.flash();
                }
                if (enemy == Dungeon.hero) {
                    Camera.main.shake(2.0f, 0.3f);
                    if (!enemy.isAlive()) {
                        Dungeon.fail(this.getClass());
                        GLog.n(Messages.get(TiacauhRitualist.class, "zap_kill"));
                    }
                }
            } else {
                enemy.sprite.showStatus(0xFFFF00, enemy.defenseVerb());
            }
            if (this.sprite != null && (this.sprite.visible || enemy.sprite.visible)) {
                this.sprite.zap(enemy.pos);
                return false;
            }
            return true;
        }
        this.spend(1.0f);
        return true;
    }

    public void onZapComplete() {
        this.next();
    }

    public void call() {
        this.next();
    }

    public static class TiacauhBolat {
    }
}
