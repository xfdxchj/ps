/* 术式之书：选择要释放的术式（类似牧师圣典的法术菜单）。 */
package com.shatteredpixel.shatteredpixeldungeon.endcontent.jujutsu;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

public class WndJujutsuSpells extends WndOptions {

	private final TechniqueBook book;
	private final Hero hero;

	public WndJujutsuSpells(TechniqueBook book, Hero hero){
		super("术式之书",
				"选择要释放的术式：\n充能 " + book.chargeNow() + "/" + book.chargeCapNow() + "（苍 " + TechniqueBook.COST_CANG
						+ " / 赫 " + TechniqueBook.COST_HE + " / 无量空处 " + TechniqueBook.COST_DOMAIN + "）",
				"苍（" + TechniqueBook.COST_CANG + " 充能）",
				"赫（" + TechniqueBook.COST_HE + " 充能）",
				"无量空处（" + TechniqueBook.COST_DOMAIN + " 充能）",
				"取消");
		this.book = book;
		this.hero = hero;
	}

	@Override
	protected void onSelect(int index){
		if (index == 0) book.execute(hero, TechniqueBook.AC_CANG);
		else if (index == 1) book.execute(hero, TechniqueBook.AC_HE);
		else if (index == 2) book.execute(hero, TechniqueBook.AC_DOMAIN);
	}
}
