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

package com.shatteredpixel.shatteredpixeldungeon.messages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.I18NBundle;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Locale;

/*
	Simple wrapper class for libGDX I18NBundles.

	The core idea here is that each string resource's key is a combination of the class definition and a local value.
	An object or static method would usually call this with an object/class reference (usually its own) and a local key.
	This means that an object can just ask for "name" rather than, say, "items.weapon.enchantments.death.name"
 */
public class Messages {

	private static ArrayList<I18NBundle> bundles;
	private static Languages lang;
	private static Locale locale;

	public static final String NO_TEXT_FOUND = "!!!NO TEXT FOUND!!!";

	public static Languages lang(){
		return lang;
	}

	public static Locale locale(){
		return locale;
	}

	/**
	 * Setup Methods
	 */

	private static String[] prop_files = new String[]{
			Assets.Messages.ACTORS,
			Assets.Messages.ITEMS,
			Assets.Messages.JOURNAL,
			Assets.Messages.LEVELS,
			Assets.Messages.MISC,
			Assets.Messages.PLANTS,
			Assets.Messages.SCENES,
			Assets.Messages.UI,
			Assets.Messages.WINDOWS
	};

	static{
		formatters = new HashMap<>();
		setup(SPDSettings.language());
	}

	public static void setup( Languages lang ){
		//seeing as missing keys are part of our process, this is faster than throwing an exception
		I18NBundle.setExceptionOnMissingKey(false);

		//store language and locale info for various string logic
		Messages.lang = lang;
		Locale bundleLocal;
		if (lang == Languages.ENGLISH){
			locale = Locale.ENGLISH;
			bundleLocal = Locale.ROOT; //english is source, uses root locale for fetching bundle
		} else {
			locale = new Locale(lang.code());
			bundleLocal = locale;
		}
		formatters.clear();

		bundles = new ArrayList<>();
		for (String file : prop_files) {
			if (bundleLocal.getLanguage().equals("id")){
				//This is a really silly hack to fix some platforms using "id" for indonesian and some using "in" (Android 14- mostly).
				//So if we detect "id" then we treat "###_in" as the base bundle so that it gets loaded instead of English.
				bundles.add(I18NBundle.createBundle(Gdx.files.internal(file + "_in"), bundleLocal));
			} else {
				bundles.add(I18NBundle.createBundle(Gdx.files.internal(file), bundleLocal));
			}
		}
	}



	/**
	 * Resource grabbing methods
	 */

	public static String get(String key, Object...args){
		return get(null, key, args);
	}

	public static String get(Object o, String k, Object...args){
		return get(o.getClass(), k, args);
	}

	public static String get(Class c, String k, Object...args){
		String key;
		if (c != null){
			key = c.getName().replace("com.shatteredpixel.shatteredpixeldungeon.", "");
			key += "." + k;
		} else
			key = k;

		String value = getFromBundle(key.toLowerCase(Locale.ENGLISH));
		if (value != null){
			if (args.length > 0) return format(value, args);
			else return value;
		} else {
			//this is so child classes can inherit properties from their parents.
			//in cases where text is commonly grabbed as a utility from classes that aren't mean to be instantiated
			//(e.g. flavourbuff.dispTurns()) using .class directly is probably smarter to prevent unnecessary recursive calls.
			if (c != null && c.getSuperclass() != null){
				return get(c.getSuperclass(), k, args);
			} else {
				return NO_TEXT_FOUND;
			}
		}
	}

	private static String getFromBundle(String key){
		String result;
		for (I18NBundle b : bundles){
			result = b.get(key);
			//if it isn't the return string for no key found, return it
			if (result.length() != key.length()+6 || !result.contains(key)){
				return result;
			}
		}
		return null;
	}

	/**
	 * END(文案走 properties): 某个消息键是否存在。
	 *
	 * <h3>为什么需要它</h3>
	 * 挑战描述有两处来源：注册表里的 Java 字符串，与 properties 里的
	 * {@code challenges.xxx_desc}。文档所有者要求"文案改在 properties 里"
	 * （改起来不用编译），所以 {@code describe()} 要**优先读 properties**、
	 * 读不到再退回 Java。
	 *
	 * <p>问题是 {@code Messages.get()} 找不到键时会返回
	 * {@code "!!!key!!!"} 这样的占位串，光看返回值分不清
	 * "真没有"和"文案恰好长这样"。所以这里提供一个明确的探测方法。
	 *
	 * @param o  取键前缀的对象（与 {@code Messages.get(o, k)} 同一套规则）
	 * @param k  键名
	 * @return true 表示该键在任一语言包里存在
	 */
	public static boolean exists(Object o, String k){
		String key;
		if (o != null){
			key = o.getClass().getName()
					.replace("com.shatteredpixel.shatteredpixeldungeon.", "");
			key += "." + k;
		} else {
			key = k;
		}
		return getFromBundle(key.toLowerCase(Locale.ENGLISH)) != null;
	}



	/**
	 * String Utility Methods
	 */

	public static String format( String format, Object...args ) {
		try {
			return String.format(locale(), format, args);
		} catch (IllegalFormatException e) {
			ShatteredPixelDungeon.reportException( new Exception("formatting error for the string: " + format, e) );
			return format;
		}
	}

	private static HashMap<String, DecimalFormat> formatters;

	public static String decimalFormat( String format, double number ){
		if (!formatters.containsKey(format)){
			formatters.put(format, new DecimalFormat(format, DecimalFormatSymbols.getInstance(locale())));
		}
		return formatters.get(format).format(number);
	}

	public static String capitalize( String str ){
		if (str.length() == 0)  return str;
		else                    return str.substring( 0, 1 ).toUpperCase(locale) + str.substring( 1 );
	}

	//Words which should not be capitalized in title case, mostly prepositions which appear ingame
	//This list is not comprehensive!
	private static final HashSet<String> noCaps = new HashSet<>(
			Arrays.asList("a", "an", "and", "of", "by", "to", "the", "x", "for")
	);

	public static String titleCase( String str ){
		//English capitalizes every word except for a few exceptions
		if (lang == Languages.ENGLISH){
			String result = "";
			//split by any unicode space character
			for (String word : str.split("(?<=\\p{Zs})")){
				if (noCaps.contains(word.trim().toLowerCase(Locale.ENGLISH).replaceAll(":|[0-9]", ""))){
					result += word;
				} else {
					result += capitalize(word);
				}
			}
			//first character is always capitalized.
			return capitalize(result);
		}

		//Otherwise, use sentence case
		return capitalize(str);
	}

	public static String upperCase( String str ){
		return str.toUpperCase(locale);
	}

	public static String lowerCase( String str ){
		return str.toLowerCase(locale);
	}
}