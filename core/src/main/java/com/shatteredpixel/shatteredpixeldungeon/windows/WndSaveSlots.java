/*
 * 破碎的地牢 (End fork) — 存档槽位界面
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.watabou.noosa.Game;

import java.io.IOException;
import java.util.ArrayList;

/**
 * END(新增): 存档槽位界面 —— 在游戏内浏览全部存档槽，并对任一槽执行
 * 保存 / 读取 / 删除。
 *
 * <h3>为什么要它</h3>
 * 原版只有 {@code StartScene} 的槽位列表：点一个槽只能"继续/删除"，
 * 且**只有在标题界面才能看到**。游戏进行中想存到另一个槽、或想读取
 * 另一个槽，是没有入口的（只能退出到标题界面，而退出时会自动存回
 * {@code curSlot}）。
 *
 * <h3>行为约定</h3>
 * <ul>
 *   <li><b>保存</b>：把当前进度写入该槽（{@link Dungeon#saveAll()}）。
 *       写入别的槽<em>不会</em>切换 {@code curSlot}，当前这局仍写在原槽，
 *       相当于"另存为一份备份"。</li>
 *   <li><b>读取</b>：切换 {@code curSlot} 并重载该槽 —— 用
 *       {@link InterlevelScene.Mode#CONTINUE} 走原版的读档路径。</li>
 *   <li><b>删除</b>：二次确认后删档；删的若是当前槽，会先返回标题界面。</li>
 * </ul>
 *
 * <h3>注意</h3>
 * 读取操作会丢弃当前未保存的进度。因此读取前先做一次确认 ——
 * 这与"删除"同等重要，因为两者都会让你失去当前这局。
 */
public class WndSaveSlots extends Window {

	private static final int WIDTH		= 120;
	private static final int TTL_HEIGHT	= 16;
	private static final int BTN_HEIGHT	= 16;
	private static final int GAP		= 1;
	/** 槽位按钮右侧三个小按钮的宽度。 */
	private static final int ACT_W		= 22;

	public WndSaveSlots() {

		super();

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				(WIDTH - title.width()) / 2,
				(TTL_HEIGHT - title.height()) / 2
		);
		PixelScene.align(title);
		add( title );

		float pos = TTL_HEIGHT;

		for ( int slot = 1; slot <= GamesInProgress.MAX_SLOTS; slot++ ) {
			final int curSlot = slot;
			final GamesInProgress.Info info = GamesInProgress.check( slot );
			final boolean isThisSlot = (slot == GamesInProgress.curSlot);

			if (slot > 1) pos += GAP;

			//==== 槽位名称 / 概况 ====
			//已占用的槽显示"英雄 Lv N · 第 M 层"，空槽显示"空槽位"。
			//当前正在玩的槽额外标注，避免"另存为"时看错槽。
			String label;
			if (info == null) {
				label = Messages.get(this, "empty");
			} else {
				String cls = (info.subClass != null && info.subClass != com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.NONE)
						? info.subClass.title()
						: (info.heroClass != null ? info.heroClass.title() : "");
				label = Messages.get(this, "slot_info", info.level, cls, info.depth);
			}
			if (isThisSlot) {
				label = label + Messages.get(this, "current_mark");
			}

			RenderedTextBlock txt = PixelScene.renderTextBlock( label, 7 );
			//长名字裁掉，别盖到右边的按钮上
			float labelW = WIDTH - ACT_W*3 - GAP*3 - 4;
			while (txt.width() > labelW && txt.text().length() > 1) {
				txt.text( txt.text().substring(0, txt.text().length() - 1) );
			}
			txt.setPos( 2, pos + (BTN_HEIGHT - txt.height())/2 );
			PixelScene.align(txt);
			add( txt );

			//==== 三个操作按钮 ====
			float bx = WIDTH - (ACT_W*3 + GAP*2);

			//保存：任何槽都可以保存（含当前槽，等于手动存一次）
			RedButton btnSave = new RedButton( Messages.get(this, "save") ) {
				@Override
				protected void onClick() {
					super.onClick();
					saveTo( curSlot );
				}
			};
			btnSave.setRect( bx, pos, ACT_W, BTN_HEIGHT );
			add( btnSave );

			//读取：空槽不可读
			RedButton btnLoad = new RedButton( Messages.get(this, "load") ) {
				@Override
				protected void onClick() {
					super.onClick();
					confirmLoad( curSlot, info );
				}
			};
			btnLoad.setRect( bx + ACT_W + GAP, pos, ACT_W, BTN_HEIGHT );
			btnLoad.active = (info != null);
			add( btnLoad );

			//删除：空槽不可删
			RedButton btnDel = new RedButton( Messages.get(this, "delete") ) {
				@Override
				protected void onClick() {
					super.onClick();
					confirmDelete( curSlot, info );
				}
			};
			btnDel.setRect( bx + (ACT_W + GAP)*2, pos, ACT_W, BTN_HEIGHT );
			btnDel.active = (info != null);
			add( btnDel );

			pos += BTN_HEIGHT;
		}

		pos += 4;

