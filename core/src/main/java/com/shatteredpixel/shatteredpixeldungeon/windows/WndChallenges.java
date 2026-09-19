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
	/**
	 * END(诊断): UI 布局/偏移诊断开关。
	 * 排查偏移问题时置 true；定稿后必须为 false（否则每帧刷屏）。
	 */
	private static final boolean UI_DEBUG = false;  //诊断开关。排查 UI 问题时改回 true。
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
		if (editable) {
			buildRandomBar();
		}

		//==== END(修复·布局统一): 改用 relayout() ====
		//原先这里手写了一遍布局计算，与 rebuildAll 各算各的 ——
		//两处一旦不同步就会出空白/重叠（这正是文档所有者遇到的两个现象）。
		//现在两条路径共用 relayout()，只有一份公式。
		buildList();
		relayout();

		passLevelText.setPos( 4, pane.top() + pane.height() + 2 );
		updatePassLevel();
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

		//==== END(改版·每行 4 个): 分类铺成 4 列网格 ====
		//窗口宽 120，每行 4 个 → 每格宽约 29.75（比之前的 23.6 更宽，字更清楚）。
		//分类名是 2 个字（经典/药剂/经济/特殊/地图/战斗/怪物/环境/装备/格林），
		//6 号字两个字约 12 像素，23 宽足够，所以**标签里不再带数量** ——
		//数字会把按钮挤到只剩几个像素。
		//当前分类的数量改由列表标题/底部文字体现。
		//END(改版): 每行 **4 个** —— 11 个分类 → 4+4+3 三行
		final int COLS = 4;
		int rows = (groups.size() + COLS - 1) / COLS;      //11 个 → 3 排
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

		//==== END(修复·随机按钮与分类栏重叠): 不要在这里设置位置 ====
		//原先这里是 {@code catContent.setPos(0, 0)} —— 那是**错的**：
		//本方法会被 {@code rebuildAll()} 反复调用（勾选任一条规则时），
		//而每次调用都会把分类栏拉回 y=0，也就是**盖到标题与随机条上**。
		//玩家看到的就是"随机按钮和第一排分类重叠"。
		//
		//位置应该由布局代码**唯一负责**（构造时设一次即可，见下方的
		//{@code catContent.setPos(0, top)}）。这里只负责内容与尺寸。
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

		//4 列网格：按坐标反算行列（必须与 buildCategoryBar 的 COLS 一致）
		final int COLS = 4;
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
				showDetail( d );
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

		//==== END(修复·切换分类后不回到顶部): 把滚动位置归零 ====
		//文档所有者报告："一个有 30 条挑战的分类，你划到 30 条，
		//再切换到一个有 10 条挑战的分类，它就会什么也不显示。
		//因为它的位置到了 30 条的位置，你必须要移动一下才能回到相应的位置。"
		//
		//根因：{@code content.clear()} + {@code buildList()} 换了内容，
		//但 {@code content.camera.scroll} 还停在旧分类的位置 ——
		//新分类内容更短时，滚动条仍指在"下面"，于是可视区落在空白上。
		//
		//修法：换分类后**把滚动位置归零**（回到顶部）。
		//ScrollPane.scrollTo() 自带边界裁剪（见其实现），
		//所以即使新内容更短也只是被夹到合法范围，不会出错。
		//
		//注意：要在 resize() **之后**调用 —— scrollTo 的裁剪依赖
		//pane 的 height()（可视区高度），那是 resize 时定下来的。
		if (pane != null) {
			pane.scrollTo(0, 0);
		}
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

		//==== END(修复·随机条与第一行分类重叠): 必须覆写 layout() ====
		//文档所有者实测："第一行 4 个分类 + 随机条" —— 两者画在同一排。
		//
		//根因：{@code Group.draw()} 遍历子控件时使用的
		//是**子控件自己的 x/y（绝对坐标）**，不会加上父级偏移：
		//    for (Gizmo g : members) { g.draw(); }   // ← 没有 x + g.x
		//
		//所以我原来写的 {@code minus.setRect(0, 0, ...)} 意味着
		//"画在窗口左上角 (0,0)" —— 也就是标题那一排。
		//而 {@code randomBar.setRect(0, 68, ...)} 只改了 randomBar 自己，
		//子控件纹丝不动。
		//
		//正确做法（原版 IconTitle / TalentsTab 等 Component 子类都这么做）：
		//**覆写 layout()**，在里按 {@code x + 相对偏移} 摆放子控件。
		//这样每次 setRect() 都会重新摆位。
		randomBar = new Component() {
			@Override
			protected void layout() {
				super.layout();
				if (members == null) return;
				//相对于本 Component 的偏移（与窗口坐标同一套，只是加了 x/y）
				float ox = x;
				float oy = y;

				float cx = ox;
				if (randomMinus != null) {
					randomMinus.setRect(cx, oy, 12, 16);
					cx += 14;
				}
				if (randomPlus != null) {
					randomPlus.setRect(cx, oy, 12, 16);
					cx += 14;
				}
				if (randomRoll != null) {
					randomRoll.setRect(cx, oy, WIDTH - cx, 16);
				}
				posTargetText();
			}
		};
		add( randomBar );

		final int maxT = ChallengeRandomizer.maxTarget( includePending );
		if (targetLevel > maxT) targetLevel = maxT;
		if (targetLevel < ChallengeRandomizer.MIN_TARGET) {
			targetLevel = ChallengeRandomizer.MIN_TARGET;
		}

		randomMinus = new RedButton( "-", 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				if (targetLevel > ChallengeRandomizer.MIN_TARGET) targetLevel--;
				updateTargetText();
			}
		};
		randomBar.add( randomMinus );

		targetText = PixelScene.renderTextBlock( "", 7 );
		targetText.hardlight( 0xFFFF88 );
		randomBar.add( targetText );

		randomPlus = new RedButton( "+", 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				if (targetLevel < ChallengeRandomizer.maxTarget( includePending )) targetLevel++;
				updateTargetText();
			}
		};
		randomBar.add( randomPlus );

		randomRoll = new RedButton( Messages.get( this, "roll" ), 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				mask = ChallengeRandomizer.roll( targetLevel, includePending );
				rebuildAll();
			}
		};
		randomBar.add( randomRoll );

		updateTargetText();
	}

	/** END(修复): 随机条的三个按钮（layout() 里要用）。 */
	private RedButton randomMinus, randomPlus, randomRoll;

	/** END(修复): 把目标数字放到 "-" 与 "+" 之间。 */
	private void posTargetText() {
		if (targetText == null || randomBar == null) return;
		targetText.setPos(randomBar.top() == 0 ? 15 : randomBar.left() + 15,
				randomBar.top() + (16 - targetText.height()) / 2);
	}

	private void updateTargetText() {
		if (targetText == null) return;
		targetText.text( Messages.get( this, "target", targetLevel ) );
		//END(修复): 用 posTargetText() 统一摆位 —— 那里会加上 randomBar 的绝对偏移。
		posTargetText();
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
					showDetail( d );
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
	 * END(修复·关闭挑战窗口后仍能点到详细介绍): 打开某条规则的详情窗口。
	 *
	 * <h3>原先的问题</h3>
	 * 两处调用都写成：
	 * <pre>
	 *   ShatteredPixelDungeon.scene().add( new WndMessage( describe( d ) ) );
	 * </pre>
	 * 两个毛病：
	 * <ol>
	 *   <li>用 {@code scene().add()} 而不是 {@code GameScene.show()} ——
	 *       前者只把窗口挂到场景上，**不经过 GameScene 的窗口栈管理**，
	 *       所以挑战窗口关闭时它不会被一起清理</li>
	 *   <li><b>没有生命周期守卫</b> —— 挑战窗口已经销毁后，队列里残留的
	 *       点击事件仍会走到这里，于是"关了窗口还能弹出介绍"</li>
	 * </ol>
	 *
	 * <h3>现在的做法</h3>
	 * <ul>
	 *   <li>先检查 {@link #isAlive()} —— 窗口已关闭就直接忽略</li>
	 *   <li>用 {@code GameScene.show()} 打开详情（那是"在最前面显示一个窗口"
	 *       的规范入口，会自动处理层级与关闭）</li>
	 * </ul>
	 */
	private void showDetail( ChallengeDef d ) {
		if (d == null) return;

		//END(诊断): 打印"谁在什么时候要求打开详情" ——
		//用来定位"关掉挑战窗口后还能弹出介绍"的真实来源。
		if (UI_DEBUG) {
			System.out.println("[详情诊断] 请求打开: " + d.id + " " + d.name
					+ "  isAlive=" + isAlive()
					+ "  parent=" + (parent == null ? "null" : parent.getClass().getSimpleName())
					+ "  visible=" + visible);
		}

		if (!isAlive()) return;                 //窗口已销毁 → 忽略残留点击

		com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(
				new WndMessage( describe( d ) ));
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
	 * END(修复·二次打开闪退 / 关闭后仍能点击): 本窗口是否还能安全操作。
	 *
	 * <h3>为什么不能只探测 Group.members</h3>
	 * 上一版用"往 catContent 里 add 一个临时控件"来判断 ——
	 * 那只能发现 {@code Group.destroy()} 已经把 {@code members} 置 null 的情况。
	 *
	 * <p>但 {@code Window.hide()} 的路径是：
	 * <pre>
	 *   parent.erase(this);   // 先从场景树摘掉
	 *   destroy();            // 再销毁
	 * </pre>
	 * 在 {@code erase()} 之后、{@code destroy()} 之前的那个瞬间，
	 * 窗口已经**不可见、不该响应任何输入**了，但 members 还在 ——
	 * 于是残留的点击事件照样能走到这里。
	 *
	 * <h3>可靠判据</h3>
	 * <ul>
	 *   <li>{@code parent == null} → 已经从场景树摘掉，不再响应</li>
	 *   <li>{@code visible == false} → 已隐藏</li>
	 *   <li>两者都不满足时，再做一次 members 探测兜底</li>
	 * </ul>
	 */
	private boolean isAlive() {
		try {
			//① 已从场景树摘掉 / 已隐藏 → 一定不能操作
			if (parent == null) return false;
			if (!visible) return false;

			if (catContent == null || content == null) return false;

			//② 兜底：members 已被销毁的探测
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

		//==== END(修复·挑战区后方的空白): 重建后必须重算整体布局 ====
		//文档所有者报告："挑战区后面和正式挑战中间有一块空白。"
		//
		//根因：本方法只重建了"分类栏 + 列表内容"，但**没有重算各控件的
		//y 位置**。分类栏的排数由分组数决定（{@code catRows}），
		//如果这个值变了（或列表高度变了），下方控件仍停在旧位置，
		//中间就留出一段没有任何控件的空白。
		//
		//所以这里复刻一次构造时的布局计算 —— 与构造路径保持**同一套公式**，
		//避免两处不一致（那正是这类 bug 的温床）。
		relayout();
	}

	/**
	 * END(修复): 按当前 {@code catRows} / 内容高度重算所有控件的 y 位置。
	 *
	 * <p>与构造时的布局逻辑保持一致。抽成方法是为了让
	 * "构造"与"重建"两条路径用**同一份代码**，不会各自漂移。
	 */
	private void relayout() {
		float top = TTL_HEIGHT;

		//==== END(改版·每行 4 个 + 随机条下移): 分类栏在标题正下方 ====
		//文档所有者要求：
		//  · 分类栏每行 **4 个**（原为 5 个）—— 11 个分类 → 4+4+3 三行
		//  · **随机条移到分类栏下方**（原在标题与分类栏之间）
		//
		//新布局：
		//  标题      0 .. 16
		//  分类栏    16 .. 67    （3 排 × 17）
		//  随机条    67 .. 85
		//  列表      85 ..
		if (catContent != null) {
			catContent.setPos(0, top);
		}
		top += catHeight() + 1;

		if (editable && randomBar != null) {
			randomBar.setRect(0, top, WIDTH, RANDOM_BAR_H);
			top += RANDOM_BAR_H;
		}

		//列表
		float bottomH = 14;
		float maxByScreen = com.watabou.noosa.Camera.main.height - top - bottomH - 6;
		float listH = Math.min(content.height(),
				Math.min(MAX_LIST_H, Math.max(48, maxByScreen)));
		pane.setRect(0, top, WIDTH, listH);
		resize(WIDTH, (int) (top + listH + bottomH));

		if (passLevelText != null) {
			passLevelText.setPos(4, top + listH + 2);
		}

		//==== END(诊断·布局数值): 打印每一段控件的实际 y 范围 ====
		//文档所有者报告"第三行的分类与列表第一条之间有空白" ——
		//这条日志把每段的**起止 y** 列出来，一眼就能看出空白落在哪一段。
		if (UI_DEBUG) {
			System.out.println("=== [布局] 分段 y 范围（窗口高 " + (camera == null ? -1 : camera.height) + "）===");
			System.out.println("  标题      0 .. " + TTL_HEIGHT);
			if (editable && randomBar != null) {
				System.out.println("  随机条    " + randomBar.top() + " .. "
						+ randomBar.bottom() + "   (高 " + RANDOM_BAR_H + ")");
			}
			System.out.println("  分类栏    " + catContent.top() + " .. "
					+ (catContent.top() + catContent.height())
					+ "   (catRows=" + catRows + ", catHeight=" + catHeight()
					+ ", 按钮数=" + catButtons.size() + ")");
			System.out.println("  列表      " + pane.top() + " .. "
					+ (pane.top() + pane.height())
					+ "   (listH=" + listH + ", 内容高=" + content.height() + ")");
			System.out.println("  底部文字  " + (top + listH + 2));

			//逐排打印分类按钮的实际屏幕 y
			for (int i = 0; i < catButtons.size(); i++) {
				System.out.println("    分类[" + i + "] "
						+ catContent.top() + "+" + catButtons.get(i).top()
						+ " => " + (catContent.top() + catButtons.get(i).top())
						+ " .. " + (catContent.top() + catButtons.get(i).bottom()));
			}
		}
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

		//==== END(诊断·随机条位置): 每帧打印随机条与分类栏的实际 y ====
		//文档所有者描述："第一行 4 个分类 + 随机条" —— 两者同一排。
		//但布局日志说 randomBar.y = 68、分类栏 16..67，两者不重叠。
		//所以要么 relayout() 没被调用，要么 y 被别处覆盖了 ——
		//这条日志在**每帧**打印实际值，能直接定位。
		if (UI_DEBUG && uiPosFrames < 5) {
			uiPosFrames++;
			System.out.println("[位置诊断·第" + uiPosFrames + "帧]"
					+ "  randomBar.y=" + (randomBar == null ? "null" : ("" + randomBar.top()))
					+ "  catContent.y=" + (catContent == null ? "null" : ("" + catContent.top()))
					+ "  catContent.h=" + (catContent == null ? "null" : ("" + catContent.height()))
					+ "  pane.y=" + (pane == null ? "null" : ("" + pane.top())));
		}

		//==== END(诊断·偏移): 在 update() 之后验证 content.camera 的真实位置 ====
		//之前的诊断打在**构造时**（layout 之后），那时 update() 还没跑过 ——
		//所以它显示的 (1280, xxx) 只代表"刚建好时"，不代表稳定状态。
		//这里在每帧纠正**之后**再检查一次，才能真正判断修复有没有生效。
		//
		//只打印前 3 帧，避免刷屏。
		if (UI_DEBUG && pane != null && pane.content() != null
				&& pane.content().camera != null && uiDebugFrames < 3) {
			uiDebugFrames++;
			com.watabou.noosa.Camera inner = pane.content().camera;
			com.watabou.noosa.Camera resolved = pane.camera();
			float wantX = (pane.left() - camera.scroll.x) * camera.zoom + camera.x;
			float wantY = (pane.top()  - camera.scroll.y) * camera.zoom + camera.y;

			System.out.println("[偏移诊断·第" + uiDebugFrames + "帧]");
			System.out.println("  窗口 camera 字段 = " + (camera == null ? "null" : 
					("x=" + camera.x + " y=" + camera.y + " zoom=" + camera.zoom
							+ " scroll=" + camera.scroll.x + "," + camera.scroll.y)));
			System.out.println("  pane.camera() 解析到 = " + (resolved == null ? "null" :
					("x=" + resolved.x + " y=" + resolved.y + " zoom=" + resolved.zoom
							+ " scroll=" + resolved.scroll.x + "," + resolved.scroll.y
							+ (resolved == camera ? "  [=窗口 camera]"
								: (resolved == com.shatteredpixel.shatteredpixeldungeon
										.scenes.PixelScene.uiCamera ? "  [=uiCamera!]"
										: "  [=其它]")))));
			System.out.println("  content.camera = (" + inner.x + "," + inner.y
					+ ")  期望=(" + (int) wantX + "," + (int) wantY + ")"
					+ (Math.abs(inner.x - wantX) < 2 && Math.abs(inner.y - wantY) < 2
							? "  [OK]" : "  [!! 仍不匹配]"));
		}
	}

	/** END(诊断): 已打印的帧数，避免刷屏。 */
	private int uiDebugFrames = 0;

	/** END(诊断): 位置诊断已打印的帧数。 */
	private int uiPosFrames = 0;

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
		//END(重构): 实现抽到 ScrollPaneCamera 工具类 ——
		//WndMoneyIsPower 也要用同一套修正，不该复制两份。
		com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPaneCamera
				.placeContentCamera(sp, this, h);
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
		//END(重构): 同上，委托给工具类。
		com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPaneCamera
				.bindCamera(sp, this);
	}

	@Override
	public void hide() {
		if (UI_DEBUG) {
			System.out.println("[详情诊断] 挑战窗口 hide() 被调用");
		}
		super.hide();
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
