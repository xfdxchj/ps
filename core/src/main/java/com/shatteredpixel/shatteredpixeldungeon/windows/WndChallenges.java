/*
 * 破碎的地牢 (End fork) — 挑战选择界面（注册表驱动 · 分类模式）
 *
 * 本文件基于原版 Shattered Pixel Dungeon 的 WndChallenges 重写。
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeDef;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRandomizer;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRegistry;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRelation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * END(挑战框架): 挑战规则选择窗口 —— **分类选择模式**。
 *
 * <h3>布局</h3>
 * <pre>
 *   标题
 *   [随机条：- N + [随机]]        （仅开局可选时显示）
 *   [分类1][分类2][分类3][分类4]   ← 一行分类按钮，可翻页/滚动
 *   ─────────────────────────
 *   该分类下的挑战列表（可滚动）
 *   ─────────────────────────
 *   通过等级 / 已选条数
 * </pre>
 *
 * <h3>为什么改成分类而不是一个大滚动列表</h3>
 * 规则有 149 条，全塞进一个滚动列表里找一条要翻很久，
 * 而且用户明确反馈"不要全部靠滚动"。按分类切分后，
 * 每个分类最多 27 条，一屏基本看得完，滚动只在必要时出现。
 *
 * <h3>其余行为</h3>
 * <ul>
 *   <li>未实装项置灰并标注「未实装」</li>
 *   <li>互斥项自动置灰</li>
 *   <li>前置未满足置灰（如 166 神圣天使需 4 条神圣类）</li>
 *   <li>已在使用的但"与已勾选规则冲突"的项加「(部分无效)」标记</li>
 *   <li>底部实时显示通过等级</li>
 * </ul>
 */
public class WndChallenges extends Window {

	private static final int WIDTH		= 120;
	private static final int TTL_HEIGHT = 16;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 1;
	/** 分类按钮高度。 */
	/**
	 * END(修复·分类按钮被压扁): 分类栏高度。
	 *
	 * <p>原本写 14，但 {@code RedButton} 的九宫格边框本身要吃掉上下各几像素，
	 * 14 虚拟像素装不下"文字 + 边框"，实测表现为**按钮被压扁、文字被裁切**。
	 * 改用与其它按钮一致的 16。
	 */
	private static final int CAT_H      = 16;

	/** END: UI 布局诊断开关。定稿后关闭，避免刷屏。 */
	private static final boolean UI_DEBUG = false;
	/** 滚动区期望高度上限（实际还会受屏幕高度约束）。 */
	private static final int MAX_LIST_H = 150;

	//==== END(随机挑战) ====
	private static final int RANDOM_BAR_H = 18;

	private boolean editable;

	/** 当前选择状态（唯一权威）。 */
	private ChallengeMask mask;

	/** 当前正在查看的分类（null = 尚未选定，默认第一个）。 */
	private String currentGroup;

	private ArrayList<CheckBox> boxes = new ArrayList<>();

	/**
	 * 与 {@link #boxes} 一一对应的规则定义。
	 *
	 * <p>点击由 {@code ScrollPane.onClick -> handleListClick} 手动分发时需要它
	 * 反查被点中的是哪条规则；**必须与 boxes 同时清空**，否则会错位。
	 */
	private ArrayList<ChallengeDef> defs = new ArrayList<>();

	/** 分类按钮行（两排网格，铺满窗口宽度）。 */
	private Component catPane;   //（已弃用：分类栏现在是两排普通 Component，按钮自己收点击）
	private Component catContent;
	private ArrayList<RedButton> catButtons = new ArrayList<>();
	private ArrayList<String> groups = new ArrayList<>();

	/** 列表区。 */
	private ScrollPane pane;
	private Component content;

	private RenderedTextBlock passLevelText;

	//随机条
	private Component randomBar;
	private int targetLevel = 6;
	private boolean includePending = false;
	private RenderedTextBlock targetText;

	/**
	 * @param checked  旧 int 掩码（兼容原版调用点）；会翻译成完整掩码
	 * @param editable 是否可编辑
	 */
	public WndChallenges( int checked, boolean editable ) {
		this( ChallengeRegistry.fromLegacyInt( checked ), editable );
	}

	/** 新写法：直接传完整掩码。 */
	public WndChallenges( ChallengeMask checked, boolean editable ) {

		super();

		this.editable = editable;
		this.mask = (checked == null) ? ChallengeMask.empty() : checked;

		//分类清单（保持注册表顺序）
		groups.addAll( ChallengeRegistry.groups() );
		if (!groups.isEmpty()) {
			currentGroup = groups.get(0);
		}

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2, (TTL_HEIGHT - title.height()) / 2 );
		PixelScene.align(title);
		add( title );

