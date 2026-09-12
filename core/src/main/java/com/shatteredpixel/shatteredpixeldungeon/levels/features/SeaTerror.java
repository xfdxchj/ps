//END(port from Arknights): SeaTerror
package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.NervousImpairment;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Platform;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.SeaPlatform;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class SeaTerror
implements Bundlable {
    public int pos;
    public boolean isActive;
    private static final String POS = "pos";
    private static final String SEA_TERROR_ACTIVE = "seaTerrorActive";

    public void activate() {
        if (this.isCovered()) {
            return;
        }
        boolean updated = false;
        int evaporatedTiles = 0;
        evaporatedTiles = Random.chances(new float[]{0.0f, 0.0f, 0.0f, 2.0f, 1.0f, 1.0f});
        for (int i = 0; i < evaporatedTiles; ++i) {
            int relativeCell = Random.Int(8);
            if (Dungeon.level.map[this.pos + PathFinder.NEIGHBOURS8[relativeCell]] != 1 && Dungeon.level.map[this.pos + PathFinder.NEIGHBOURS8[relativeCell]] != 14 && Dungeon.level.map[this.pos + PathFinder.NEIGHBOURS8[relativeCell]] != 20 && Dungeon.level.map[this.pos + PathFinder.NEIGHBOURS8[relativeCell]] != 29 && Dungeon.level.map[this.pos + PathFinder.NEIGHBOURS8[relativeCell]] != 90 || Dungeon.level.seaTerrors.get(this.pos + PathFinder.NEIGHBOURS8[relativeCell]) != null) continue;
            Dungeon.level.addSeaTerror(this.pos + PathFinder.NEIGHBOURS8[relativeCell]);
            if (!(com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.scene() instanceof GameScene) || !Dungeon.level.heroFOV[this.pos]) continue;
            CellEmitter.get(this.pos + PathFinder.NEIGHBOURS8[relativeCell]).burst(Speck.factory(12), 10);
            GameScene.updateMap(this.pos + PathFinder.NEIGHBOURS8[relativeCell]);
            Dungeon.observe();
        }
    }

    public void destroy() {
        Dungeon.level.destroySeaTerror(this.pos);
        if (Dungeon.level.heroFOV[this.pos]) {
            CellEmitter.get(this.pos).burst(FlameParticle.FACTORY, 6);
        }
    }

    public void spendTime(Char ch, float time) {
        if (this.isCovered()) {
            return;
        }
        if (ch instanceof Hero) {
            if (ch.buff(NervousImpairment.class) == null) {
                Buff.affect(ch, NervousImpairment.class);
            }
            float nervousDamage = 2.0f * time;
            if (Dungeon.extrastage_Sea && Dungeon.depth >= 40) {
                nervousDamage *= 2.0f;
            }
            ch.buff(NervousImpairment.class).sum(nervousDamage);
        }
        if (ch instanceof Mob) {
            ch.activateSeaTerror();
        }
    }

    private boolean isCovered() {
        Platform platform = (Platform)Dungeon.level.platforms.get(this.pos);
        return platform instanceof SeaPlatform;
    }

    public String desc() {
        String desc = Messages.get(this, "desc");
        return desc;
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        this.pos = bundle.getInt(POS);
        this.isActive = bundle.getBoolean(SEA_TERROR_ACTIVE);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        bundle.put(POS, this.pos);
        bundle.put(SEA_TERROR_ACTIVE, this.isActive);
    }
}
