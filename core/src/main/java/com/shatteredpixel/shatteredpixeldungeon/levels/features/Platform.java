//END(port from Arknights): Platform
package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;
import java.util.List;

public abstract class Platform
implements Bundlable {
    public String platformName = Messages.get(this, "name");
    public int image;
    public int pos;
    protected Class<? extends Generator> generatorClass;
    private static final String POS = "pos";

    public void trigger() {
        Char ch = Actor.findChar(this.pos);
        this.activate(ch);
    }

    public abstract void activate(Char var1);

    public void destroy() {
        Dungeon.level.destroyPlatform(this.pos);
        if (Dungeon.level.heroFOV[this.pos]) {
            CellEmitter.get(this.pos).burst(FlameParticle.FACTORY, 6);
        }
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        this.pos = bundle.getInt(POS);
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        bundle.put(POS, this.pos);
    }

    public String desc() {
        String desc = Messages.get(this, "desc");
        return desc;
    }

    public static class Generator
    extends Item {
        protected Class<? extends Platform> platformClass;

        public Generator() {
            this.stackable = true;
            this.defaultAction = "THROW";
        }

        public List<Platform> generate(int pos, Level level) {
            Platform platform = Reflection.newInstance(this.platformClass);
            platform.pos = pos;
            return List.of(platform);
        }

        @Override
        public boolean isUpgradable() {
            return false;
        }

        @Override
        public boolean isIdentified() {
            return true;
        }

        @Override
        public int value() {
            return 5 * this.quantity;
        }
    }
}
