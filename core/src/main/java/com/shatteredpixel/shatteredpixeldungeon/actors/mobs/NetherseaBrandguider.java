//END(port from Arknights): NetherseaBrandguider
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SeaTerror;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.NetherseaBrandguiderSprite;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class NetherseaBrandguider
extends Mob {
    private boolean terrorSpawned;
    private static final String VAL = "firstTEEROR";

    public NetherseaBrandguider() {
        this.spriteClass = NetherseaBrandguiderSprite.class;
        this.HT = 160;
        this.HP = 160;
        this.EXP = 18;
        this.maxLvl = 38;
        this.defenseSkill = 20;
        this.loot = Generator.Category.SCROLL;
        this.lootChance = 0.33f;
        this.properties.add(Char.Property.SEA);
        this.terrorSpawned = false;
    }

    @Override
    public int damageRoll() {
        return Random.NormalIntRange(38, 55);
    }

    @Override
    public int attackSkill(Char target) {
        return 44;
    }

    @Override
    public int drRoll() {
        if (this.buff(Reinforced.class) != null) {
            return Random.NormalIntRange(10, 55);
        }
        return Random.NormalIntRange(0, 20);
    }

    @Override
    public void damage(int dmg, Object src) {
        super.damage(dmg, src);
        if (this.isAlive() && this.HT / 2 >= this.HP) {
            Buff.affect(this, Reinforced.class);
        }
    }

    @Override
    protected boolean act() {
        if (!this.terrorSpawned) {
            SeaTerror seaTerror = Dungeon.level.addSeaTerror(this.pos);
            seaTerror.activate();
            GameScene.updateMap(this.pos);
            this.terrorSpawned = true;
        }
        if (!(this.HT / 2 < this.HP && !this.shouldAlwaysGenerateSeaTerror() || this.buff(Silence.class) != null || Dungeon.level.seaTerrors.get(this.pos) != null || Dungeon.level.map[this.pos] != 1 && Dungeon.level.map[this.pos] != 29 && Dungeon.level.map[this.pos] != 20)) {
            Dungeon.level.addSeaTerror(this.pos);
            CellEmitter.get(this.pos).burst(Speck.factory(12), 10);
            GameScene.updateMap(this.pos);
            Dungeon.observe();
        }
        return super.act();
    }

    protected boolean shouldAlwaysGenerateSeaTerror() {
        return false;
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(VAL, this.terrorSpawned);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.terrorSpawned = bundle.getBoolean(VAL);
    }

    public static class Reinforced
    extends Buff {
        public Reinforced() {
            this.type = Buff.buffType.POSITIVE;
            this.announced = true;
        }

        @Override
        public boolean act() {
            if (this.target.HP > this.target.HT / 2) {
                this.detach();
            } else {
                this.spend(1.0f);
            }
            return true;
        }

        @Override
        public void fx(boolean on) {
            if (on && this.target.sprite != null) {
                this.target.sprite.shieldHalo(39355);
            } else if (!on && this.target.sprite != null) {
                this.target.sprite.clearShieldHalo();
            }
        }

        @Override
        public int icon() {
            return 20;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0.0f, 0.6f, 0.73f);
        }

        @Override
        public String toString() {
            return Messages.get(this, "name");
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc");
        }
    }
}
