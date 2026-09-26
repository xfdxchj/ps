/*
 * Decompiled with CFR 0.152.
 */
package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Random;

public class LaserParticle
extends PixelParticle {
    public static final Emitter.Factory FACTORY = new Emitter.Factory(){

        @Override
        public void emit(Emitter emitter, int index, float x, float y) {
            ((LaserParticle)emitter.recycle(LaserParticle.class)).reset(x, y);
        }

        @Override
        public boolean lightMode() {
            return true;
        }
    };
    public static final Emitter.Factory BURST = new Emitter.Factory(){

        @Override
        public void emit(Emitter emitter, int index, float x, float y) {
            ((LaserParticle)emitter.recycle(LaserParticle.class)).resetBurst(x, y);
        }

        @Override
        public boolean lightMode() {
            return true;
        }
    };

    public LaserParticle() {
        this.lifespan = 1.0f;
        this.color(6085375);
        this.speed.polar(Random.Float(6.283185f), Random.Float(24.0f, 32.0f));
    }

    public void reset(float x, float y) {
        this.revive();
        this.left = this.lifespan;
        this.x = x - this.speed.x * this.lifespan;
        this.y = y - this.speed.y * this.lifespan;
    }

    @Override
    public void update() {
        super.update();
        float p = this.left / this.lifespan;
        this.am = p < 0.5f ? p * p * 4.0f : (1.0f - p) * 2.0f;
        this.size(Random.Float(5.0f * this.left / this.lifespan));
    }

    public void resetBurst(float x, float y) {
        this.revive();
        this.x = x;
        this.y = y;
        this.speed.polar(Random.Float(6.283185f), Random.Float(16.0f, 32.0f));
        this.left = this.lifespan;
    }
}

