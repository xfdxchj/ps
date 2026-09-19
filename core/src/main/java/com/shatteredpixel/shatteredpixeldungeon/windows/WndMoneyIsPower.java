/*
 * 破碎的地牢 (End fork) — 挑战 41「钱是万能」的购买窗口
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * END(挑战 41 钱是万能): 「钱是万能」采购窗口。
 *
 * <h3>原表效果</h3>
 * "消耗金币直接购买各种物品"
 *
 * <h3>与 160 氪金大佬的区别（文档所有者明确）</h3>
 * <ul>
 *   <li><b>41 钱是万能</b>：用金币**买任何物品**（包括平时买不到的）</li>
 *   <li><b>160 氪金大佬</b>：用金币**给已有物品升级**</li>
 * </ul>
 * 两者互不重叠。
 *
 * <h3>做法</h3>
 * 列出所有可购买的物品类别，每类一个按钮。
 * 点一下随机给该类别的一件物品（价格固定）。
 * 这样"买任何物品"是真的"任何" —— 而不是只从商店现有库存里挑。
 */
public class WndMoneyIsPower extends Window {

	private static final int WIDTH      = 130;
	private static final int TTL_HEIGHT = 18;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP        = 1;

	/** 可购买的项目：{显示名, 类别, 价格}。 */
	private static final Object[][] STOCK = {
			{ "随机药水",   "POTION",   80  },
			{ "随机卷轴",   "SCROLL",   80  },
			{ "随机种子",   "SEED",     40  },
			{ "随机符石",   "STONE",    60  },
			{ "随机武器",   "WEAPON",   200 },
			{ "随机护甲",   "ARMOR",    200 },
			{ "随机法杖",   "WAND",     250 },
			{ "随机戒指",   "RING",     250 },
			{ "随机神器",   "ARTIFACT", 400 },
			{ "力量药水",   "@STR",     500 },
			{ "升级卷轴",   "@SOU",     600 },
			{ "经验药水",   "@EXP",     400 },
	};

	public WndMoneyIsPower() {
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock("钱是万能", 12);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2, (TTL_HEIGHT - title.height()) / 2);
		PixelScene.align(title);
		add(title);

		//金币显示
		RenderedTextBlock goldText = PixelScene.renderTextBlock(
				"当前金币：" + Dungeon.gold, 6);
		goldText.hardlight(0xFFFF44);
		goldText.setPos(4, TTL_HEIGHT);
		add(goldText);

		float pos = TTL_HEIGHT + goldText.height() + 3;

		//可滚动的内容区
		Component content = new Component();
		float cPos = 0;

		for (Object[] entry : STOCK) {
			final String label = (String) entry[0];
			final String key   = (String) entry[1];
			final int    price = (Integer) entry[2];

			RedButton btn = new RedButton(label + "  " + price + "G") {
				@Override
				protected void onClick() {
					buy(label, key, price);
				}
			};
			btn.setRect(0, cPos, WIDTH - 12, BTN_HEIGHT);
			btn.enable(Dungeon.gold >= price);
			content.add(btn);

			cPos = btn.bottom() + GAP;
		}

		content.setSize(WIDTH - 12, cPos);

		ScrollPane pane = new ScrollPane(content);
		pane.setRect(0, pos, WIDTH, Math.min(150, cPos));
		add(pane);

		pos = pane.bottom() + 2;

		RedButton close = new RedButton("关闭") {
			@Override
			protected void onClick() {
				hide();
			}
		};
		close.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(close);

		resize(WIDTH, (int) close.bottom());
	}

	/** END(41): 尝试购买。 */
	private void buy(String label, String key, int price) {
		if (Dungeon.gold < price) {
			GLog.w("金币不够。");
			return;
		}

		Item item = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.purchaseItem(key);
		if (item == null) {
			GLog.w("这件东西暂时缺货。");
			return;
		}

		Dungeon.gold -= price;
		item.identify();
		item.collect();

		GLog.p("花 " + price + " 金币买到了" + item.name() + "。");
		hide();
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
				new WndMoneyIsPower());      //刷新金币显示
	}
}
