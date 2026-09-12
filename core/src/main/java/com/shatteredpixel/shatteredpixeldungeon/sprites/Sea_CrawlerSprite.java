//END(port from Arknights): Sea_CrawlerSprite
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class Sea_CrawlerSprite
extends MobSprite {
    public Sea_CrawlerSprite() {
        this.texture("sprites/sea_crawler.png");
        TextureFilm frames = new TextureFilm(this.texture, 34, 36);
        this.idle = new MovieClip.Animation(10, true);
        this.idle.frames(frames, 0);
        this.run = new MovieClip.Animation(8, true);
        this.run.frames(frames, 0, 1, 2, 3, 4, 5);
        this.attack = new MovieClip.Animation(15, false);
        this.attack.frames(frames, 0);
        this.die = new MovieClip.Animation(10, false);
        this.die.frames(frames, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17);
        this.play(this.idle);
    }
}
