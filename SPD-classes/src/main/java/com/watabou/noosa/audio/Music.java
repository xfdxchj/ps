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

package com.watabou.noosa.audio;

import com.badlogic.gdx.Gdx;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;

public enum Music {
	
	INSTANCE;
	
	private com.badlogic.gdx.audio.Music player;
	
	private String lastPlayed;
	private boolean looping;
	
	/** END(诊断 130): 音乐播放诊断开关。定稿后改回 false。 */
	public static boolean DEBUG_MUSIC = true;

	private boolean enabled = true;
	private float volume = 1f;

	private float fadeTime = -1f;
	private float fadeTotal = -1f;
	private Callback onFadeOut = null;

	String[] trackList;
	float[] trackChances;
	private final ArrayList<String> trackQueue = new ArrayList<>();
	boolean shuffle = false;

	//END(移植自魔绫): 桌面端高质量 ogg 的线程安全闪退规避——所有 BGM 切换统一走这个入口。
	public static void playModeBGM(String name, boolean loop) {
		if (DeviceCompat.isDesktop()) {
			Game.runOnRenderThread(() -> Music.INSTANCE.play(name, loop));
		} else {
			Music.INSTANCE.play(name, loop);
		}
	}

	//==== END(挑战 130 格林之音): BGM 替换钩子 ====
	//为什么用回调而不是直接调用：本类在 SPD-classes（底层模块），
	//不能反向依赖 core（会形成循环依赖、编译不过）。
	//因此由 core 在启动时注册一个"曲目转换器"，这里只负责调用。
	//未注册时（或转换器返回原值）行为与原来完全一致，零影响。

	/** 曲目转换器：输入原曲目路径，返回实际要播放的路径。 */
	public interface TrackMapper {
		String map(String original);

		/**
		 * END(修复·取消勾选 130 后音乐没换回): 告知"当前实际在播的曲目"。
		 *
		 * <p>映射之后才知道到底播了哪首 —— 取消勾选时那一侧要靠它
		 * 找到该换回的原版曲。默认空实现，不影响既有实现类。
		 */
		default void notePlaying(String playing) {}
	}

	private static TrackMapper trackMapper;

	/** 由 core 层注册（见 ChallengeSfx 的初始化）。传 null 可取消。 */
	public static void setTrackMapper(TrackMapper mapper) {
		trackMapper = mapper;
	}

	public synchronized void play( String assetName, boolean looping ) {

		//==== END(诊断 130): 确认 play() 到底有没有被调用 ====
		//文档所有者反馈"格林之音还是没生效"，但连一条 [格林之音] 日志都没有 ——
		//那说明 **play() 根本没被走到**（而不是映射表的问题）。
		//
		//这条日志回答三个问题：
		//  1) play() 有没有被调用
		//  2) 音乐开关是不是关的（enabled=false 会在下面直接 return）
		//  3) 收到的原始曲目名是什么
		if (DEBUG_MUSIC) {
			System.out.println("[Music.play] 收到=" + assetName
					+ "  enabled=" + enabled
					+ "  mapper=" + (trackMapper == null ? "未注册" : "已注册")
					+ "  isPlaying=" + isPlaying()
					+ "  lastPlayed=" + lastPlayed);
		}

		//END(挑战 130): 在**所有**处理之前替换曲目名 ——
		//这样 lastPlayed 比较、iOS 的 .mp3 替换、play(assetName,null) 全都拿到替换后的路径，
		//不会出现"已经换成格林 BGM 了但 lastPlayed 记的是原曲"导致的重复播放。
		if (trackMapper != null && assetName != null) {
			String mapped = trackMapper.map(assetName);
			if (mapped != null) {
				assetName = mapped;
			}
		}

		//==== END(修复·取消勾选 130 后音乐没换回) ====
		//映射**之后**的 assetName 才是"实际在播的曲目" ——
		//把它告诉 trackMapper 那一侧，取消勾选时才知道该把哪首换回原版
		//（见 ChallengeSfx.reconcileGrimmMusic）。
		if (trackMapper != null) {
			trackMapper.notePlaying(assetName);
		}

		//iOS cannot play ogg, so we use an mp3 alternative instead
		if (assetName != null && DeviceCompat.isiOS()){
			assetName = assetName.replace(".ogg", ".mp3");
		}
		
		if (isPlaying() && lastPlayed != null && lastPlayed.equals( assetName )) {
			player.setVolume(volumeWithFade());
			return;
		}
		
		stop();
		
		lastPlayed = assetName;
		trackList = null;

		this.looping = looping;
		this.shuffle = false;

		if (!enabled || assetName == null) {
			return;
		}

		play(assetName, null);
	}