		//==== 当前槽的详细信息（种子 / 已收集金币 / 最深记录）====
		//放在最下面，不与上面的操作按钮抢注意力。
		GamesInProgress.Info cur = GamesInProgress.check( GamesInProgress.curSlot );
		String detail;
		if (cur == null) {
			detail = Messages.get(this, "no_current");
		} else {
			detail = Messages.get(this, "detail",
					cur.goldCollected, cur.maxDepth,
					cur.customSeed != null && !cur.customSeed.isEmpty()
							? cur.customSeed
							: DungeonSeed.convertToCode(cur.seed));
		}
		RenderedTextBlock infoBlock = PixelScene.renderTextBlock( detail, 6 );
		infoBlock.maxWidth( WIDTH - 8 );
		infoBlock.hardlight( 0xCCCCCC );
		add( infoBlock );
		infoBlock.setPos( 4, pos );
		pos += infoBlock.height();

		pos += 4;

		//==== 底部：编辑模式下的"返回标题界面" ====
		//游戏内读档/删档后需要重开场景；标题界面下则只需关窗。
		RedButton btnClose = new RedButton( Messages.get(this, "close") ) {
			@Override
			protected void onClick() {
				super.onClick();
				hide();
			}
		};
		btnClose.setRect( 0, pos, WIDTH, BTN_HEIGHT );
		add( btnClose );

		resize( WIDTH, (int)(pos + BTN_HEIGHT) );
	}

	/**
	 * 把当前进度写入指定槽。
	 *
	 * <p>刻意**不修改** {@code curSlot}：这是"另存为"，当前这局之后仍写在
	 * 原来的槽，新槽是一份可以随时读回去的快照。
	 *
	 * <p>注意 {@code Dungeon.saveAll()} 内部用的是 {@code curSlot}，所以这里
	 * 临时借用一下 {@code GamesInProgress.curSlot}，写完立刻还原 —— 否则用户的
	 * "当前槽"就被悄悄换掉了。
	 *
	 * <p>{@code saveAll()} 内部还会调 {@code GamesInProgress.set(curSlot)} 刷新缓存，
	 * 所以槽位概况会立刻变成最新数据；这里只需重开窗口让列表重画。
	 */
	private void saveTo( int slot ) {
		int prevSlot = GamesInProgress.curSlot;
		try {
			GamesInProgress.curSlot = slot;
			Dungeon.saveAll();
			//让列表显示刚写入的数据
			hide();
			GameScene.show( new WndSaveSlots() );
		} catch (IOException e) {
			ShatteredPixelDungeon.reportException( e );
			GameScene.show( new WndMessage( Messages.get(this, "save_failed", slot) ) );
		} finally {
			GamesInProgress.curSlot = prevSlot;
		}
	}

	/** 读取是破坏性操作（丢弃当前未保存进度），先确认。 */
	private void confirmLoad( final int slot, GamesInProgress.Info info ) {
		if (info == null) return;

		final Window self = this;

		ShatteredPixelDungeon.scene().add( new WndOptions(
				Icons.get( Icons.WARNING ),
				Messages.get(this, "load_warn_title"),
				Messages.get(this, "load_warn_body", slot),
				Messages.get(this, "load_warn_yes", slot),
				Messages.get(this, "load_warn_no") ) {
			@Override
			protected void onSelect( int index ) {
				if (index != 0) return;

				self.hide();
				Dungeon.hero = null;
				Dungeon.daily = Dungeon.dailyReplay = false;
				ActionIndicator.clearAction();
				GamesInProgress.curSlot = slot;
				InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
				ShatteredPixelDungeon.switchScene( InterlevelScene.class );
			}
		} );
	}

	/** 删除同样先确认；删的是当前槽时把列表一起收掉。 */
	private void confirmDelete( final int slot, GamesInProgress.Info info ) {
		if (info == null) return;

		final boolean wasCurrent = (slot == GamesInProgress.curSlot);
		//捕获本窗口，供回调里关闭它 —— hide() 只作用于调用者自己，
		//在 WndOptions 的 onSelect 里调 hide() 关掉的是那个确认框，不是本窗口。
		final Window self = this;

		ShatteredPixelDungeon.scene().add( new WndOptions(
				Icons.get( Icons.WARNING ),
				Messages.get(this, "erase_warn_title"),
				Messages.get(this, "erase_warn_body"),
				Messages.get(this, "erase_warn_yes"),
				Messages.get(this, "erase_warn_no") ) {
			@Override
			protected void onSelect( int index ) {
				if (index != 0) return;

				Dungeon.deleteGame( slot, true );

				if (wasCurrent) {
					//当前这局的存档没了 —— 只能回标题界面重开
					ShatteredPixelDungeon.switchNoFade( StartScene.class );
				} else {
					//删的是别的槽：关掉本窗口，重开一个让列表刷新。
					//必须显式关掉旧窗口，否则会叠出两份存档列表。
					self.hide();
					GameScene.show( new WndSaveSlots() );
				}
			}
		} );
	}

	@Override
	public void onBackPressed() {
		//关闭前刷新缓存，保证下次打开（或存档列表）看到最新状态
		for (int i = 1; i <= GamesInProgress.MAX_SLOTS; i++) {
			GamesInProgress.setUnknown( i );
		}
		super.onBackPressed();
	}

	/** 供外部按需求构建（目前未使用，保留以便后续从其它入口调起）。 */
	public static ArrayList<GamesInProgress.Info> allSlots() {
		return GamesInProgress.checkAll();
	}
}
