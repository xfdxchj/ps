/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.FT;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ConeAOE;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import java.util.ArrayList;

public class FT
extends Gun {
    public FT() {
        this.round = this.max_round = 2;
        this.shootingAccuracy = 1.5f;
    }

    @Override
    public int baseBulletMax(int lvl) {
        return 3 * (this.tier + 1) + lvl * (this.tier + 1);
    }

    @Override
    public int bulletUse() {
        return Math.max(0, (this.maxRound() - this.round) * 2);
    }

    @Override
    public Gun.Bullet knockBullet() {
        return new FTBullet();
    }

    public class FTBullet
    extends Gun.Bullet {
        public FTBullet() {
            super();
            this.hitSound = "sounds/burning.mp3";
            this.image = ItemSpriteSheet.NO_BULLET;
        }

        @Override
        protected void onThrow(int cell) {
            if (cell != FTBullet.curUser.pos) {
                Ballistica aim = new Ballistica(FTBullet.curUser.pos, cell, 0);
                int maxDist = FT.this.tier + 1;
                int dist = Math.min(aim.dist, maxDist);
                ConeAOE cone = new ConeAOE(aim, dist, 30.0f, 13);
                for (Ballistica ray : cone.outerRays) {
                    ((MagicMissile)FTBullet.curUser.sprite.parent.recycle(MagicMissile.class)).reset(102, (Visual)FTBullet.curUser.sprite, (int)ray.path.get(ray.dist), null);
                }
                ArrayList<Char> chars = new ArrayList<Char>();
                for (int cells : cone.cells) {
                    Char ch;
                    if (Dungeon.level.map[cells] == com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WATER) {
                        Level.set(cells, com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY);
                        GameScene.updateMap(cells);
                    }
                    if (!Dungeon.level.adjacent(FTBullet.curUser.pos, cells) || Dungeon.level.flamable[cells]) {
                        GameScene.add(Blob.seed(cells, 2, Fire.class));
                    }
                    if ((ch = Actor.findChar(cells)) == null || ch.alignment == Dungeon.hero.alignment) continue;
                    chars.add(ch);
                }
                for (Char ch : chars) {
                    for (int i = 0; i < FT.this.shotPerShoot(); ++i) {
                        curUser.shoot(ch, this);
                    }
                    if (ch != Dungeon.hero || ch.isAlive()) continue;
                    Dungeon.fail(this.getClass());
                    Badges.validateDeathFromFriendlyMagic();
                    GLog.n(Messages.get(Gun.class, "ondeath"), new Object[0]);
                }
                MagicMissile.boltFromChar(FTBullet.curUser.sprite.parent, 102, FTBullet.curUser.sprite, cone.coreRay.path.get(dist * 2 / 3), new Callback(){

                    @Override
                    public void call() {
                    }
                });
            }
            Invisibility.dispel();
            this.onShoot();
        }

        @Override
        public void throwSound() {
            Sample.INSTANCE.play("sounds/burning.mp3", 1.0f);
        }
    }
}

