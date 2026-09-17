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

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Preferences;
import com.badlogic.gdx.utils.Architecture;
import com.badlogic.gdx.utils.Os;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.services.news.News;
import com.shatteredpixel.shatteredpixeldungeon.services.news.NewsImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.UpdateImpl;
import com.shatteredpixel.shatteredpixeldungeon.services.updates.Updates;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import com.watabou.utils.Point;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

public class DesktopLauncher {

	public static void main (String[] args) {

		if (!DesktopLaunchValidator.verifyValidJVMState(args)){
			return;
		}

		//detection for FreeBSD (which is equivalent to linux for us)
		//TODO might want to merge request this to libGDX
		if (System.getProperty("os.name").contains("FreeBSD")) {
			SharedLibraryLoader.os = Os.Linux;
			//this overrides incorrect values set in SharedLibraryLoader's static initializer
			if (System.getProperty("os.arch").contains("64") || System.getProperty("os.arch").startsWith("armv8")){
				SharedLibraryLoader.bitness = Architecture.Bitness._64;
			}
		}
		
		final String title;
		if (DesktopLauncher.class.getPackage().getSpecificationTitle() == null){
			title = System.getProperty("Specification-Title");
		} else {
			title = DesktopLauncher.class.getPackage().getSpecificationTitle();
		}

		//END(修复·存档目录): jar 的 Manifest 在未打包运行（例如从 lib 目录直接跑）时是空的，
		//getSpecificationTitle() 与系统属性都取不到值 → title == null
		//→ 存档目录会变成 ".shatteredpixel/null/"。
		//这里兜底一个固定名字，保证存档目录永远稳定。
		final String saveTitle = (title == null || title.isEmpty())
				? "Shattered Pixel Dungeon"
				: title;
		
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable throwable) {
				Game.reportException(throwable);
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				throwable.printStackTrace(pw);
				pw.flush();
				String exceptionMsg = sw.toString();

				//shorten/simplify exception message to make it easier to fit into a message box
				exceptionMsg = exceptionMsg.replaceAll("\\(.*:([0-9]*)\\)", "($1)");
				exceptionMsg = exceptionMsg.replace("com.shatteredpixel.shatteredpixeldungeon.", "");
				exceptionMsg = exceptionMsg.replace("com.watabou.", "");
				exceptionMsg = exceptionMsg.replace("com.badlogic.gdx.", "");
				exceptionMsg = exceptionMsg.replace("\t", "  "); //shortens length of tabs

				//replace ' and " with similar equivalents as tinyfd hates them for some reason
				exceptionMsg = exceptionMsg.replace('\'', '’');
				exceptionMsg = exceptionMsg.replace('"', '”');

				if (exceptionMsg.length() > 1000){
					exceptionMsg = exceptionMsg.substring(0, 1000) + "...";
				}

				if (exceptionMsg.contains("Couldn’t create window")){
					TinyFileDialogs.tinyfd_messageBox(title + " Has Crashed!",
							title + " was not able to initialize its graphics display, sorry about that!\n\n" +
									"This usually happens when your graphics card has misconfigured drivers or does not support openGL 2.0+.\n\n" +
									"If you are certain the game should work on your computer, please message the developer (Evan@ShatteredPixel.com)\n\n" +
									"version: " + Game.version + "\n" +
									exceptionMsg,
							"ok", "error", false);
				} else {
					TinyFileDialogs.tinyfd_messageBox(title + " Has Crashed!",
							title + " has run into an error it cannot recover from and has crashed, sorry about that!\n\n" +
									"If you could, please email this error message to the developer (Evan@ShatteredPixel.com):\n\n" +
									"version: " + Game.version + "\n" +
									exceptionMsg,
							"ok", "error", false);
				}
				System.exit(1);
			}
		});
		
		Game.version = DesktopLauncher.class.getPackage() != null
				? DesktopLauncher.class.getPackage().getSpecificationVersion()
				: null;
		if (Game.version == null) {
			Game.version = System.getProperty("Specification-Version");
		}
		if (Game.version == null) {
			Game.version = "0.0.1-end"; //unpacked run: no manifest version; must be non-null for DeviceCompat etc.
		}
		
		try {
			String implVer = DesktopLauncher.class.getPackage() != null
					? DesktopLauncher.class.getPackage().getImplementationVersion()
					: null;
			Game.versionCode = implVer == null ? -1 : Integer.parseInt(implVer);
		} catch (NumberFormatException e) {
			//running from unpacked install there may be no Implementation-Version; fall back to sys prop, else -1
			try {
				String v = System.getProperty("Implementation-Version");
				Game.versionCode = (v == null) ? -1 : Integer.parseInt(v);
			} catch (NumberFormatException e2) {
				Game.versionCode = -1;
			}
		}

		if (UpdateImpl.supportsUpdates()){
			Updates.service = UpdateImpl.getUpdateService();
		}
		if (NewsImpl.supportsNews()){
			News.service = NewsImpl.getNewsService();
		}
		
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		
		config.setTitle( title );

		//if I were implementing this from scratch I would use the full implementation title for saves
		// (e.g. /.shatteredpixel/shatteredpixeldungeon), but we have too much existing save
		// date to worry about transferring at this point.
		String vendor = DesktopLauncher.class.getPackage().getImplementationTitle();
		if (vendor == null) {
			vendor = System.getProperty("Implementation-Title");
		}
		if (vendor == null || vendor.indexOf('.') < 0) {
			vendor = "shatteredpixel"; //running from unpacked jars; use a sane save-path owner
		} else {
			String[] vp = vendor.split("\\.");
			if (vp.length >= 2) vendor = vp[1];
		}
		vendor = vendor == null ? "shatteredpixel" : vendor;

		String basePath = "";
		Files.FileType baseFileType = null;
		if (SharedLibraryLoader.os == Os.Windows) {
			if (System.getProperties().getProperty("os.name").equals("Windows XP")) {
				basePath = "Application Data/." + vendor + "/" + saveTitle + "/";
			} else {
				basePath = "AppData/Roaming/." + vendor + "/" + saveTitle + "/";
			}
			baseFileType = Files.FileType.External;
		} else if (SharedLibraryLoader.os == Os.MacOsX) {
			basePath = "Library/Application Support/" + saveTitle + "/";
			baseFileType = Files.FileType.External;
		} else if (SharedLibraryLoader.os == Os.Linux) {
			String XDGHome = System.getenv("XDG_DATA_HOME");
			if (XDGHome == null) XDGHome = System.getProperty("user.home") + "/.local/share";

			String titleLinux = saveTitle.toLowerCase(Locale.ROOT).replace(" ", "-");
			basePath = XDGHome + "/." + vendor + "/" + titleLinux + "/";

			baseFileType = Files.FileType.Absolute;
		}

		//END(新增·继承原版存档): 首次运行时，如果本目录还没有任何存档，
		//就把【原版《破碎的像素地牢》】的存档/成就/排行榜复制过来，
		//让玩家无缝继承原版的进度与成就。
		//
		//查找顺序（只读，不修改原版目录）：
		//   1. 同级的 "Shattered Pixel Dungeon"
		//   2. 旧名 "Tomorrow RogueNight"（本 MOD 的早期目录名）
		//   3. "." + vendor + "/null"（Manifest 缺失时误建的目录）
		//   4. 环境变量 DSH_IMPORT_SAVE 指定的目录
		try {
			importVanillaSavesIfFirstRun( baseFileType, basePath, vendor );
		} catch (Throwable t) {
			System.err.println("[存档继承] 跳过（" + t + "）");
		}

		config.setPreferencesConfig( basePath, baseFileType );
		SPDSettings.set( new Lwjgl3Preferences( new Lwjgl3FileHandle(basePath + SPDSettings.DEFAULT_PREFS_FILE, baseFileType) ));
		FileUtils.setDefaultFileProperties( baseFileType, basePath );
		
		config.setWindowSizeLimits( 720, 400, -1, -1 );
		Point p = SPDSettings.windowResolution();
		config.setWindowedMode( p.x, p.y );

		config.setMaximized(SPDSettings.windowMaximized());

		//going fullscreen on launch is a bit buggy
		// so game always starts windowed and then switches in DesktopPlatformSupport.updateSystemUI
		//config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());

		//records whether window is maximized or not for settings
		DesktopWindowListener listener = new DesktopWindowListener();
		config.setWindowListener( listener );
		
		config.setWindowIcon("icons/icon_16.png", "icons/icon_32.png", "icons/icon_48.png",
				"icons/icon_64.png", "icons/icon_128.png", "icons/icon_256.png");

		new Lwjgl3Application(new ShatteredPixelDungeon(new DesktopPlatformSupport()), config);
	}

	/**
	 * END(新增·继承原版存档)
	 *
	 * <p>首次运行时，如果本 MOD 的存档目录还是空的，就把【原版《破碎的像素地牢》】
	 * 的存档、成就、图鉴、排行榜复制过来，让玩家无缝继承原版进度。
	 *
	 * <p>只在【目标目录没有任何存档】时才复制，绝不会覆盖既有进度；
	 * 源目录只读，不会被修改。
	 *
	 * <p>候选源目录（按顺序尝试）：
	 * <ol>
	 *   <li>{@code DSH_IMPORT_SAVE} 环境变量指定的目录（手动指定）</li>
	 *   <li>同级的 {@code Shattered Pixel Dungeon}（原版）</li>
	 *   <li>同级的 {@code Tomorrow RogueNight}（本 MOD 早期目录名）</li>
	 *   <li>同级的 {@code null}（Manifest 缺失时误建的目录）</li>
	 * </ol>
	 *
	 * <p>复制的文件：{@code game1..game6/}、{@code badges.dat}、
	 * {@code rankings.dat}、{@code journal.dat}、{@code bones.dat}。
	 * 设置（{@code settings.xml}）与按键绑定不复制，避免分辨率等被旧值覆盖。
	 */
	private static void importVanillaSavesIfFirstRun( Files.FileType type,
	                                                  String basePath,
	                                                  String vendor ) {
		java.io.File dest = resolveFile( type, basePath );
		if (dest == null) return;

		// 目标目录已经有存档 → 什么都不做
		if (dest.exists() && hasAnySave(dest)) {
			return;
		}

		// 候选源目录
		java.util.List<java.io.File> candidates = new java.util.ArrayList<>();

		String env = System.getenv("DSH_IMPORT_SAVE");
		if (env != null && !env.isEmpty()) {
			candidates.add(new java.io.File(env));
		}

		java.io.File parent = dest.getParentFile();     // .../.shatteredpixel/
		if (parent != null) {
			candidates.add(new java.io.File(parent, "Shattered Pixel Dungeon"));
			candidates.add(new java.io.File(parent, "Tomorrow RogueNight"));
			candidates.add(new java.io.File(parent, "null"));
		}

		java.io.File src = null;
		for (java.io.File c : candidates) {
			if (c.exists() && !c.equals(dest) && hasAnySave(c)) {
				src = c;
				break;
			}
		}
		if (src == null) return;

		// END(存档选择 UI): 找到原版存档后，先问玩家要不要继承，而不是静默复制。
		// tinyfd 的 yes/no：点 yes=true 继承原版存档，点 no=false 用全新存档。
		boolean importSave = TinyFileDialogs.tinyfd_messageBox(
				"检测到原版《破碎的像素地牢》存档",
				"在「" + src.getName() + "」目录下检测到原版存档与成就。\n\n" +
						"是否继承原版进度（存档 / 成就 / 图鉴 / 排行榜）？\n\n" +
						"· 是 = 导入原版存档，无缝继续冒险\n" +
						"· 否 = 使用全新存档，从零开始",
				"yesno", "question", false);

		if (!importSave) {
			System.out.println("[存档继承] 玩家选择使用全新存档，跳过导入。");
			return;
		}

		System.out.println("[存档继承] 从「" + src.getName() + "」导入原版进度…");

		dest.mkdirs();

		int copied = 0;
		// 6 个存档槽
		for (int i = 1; i <= 6; i++) {
			copied += copyDir(new java.io.File(src, "game" + i),
					          new java.io.File(dest, "game" + i));
		}
		// 成就 / 图鉴 / 排行榜 / 骸骨
		for (String f : new String[]{ "badges.dat", "rankings.dat",
				                      "journal.dat", "bones.dat" }) {
			if (copyFile(new java.io.File(src, f), new java.io.File(dest, f))) {
				copied++;
			}
		}

		System.out.println("[存档继承] 完成，导入 " + copied + " 项。");
	}

	/** 该目录里有没有任何存档内容。 */
	private static boolean hasAnySave( java.io.File dir ) {
		if (dir == null || !dir.isDirectory()) return false;
		for (int i = 1; i <= 6; i++) {
			java.io.File g = new java.io.File(dir, "game" + i);
			if (g.isDirectory() && g.list() != null && g.list().length > 0) {
				return true;
			}
		}
		// 就算没有进行中的存档，只要成就/排行榜存在也算"有内容"
		return new java.io.File(dir, "badges.dat").exists()
				|| new java.io.File(dir, "rankings.dat").exists();
	}

	/** FileType + 相对路径 → 绝对 File（libGDX 的 External 是相对用户目录）。 */
	private static java.io.File resolveFile( Files.FileType type, String basePath ) {
		try {
			String p = basePath.replace('/', java.io.File.separatorChar);
			if (type == Files.FileType.External) {
				String home = System.getProperty("user.home");
				return new java.io.File(home, p);
			} else if (type == Files.FileType.Absolute) {
				return new java.io.File(p);
			} else if (type == Files.FileType.Local) {
				return new java.io.File(System.getProperty("user.dir"), p);
			}
		} catch (Throwable ignored) { }
		return null;
	}

	/** 递归复制目录（不覆盖已存在的文件）。返回复制的文件数。 */
	private static int copyDir( java.io.File src, java.io.File dst ) {
		if (src == null || !src.isDirectory()) return 0;
		dst.mkdirs();
		int n = 0;
		java.io.File[] kids = src.listFiles();
		if (kids == null) return 0;
		for (java.io.File k : kids) {
			java.io.File t = new java.io.File(dst, k.getName());
			if (k.isDirectory()) {
				n += copyDir(k, t);
			} else if (!t.exists()) {
				if (copyFile(k, t)) n++;
			}
		}
		return n;
	}

	/** 复制单个文件（不覆盖）。 */
	private static boolean copyFile( java.io.File src, java.io.File dst ) {
		if (src == null || !src.isFile() || dst.exists()) return false;
		try {
			dst.getParentFile().mkdirs();
			java.nio.file.Files.copy( src.toPath(), dst.toPath(),
					java.nio.file.StandardCopyOption.COPY_ATTRIBUTES );
			return true;
		} catch (Throwable t) {
			System.err.println("[存档继承] 复制失败 " + src.getName() + ": " + t);
			return false;
		}
	}
}
