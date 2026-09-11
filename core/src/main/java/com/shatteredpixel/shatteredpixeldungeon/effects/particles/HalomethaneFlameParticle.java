package com.shatteredpixel.shatteredpixeldungeon.effects.particles;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;

//END(移植自魔绫·挑战区): 卤甲烷/霜焰式粒子（DeathRong 死亡用）
public class HalomethaneFlameParticle extends PixelParticle.Shrinking {

    public static final Emitter.Factory FACTORY = new Emitter.Factory() {
        @Override
        public void emit( Emitter emitter, int index, float x, float y ) {
            ((HalomethaneFlameParticle)emitter.recycle( HalomethaneFlameParticle.class )).reset( x, y );
        }
        @Override
        public boolean lightMode() {
            return true;
        };
    };

    public HalomethaneFlameParticle() {
        super();

        if(Dungeon.branch == 2 && Dungeon.depth == 8){
            color(0xE43C2F);
        } else{
            color(0x00DFFF);
        }

        lifespan = 0.6f;

        acc.set( 0, -80 );
    }

    public void reset( float x, float y ) {
        revive();

        this.x = x;
        this.y = y;

        left = lifespan;

        size = 4;
        speed.set( 0 );
    }

    @Override
    public void update() {
        super.update();
        float p = left / lifespan;
        am = p > 0.8f ? (1 - p) * 5 : 1;
    }
}
