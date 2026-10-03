/*
 * 破碎的地牢 (End fork) — 挑战 237 宿傩的精灵。
 *
 * 按文档所有者要求，直接用**原版古神**贴图（yog.png 的备份，16x16 帧）。
 */
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class SukunaSprite extends MobSprite {

	public SukunaSprite() {
		super();
		perspectiveRaise = 5 / 16f;
		texture( Assets.Sprites.SUKUNA_GRIMM );

		TextureFilm frames = new TextureFilm( texture, 50, 42 );

		idle = new Animation( 10, true );
		idle.frames( frames, 0, 1, 2, 3, 4, 5, 6, 7, 8 );

		run = idle.clone();

		attack = new Animation( 12, false );
		attack.frames( frames, 0, 2, 4, 6, 8, 0 );

		die = new Animation( 10, false );
		die.frames( frames, 8, 7, 6, 5, 4, 3, 2, 1, 0 );

		play( idle );
	}
}
