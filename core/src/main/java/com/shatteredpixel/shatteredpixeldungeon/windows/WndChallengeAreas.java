package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeArea;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;

/**
 * END(移植自魔绫·挑战区): 开局“挑战区域”多选窗口。
 * 勾选后的结果保存在 {@code SPDSettings.challengeAreas()}（位掩码，bit=区域 id）。
 * 进入游戏时由 {@link ChallengeArea#applySelection(int)} 写入 Statistics。
 */
public class WndChallengeAreas extends Window {

	private static final int WIDTH		= 120;
	private static final int TTL_HEIGHT = 16;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 1;

	private boolean editable;
	private ArrayList<CheckBox> boxes;

	public WndChallengeAreas( int checked, boolean editable ) {

		super();

		this.editable = editable;

		RenderedTextBlock title = PixelScene.renderTextBlock( "挑战区域（只能选一个）", 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				(WIDTH - title.width()) / 2,
				(TTL_HEIGHT - title.height()) / 2
		);
		PixelScene.align(title);
		add( title );

		boxes = new ArrayList<>();

		float pos = TTL_HEIGHT;
		for (int i = 0; i < ChallengeArea.ALL.length; i++) {

			final ChallengeArea area = ChallengeArea.ALL[i];
			final int myIndex = i;

			String label = area.name + (area.implemented ? "" : "（未实装）");

			//END(修复): 改为【单选】—— 同时勾选多个区会互相干扰（层号串接、剧情分支冲突），
			//容易触发难以定位的 bug。点任意一个时，取消其余所有勾选。
			CheckBox cb = new CheckBox( Messages.titleCase(label) ) {
				@Override
				protected void onClick() {
					boolean turningOn = !checked();
					for (int j = 0; j < boxes.size(); j++) {
						if (boxes.get(j) == this) {
							boxes.get(j).checked( turningOn );
						} else {
							boxes.get(j).checked( false );
						}
					}
				}
			};
			cb.checked( ChallengeArea.isSelected(checked, area) );
			//未实装的区域暂不可勾选，避免选进去后无事发生
			cb.active = editable && area.implemented;

			if (i > 0) {
				pos += GAP;
			}
			cb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );

			add( cb );
			boxes.add( cb );

			pos = cb.bottom();
		}

		resize( WIDTH, (int)pos );
	}

	@Override
	public void onBackPressed() {

		if (editable) {
			int value = 0;
			for (int i = 0; i < boxes.size(); i++) {
				if (boxes.get(i).checked()) {
					value = ChallengeArea.toggle(value, ChallengeArea.ALL[i]);
				}
			}
			//END(修复): 数据层兜底 —— 只保留一个（单选）
			value = ChallengeArea.firstSelectedOnly( value );
			SPDSettings.challengeAreas( value );
		}

		super.onBackPressed();
	}
}
