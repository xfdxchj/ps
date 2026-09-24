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

package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.TitleScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.WelcomeScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;

public class ShatteredPixelDungeon extends Game {

	//rankings from v1.2.3 and older use a different score formula, so this reference is kept
	public static final int v1_2_3 = 628;

	//savegames from versions older than v2.5.4 are no longer supported, and data from them is ignored
	public static final int v2_5_4 = 802;

	public static final int v3_0_2 = 833;
	public static final int v3_1_1 = 850;
	public static final int v3_2_5 = 877;
	public static final int v3_3_0 = 883;
	
	public ShatteredPixelDungeon( PlatformSupport platform ) {
		super( sceneClass == null ? WelcomeScene.class : sceneClass, platform );

		//pre-v3.3.0
		com.watabou.utils.Bundle.addAlias(
				com.shatteredpixel.shatteredpixeldungeon.items.keys.WornKey.class,
				"com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey" );

	}
	
	@Override
	public void create() {
		super.create();

		updateSystemUI();
		SPDAction.loadBindings();
		
		Music.INSTANCE.enable( SPDSettings.music() );
		Music.INSTANCE.volume( SPDSettings.musicVol()*SPDSettings.musicVol()/100f );
		Sample.INSTANCE.enable( SPDSettings.soundFx() );
		Sample.INSTANCE.volume( SPDSettings.SFXVol()*SPDSettings.SFXVol()/100f );

		//==== END(移植·我的世界 228): 注册音效替换钩子 ====
		//必须在批量加载之前注册 —— 否则这批音效会按原路径加载。
		com.watabou.noosa.audio.Sample.setPathMapper(
			com.shatteredpixel.shatteredpixeldungeon.endcontent.JingmiAssets::soundFor);

		Sample.INSTANCE.load( Assets.Sounds.all );

		//END(挑战 130 格林之音): 注册 BGM 替换钩子。
		//为什么在这里而不是 Dungeon.init()：Music.setTrackMapper 是**全局静态**，
		//只需注册一次；而 Dungeon.init() 每开一局都会调用。
		//转换器内部自行判断当前掩码是否勾选了 130，未勾选时原样返回，零影响。
		Music.setTrackMapper(
				com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeSfx::grimmTrackFor);

		//END(诊断 130): 确认钩子注册成功。
		//这条日志只有一个目的：区分"钩子没注册"与"钩子注册了但没匹配上曲目"。
		//如果游戏里 BGM 换成格林主题却没看到 [格林之音] 日志，
		//说明是 Music.play 那条路径没走到（而不是映射表的问题）。
		System.out.println("[格林之音] TrackMapper 已注册至 Music（挑战 130 的 BGM 替换钩子）");

		//END(挑战 189 时间加速): 注册全局速度倍率钩子。
		//与 TrackMapper 同一套做法（底层模块不能反向依赖 core）。
		//未勾选 189 时返回 1，行为与原来完全一致。
		com.watabou.noosa.Game.setTimeScaleProvider(
				com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeEffects::timeScale);

	}

	@Override
	public void finish() {
		if (!DeviceCompat.isiOS()) {
			super.finish();
		} else {
			//can't exit on iOS (Apple guidelines), so just go to title screen
			switchScene(TitleScene.class);
		}
	}

	public static void switchNoFade(Class<? extends PixelScene> c){
		switchNoFade(c, null);
	}

	public static void switchNoFade(Class<? extends PixelScene> c, SceneChangeCallback callback) {
		PixelScene.noFade = true;
		switchScene( c, callback );
	}
	
	public static void seamlessResetScene(SceneChangeCallback callback) {
		if (scene() instanceof PixelScene){
			((PixelScene) scene()).saveWindows();
			switchNoFade((Class<? extends PixelScene>) sceneClass, callback );
		} else {
			resetScene();
		}
	}
	
	public static void seamlessResetScene(){
		seamlessResetScene(null);
	}
	
	@Override
	protected void switchScene() {
		super.switchScene();
		if (scene instanceof PixelScene){
			((PixelScene) scene).restoreWindows();
		}
	}
	
	@Override
	public void resize( int width, int height ) {
		if (width == 0 || height == 0){
			return;
		}

		if (scene instanceof PixelScene &&
				(height != Game.height || width != Game.width)) {
			PixelScene.noFade = true;
			((PixelScene) scene).saveWindows();
		}

		super.resize( width, height );

		updateDisplaySize();

	}
	
	@Override
	public void destroy(){
		super.destroy();
		GameScene.endActorThread();
	}
	
	public void updateDisplaySize(){
		platform.updateDisplaySize();
	}

	public static void updateSystemUI() {
		platform.updateSystemUI();
	}
}