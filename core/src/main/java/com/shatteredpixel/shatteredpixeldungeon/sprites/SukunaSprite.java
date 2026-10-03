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
		texture( Assets.Sprites.SUKUNA_USER3 );

		TextureFilm frames = new TextureFilm( texture, 37, 64 );

		idle = new Animation( 14, true );
		idle.frames( frames, 0, 0, 1, 1 );

		run = idle.clone();

		attack = new Animation( 8, false );
		attack.frames( frames, 0, 1, 0 );

		die = new Animation( 10, false );
		die.frames( frames, 1, 0 );

		play( idle );
	}
}
