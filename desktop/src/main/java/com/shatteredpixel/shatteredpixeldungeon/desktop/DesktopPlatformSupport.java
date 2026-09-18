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

package com.shatteredpixel.shatteredpixeldungeon.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.input.ControllerHandler;
import com.watabou.noosa.Game;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.Point;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DesktopPlatformSupport extends PlatformSupport {

	//we recall previous window sizes as a workaround to not save maximized size to settings
	//have to do this as updateDisplaySize is called before maximized is set =S
	protected static Point[] previousSizes = null;

	@Override
	public void updateDisplaySize() {
		if (previousSizes == null){
			previousSizes = new Point[2];
			previousSizes[1] = SPDSettings.windowResolution();
		} else {
			previousSizes[1] = previousSizes[0];
		}
		previousSizes[0] = new Point(Game.width, Game.height);
		if (!SPDSettings.fullscreen()) {
			SPDSettings.windowResolution( previousSizes[0] );
		}
	}

	private static boolean first = true;

	@Override
	public void updateSystemUI() {
		Gdx.app.postRunnable( new Runnable() {
			@Override
			public void run () {
				if (SPDSettings.fullscreen()){
					int monitorNum = 0;
					if (!first){
						Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
						for (int i = 0; i < monitors.length; i++){
							if (((Lwjgl3Graphics.Lwjgl3Monitor)Gdx.graphics.getMonitor()).getMonitorHandle()
									== ((Lwjgl3Graphics.Lwjgl3Monitor)monitors[i]).getMonitorHandle()) {
								monitorNum = i;
							}
						}
					} else {
						monitorNum = SPDSettings.fulLScreenMonitor();
					}

					Graphics.Monitor[] monitors = Gdx.graphics.getMonitors();
					if (monitors.length <= monitorNum) {
						monitorNum = 0;
					}
					Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(monitors[monitorNum]));
					SPDSettings.fulLScreenMonitor(monitorNum);
				} else {
					Point p = SPDSettings.windowResolution();
					Gdx.graphics.setWindowedMode( p.x, p.y );
				}
				first = false;
			}
		} );
	}
	
	@Override
	public boolean connectedToUnmeteredNetwork() {
		return true; //no easy way to check this in desktop, just assume user doesn't care
	}

	@Override
	public boolean supportsVibration() {
		//only supports vibration via controller
		return ControllerHandler.vibrationSupported();
	}

	/* FONT SUPPORT */
	
	//custom pixel font, for use with Latin and Cyrillic languages
	private static FreeTypeFontGenerator basicFontGenerator;
	//droid sans fallback, for asian fonts
	private static FreeTypeFontGenerator asianFontGenerator;
	
	@Override
	public void setupFontGenerators(int pageSize, boolean systemfont) {
		//don't bother doing anything if nothing has changed
		if (fonts != null && this.pageSize == pageSize && this.systemfont == systemfont){
			return;
		}
		this.pageSize = pageSize;
		this.systemfont = systemfont;

		resetGenerators(false);
		fonts = new HashMap<>();

		if (systemfont) {
			basicFontGenerator = asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		} else {
			basicFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/pixel_font.ttf"));
			asianFontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/droid_sans.ttf"));
		}
		
		fonts.put(basicFontGenerator, new HashMap<>());
		fonts.put(asianFontGenerator, new HashMap<>());
		
		packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
	}
	
	private static Matcher asianMatcher = Pattern.compile("\\p{InHangul_Syllables}|" +
			"\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}|" +
			"\\p{InHiragana}|\\p{InKatakana}").matcher("");

	@Override
	protected FreeTypeFontGenerator getGeneratorForString( String input ){
		if (asianMatcher.reset(input).find()){
			return asianFontGenerator;
		} else {
			return basicFontGenerator;
		}
	}
	
	//splits on newline (for layout), chinese/japanese (for font choice), and '_'/'**' (for highlighting)
	private Pattern regularsplitter = Pattern.compile(
			"(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");
	
	//additionally splits on spaces, so that each word can be laid out individually
	private Pattern regularsplitterMultiline = Pattern.compile(
			"(?<= )|(?= )|(?<=\n)|(?=\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|" +
					"(?<=\\p{InHiragana})|(?=\\p{InHiragana})|" +
					"(?<=\\p{InKatakana})|(?=\\p{InKatakana})|" +
					"(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|" +
					"(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})");
	
	@Override
	public String[] splitforTextBlock(String text, boolean multiline) {
		if (multiline) {
			return regularsplitterMultiline.split(text);
		} else {
			return regularsplitter.split(text);
		}
	}

	//==== END(存档继承): 手动导入外部存档 ====
	/**
	 * END(存档继承): 桌面端实现 —— 弹目录选择框，导入原版存档。
	 *
	 * <p>与启动时的自动导入的区别：**允许覆盖**。
	 * 自动导入只在目标为空时执行，手动导入是玩家显式要求，
	 * 所以要覆盖已有文件（但会先问一次确认）。
	 *
	 * @return 导入条目数；0 = 用户取消；-1 = 无可用源
	 */
	/**
	 * END(存档继承): 桌面端是否可用**系统对话框**。
	 *
	 * <p>为什么需要探测：LWJGL 的 tinyfd 需要原生库（`lwjgl_tinyfd.dll` 等）。
	 * 上游只声明了 API jar、没带 natives —— 因为它只在"崩溃提示"里用，
	 * 崩了也不在乎再崩一次。但本功能在**正常运行中**调用它，
	 * 缺库会抛 `UnsatisfiedLinkError`（是 **Error 不是 Exception**），
	 * 直接把游戏拖崩。
	 *
	 * <p>所以这里先做一次惰性探测：能加载才认为可用。
	 * 不可用时 {@link #importSupported()} 返回 false，UI 直接不显示按钮 ——
	 * 比"显示按钮但一点就崩"好得多。
	 */
	private static Boolean tinyfdAvailable;

	private static synchronized boolean tinyfdWorks() {
		if (tinyfdAvailable != null) return tinyfdAvailable;
		try {
			//调用一个**无副作用**的 native 方法：
			//读取全局开关不会弹窗、不改状态，但会强制链接 lwjgl_tinyfd 原生库。
			//缺 natives 时在这里抛 UnsatisfiedLinkError。
			org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_getGlobalInt("tinyfd_verbose");
			tinyfdAvailable = Boolean.TRUE;
		} catch (Throwable t) {
			//UnsatisfiedLinkError / NoClassDefFoundError 都会落到这里
			//（它们是 Error 不是 Exception，所以必须 catch Throwable）
			System.err.println("[存档继承] 系统对话框不可用（缺少 lwjgl_tinyfd 原生库）：" + t);
			tinyfdAvailable = Boolean.FALSE;
		}
		return tinyfdAvailable;
	}

	/** END(存档继承): 桌面端支持手动导入。 */
	@Override
	public boolean importSupported() {
		return tinyfdWorks();
	}

	@Override
	public int importExternalSaves() {

		//没有原生库就别往下走 —— 直接告诉 UI"不支持"
		if (!tinyfdWorks()) {
			return -1;
		}
		//目标 = 当前 MOD 的存档目录（External + 包名）
		java.io.File dest = resolveSaveDir();
		if (dest == null) {
			return -1;
		}

		//候选源目录：与原版/旧版本的常见位置一致
		java.util.List<java.io.File> candidates = new java.util.ArrayList<>();

		String env = System.getenv("DSH_IMPORT_SAVE");
		if (env != null && !env.isEmpty()) {
			candidates.add(new java.io.File(env));
		}

		java.io.File parent = dest.getParentFile();
		if (parent != null) {
			candidates.add(new java.io.File(parent, "Shattered Pixel Dungeon"));
			candidates.add(new java.io.File(parent, "Tomorrow RogueNight"));
			candidates.add(new java.io.File(parent, "null"));
		}

		java.io.File src = null;
		for (java.io.File c : candidates) {
			if (c.exists() && !c.equals(dest) && hasSaveContent(c)) {
				src = c;
				break;
			}
		}

		//没找到就弹目录选择框，让玩家自己指
		//再加一层 try：即使探测通过，运行中的原生调用仍可能失败，
		//绝不能让一个"导入存档"功能把整个游戏拖崩。
		if (src == null) {
			try {
				String picked = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_selectFolderDialog(
						"选择要导入的原版存档目录（含 game1 / badges.dat 等）",
						System.getProperty("user.home"));
				if (picked != null && !picked.isEmpty()) {
					java.io.File f = new java.io.File(picked);
					if (hasSaveContent(f)) src = f;
				}
			} catch (Throwable t) {
				System.err.println("[存档继承·手动] 目录选择框不可用：" + t);
				return -1;
			}
		}

		if (src == null) {
			return -1;
		}

		//覆盖前确认
		boolean ok;
		try {
			ok = org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_messageBox(
					"导入存档",
					"将从「" + src.getName() + "」导入：\n" +
							"存档槽、成就、图鉴、排行榜、骸骨。\n\n" +
							"已存在的同名文件会被覆盖。确定继续吗？",
					"yesno", "warning", false);
		} catch (Throwable t) {
			System.err.println("[存档继承·手动] 确认框不可用，取消导入：" + t);
			return 0;
		}
		if (!ok) {
			return 0;
		}

		dest.mkdirs();

		int copied = 0;
		for (int i = 1; i <= 6; i++) {
			copied += copyDirOverwrite(new java.io.File(src, "game" + i),
					                   new java.io.File(dest, "game" + i));
		}
		for (String f : new String[]{ "badges.dat", "rankings.dat",
				                      "journal.dat", "bones.dat" }) {
			if (copyFileOverwrite(new java.io.File(src, f), new java.io.File(dest, f))) {
				copied++;
			}
		}

		System.out.println("[存档继承·手动] 从「" + src.getName() + "」导入 " + copied + " 项。");
		return copied;
	}

	/** 当前 MOD 的存档目录（External 根 + 包名）。 */
	private static java.io.File resolveSaveDir() {
		try {
			String home = System.getProperty("user.home");
			//libGDX 的 External 根：Windows 是 %APPDATA%，其它是 ~/.prefs 附近
			String os = System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT);
			java.io.File base;
			if (os.contains("win")) {
				String appdata = System.getenv("APPDATA");
				base = (appdata != null) ? new java.io.File(appdata) : new java.io.File(home);
			} else {
				base = new java.io.File(home);
			}
			return new java.io.File(base, ".shatteredpixel");
		} catch (Throwable t) {
			return null;
		}
	}

	/** 该目录里有没有存档内容。 */
	private static boolean hasSaveContent( java.io.File dir ) {
		if (dir == null || !dir.isDirectory()) return false;
		for (int i = 1; i <= 6; i++) {
			java.io.File g = new java.io.File(dir, "game" + i);
			if (g.isDirectory() && g.list() != null && g.list().length > 0) return true;
		}
		return new java.io.File(dir, "badges.dat").exists()
				|| new java.io.File(dir, "rankings.dat").exists();
	}

	/** 递归复制目录（**覆盖**已存在文件）。 */
	private static int copyDirOverwrite( java.io.File src, java.io.File dst ) {
		if (src == null || !src.isDirectory()) return 0;
		dst.mkdirs();
		int n = 0;
		java.io.File[] kids = src.listFiles();
		if (kids == null) return 0;
		for (java.io.File k : kids) {
			java.io.File t = new java.io.File(dst, k.getName());
			if (k.isDirectory()) {
				n += copyDirOverwrite(k, t);
			} else if (copyFileOverwrite(k, t)) {
				n++;
			}
		}
		return n;
	}

	/** 复制单个文件（**覆盖**）。 */
	private static boolean copyFileOverwrite( java.io.File src, java.io.File dst ) {
		if (src == null || !src.isFile()) return false;
		try {
			dst.getParentFile().mkdirs();
			java.nio.file.Files.copy( src.toPath(), dst.toPath(),
					java.nio.file.StandardCopyOption.REPLACE_EXISTING,
					java.nio.file.StandardCopyOption.COPY_ATTRIBUTES );
			return true;
		} catch (Throwable t) {
			System.err.println("[存档继承·手动] 复制失败 " + src.getName() + ": " + t);
			return false;
		}
	}
}
