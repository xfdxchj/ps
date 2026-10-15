/*
 * 破碎的地牢 (End fork) — 崩溃/诊断日志落盘。
 *
 * 手机版没有落盘日志，崩溃只能看 logcat。这里把所有未捕获异常、
 * Game.reportException，以及 Dbg 的诊断行都写进一个文件。
 *
 * 位置（Android，external 应用目录）：
 *   /sdcard/Android/data/<包名>/files/pd_log.txt
 * 电脑上用 adb 取：
 *   adb pull /sdcard/Android/data/<包名>/files/pd_log.txt
 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;

public final class CrashLog {

	private CrashLog() {}

	public static final String FILE_NAME = "pd_log.txt";
	private static final int MAX_RECENT = 400;

	private static final ArrayDeque<String> recent = new ArrayDeque<>();
	private static final SimpleDateFormat TS = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US);

	/** Dbg 每打一行就存进最近缓冲，崩溃时一起写出去。 */
	public static synchronized void record(String line){
		try {
			recent.addLast(TS.format(new Date()) + "  " + line);
			while (recent.size() > MAX_RECENT) recent.removeFirst();
		} catch (Throwable ignored) {}
	}

	/** 写一次崩溃。线程安全，先写异常再写最近的诊断行。 */
	public static synchronized void write(Throwable t){
		StringBuilder sb = new StringBuilder();
		sb.append("\n================ CRASH ").append(new Date()).append(" ================\n");
		if (t != null){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.flush();
			sb.append(sw);
		}
		sb.append("---- recent logs (").append(recent.size()).append(") ----\n");
		for (String s : recent) sb.append(s).append('\n');
		sb.append("================ END CRASH ================\n");
		append(sb.toString());
	}

	/** 写一段普通文本（例如启动信息）。 */
	public static synchronized void info(String text){
		append(TS.format(new Date()) + "  " + text + '\n');
	}

	private static void append(String text){
		try {
			FileHandle f = logFile();
			if (f != null) f.writeString(text, true);
		} catch (Throwable ignored) {}
	}

	/** external 应用目录：Android 上是 /sdcard/Android/data/<包名>/files/。 */
	public static FileHandle logFile(){
		try {
			if (Gdx.files == null) return null;
			return Gdx.files.external(FILE_NAME);
		} catch (Throwable e){
			return null;
		}
	}

	public static String pathHint(){
		try {
			FileHandle f = logFile();
			return f == null ? "" : f.file().getAbsolutePath();
		} catch (Throwable e){
			return "";
		}
	}
}
