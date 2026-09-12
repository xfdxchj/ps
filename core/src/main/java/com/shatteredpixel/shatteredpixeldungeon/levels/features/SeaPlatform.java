//END(port from Arknights): SeaPlatform
package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
//END(暂缓): SeaBossLevel2 属海嗣Boss，本轮未搬
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Platform;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Reflection;
import java.util.ArrayList;
import java.util.List;

public class SeaPlatform
extends Platform {
    public SeaPlatform() {
        this.image = 5;
        this.generatorClass = LittleHandy.class;
    }

    @Override
    public void activate(Char ch) {
    }

    public static class LittleHandy
    extends Platform.Generator {
        public LittleHandy() {
            this.image = ItemSpriteSheet.SAINT_HAND;
            this.platformClass = SeaPlatform.class;
            this.bones = false;
        }

        @Override
        protected void onThrow(int cell) {
            if (false /*END(暂缓): 海嗣Boss未搬*/ && Dungeon.level.seaTerrors.get(cell) != null) {
                Dungeon.level.createPlatform(this, cell);
            } else {
                super.onThrow(cell);
            }
        }

        @Override
        public List<Platform> generate(int pos, Level level) {
            if (level != null && level.heroFOV != null && level.heroFOV[pos]) {
                Sample.INSTANCE.play("sounds/grass.mp3");
            }
            ArrayList<Platform> platforms = new ArrayList<Platform>();
            for (int n : PathFinder.NEIGHBOURS9) {
                int c = pos + n;
                if (c < 0 || c >= Dungeon.level.length() || Dungeon.level.platforms.get(c) != null || Dungeon.level.seaTerrors.get(c) == null || Dungeon.level.map[c] == 24) continue;
                if (Dungeon.level.heroFOV[c]) {
                    CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
                }
                Platform platform = (Platform)Reflection.newInstance(this.platformClass);
                platform.pos = c;
                platforms.add(platform);
            }
            return platforms;
        }

        @Override
        public String desc() {
            if (false /*END(暂缓): 海嗣Boss未搬*/) {
                return Messages.get(this, "desc_active");
            }
            return Messages.get(this, "desc");
        }
    }

    public static class EnhancedLittleHandy
    extends LittleHandy {
        public static final String AC_SPAWN = "SPAWN";
        private static final ItemSprite.Glowing GLOW = new ItemSprite.Glowing(0x44AAFF, 0.4f);
        private final CellSelector.Listener spawnListener;

        public EnhancedLittleHandy() {
            this.defaultAction = AC_SPAWN;
            this.stackable = false;
            this.bones = false;
            this.spawnListener = new CellSelector.Listener(){

                @Override
                public void onSelect(Integer cell) {
                    if (cell == null) {
                        return;
                    }
                    if (!Dungeon.level.heroFOV[cell]) {
                        GLog.w(Messages.get(EnhancedLittleHandy.class, "bad_target"));
                        return;
                    }
                    java.util.List<Platform> placed = Dungeon.level.createPlatform(EnhancedLittleHandy.this, cell);
                    if (placed.isEmpty()) {
                        GLog.w(Messages.get(EnhancedLittleHandy.class, "no_valid_tiles"));
                    } else {
                        curUser.spendAndNext(1.0f);
                    }
                }

                @Override
                public String prompt() {
                    return Messages.get(EnhancedLittleHandy.class, "prompt");
                }
            };
        }

        @Override
        public ItemSprite.Glowing glowing() {
            return GLOW;
        }

        @Override
        public ArrayList<String> actions(Hero hero) {
            ArrayList<String> actions = super.actions(hero);
            actions.add(AC_SPAWN);
            return actions;
        }

        @Override
        public void execute(Hero hero, String action) {
            super.execute(hero, action);
            if (false /*END(暂缓): 海嗣Boss未搬*/ && action.equals(AC_SPAWN)) {
                GameScene.selectCell(this.spawnListener);
            } else {
                GLog.w(Messages.get(this, "inactive"));
            }
        }
    }
}
