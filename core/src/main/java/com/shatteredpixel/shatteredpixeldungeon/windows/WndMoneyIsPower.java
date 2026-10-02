/*
 * 破碎的地牢 (End fork) — 挑战 41「等价交换」的购买窗口
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
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * END(挑战 41 等价交换): 「等价交换」采购窗口。
 *
 * <p>列出可购买的物品类别，点一下随机给该类别的一件物品，
 * **按该物品的真实价值（item.value()）支付金币**，所以神器不会
 * 再出现"只要 400 金"的情况。
 *
 * <p>与 160 氪金大佬的区别：41 是购买，160 是给已有物品升级。
 */
public class WndMoneyIsPower extends Window {

	private static final int WIDTH      = 130;
	private static final int TTL_HEIGHT = 18;
	private static final int BTN_HEIGHT = 18;
	private static final int GAP        = 1;

	/** 可购买的项目：{显示名, 类别}。价格在点选后按物品实际价值算。 */
	private static final String[][] STOCK = {
			{ "随机药水",   "POTION"   },
			{ "随机卷轴",   "SCROLL"   },
			{ "随机种子",   "SEED"     },
			{ "随机符石",   "STONE"    },
			{ "随机武器",   "WEAPON"   },
			{ "随机护甲",   "ARMOR"    },
			{ "随机法杖",   "WAND"     },
			{ "随机戒指",   "RING"     },
			{ "随机神器",   "ARTIFACT" },
			{ "力量药水",   "@STR"     },
			{ "升级卷轴",   "@SOU"     },
			{ "经验药水",   "@EXP"     },
	};

	private ScrollPane pane;
	private final ArrayList<RedButton> buttons = new ArrayList<>();
	private final ArrayList<Runnable> actions = new ArrayList<>();

	public WndMoneyIsPower() {
		super();

		RenderedTextBlock title = PixelScene.renderTextBlock("等价交换", 12);
		title.hardlight(TITLE_COLOR);
		title.setPos((WIDTH - title.width()) / 2, (TTL_HEIGHT - title.height()) / 2);
		PixelScene.align(title);
		add(title);

		RenderedTextBlock goldText = PixelScene.renderTextBlock(
				"当前金币：" + Dungeon.gold, 6);
		goldText.hardlight(0xFFFF44);
		goldText.setPos(4, TTL_HEIGHT);
		add(goldText);

		float pos = TTL_HEIGHT + goldText.height() + 3;

		Component content = new Component();
		float cPos = 0;

		for (final String[] entry : STOCK) {
			final String label = entry[0];
			final String key   = entry[1];
			final Runnable action = () -> buy(label, key);
			RedButton btn = new RedButton(label + "  （按物品价值）") {
				@Override
				protected void onClick() {
					action.run();
				}
			};
			btn.setRect(0, cPos, WIDTH - 12, BTN_HEIGHT);
			content.add(btn);
			buttons.add(btn);
			actions.add(action);
			cPos = btn.bottom() + GAP;
		}

		content.setSize(WIDTH - 12, cPos);

		//先 add 再接父链，避免 ScrollPane.camera() 为 null
		pane = new ScrollPane(content) {
			@Override
			public void onClick(float x, float y) {
				//ScrollPane 会吃掉点击，这里手动转发给内部按钮
				for (int i = 0; i < buttons.size(); i++) {
					RedButton b = buttons.get(i);
					if (y >= b.top() && y <= b.bottom()) {
						if (b.active) actions.get(i).run();
						return;
					}
				}
			}
		};
		add(pane);
		pane.setRect(0, pos, WIDTH, Math.min(150, cPos));
		com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPaneCamera
				.bindCamera(pane, this);

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

	@Override
	public void update() {
		super.update();
		if (pane != null) {
			com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPaneCamera
					.placeContentCamera(pane, this, pane.height());
		}
	}

	/** END(41): 先取物品，再按它的真实价值收费。 */
	private void buy(String label, String key) {
		Item item = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
				.ChallengeEffects.purchaseItem(key);
		if (item == null) {
			GLog.w("这件东西暂时缺货。");
			return;
		}
		int price = Math.max(1, item.value());
		if (Dungeon.gold < price) {
			GLog.w("金币不够，需要 " + price + " 金币。");
			return;
		}

		Dungeon.gold -= price;
		item.identify();
		item.collect();

		GLog.p("花 " + price + " 金币买到了" + item.name() + "。");
		hide();
		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
				new WndMoneyIsPower());
	}
}