	public synchronized void playTracks( String[] tracks, float[] chances, boolean shuffle){

		if (DEBUG_MUSIC) {
			System.out.println("[Music.playTracks] 曲目数=" + (tracks == null ? -1 : tracks.length)
					+ "  enabled=" + enabled
					+ "  第一首=" + (tracks != null && tracks.length > 0 ? tracks[0] : "无"));
		}

		if (tracks == null || tracks.length == 0 || tracks.length != chances.length){
			stop();
			return;
		}

		//iOS cannot play ogg, so we use an mp3 alternative instead
		if (tracks != null && DeviceCompat.isiOS()){
			for (int i = 0; i < tracks.length; i ++){
				tracks[i] = tracks[i].replace(".ogg", ".mp3");
			}
		}

		if (isPlaying() && this.trackList != null && tracks.length == trackList.length){

			//lists are considered the same if they are identical or merely shifted
			// e.g. the regular title theme and the victory theme are considered equivalent
			boolean sameList = false;
			for (int ofs = 0; ofs < tracks.length; ofs++){
				sameList = true;
				for (int j = 0; j < tracks.length; j++){
					int i = (j+ofs)%tracks.length;
					if (!tracks[i].equals(trackList[j]) || chances[i] != trackChances[j]){
						sameList = false;
						break;
					}
				}
				if (sameList) break;
			}

			if (sameList) {
				player.setVolume(volumeWithFade());
				return;
			}
		}

		stop();

		lastPlayed = null;
		trackList = tracks;
		trackChances = chances;
		trackQueue.clear();

		for (int i = 0; i < trackList.length; i++){
			if (Random.Float() < trackChances[i]){
				trackQueue.add(trackList[i]);
			}
		}

		this.looping = false;
		this.shuffle = shuffle;

		//==== END(诊断 130): 打印队列构建结果 ====
		//上一版只打了"收到 6 首"，但没有任何 [Music.play] ——
		//说明 playTracks 内部**没走到 play()**。这条日志区分两种可能：
		//  · 队列为空（所有 Random.Float() 都没中概率）
		//  · 队列非空但 enabled=false
		if (DEBUG_MUSIC) {
			System.out.println("[Music.playTracks] 队列=" + trackQueue.size()
					+ " / " + trackList.length
					+ "  enabled=" + enabled
					+ "  队列内容=" + trackQueue);
		}

		if (!enabled || trackQueue.isEmpty()){
			if (DEBUG_MUSIC && trackQueue.isEmpty()) {
				System.out.println("[Music.playTracks] 队列为空 → 不播放任何曲目！");
			}
			return;
		}

		play(trackQueue.remove(0), trackLooper);
	}

	public synchronized void fadeOut(float duration, Callback onComplete){
		if (fadeTotal == -1f) {
			fadeTotal = duration;
			fadeTime = 0f;
		} else {
			fadeTime = (fadeTime/fadeTotal) * duration;
			fadeTotal = duration;
		}
		onFadeOut = onComplete;
	}

	public synchronized void update(){
		if (fadeTotal > 0f && !paused){
			fadeTime += Game.elapsed;

			if (player != null) {
				player.setVolume(volumeWithFade());
			}

			if (fadeTime >= fadeTotal) {
				fadeTime = fadeTotal = -1f;
				if (onFadeOut != null){
					onFadeOut.call();
				}
			}
		}
	}

