//END(port from Arknights): WaterParticle
package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.Random;

public class WaterParticle
extends PixelParticle {
    public static final Emitter.Factory FALLING = new Emitter.Factory(){

        @Override
        public void emit(Emitter emitter, int index, float x, float y) {
            ((WaterParticle)emitter.recycle(WaterParticle.class)).resetFalling(x, y);
        }
    };
    public static final Emitter.Factory SPLASHING = new Emitter.Factory(){

        @Override
        public void emit(Emitter emitter, int index, float x, float y) {
            ((WaterParticle)emitter.recycle(WaterParticle.class)).resetSplashing(x, y);
        }
    };

    public WaterParticle() {
        this.color(ColorMath.random(413351, 138558));
        this.angle = Random.Float(-30.0f, 30.0f);
    }

    public void reset(float x, float y) {
        this.revive();
        this.x = x;
        this.y = y;
        this.lifespan = 0.5f;
        this.left = 0.5f;
        this.size = 16.0f;
    }

    public void resetFalling(float x, float y) {
        this.reset(x, y);
        this.lifespan = 1.0f;
        this.left = 1.0f;
        this.size = 8.0f;
        this.acc.y = 30.0f;
        this.speed.y = -5.0f;
        this.angularSpeed = Random.Float(-90.0f, 90.0f);
    }

    public void resetSplashing(float x, float y) {
        this.reset(x, y);
        this.angularSpeed = Random.Float(-90.0f, 90.0f);
        this.size = 24.0f;
        this.acc.y = 30.0f;
        this.speed.y = 32.0f;
        this.y -= this.speed.y * this.lifespan;
    }

    @Override
    public void update() {
        super.update();
        float p = this.left / this.lifespan;
        this.size((p < 0.5f ? p : 1.0f - p) * this.size);
    }
}