		//底部：通过等级
		passLevelText = PixelScene.renderTextBlock( "", 7 );
		passLevelText.hardlight( 0xCCCCCC );
		add( passLevelText );

		//分类按钮行
		//==== END(改版·分类排两排，不滚动): 不再用 ScrollPane ====
		//原先分类栏是横向滚动的 ScrollPane，两个问题：
		//  · 只有 16 虚拟像素高（80 物理像素），手指在上面几乎拖不动，分类划不到后面
		//  · ScrollPane 会吃掉点击，导致分类按钮点不动（要靠手动转发）
		//改为**两排网格、铺满窗口宽度**后，10 个分类全部可见，
		//既不需要滚动，也不需要手动转发点击 —— 直接用普通 Component。
		catContent = new Component();
		add( catContent );
		//END(修复): 必须在 add() 之后立刻绑定窗口相机 ——
		//（分类栏已改为普通 Component，见下方说明，不再需要相机绑定）
		buildCategoryBar();

		//列表区
		content = new Component();

		//==== END(修复·滑动条抢点击 / 点击抢滑动): 照原版 WndKeyBindings 的做法 ====
		//1) ScrollPane 的 PointerController 在 DOWN 时返回 true
		//   （PointerArea.onSignal 第 67 行），Signal.dispatch 遇到 true 就 return，
		//   下层组件永远收不到点击。所以点击必须由 ScrollPane.onClick(x,y)
		//   手动转发 —— 这正是 WndKeyBindings:109 的做法。
		//2) 同时覆写 layout()，在 super 之后摆正内部裁剪相机。
		//   放在 layout() 而不是每帧 update()：每帧 resize() 会反复动 GL viewport，
		//   既浪费也可能干扰手势判定。layout() 只在尺寸变化时跑，正好够用。
		pane = new ScrollPane( content ) {
			@Override
			protected void layout() {
				super.layout();
				placeContentCamera( this, height() );
			}
			@Override
			public void onClick( float x, float y ) {
				handleListClick( x, y );
			}
		};
		add( pane );
		bindScrollPaneCamera( pane );

		//随机条（仅开局可选时）
		float top = TTL_HEIGHT;
		if (editable) {
			buildRandomBar();
			randomBar.setRect( 0, TTL_HEIGHT, WIDTH, RANDOM_BAR_H );
			top = TTL_HEIGHT + RANDOM_BAR_H;
		}

		//布局
		//分类栏：两排网格，直接用普通 Component 铺在窗口上（不再滚动）
		catContent.setPos( 0, top );
		top += catHeight() + 1;

		buildList();

		//END(修复·UI 越界): 列表高度必须受**屏幕**约束。
		//原版只有 12 条、高度自然适配；扩到 100+ 条后若写死，
		//窗口底部会跑到屏幕外，表现为"下半截点不到"。
		float bottomH = 14;
		float maxByScreen = com.watabou.noosa.Camera.main.height - top - bottomH - 6;
		float listH = Math.min( content.height(),
				Math.min( MAX_LIST_H, Math.max(48, maxByScreen)) );
		pane.setRect( 0, top, WIDTH, listH );
		resize( WIDTH, (int)(top + listH + bottomH) );

		passLevelText.setPos( 4, top + listH + 2 );
		updatePassLevel();

