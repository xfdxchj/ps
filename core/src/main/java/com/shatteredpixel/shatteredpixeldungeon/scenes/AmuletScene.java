/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.effects.BadgeBanner;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Amulet;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.tweeners.Delayer;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;

public class AmuletScene extends PixelScene {
	
	private static final int WIDTH			= 120;
	private static final int BTN_HEIGHT		= 20;
	private static final float SMALL_GAP	= 2;
	private static final float LARGE_GAP	= 8;
	
	public static boolean noText = false;
	
	private Image amulet;

	{
		inGameScene = true;
	}

	StyledButton btnExit = null;
	StyledButton btnStay = null;
	
	@Override
	public void create() {
		super.create();
		
		RenderedTextBlock text = null;
		if (!noText) {
			text = renderTextBlock( Messages.get(this, "text"), 8 );
			text.maxWidth( PixelScene.landscape() ? 2*WIDTH-4 : WIDTH);
		}
		
		amulet = new Image( Assets.Sprites.AMULET );
		add( amulet );

		btnExit = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "exit") ) {
			@Override
			protected void onClick() {
				Dungeon.win( Amulet.class );
				Dungeon.deleteGame( GamesInProgress.curSlot, true );
				Badges.saveGlobal();
				btnExit.enable(false);
				btnStay.enable(false);

				AmuletScene.this.add(new Delayer(0.1f){
					@Override
					protected void onComplete() {
						if (BadgeBanner.isShowingBadges()){
							AmuletScene.this.add(new Delayer(3f){
								@Override
								protected void onComplete() {
									Game.switchScene( RankingsScene.class );
								}
							});
						} else {
							Game.switchScene( RankingsScene.class );
						}
					}
				});
				Music.INSTANCE.playTracks(
						new String[]{Assets.Music.THEME_2, Assets.Music.THEME_1},
						new float[]{1, 1},
						false);
			}
		};
		btnExit.icon(new ItemSprite(ItemSpriteSheet.AMULET));
		btnExit.setSize( WIDTH, BTN_HEIGHT );
		add( btnExit );
		
		btnStay = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "stay") ) {
			@Override
			protected void onClick() {
				onBackPressed();
				btnExit.enable(false);
				btnStay.enable(false);
			}
		};
		btnStay.icon(Icons.CLOSE.get());
		btnStay.setSize( WIDTH, BTN_HEIGHT );
		add( btnStay );

		//==== END(真·无尽): 九轮走完后，护符上多一个"陷入无尽轮回" ====
		//文档所有者定稿："可以在第九次后的古神护符加一个，陷入无尽轮回，
		//开始真正的无尽。" 点了就不再走结局，直接下到下一层继续轮回。
		final boolean showEndless = com.shatteredpixel.shatteredpixeldungeon.endcontent
			.Reincarnation.enabled()
			&& com.shatteredpixel.shatteredpixeldungeon.endcontent.Reincarnation.cycles()
				>= com.shatteredpixel.shatteredpixeldungeon.endcontent.Reincarnation.maxCycles()
			&& !com.shatteredpixel.shatteredpixeldungeon.endcontent.Reincarnation.isTrueEndless();
		StyledButton btnEndless = null;
		if (showEndless) {
			btnEndless = new StyledButton(Chrome.Type.GREY_BUTTON_TR, "陷入无尽轮回") {
				@Override
				protected void onClick() {
					com.shatteredpixel.shatteredpixeldungeon.endcontent.Reincarnation
						.startTrueEndless();
					com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
						"护符不肯放你走 —— 你坠入了更深的轮回。");
					btnExit.enable(false);
					btnStay.enable(false);
					//直接下到下一层：shouldEnd() 已被关掉，那里会生成新的轮回层
					com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.mode =
						com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.Mode.DESCEND;
					com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition =
						new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition();
					com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene
						.curTransition.destDepth = Dungeon.depth + 1;
					com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene
						.curTransition.destBranch = Dungeon.branch;
					com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.destType =
						com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type
							.REGULAR_ENTRANCE;
					com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.curTransition.type =
						com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type
							.REGULAR_EXIT;
					com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene
						.curTransition.centerCell = -1;
					com.watabou.noosa.Game.switchScene(
						com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.class);
				}
			};
			btnEndless.setSize( WIDTH, BTN_HEIGHT );
			add( btnEndless );
		}

		RectF insets = getCommonInsets();
		int w = (int) (Camera.main.width - insets.left + insets.right);
		int h = (int) (Camera.main.height - insets.top + insets.bottom);

		float height;
		if (noText) {
			height = amulet.height + LARGE_GAP + btnExit.height() + SMALL_GAP + btnStay.height() + (btnEndless != null ? SMALL_GAP + btnEndless.height() : 0);
			
			amulet.x = insets.left + (w - amulet.width) / 2;
			amulet.y = insets.top + (h - height) / 2;
			align(amulet);

			btnExit.setPos( insets.left + (w - btnExit.width()) / 2, amulet.y + amulet.height + LARGE_GAP );
			btnStay.setPos( btnExit.left(), btnExit.bottom() + SMALL_GAP );
			if (btnEndless != null) btnEndless.setPos( btnStay.left(), btnStay.bottom() + SMALL_GAP );
			
		} else {
			height = amulet.height + LARGE_GAP + text.height() + LARGE_GAP + btnExit.height() + SMALL_GAP + btnStay.height() + (btnEndless != null ? SMALL_GAP + btnEndless.height() : 0);

			amulet.x = insets.left + (w - amulet.width) / 2;
			amulet.y = insets.top + (h - height) / 2;
			align(amulet);

			text.setPos(insets.left + (w - text.width()) / 2, amulet.y + amulet.height + LARGE_GAP);
			align(text);
			add(text);

			btnExit.setPos( insets.left + (w - btnExit.width()) / 2, text.top() + text.height() + LARGE_GAP );
			btnStay.setPos( btnExit.left(), btnExit.bottom() + SMALL_GAP );
			if (btnEndless != null) btnEndless.setPos( btnStay.left(), btnStay.bottom() + SMALL_GAP );
		}

		new Flare( 8, 48 ).color( 0xFFDDBB, true ).show( amulet, 0 ).angularSpeed = +30;
		
		fadeIn();
	}
	
	@Override
	protected void onBackPressed() {
		if (btnExit.isActive()) {
			InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			Game.switchScene(InterlevelScene.class);
		}
	}
	
	private float timer = 0;
	
	@Override
	public void update() {
		super.update();
		
		if ((timer -= Game.elapsed) < 0) {
			timer = Random.Float( 0.5f, 5f );
			
			Speck star = (Speck)recycle( Speck.class );
			star.reset( 0, amulet.x + 10.5f, amulet.y + 5.5f, Speck.DISCOVER );
			add( star );
		}
	}
}
