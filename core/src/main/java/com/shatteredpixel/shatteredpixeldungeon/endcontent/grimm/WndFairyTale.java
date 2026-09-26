/*
 * 破碎的地牢 (End fork) — 挑战 129「心爱的少女」：未知的童话书阅读窗口
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.endcontent.grimm;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;

/**
 * END(129): 逐页阅读《未知的童话书》。
 *
 * <p>每页显示：页码 + 角色名 + 童话出处 + 正文。
 * 还没补上的页显示"这一页还是空白的"。
 * 打开时自动跳到第一页已补上的内容。
 */
public class WndFairyTale extends Window {

	private static final int WIDTH_P = 120;
	private static final int WIDTH_L = 144;
	private static final int MARGIN = 2;
	private static final int BUTTON_HEIGHT = 18;

	private final UnknownFairyTale book;
	private final int width;
	private int page = 0;

	private final IconTitle titlebar;
	private final RenderedTextBlock body;
	private final RedButton btnPrev;
	private final RedButton btnNext;

	public WndFairyTale(UnknownFairyTale book) {
		this.book = book;
		width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		titlebar = new IconTitle(new ItemSprite(book), "");
		titlebar.setRect(0, 0, width, 0);
		add(titlebar);

		body = PixelScene.renderTextBlock("", 6);
		body.maxWidth(width - MARGIN * 2);
		add(body);

		btnPrev = new RedButton("上一页") {
			@Override
			protected void onClick() {
				if (page > 0) {
					page--;
					refresh();
				}
			}
		};
		add(btnPrev);

		btnNext = new RedButton("下一页") {
			@Override
			protected void onClick() {
				if (page < UnknownFairyTale.PAGES - 1) {
					page++;
					refresh();
				}
			}
		};
		add(btnNext);

		//打开时跳到第一页已补上的内容；一页都没有就从第 1 页开始
		for (int i = 0; i < UnknownFairyTale.PAGES; i++) {
			if (book.has(i)) {
				page = i;
				break;
			}
		}
		refresh();
	}

	private void refresh() {
		titlebar.label(book.name() + "（" + (page + 1) + "/" + UnknownFairyTale.PAGES + "）");

		if (book.has(page)) {
			String tale = FairyFragment.fairyTaleName(page);
			String lore = FairyFragment.characterLore(page);
			body.text("第 " + (page + 1) + " 页 · " + FairyFragment.characterName(page)
					+ (tale.isEmpty() ? "" : "（出自《" + tale + "》）")
					+ "\n\n" + lore);
		} else {
			body.text("第 " + (page + 1) + " 页\n\n这一页还是空白的。");
		}

		body.setPos(MARGIN, titlebar.bottom() + MARGIN);
		float pos = body.top() + body.height() + MARGIN;
		float bw = (width - MARGIN * 3) / 2f;
		btnPrev.setRect(MARGIN, pos, bw, BUTTON_HEIGHT);
		btnNext.setRect(MARGIN * 2 + bw, pos, bw, BUTTON_HEIGHT);

		btnPrev.active = page > 0;
		btnNext.active = page < UnknownFairyTale.PAGES - 1;

		resize(width, (int)(pos + BUTTON_HEIGHT + MARGIN));
	}
}