		//==== END(临时诊断·UI 偏移): 打印真实尺寸，用于定位偏移 300~700 像素的原因 ====
		//定稿后应删除。运行一次打开挑战窗口即可在控制台看到这些数字。
		try {
			com.watabou.noosa.Camera uiCam = PixelScene.uiCamera;
			com.watabou.noosa.Camera mainCam = com.watabou.noosa.Camera.main;
			System.out.println("=== WndChallenges 布局诊断 ===");
			System.out.println("  Game.width/height (物理) = "
					+ com.watabou.noosa.Game.width + " x " + com.watabou.noosa.Game.height);
			System.out.println("  本窗口 width/height (虚拟) = " + WIDTH + " x " + (int)(top + listH + bottomH));
			System.out.println("  本窗口 camera: x=" + camera.x + " y=" + camera.y
					+ " w=" + camera.width + " h=" + camera.height
					+ " zoom=" + camera.zoom
					+ " screenW=" + camera.screenWidth() + " screenH=" + camera.screenHeight());
			if (uiCam != null) {
				System.out.println("  uiCamera: x=" + uiCam.x + " y=" + uiCam.y
						+ " w=" + uiCam.width + " h=" + uiCam.height
						+ " zoom=" + uiCam.zoom
						+ " screenW=" + uiCam.screenWidth() + " screenH=" + uiCam.screenHeight()
						+ " visible=" + uiCam.visible);
			}
			if (mainCam != null) {
				System.out.println("  mainCamera: w=" + mainCam.width + " h=" + mainCam.height
						+ " zoom=" + mainCam.zoom
						+ " screenW=" + mainCam.screenWidth() + " screenH=" + mainCam.screenHeight());
			}
			System.out.println("  计算用 maxByScreen = " + maxByScreen);
			System.out.println("  最终 listH = " + listH);

			//---- 对比：内容坐标 vs 内容实际落到的屏幕位置 ----
			//若窗口 camera 居中而内容偏，差值会在这里暴露出来。
			System.out.println("  --- 内容定位对比 ---");
			dumpChild("列表 pane", pane);
			dumpChild("底部 passLevelText", passLevelText);
			System.out.println("  camera.scroll = (" + camera.scroll.x + ", " + camera.scroll.y + ")");
			System.out.println("  camera 尺寸 = " + camera.width + " x " + camera.height
					+ "  屏幕尺寸 = " + camera.screenWidth() + " x " + camera.screenHeight()
					+ "  x=" + camera.x + " y=" + camera.y + " zoom=" + camera.zoom);
			System.out.println("  uiCamera 尺寸 = " + com.shatteredpixel.shatteredpixeldungeon
					.scenes.PixelScene.uiCamera.width + " x "
					+ com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera.height
					+ "  物理 = " + com.shatteredpixel.shatteredpixeldungeon.scenes
							.PixelScene.uiCamera.screenWidth() + " x "
					+ com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.uiCamera
							.screenHeight());

			//---- 滚动容器内部 camera：这才是"内容实际渲染到哪"的权威数据 ----
			//ScrollPane 给 content 单独分配了一个 Camera（用于 GL 裁剪），
			//它的 x/y/scroll 决定了列表真正画在屏幕的什么位置。
			System.out.println("  --- ScrollPane 内部 ---");
			dumpScrollPane("列表", pane, content);
		} catch (Throwable t) {
			System.out.println("  诊断失败: " + t);
		}
	}

	/** 打印 ScrollPane 及其 content 的 camera 状态（诊断用）。 */
	private void dumpScrollPane(String label, ScrollPane sp, Component inner) {
		if (sp == null) { System.out.println("    " + label + " ScrollPane=null"); return; }
		com.watabou.noosa.Camera c = inner == null ? null : inner.camera;
		System.out.println("    [" + label + "] ScrollPane left=" + sp.left() + " top=" + sp.top()
				+ " w=" + sp.width() + " h=" + sp.height());
		if (inner != null) {
			System.out.println("         content 尺寸 = " + inner.width() + " x " + inner.height());
		}
		if (c != null) {
			System.out.println("         content.camera x=" + c.x + " y=" + c.y
					+ " w=" + c.width + " h=" + c.height
					+ " scroll=(" + c.scroll.x + "," + c.scroll.y + ")"
					+ " zoom=" + c.zoom
					+ " screen=" + c.screenWidth() + "x" + c.screenHeight());
			System.out.println("         => 内容左上角落屏幕 ("
					+ ((0 - c.scroll.x) * c.zoom + c.x) + ", "
					+ ((0 - c.scroll.y) * c.zoom + c.y) + ")"
					+ "  可视区高 " + c.screenHeight() + " 物理像素");
			//期望值：窗口坐标系里的位置
			float wantX = (sp.left() - camera.scroll.x) * camera.zoom + camera.x;
			float wantY = (sp.top()  - camera.scroll.y) * camera.zoom + camera.y;
			System.out.println("         期望 x=" + (int) wantX + " y=" + (int) wantY
					+ (Math.abs(c.x - wantX) < 2 && Math.abs(c.y - wantY) < 2
						? "   [OK]" : "   [!! 不匹配]"));
		} else {
			System.out.println("         content.camera = null（未被 ScrollPane 接管）");
		}
	}

	/** 打印一个子组件的坐标（诊断用）。 */
	private void dumpChild(String label, com.watabou.noosa.ui.Component c) {
		if (c == null) {
			System.out.println("    " + label + " = null");
			return;
		}
		System.out.println("    " + label
				+ " 内容坐标 left=" + c.left() + " top=" + c.top()
				+ " w=" + c.width() + " h=" + c.height()
				+ "  camera=" + (c.camera == null ? "null"
						: ("x=" + c.camera.x + " y=" + c.camera.y
						   + " scroll=(" + c.camera.scroll.x + "," + c.camera.scroll.y + ")")));
	}

	//==== 分类按钮行 ====

	/**
	 * 构建分类按钮行。
	 *
	 * <p>每个分类一个按钮，横向排列；总宽超出窗口时可横向滚动
	 * （分类有 10 个，120px 宽放不下，必须能滑）。
	 * 当前分类高亮，点击即切换并重建列表。
	 */
	private void buildCategoryBar() {
		catContent.clear();
		catButtons.clear();

		//==== END(改版·两排网格): 分类铺成两排，全部可见 ====
		//窗口宽 120，两排各放 5 个 → 每格宽约 23.6。
		//分类名是 2 个字（经典/药剂/经济/特殊/地图/战斗/怪物/环境/装备/格林），
		//6 号字两个字约 12 像素，23 宽足够，所以**标签里不再带数量** ——
		//数字会把按钮挤到只剩几个像素。
		//当前分类的数量改由列表标题/底部文字体现。
		final int COLS = 5;
		int rows = (groups.size() + COLS - 1) / COLS;      //10 个 → 2 排
		float cellW = (WIDTH - (COLS - 1)) / (float) COLS;

		for (int i = 0; i < groups.size(); i++) {
			final String g = groups.get(i);
			int row = i / COLS;
			int col = i % COLS;

			RedButton btn = new RedButton( g, 6 ) {
				@Override
				protected void onClick() {
					super.onClick();
					switchGroup( g );
				}
			};
			btn.setRect( col * (cellW + 1), row * (CAT_H + 1), cellW, CAT_H );
			//当前分类高亮
			btn.textColor( g.equals(currentGroup) ? 0xFFFF88 : 0xCCCCCC );

			catContent.add( btn );
			catButtons.add( btn );
		}

		catContent.setSize( WIDTH, rows * (CAT_H + 1) );
		catContent.setPos( 0, 0 );
		this.catRows = rows;
	}

	/** 分类栏占用的总高度（两排时为 2×16+1 = 33）。 */
	private int catRows = 2;

	private float catHeight() {
		return catRows * (CAT_H + 1);
	}

	/**
	 * END(修复·滑动条抢点击): 处理分类栏的点击。
	 *
	 * <p>调用点：{@code catPane} 覆写的 {@code onClick(x, y)}。
	 * 坐标已由 {@code ScrollPane} 换算成**内容坐标**（即 catContent 的坐标系）。
	 *
	 * <p>为什么不用按钮自己的 onClick：{@code ScrollPane.PointerController}
	 * 在 DOWN 时返回 true，事件不会继续分发给下层按钮（见 ScrollPane 构造处的说明）。
	 */
	private void handleCategoryClick( float x, float y ) {
		if (y < 0 || y > catHeight()) return;

		//两排网格：按坐标反算行列
		final int COLS = 5;
		float cellW = (WIDTH - (COLS - 1)) / (float) COLS;
		int col = (int) (x / (cellW + 1));
		int row = (int) (y / (CAT_H + 1));
		if (col < 0 || col >= COLS || row < 0) return;

		int index = row * COLS + col;
		if (index >= 0 && index < groups.size()) {
			switchGroup( groups.get(index) );
		}
	}

	/**
	 * END(修复·滑动条抢点击): 处理挑战列表的点击。
	 *
	 * <p>调用点：{@code pane} 覆写的 {@code onClick(x, y)}。
	 * 坐标已换算成内容坐标（列表的坐标系）。
	 *
	 * <p>命中判定：先看 y 落在哪个条目上；
	 * 若 x 落在条目右侧的"问号"图标区，则打开详情窗口，否则切换勾选。
	 */
	private void handleListClick( float x, float y ) {
		//END(修复·关闭后残留点击·弹介绍): 与 toggleChallenge 同样的守卫。
		//用户报告：关闭挑战窗口后，在原"问号"按钮位置点击仍能弹出介绍窗口。
		//根因：handleListClick 没加 isAlive，残留点击事件仍能走到 WndMessage 分支。
		if (!isAlive()) return;
		for (int i = 0; i < boxes.size(); i++) {
			CheckBox cb = boxes.get(i);
			if (y < cb.top() || y > cb.bottom()) continue;

			ChallengeDef d = (i < defs.size()) ? defs.get(i) : null;
			if (d == null) return;

			//右侧是"问号"详情区（宽度 16）
			if (x >= cb.right()) {
				ShatteredPixelDungeon.scene().add( new WndMessage( describe( d ) ) );
				return;
			}

			if (!cb.active) return;         //不可选（未实装/互斥/前置未满足）

			toggleChallenge( d );
			return;
		}
	}

	/** 切换分类：更新高亮 + 重建列表。 */
	private void switchGroup( String g ) {
		currentGroup = g;

		//刷新按钮高亮
		for (int i = 0; i < catButtons.size(); i++) {
			catButtons.get(i).textColor(
					groups.get(i).equals(currentGroup) ? 0xFFFF88 : 0xCCCCCC );
		}

		content.clear();
		boxes.clear();
		defs.clear();                     //与 boxes 同步清空
		buildList();

		//列表高度可能变化，重算窗口
		float top = TTL_HEIGHT + (editable ? RANDOM_BAR_H : 0) + catHeight() + 1;
		float bottomH = 14;
		float maxByScreen = com.watabou.noosa.Camera.main.height - top - bottomH - 6;
		float listH = Math.min( content.height(),
				Math.min( MAX_LIST_H, Math.max(48, maxByScreen)) );
		pane.setRect( 0, top, WIDTH, listH );
		resize( WIDTH, (int)(top + listH + bottomH) );
		passLevelText.setPos( 4, top + listH + 2 );
	}

	//==== 随机条 ====

	/**
	 * END(随机挑战): 顶部功能条 —— 目标分调整 + 「随机」按钮。
	 *
	 * <p>注意：按钮文字**只能用 ASCII**。这里曾用 U+2212 数学减号 '−'，
	 * droid_sans-45 没有该字形，会抛
	 * "font file droid_sans-45 could not render ?" 并中断整个窗口构建。
	 */
	private void buildRandomBar() {

		randomBar = new Component();
		add( randomBar );

		final int maxT = ChallengeRandomizer.maxTarget( includePending );
		if (targetLevel > maxT) targetLevel = maxT;
		if (targetLevel < ChallengeRandomizer.MIN_TARGET) {
			targetLevel = ChallengeRandomizer.MIN_TARGET;
		}

		RedButton minus = new RedButton( "-", 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				if (targetLevel > ChallengeRandomizer.MIN_TARGET) targetLevel--;
				updateTargetText();
			}
		};
		minus.setRect( 0, 0, 12, 16 );
		randomBar.add( minus );

		targetText = PixelScene.renderTextBlock( "", 7 );
		targetText.hardlight( 0xFFFF88 );
		randomBar.add( targetText );

		RedButton plus = new RedButton( "+", 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				if (targetLevel < ChallengeRandomizer.maxTarget( includePending )) targetLevel++;
				updateTargetText();
			}
		};
		plus.setRect( 14, 0, 12, 16 );
		randomBar.add( plus );

		RedButton roll = new RedButton( Messages.get( this, "roll" ), 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				mask = ChallengeRandomizer.roll( targetLevel, includePending );
				rebuildAll();
			}
		};
		roll.setRect( 28, 0, WIDTH - 28, 16 );
		randomBar.add( roll );

		updateTargetText();
	}

	private void updateTargetText() {
		if (targetText == null) return;
		targetText.text( Messages.get( this, "target", targetLevel ) );
		targetText.setPos( 15 + (12 - targetText.width())/2, (16 - targetText.height())/2 );
		PixelScene.align( targetText );
	}

	//==== 列表 ====

	/** 构建**当前分类**下的挑战列表。 */
	private void buildList() {

		float pos = 0;

		if (currentGroup == null) {
			content.setSize( WIDTH, 0 );
			return;
		}

		List<ChallengeDef> inGroup = ChallengeRegistry.inGroup( currentGroup );

		if (inGroup.isEmpty()) {
			content.setSize( WIDTH, 0 );
			return;
		}

		for (ChallengeDef def : inGroup) {

			final ChallengeDef d = def;

			String label = d.name;
			if (!d.isImplemented()) {
				label = label + Messages.get( this, "not_implemented" );
			}
			if (!d.restrictedBy( mask ).isEmpty()) {
				label = label + Messages.get( this, "restricted_mark" );
			}

			CheckBox cb = new CheckBox( Messages.titleCase( label ) ) {
				@Override
				protected void onClick() {
					//这条路径在 ScrollPane 里实际收不到事件（被 PointerController 吃掉），
					//真正生效的是 ScrollPane.onClick -> handleListClick。
					//保留它是为了将来若把列表移出 ScrollPane 时仍能工作。
					toggleChallenge( d );
				}
			};
			cb.setRect( 0, pos, WIDTH - 16, BTN_HEIGHT );
			cb.checked( mask.has( d.id ) );
			cb.active = editable && canToggle( d );

			content.add( cb );
			boxes.add( cb );
			defs.add( d );                    //与 boxes 一一对应（handleListClick 要用）

			//问号按钮：查看详情
			IconButton info = new IconButton( Icons.get( Icons.INFO ) ) {
				@Override
				protected void onClick() {
					super.onClick();
					ShatteredPixelDungeon.scene().add( new WndMessage( describe( d ) ) );
				}
			};
			info.setRect( cb.right(), pos, 16, BTN_HEIGHT );
			content.add( info );

			pos = cb.bottom() + GAP;
		}

		content.setSize( WIDTH, pos );
		content.setPos( 0, 0 );
	}

	/**
	 * END(修复·滑动条抢点击): 切换一条挑战的勾选状态。
	 *
	 * <p>把逻辑抽出来是为了让"CheckBox 自己的 onClick"与
	 * "ScrollPane 手动分发"两条路径**行为完全一致**。
	 *
	 * <p>END(修复·二次打开闪退): 开头检查窗口是否还活着。
	 * 实测堆栈：
	 * <pre>
	 *   NullPointerException: ... "this.members" is null
	 *     at Group.add(Group.java:118)
	 *     at WndChallenges.buildCategoryBar(WndChallenges.java:352)
	 *     at WndChallenges.rebuildAll(WndChallenges.java:601)
	 *     at WndChallenges.toggleChallenge(WndChallenges.java:596)
	 *     at WndChallenges$6.onClick(WndChallenges.java:553)
	 * </pre>
	 * 原因是 {@code Group.destroy()} 会把 {@code members} 置为 null，
	 * 而窗口关闭后**队列里残留的点击事件**仍会触发 CheckBox 的 onClick，
	 * 于是往一个已销毁的 Group 里 add → 崩。
	 */
	private void toggleChallenge( ChallengeDef d ) {
		if (d == null) return;
		if (!isAlive()) return;              //窗口已销毁 → 直接忽略
		if (!editable) return;
		if (!canToggle( d ) && !mask.has( d.id )) return;

		boolean on = !mask.has( d.id );
		mask = on ? mask.with( d.id ) : mask.without( d.id );
		//互斥/前置会随选择变化，重建整表以刷新置灰
		rebuildAll();
	}

	/**
	 * END(修复·二次打开闪退): 本窗口是否还能安全操作。
	 *
	 * <p>判据是"往内容区 add 一个控件不会炸"。
	 *
	 * <p>为什么不用检查 {@code members == null}：
	 * {@code Group.members} 是 {@code protected}，本类在 {@code windows} 包、
	 * 而 {@code Group} 在 {@code com.watabou.noosa} —— 跨包访问不到。
	 *
	 * <p>所以改用**试探法**：{@code Group.add()} 在 members 为 null 时会 NPE，
	 * 那就加一个临时控件并立刻移除。代价是一次无害的分配，
	 * 换来确定性（比读一个访问不到的字段可靠）。
	 */
	private boolean isAlive() {
		try {
			if (catContent == null || content == null) return false;
			com.watabou.noosa.Gizmo probe = new com.watabou.noosa.Gizmo();
			catContent.add(probe);
			catContent.remove(probe);
			return true;
		} catch (Throwable t) {
			//members 为 null（窗口已 destroy）→ 这里会抛
			return false;
		}
	}

	/** 选择变化后重建（分类高亮 + 列表 + 通过等级）。 */
	private void rebuildAll() {
		if (!isAlive()) return;              //同上：已销毁就不要重建

		buildCategoryBar();
		content.clear();
		boxes.clear();
		defs.clear();                     //必须同步清空，否则会与 boxes 错位
		buildList();
		updatePassLevel();
	}

	/**
	 * 该条规则当前是否允许勾选。
	 *
	 * <p>不允许的情况：未实装 / 互斥冲突 / 前置未满足。
	 * <p>**已勾选的项永远允许取消**，否则玩家会被卡死。
	 */
	private boolean canToggle( ChallengeDef d ) {
		if (mask.has( d.id )) return true;
		if (!d.isImplemented()) return false;
		if (!d.prerequisitesMet( mask )) return false;
		if (!d.conflictingWith( mask ).isEmpty()) return false;
		return true;
	}

	private void updatePassLevel() {
		int level = mask.passLevel();
		int count = mask.activeCount();
		passLevelText.text( Messages.get( this, "pass_level", level, count ) );
	}

	//==== 详情 ====

	private String describe( ChallengeDef d ) {
		StringBuilder sb = new StringBuilder();

		sb.append( d.name );
		if (!d.isImplemented()) {
			sb.append( Messages.get( this, "not_implemented" ) );
		}
		sb.append( "\n\n" );
		sb.append( d.group ).append( "   " ).append( d.tendencyName() )
				.append( "   Lv" ).append( d.level );
		if (!d.effect.isEmpty()) {
			sb.append( "\n\n" ).append( d.effect );
		}

		appendRelations( sb, d, ChallengeRelation.Type.EXCLUSIVE,
				Messages.get( this, "rel_exclusive" ) );
		appendRelations( sb, d, ChallengeRelation.Type.SYNERGY,
				Messages.get( this, "rel_synergy" ) );
		appendRelations( sb, d, ChallengeRelation.Type.RESTRICTION,
				Messages.get( this, "rel_restriction" ) );
		appendRelations( sb, d, ChallengeRelation.Type.PREREQUISITE,
				Messages.get( this, "rel_prerequisite" ) );

		List<Integer> dead = d.restrictedBy( mask );
		if (!dead.isEmpty()) {
			StringBuilder names = new StringBuilder();
			for (int id : dead) {
				ChallengeDef o = ChallengeRegistry.byId( id );
				if (names.length() > 0) names.append( "、" );
				names.append( o != null ? o.name : ("#" + id) );
			}
			sb.append( "\n\n" ).append( Messages.get( this, "restricted_now", names.toString() ) );
		}

		return sb.toString();
	}

	private void appendRelations( StringBuilder sb, ChallengeDef d,
								  ChallengeRelation.Type type, String label ) {
		List<ChallengeRelation> rels = d.relationsOf( type );
		if (rels.isEmpty()) return;

		StringBuilder ids = new StringBuilder();
		for (ChallengeRelation r : rels) {
			for (int id : r.targets) {
				ChallengeDef other = ChallengeRegistry.byId( id );
				if (ids.length() > 0) ids.append( "、" );
				ids.append( other != null ? other.name : ("#" + id) );
			}
		}
		sb.append( "\n\n" ).append( label ).append( "：" ).append( ids );
	}

	/**
	 * END(修复·列表偏移 330/570 像素): 显式摆正 ScrollPane 的内部 camera。
	 *
	 * <h3>问题所在</h3>
	 * {@code ScrollPane.layout()} 里这样定位它的内部裁剪 camera：
	 * <pre>
	 *   Point p = camera().cameraToScreen( x, y );
	 *   content.camera.x = p.x;
	 *   content.camera.y = p.y;
	 * </pre>
	 * 而 {@code Camera.cameraToScreen()} 的公式是
	 * {@code (x - scroll.x) * zoom + this.x} —— 它期望调用方的
	 * {@code camera()} 是**用于 UI 的相机**。
	 *
	 * <p>实测（2560×1335 屏幕、zoom=5）：窗口在 {@code (950, 99)}，
	 * 但 {@code content.camera} 被算成了 {@code (1280, 922)} ——
	 * 右偏 330、下偏 570 物理像素，正好表现为"列表往右下溢出、
	 * 盖住英雄立绘"。
	 *
	 * <h3>为什么要在这里纠正</h3>
	 * 同一套 {@code ScrollPane} 在 {@code WndJournal} 等窗口里是正常的，
	 * 差异在于它们的 ScrollPane 挂在**中间层的 Component** 上，
	 * 而这里是直接挂在 {@code Window} 上 —— 父级链不同，
	 * {@code camera()} 解析出来的相机就不同。
	 *
	 * <p>与其改动公共的 {@code ScrollPane}（会影响所有窗口），
	 * 不如在本窗口内**显式把内部 camera 摆到窗口坐标系里的正确位置**：
	 * 内容点 (cx,cy) 相对窗口左上角，因此屏幕位置应为
	 * {@code (cx - chrome.scroll) * zoom + Window.camera.x}。
	 */
	@Override
	public void update() {
		super.update();

		//==== END(修复·实测仍在 (1280,922)): 直接纠正 content.camera ====
		//上一版只把 ScrollPane 自身的 camera 绑定到窗口相机，期望它自己的
		//layout() 就能算出正确位置。实测**没有生效** —— content.camera 仍被
		//算成 (1280, 922)（= uiCamera 的中心 scroll），而不是 (980, 384)。
		//
		//原因：ScrollPane 的 content.camera 是它**自己 new 并 Camera.add() 的
		//独立相机**，其 x/y 只由 ScrollPane.layout() 里那句
		//`cs.x = camera().cameraToScreen(x,y).x` 设定；而 layout() 的调用时机
		//与 camera() 当时解析到的对象都不受我们控制。
		//
		//所以改为：每帧直接把 content.camera 摆到窗口坐标系里的正确位置。
		//这是唯一能对抗 layout() 覆盖的做法，且开销只是两次赋值。
		bindScrollPaneCamera( pane );
		placeContentCamera( pane, pane == null ? 0 : pane.height() );
	}

	/**
	 * END(修复·列表偏移): 把 ScrollPane 的内部裁剪相机摆到窗口里的正确位置。
	 *
	 * <p>窗口渲染内容点 (cx, cy) 时落在屏幕 {@code (cx - scroll)*zoom + camera.x}。
	 * content.camera 是**内容区自己的相机**，所以它应该落在：
	 * <pre>
	 *   x = (pane.left() - Window.camera.scroll.x) * zoom + Window.camera.x
	 *   y = (pane.top()  - Window.camera.scroll.y) * zoom + Window.camera.y
	 * </pre>
	 * 并让它裁剪出 {@code pane.width() × pane.height()} 的区域。
	 *
	 * <p>同时必须保留 content.camera 的 **scroll**（那是滚动位置），
	 * 只改 x/y/尺寸，否则滚动会被重置到顶部。
	 */
	private void placeContentCamera( ScrollPane sp, float h ) {
		if (sp == null || sp.content() == null) return;
		com.watabou.noosa.Camera inner = sp.content().camera;
		if (inner == null) return;
		if (camera == null) return;

		inner.x = (int) ((sp.left() - camera.scroll.x) * camera.zoom + camera.x);
		inner.y = (int) ((sp.top()  - camera.scroll.y) * camera.zoom + camera.y);
		inner.zoom = camera.zoom;
		inner.resize( Math.max(1, (int) sp.width()), Math.max(1, (int) h) );
	}

	/**
	 * END(修复·列表右下偏移 / 分类栏不能左右滑 / 点击后又跳回去):
	 * 把 ScrollPane 的父级相机**显式绑定**到本窗口的 camera。
	 *
	 * <h3>根因</h3>
	 * {@code ScrollPane.layout()} 用 {@code camera().cameraToScreen(x, y)} 定位
	 * 它内部的裁剪 camera，而 {@code Gizmo.camera()} 是**向上查找并缓存**的：
	 * <pre>
	 *   if (camera != null) return camera;          // 已缓存就直接用
	 *   else if (parent != null) return camera = parent.camera();
	 *   else return null;                            // ← 此时返回 null
	 * </pre>
	 * 只要在 {@code add()} 生效**之前**调用过一次 {@code camera()}，
	 * 或者中间隔了一层不带 camera 的父级，解析出来的就不是 Window.camera。
	 * 实测（2560×1335、zoom=5）：窗口在 {@code (950, 99)}，
	 * 而 content.camera 被算成 {@code (1280, 922)} —— 右偏 330、下偏 570。
	 *
	 * <h3>为什么表现为三个症状</h3>
	 * <ul>
	 *   <li><b>列表偏右下</b>：裁剪相机就在错的位置上画。</li>
	 *   <li><b>分类栏不能左右滑</b>：{@code PointerController.onScroll} 用
	 *       {@code content.camera.screenToCamera()} 把点击换算回内容坐标，
	 *       相机位置错 → 换算全错 → 拖不动。</li>
	 *   <li><b>点一下又跳回去</b>：每次 {@code setRect} 都会触发
	 *       {@code ScrollPane.layout()}，它会用错误的相机**再覆盖一次**，
	 *       把任何临时纠正冲掉。这就是那个"时序问题"。</li>
	 * </ul>
	 *
	 * <h3>修法</h3>
	 * 不去逐帧纠正结果（会被覆盖），而是**修正输入**：
	 * 直接给 ScrollPane 的 {@code camera} 字段赋上窗口相机，
	 * 之后它自己的 layout() 就会算出正确位置，无需干预。
	 */
	private void bindScrollPaneCamera( ScrollPane sp ) {
		if (sp == null) return;
		if (sp.camera != camera) {
			sp.camera = camera;
		}
	}

	@Override
	public void onBackPressed() {

		if (editable) {
			//写回设置（开局选择才 editable，查看已有存档时是 false）。
			//SPDSettings.challengeMask() 会同时更新旧 int 键，
			//让老代码路径也能读到已实装的那 12 条。
			com.shatteredpixel.shatteredpixeldungeon.SPDSettings.challengeMask( mask );
			Dungeon.setChallengeMask( mask );
		}

		super.onBackPressed();
	}
}
