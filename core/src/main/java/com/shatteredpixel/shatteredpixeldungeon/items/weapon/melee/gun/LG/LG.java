/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.LG;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import java.util.ArrayList;

public class LG
extends Gun {
    public LG() {
        this.round = this.max_round = 2;
        this.shootingAccuracy = 1.5f;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return 3 * (this.tier + 1) + lvl * (this.tier + 1);
    }

    @Override
    public int bulletUse() {
        return Math.max(0, (this.maxRound() - this.round) * 3);
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new LGBullet();
    }

    public class LGBullet
    extends Gun.Bullet {
        public LGBullet() {
            this.hitSound = "sounds/burning.mp3";
            this.image = ItemSpriteSheet.NO_BULLET;
        }

        @Override
        protected void onThrow(int cell) {
            if (cell != LGBullet.curUser.pos) {
                float multi;
                Ballistica aim = new Ballistica(LGBullet.curUser.pos, cell, 0);
                ArrayList<Char> chars = new ArrayList<Char>();
                int maxDist = 2 * (LG.this.tier + 1);
                int dist = Math.min(aim.dist, maxDist);
                int cells = aim.path.get(Math.min(aim.dist, dist));
                boolean terrainAffected = false;
                for (int c : aim.subPath(1, maxDist)) {
                    Char ch = Actor.findChar(c);
                    if (ch != null) {
                        chars.add(ch);
                    }
                    if (Dungeon.level.flamable[c]) {
                        Dungeon.level.destroy(c);
                        GameScene.updateMap(c);
                        terrainAffected = true;
                    }
                    CellEmitter.center(c).burst(SparkParticle.FACTORY, 3);
                }
                if (terrainAffected) {
                    Dungeon.observe();
                }
                switch (LG.this.weightMod) {
                    default: {
                        multi = 2.0f;
                        break;
                    }
                    case LIGHT_WEIGHT: {
                        multi = 1.0f;
                        break;
                    }
                    case HEAVY_WEIGHT: {
                        multi = 3.0f;
                    }
                }
                LGBullet.curUser.sprite.parent.add(new Beam.SunRay(LGBullet.curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld(cells)));
                for (Char ch : chars) {
                    for (int i = 0; i < LG.this.shotPerShoot(); ++i) {
                        if (!curUser.shoot(ch, this)) continue;
                        ch.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10 + this.buffedLvl());
                    }
                    if (ch != Dungeon.hero || ch.isAlive()) continue;
                    Dungeon.fail(this.getClass());
                    Badges.validateDeathFromFriendlyMagic();
                    GLog.n(Messages.get(Gun.class, "ondeath"), new Object[0]);
                }
            }
            Invisibility.dispel();
            this.onShoot();
        }

        @Override
        public void throwSound() {
            Sample.INSTANCE.play("sounds/ray.mp3", 1.0f);
        }
    }
}

