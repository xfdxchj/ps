//END(port from Arknights): ShotgunWeapon
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CloserangeShot;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.GunWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShotgunWeapon
extends GunWeapon {
    protected int PELLET_COUNT = 5;
    protected float CONE_DEGREES = 60.0f;
    protected float EXTRA_PELLET_MULT = 0.33f;
    private ArrayList<Ballistica> cachedRays;

    @Override
    public int max(int lvl) {
        return 3 + 4 * this.tier + lvl * (this.tier - 1);
    }

    protected float effectiveCone() {
        float cone = this.CONE_DEGREES;
        if (this.gunAccessories != null) {
            cone *= this.gunAccessories.GetCONEcorrectionvalue();
        }
        return cone;
    }

    protected String coneDesc() {
        float cone = this.effectiveCone();
        if (cone <= 50.0f) {
            return Messages.get(ShotgunWeapon.class, "cone_narrow");
        }
        if (cone >= 70.0f) {
            return Messages.get(ShotgunWeapon.class, "cone_wide");
        }
        return Messages.get(ShotgunWeapon.class, "cone_typical");
    }

    @Override
    public String statsInfo() {
        if (this.specialBullet > 0) {
            return Messages.get(this, "stats_desc_sp", this.fireMin(), this.fireMax(), this.specialBullet, this.getMinRange(), this.getMaxRange(), this.PELLET_COUNT, this.coneDesc());
        }
        return Messages.get(this, "stats_desc", this.fireMin(), this.fireMax(), this.getMinRange(), this.getMaxRange(), this.PELLET_COUNT, this.coneDesc());
    }

    protected ArrayList<Ballistica> computePelletRays(Ballistica centerBolt) {
        int i;
        int from = Dungeon.hero.pos;
        int w = Dungeon.level.width();
        int h = Dungeon.level.height();
        PointF fromP = new PointF((float)(from % w) + 0.5f, (float)(from / w) + 0.5f);
        int target = centerBolt.collisionPos;
        PointF toP = new PointF((float)(target % w) + 0.5f, (float)(target / w) + 0.5f);
        float centerAngle = PointF.angle(fromP, toP) / ((float)Math.PI / 180);
        float scanDist = (float)this.getMaxRange() * 3.0f;
        float halfCone = this.effectiveCone() / 2.0f;
        int centerFarCell = ShotgunWeapon.angleFarCell(fromP, centerAngle, scanDist, w, h);
        ArrayList<Integer> coneCells = new ArrayList<Integer>();
        for (float a = centerAngle - halfCone; a <= centerAngle + halfCone; a += 0.5f) {
            int cell = ShotgunWeapon.angleFarCell(fromP, a, scanDist, w, h);
            if (cell == from || cell == centerFarCell || coneCells.contains(cell)) continue;
            coneCells.add(cell);
        }
        ArrayList<Ballistica> rays = new ArrayList<Ballistica>();
        if (centerFarCell != from) {
            rays.add(new Ballistica(from, centerFarCell, 7));
        } else {
            rays.add(centerBolt);
        }
        if (coneCells.isEmpty()) {
            for (i = 0; i < this.PELLET_COUNT - 1; ++i) {
                rays.add(new Ballistica(from, centerFarCell, 7));
            }
        } else if (coneCells.size() <= this.PELLET_COUNT - 1) {
            for (i = 0; i < this.PELLET_COUNT - 1; ++i) {
                int cell = (Integer)coneCells.get(Random.Int(coneCells.size()));
                rays.add(new Ballistica(from, cell, 7));
            }
        } else {
            int buckets = this.PELLET_COUNT - 1;
            for (int b = 0; b < buckets; ++b) {
                int bucketStart = b * coneCells.size() / buckets;
                int bucketEnd = (b + 1) * coneCells.size() / buckets;
                int idx = Random.IntRange(bucketStart, bucketEnd - 1);
                rays.add(new Ballistica(from, (Integer)coneCells.get(idx), 7));
            }
        }
        return rays;
    }

    protected static int angleFarCell(PointF from, float angleDeg, float dist, int w, int h) {
        PointF p = new PointF();
        p.polar(angleDeg * ((float)Math.PI / 180), dist);
        p.offset(from);
        int cx = Math.max(0, Math.min((int)Math.floor(p.x), w - 1));
        int cy = Math.max(0, Math.min((int)Math.floor(p.y), h - 1));
        return cy * w + cx;
    }

    protected int rayEndPos(Ballistica ray) {
        int cappedDist = Math.min(ray.dist, this.getMaxRange());
        return ray.path.get(cappedDist);
    }

    @Override
    protected void fx(Ballistica bolt, Callback callback) {
        this.cachedRays = this.computePelletRays(bolt);
        if (this.cachedRays.isEmpty()) {
            callback.call();
            return;
        }
        int callbackIdx = this.cachedRays.size() / 2;
        for (int i = 0; i < this.cachedRays.size(); ++i) {
            Callback cb = i == callbackIdx ? callback : null;
            ((MagicMissile)ShotgunWeapon.curUser.sprite.parent.recycle(MagicMissile.class)).reset(102, (Visual)ShotgunWeapon.curUser.sprite, this.rayEndPos(this.cachedRays.get(i)), cb);
        }
        Sample.INSTANCE.play(this.hitSound);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void onZap(Ballistica bolt) {
        CloserangeShot closerRange = Dungeon.hero.buff(CloserangeShot.class);
        float oldacc = this.ACC;
        boolean anyKill = false;
        try {
            ArrayList<Ballistica> rays = this.cachedRays;
            if (rays == null) {
                rays = this.computePelletRays(bolt);
            }
            LinkedHashMap<Char, Integer> targetPellets = new LinkedHashMap<Char, Integer>();
            ArrayList<Integer> emptyCells = new ArrayList<Integer>();
            for (Ballistica ballistica : rays) {
                int endPos = this.rayEndPos(ballistica);
                Char ch = Actor.findChar(endPos);
                if (ch != null && ch != Dungeon.hero) {
                    Integer count = (Integer)targetPellets.get(ch);
                    targetPellets.put(ch, (count != null ? count : 0) + 1);
                    continue;
                }
                if (endPos == Dungeon.hero.pos) continue;
                emptyCells.add(endPos);
            }
            if (!targetPellets.isEmpty()) {
                Buff.affect(Dungeon.hero, GunWeapon.RangedAttackTracker.class);
            }
            for (Map.Entry entry : targetPellets.entrySet()) {
                Char ch = (Char)entry.getKey();
                int pelletCount = (Integer)entry.getValue();
                int targetDr = ch.drRoll();
                int extraDr = targetDr > 0 ? Math.max(1, Math.round((float)targetDr * this.EXTRA_PELLET_MULT)) : 0;
                for (int i = 0; i < pelletCount && ch.isAlive(); ++i) {
                    float dmgMult = i == 0 ? 1.0f : this.EXTRA_PELLET_MULT;
                    boolean triggerProcs = i == 0;
                    int pelletDr = i == 0 ? targetDr : extraDr;
                    this.processGunHit(ch, dmgMult, triggerProcs, pelletDr);
                }
                if (ch.isAlive()) continue;
                anyKill = true;
            }
            java.util.Iterator<Integer> iterator = emptyCells.iterator();
            while (iterator.hasNext()) {
                int n = (Integer)iterator.next();
                com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap(n);
            }
            this.postShotCleanup(closerRange, false, anyKill);
        }
        finally {
            this.ACC = oldacc;
            this.cachedRays = null;
        }
    }
}