	private com.badlogic.gdx.audio.Music.OnCompletionListener trackLooper = new com.badlogic.gdx.audio.Music.OnCompletionListener() {
		@Override
		public void onCompletion(com.badlogic.gdx.audio.Music music) {
			//don't play the next track if we're currently in the middle of a fade
			if (fadeTotal == -1f) {
				//we do this in a separate thread to avoid graphics hitching while the music is prepared
				if (!DeviceCompat.isDesktop()) {
					new Thread() {
						@Override
						public void run() {
							playNextTrack(music);
						}
					}.start();
				} else {
					//don't use a separate thread on desktop, causes errors and makes no performance difference
					playNextTrack(music);
				}
			}
		}
	};

	private synchronized void playNextTrack(com.badlogic.gdx.audio.Music music){
		if (trackList == null || trackList.length == 0 || music != player || player.isLooping()){
			return;
		}

		Music.this.stop();

		if (trackQueue.isEmpty()) {
			for (int i = 0; i < trackList.length; i++) {
				if (Random.Float() < trackChances[i]) {
					trackQueue.add(trackList[i]);
				}
			}
			if (shuffle) Collections.shuffle(trackQueue);
		}

		if (!enabled || trackQueue.isEmpty()) {
			return;
		}

		play(trackQueue.remove(0), trackLooper);
	};

	private synchronized void play(String track, com.badlogic.gdx.audio.Music.OnCompletionListener listener){
		try {
			//==== END(修复·挑战 130 格林之音没生效): 这里也必须替换！ ====
			//本类是 private 重载，而 {@code playTracks()} 第 215 行
			//**直接调的就是它** —— 绕过了 public play() 里的 trackMapper。
			//
			//于是：Boss 层（走 public play）能换 BGM，
			//但常规层（走 playTracks → 本方法）**永远换不了**。
			//文档所有者实测"格林之音没有触发"就是这个原因。
			//
			//放在 Gdx.audio.newMusic 之前 —— 必须拿到替换后的路径再加载。
			String mapped = track;
			if (trackMapper != null && mapped != null) {
				String m = trackMapper.map(mapped);
				if (m != null) mapped = m;
			}
			if (Music.DEBUG_MUSIC) {
				System.out.println("[Music.play(内部)] " + track
						+ (mapped.equals(track) ? "  （未替换）" : ("  → " + mapped)));
			}
			track = mapped;

			fadeTime = fadeTotal = -1;

			player = Gdx.audio.newMusic(Gdx.files.internal(track));
			player.setLooping(looping);
			player.setVolume(volumeWithFade());
			if (!paused) player.play();
			if (listener != null) {
				player.setOnCompletionListener(listener);
			}
		} catch (Exception e){
			Game.reportException(e);
			player = null;
		}
	}
	
	public synchronized void end() {
		lastPlayed = null;
		trackList = null;
		stop();
	}

	private boolean paused = false;

	public synchronized boolean paused(){
		return paused;
	}
	
	public synchronized void pause() {
		paused = true;
		if (player != null) {
			player.pause();
		}
	}
	
	public synchronized void resume() {
		paused = false;
		if (player != null) {
			player.play();
			player.setLooping(looping);
		}
	}

	public synchronized void stop() {
		if (player != null) {
			player.dispose();
			player = null;
		}
	}
	
	public synchronized void volume( float value ) {
		volume = value;
		if (player != null) {
			player.setVolume( volumeWithFade() );
		}
	}

	private synchronized float volumeWithFade(){
		if (fadeTotal > 0f){
			return Math.max(0, volume * ((fadeTotal - fadeTime) / fadeTotal));
		} else {
			return volume;
		}
	}
	
	public synchronized boolean isPlaying() {
		return player != null && player.isPlaying();
	}
	
	public synchronized void enable( boolean value ) {
		enabled = value;
		if (isPlaying() && !value) {
			stop();
		} else
		if (!isPlaying() && value) {
			if (trackList != null){
				playTracks(trackList, trackChances, shuffle);
			} else if (lastPlayed != null) {
				play(lastPlayed, looping);
			}
		}
	}
	
	public synchronized boolean isEnabled() {
		return enabled;
	}
	
}
