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

	//==== END(随机挑战) ====
	/** END(改版): 随机条高度 = 滑块 22 + 按钮 16 + 间距 2。 */
	private static final int RANDOM_BAR_H = 40;

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

		//翻页栏（END 分页）
		buildPageBar();

		//==== END(新增·确定键): 底部确定按钮 ====
		//文档所有者反馈："挑战界面没有确定键，导致难以返回。"
		//
		//行为与原版其它窗口一致：点它就关闭本窗口。
		//在开局选择场景里，onBackPressed() 会顺手把勾选写回设置 ——
		//所以"确定"与"返回"是同一个语义，不存在"没保存"的问题。
		confirmBtn = new RedButton( Messages.get( this, "confirm" ) ) {
			@Override
			protected void onClick() {
				super.onClick();
				onBackPressed();
			}
		};
		add( confirmBtn );

		//==== END(新增·归零键) ====
		//文档所有者要求："加一个归零"。
		//
		//作用：一键取消**当前所有已勾选**的挑战（清空掩码）。
		//不做二次确认 —— 这是"反悔"操作，点错了再勾回来即可；
		//每次都要确认反而很烦（尤其在反复试搭配时）。
		resetBtn = new RedButton( Messages.get( this, "reset" ) ) {
			@Override
			protected void onClick() {
				super.onClick();
				if (!editable || !isAlive()) return;

				mask = com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
						.ChallengeMask.empty();
				page = 0;
				rebuildAll();
			}
		};
		add( resetBtn );

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
		if (!isAlive()) return;

		//==== END(加强·翻页后误点): 用"内容坐标 + 边界校验"双重判定 ====
		//文档所有者反馈："在切换页时，如果有点位置没有内容，点击会点到上一页的挑战。"
		//
		//除了在 rebuildPage() 里重置滚动（那是根因），这里再加一道保险：
		//把 y **夹到当前内容的合法范围内** —— 超出范围说明这一击落在
		//列表下方的空白（或偏移后的无效区），直接忽略。
		//
		//这样即使滚动偏移因为某些边界情况没归零，也不会误命中别的条目。
		if (content == null) return;
		float contentH = content.height();
		if (y < 0 || y > contentH) return;

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

			//END(修复·前置弹窗): 缺前置的条目也是 active 的（见 buildList），
			//所以这里不再区分 —— 交给 toggleChallenge 判断
			//"直接勾选"还是"弹窗询问补前置"。
			if (!cb.active) return;         //不可选（未实装/互斥）

			toggleChallenge( d );
			return;
		}
	}

	/** 切换分类：更新高亮 + 重建列表。 */
	private void switchGroup( String g ) {
		currentGroup = g;
		//END(分页): 换分类后回到第一页 ——
		//否则从"27 页的战斗"切到"只有 1 页的药剂"会停在不存在的页上。
		page = 0;

		//刷新按钮高亮
		for (int i = 0; i < catButtons.size(); i++) {
			catButtons.get(i).textColor(
					groups.get(i).equals(currentGroup) ? 0xFFFF88 : 0xCCCCCC );
		}

		content.clear();
		boxes.clear();
		defs.clear();                     //与 boxes 同步清空
		buildList();

		//==== END(分页·布局统一): 复用 relayout()，不再自己算一遍 ====
		//原先这里手写了一段布局计算，与构造路径各算各的 ——
		//两处一旦不同步就会出现空白/错位。
		//现在列表高度是固定的（PER_PAGE 条），relayout() 一份公式管所有路径。
		relayout();

		//END(分页): 换页后回到顶部（列表已不滚动，但保险起见）
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

		//==== END(改版·照原版做随机): 用 OptionSlider 取代 "- N +" ====
		//文档所有者反馈："加减号过于大，数字显示不出来，你就按原版那种弄吧"。
		//
		//原版的 {@code WndRandomize}（HeroSelectScene 内部类）用的是：
		//   OptionSlider(title, min, max, minVal, maxVal)
		//滑块自带标题与两端标号，占地小，而且**选中值由拖动决定**，
		//不需要两个大按钮去挤数字的空间。
		//
		//布局：[ 目标分滑块（占满宽度） ]
		//      [ 随机 ]（独占一行，避免与滑块挤）
		//
		//注意：{@code Group.draw()} 用**子控件自己的绝对坐标**，
		//所以这里的 layout() 必须把 x/y 加上去（这正是之前重叠的根因）。
		randomBar = new Component() {
			@Override
			protected void layout() {
				super.layout();
				if (members == null) return;
				float ox = x, oy = y;

				if (randomSlider != null) {
					randomSlider.setRect(ox, oy, WIDTH, 22);
				}
				if (randomRoll != null) {
					randomRoll.setRect(ox, oy + 22, WIDTH, 16);
				}
			}
		};
		add( randomBar );

		final int maxT = ChallengeRandomizer.maxTarget( includePending );
		if (targetLevel > maxT) targetLevel = maxT;
		if (targetLevel < ChallengeRandomizer.MIN_TARGET) {
			targetLevel = ChallengeRandomizer.MIN_TARGET;
		}

		//目标分数滑块 —— 与 HeroSelectScene.WndRandomize 里那个同款
		randomSlider = new com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider(
				Messages.get(this, "target_title"),
				Integer.toString(ChallengeRandomizer.MIN_TARGET),
				Integer.toString(maxT),
				ChallengeRandomizer.MIN_TARGET, maxT) {
			@Override
			protected void onChange() {
				//拖动时只更新值，不立刻重roll —— 玩家松手后再点"随机"
				targetLevel = getSelectedValue();
			}
		};
		randomSlider.setSelectedValue(targetLevel);
		randomBar.add(randomSlider);

		//"随机"按钮（独占一行）
		randomRoll = new RedButton( Messages.get( this, "roll" ), 6 ) {
			@Override
			protected void onClick() {
				super.onClick();
				mask = ChallengeRandomizer.roll( targetLevel, includePending );
				rebuildAll();
			}
		};
		randomBar.add( randomRoll );
	}

	/** END(改版): 随机条的控件（layout() 里要用）。 */
	private RedButton randomRoll;
	private com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider randomSlider;

	//==================================================================
	//分页（END 改版）
	//==================================================================

	/**
	 * 每页显示的挑战条数。
	 *
	 * <p>文档所有者要求："1 页 10 条挑战，用第二页第三页按键切换"。
	 */
	private static final int PER_PAGE = 7;   //END(修订): 10 -> 7（文档所有者：UI 占了全屏）

	/** END(新增·确定键): 底部按钮高度。 */
	private static final int CONFIRM_H = 16;

	/** END(新增·确定键): 底部的确定按钮。 */
	private RedButton confirmBtn;

	/** END(新增·归零键): 底部的一键清空按钮。 */
	private RedButton resetBtn;

	/** 当前页号（0 基）。切换分类时归零。 */
	private int page = 0;

	/** 翻页栏的三个按钮（上一页 / 页码 / 下一页）。 */
	private RedButton pagePrev, pageNext;
	private RenderedTextBlock pageLabel;

	/**
	 * END(分页): 构建翻页栏。
	 *
	 * <p>布局：[上一页] [ 3 / 27 ] [下一页]
	 *
	 * <p>只有一页时整条隐藏 —— 不给玩家无意义的按钮。
	 */
	private void buildPageBar() {
		if (pageBar != null) {
			pageBar.killAndErase();
		}
		pageBar = new Component() {
			@Override
			protected void layout() {
				super.layout();
				float ox = x, oy = y;
				float mid = WIDTH / 2f;

				if (pagePrev != null) pagePrev.setRect(ox, oy, 40, 16);
				if (pageNext != null) pageNext.setRect(ox + WIDTH - 40, oy, 40, 16);
				if (pageLabel != null) {
					pageLabel.setPos(ox + (WIDTH - pageLabel.width()) / 2f,
							oy + (16 - pageLabel.height()) / 2f);
					PixelScene.align(pageLabel);
				}
			}
		};
		add(pageBar);

		pagePrev = new RedButton("<", 6) {
			@Override
			protected void onClick() {
				super.onClick();
				if (page > 0) { page--; rebuildPage(); }
			}
		};
		pageBar.add(pagePrev);

		pageLabel = PixelScene.renderTextBlock("", 6);
		pageLabel.hardlight(0xFFFF88);
		pageBar.add(pageLabel);

		pageNext = new RedButton(">", 6) {
			@Override
			protected void onClick() {
				super.onClick();
				if (page < pageCount() - 1) { page++; rebuildPage(); }
			}
		};
		pageBar.add(pageNext);
	}

	/** END(分页): 页容器。 */
	private Component pageBar;

	/** END(分页): 是否需要显示翻页栏（只有一页时不显示）。 */
	private boolean pageBarVisible() {
		return pageCount() > 1;
	}

	/** END(分页): 当前分类的页数。 */
	private int pageCount() {
		if (currentGroup == null) return 1;
		int n = ChallengeRegistry.inGroup(currentGroup).size();
		return Math.max(1, (n + PER_PAGE - 1) / PER_PAGE);
	}

	/**
	 * END(分页): 刷新当前页（只重建列表，不重整布局）。
	 *
	 * <p>与 {@code rebuildAll()} 的区别：那个会连分类栏一起重建
	 * （因为勾选变化会影响其它条目的置灰），而翻页只需要换列表内容。
	 */
	private void rebuildPage() {
		if (!isAlive()) return;
		content.clear();
		boxes.clear();
		defs.clear();
		buildList();
		updatePageLabel();

		//==== END(修复·翻页后点到"上一页"的条目) ====
		//文档所有者反馈："在切换页时，如果有点位置没有内容，点击会点到上一页的挑战。"
		//
		//根因：ScrollPane.onClick 传给我们的坐标是
		//    content.camera.screenToCamera(屏幕坐标)
		// 而 screenToCamera 会**加上 content.camera.scroll**（内容滚动偏移）。
		//
		//翻页时我们换了内容（content.clear + buildList），但**没重置滚动** ——
		//上一页留下的 scroll 会让换算出来的 y 整体偏移，
		//于是点空白处会命中"偏移后"的那一条，看起来就像点到了上一页的东西。
		//
		//修法：翻页后把滚动归零（回到顶部），并把相机重新摆正。
		if (pane != null) {
			pane.scrollTo(0, 0);
		}
		if (pane != null && pane.content() != null && pane.content().camera != null) {
			pane.content().camera.scroll.set(0, 0);
		}
		bindScrollPaneCamera(pane);
	}

	/** END(分页): 更新页码文字与按钮可用状态。 */
	private void updatePageLabel() {
		if (pageLabel == null) return;
		int total = pageCount();
		//只有一页 → 整条隐藏
		boolean show = total > 1;
		if (pageBar != null) pageBar.visible = show;
		if (!show) return;

		pageLabel.text((page + 1) + " / " + total);
		if (pagePrev != null) pagePrev.enable(page > 0);
		if (pageNext != null) pageNext.enable(page < total - 1);

		//文字长度会变，让 layout 重新摆一次居中
		if (pageBar != null) pageBar.setRect(pageBar.left(), pageBar.top(),
				WIDTH, RANDOM_BAR_H - 2);
	}

	/**
	 * END(改版): 目标分数由 OptionSlider 自己显示，不需要额外文字。
	 *
	 * <p>保留这个方法名是为了不打断历史调用点（构造/切分类时都会调一次）。
	 */
	private void updateTargetText() {
		//空实现 —— 滑块自带标题与数值显示。
	}

	//==== 列表 ====

	/** 构建**当前分类**下的挑战列表（只建当前页）。 */
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

		//==== END(改版·分页): 只渲染当前页的条目 ====
		//文档所有者要求："改成分页，比如 1 页 10 条挑战，用第二页第三页按键切换，
		//这样就不需要拖动，也不会产生 bug。"
		//
		//所以这里不再把所有条目都塞进 ScrollPane，而是只建
		// [page*PER_PAGE, (page+1)*PER_PAGE) 这一段。
		//列表区高度固定 = PER_PAGE × 每行高，不再随内容变化 ——
		//这正是"界面忽大忽小"的根源。
		int from = Math.max(0, page * PER_PAGE);
		int to   = Math.min(inGroup.size(), from + PER_PAGE);

		//页号越界（切换分类后条目数变了）→ 夹到最后一页
		if (from >= inGroup.size()) {
			page = Math.max(0, (inGroup.size() - 1) / PER_PAGE);
			from = page * PER_PAGE;
			to   = Math.min(inGroup.size(), from + PER_PAGE);
		}

		for (int idx = from; idx < to; idx++) {

			final ChallengeDef d = inGroup.get(idx);

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
			//==== END(修复·前置弹窗没出现): 缺前置的条目要保持可点 ====
			//文档所有者反馈："询问前置的窗口没有出现。"
			//
			//第二道关卡在这里：canToggle() 因为"前置未满足"返回 false，
			//于是 cb.active = false —— 而 RedButton 在 inactive 时
			//**连 onClick 都不会触发**，玩家点了完全没反应。
			//
			//修法：若这条规则**只是缺前置**（其它条件都满足），
			//就让它保持 active —— 点下去会走 toggleChallenge 里的
			//"是否补齐前置"弹窗分支。
			cb.active = editable && (canToggle( d ) || canAskPrereq( d ));

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
		if (!isAlive()) return;                 //窗口已销毁 → 忽略残留点击

		//==== END(修复·点详情卡死): 必须用 addToFront，不能用 GameScene.show ====
		//文档所有者报告："在挑战界面选择时，点击挑战详细会卡死。"
		//
		//根因：{@code GameScene.show()} 的第一句是 {@code cancel()} ——
		//它会**关掉当前窗口**。而本方法正是被那个窗口的点击事件调用的，
		//于是在自身回调里把自己销毁了，紧接着的 addToFront 操作了
		//一个已 destroy() 的对象 → 卡死。
		//
		//另外 {@code GameScene.show()} 还依赖 GameScene.scene 非空，
		//而挑战窗口也能在开局选择场景（HeroSelectScene）里打开 ——
		//那种情况下同样会出问题。
		//
		//正确做法（原版 WndKeyBindings / WndSettings 都是这么写的）：
		//直接往场景最前面加，**不动**当前窗口。
		com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.scene()
				.addToFront( new WndMessage( describe( d ) ) );
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

		boolean on = !mask.has( d.id );

		//==== END(修复·前置弹窗没出现) ====
		//文档所有者反馈："询问前置的窗口没有出现。"
		//
		//根因：原来的顺序是
		//    if (!canToggle(d) && !mask.has(d.id)) return;     // ← 第一道关卡
		//    ...  然后才判断是不是缺前置并弹窗
		//而 canToggle() 里**已经**因为"前置未满足"返回 false，
		//所以第一行就直接 return 了 —— 弹窗那段代码永远走不到。
		//
		//修法：把"勾选一条缺前置的规则"这个情况**提前**处理，
		//放在 canToggle 判定之前。这样它就不会被那道关卡拦掉。
		if (on && d.hasPrerequisite() && !d.prerequisitesMet(mask)) {
			//只有"已实装"的规则才谈得上补前置（未实装的点了也没用）
			if (!d.isImplemented()) return;
			//互斥检查仍然要做：前置能补，但互斥补不了
			if (!d.conflictingWith(mask).isEmpty()) return;

			askFillPrerequisites(d);
			return;
		}

		if (!canToggle( d ) && !mask.has( d.id )) return;

		mask = on ? mask.with( d.id ) : mask.without( d.id );
		//互斥/前置会随选择变化，重建整表以刷新置灰
		rebuildAll();
	}

	/**
	 * END(新增·前置自动补齐): 询问是否把前置一并勾上。
	 *
	 * <h3>为什么用 WndOptions 而不是自己画</h3>
	 * 那是原版标准的"是/否"对话框（见 {@code WndOptions}），
	 * 行为、外观、按键响应都与游戏其它地方一致。
	 *
	 * <p>选"是" → 把本条与它的**全部前置**（含前置的前置，递归）一起勾上；
	 * 选"否" → 什么都不做（不给勾选，因为前置不满足时它是置灰的）。
	 */
	private void askFillPrerequisites( final ChallengeDef d ) {
		if (d == null || !isAlive()) return;

		//收集缺失的前置（递归展开），用来在提示里列出名字
		final List<Integer> missing = new ArrayList<>();
		collectMissingPrereqs(d, missing, new ArrayList<Integer>());

		StringBuilder names = new StringBuilder();
		for (int id : missing) {
			ChallengeDef o = ChallengeRegistry.byId(id);
			if (names.length() > 0) names.append("、");
			names.append(o != null ? o.name : ("#" + id));
		}

		com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon.scene()
				.addToFront(new com.shatteredpixel.shatteredpixeldungeon.windows
						.WndOptions(
						Messages.get(this, "prereq_title"),
						//==== END(修复·前置弹窗显示 %2Ss) ====
						//文档所有者反馈："需要前置规则 「%1$s」需要先启用以下规则：%2Ss
						//是否把它们一并勾选？"
						//
						//根因：文案里有 **两个** 占位符（%1$s 规则名、%2$s 前置列表），
						//而这里只传了**一个**参数（前置列表）。
						//String.format 遇到缺少的参数会抛异常，
						//被 Messages.format 的 catch 吞掉后**返回原始模板** ——
						//于是玩家看到的就是带 %1$s / %2$s 的原文。
						//
						//修法：按顺序把两个都传进去。
						Messages.get(this, "prereq_body", d.name, names.toString()),
						Messages.get(this, "prereq_yes"),
						Messages.get(this, "prereq_no")) {
					@Override
					protected void onSelect(int index) {
						if (index != 0) return;               //"否" → 什么都不做
						if (!isAlive()) return;               //窗口可能已被关掉

						for (int id : missing) {
							mask = mask.with(id);
						}
						rebuildAll();
					}
				});
	}

	/**
	 * END(新增): 递归收集"缺失的前置"。
	 *
	 * <p>为什么要递归：前置链可能是多层的
	 * （例如 191 需要 188+189，而 188 又可能需要别的）。
	 * 只查一层的话，玩家点"是"之后仍会有未满足的前置。
	 *
	 * @param out    收集结果（避免重复）
	 * @param visited 已访问的 id（防止关系表里出现环时死循环）
	 */
	private void collectMissingPrereqs( ChallengeDef d, List<Integer> out,
			List<Integer> visited ) {
		if (d == null) return;
		if (visited.contains(d.id)) return;      //防环
		visited.add(d.id);

		for (ChallengeRelation r : d.relationsOf(ChallengeRelation.Type.PREREQUISITE)) {
			for (int id : r.targets) {
				if (mask.has(id)) continue;      //已经有了，跳过
				if (out.contains(id)) continue;  //已收集
				out.add(id);
				ChallengeDef o = ChallengeRegistry.byId(id);
				if (o != null) collectMissingPrereqs(o, out, visited);
			}
		}
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
		//==== END(改版·通过等级挪到分类区第 4 行) ====
		//文档所有者要求："删除挑战界面的通过等级的文字，
		//或者放在分类区第 4 行的位置。"
		//
		//分类栏是 3 排（4+4+3），所以"第 4 行"就是分类栏正下方那一条。
		//放在这里的好处：
		//  · 不占额外垂直空间（那一行本来就空着）
		//  · 与"当前选了哪些分类"贴在一起，读起来连贯
		//  · 底部的确定/归零键不再和它挤在同一行
		float catBottom = top + catHeight();
		top = catBottom + 1;

		if (passLevelText != null) {
			//紧贴分类栏底部（第 4 行的位置）
			passLevelText.setPos(4, catBottom + 1);
		}

		if (editable && randomBar != null) {
			randomBar.setRect(0, top, WIDTH, RANDOM_BAR_H);
			top += RANDOM_BAR_H;
		}

		//列表
		//
		//==== END(分页): 高度**固定**，不再随内容变化 ====
		//文档所有者报告："界面会随着内容变大或变小"。
		//
		//根因：原实现用 {@code min(content.height(), ...)} ——
		//分类从 28 条切到 5 条时，列表高度跟着缩，整个窗口重排一次。
		//
		//分页后每页最多 PER_PAGE 条，高度可以写死：
		//  PER_PAGE × (BTN_HEIGHT + GAP)
		//翻页只换内容，窗口尺寸纹丝不动。
		float listH = PER_PAGE * (BTN_HEIGHT + GAP);

		//屏幕装不下时按屏幕收缩（小屏设备保底 5 条）
		//END(确定键): 底部高度改成"确定键那一行"的高度。
		float bottomH = CONFIRM_H + 2;
		float pageH = (pageBarVisible() ? 18 : 0);
		float maxByScreen = com.watabou.noosa.Camera.main.height
				- top - pageH - bottomH - 6;
		if (maxByScreen < listH) {
			listH = Math.max(5 * (BTN_HEIGHT + GAP), maxByScreen);
		}

		pane.setRect(0, top, WIDTH, listH);
		top += listH;

		//翻页栏（列表下方）
		if (pageBarVisible()) {
			if (pageBar != null) pageBar.setRect(0, top, WIDTH, 18);
			top += 18;
		}

		//==== END(新增·确定键 + 归零键): 底部两个按钮 ====
		//文档所有者反馈：
		//  · "挑战界面没有确定键，导致难以返回"
		//  · "加一个归零"
		//
		//布局：[通过等级...]              [归零] [确定]
		//两个按钮并排放在右侧，不额外占垂直空间。
		float btnW = 40f;
		float gap = 3f;
		if (confirmBtn != null) {
			confirmBtn.setRect(WIDTH - btnW, top + 1, btnW, CONFIRM_H);
		}
		if (resetBtn != null) {
			resetBtn.setRect(WIDTH - btnW * 2 - gap, top + 1, btnW, CONFIRM_H);
		}

		resize(WIDTH, (int) (top + bottomH));

		//END(改版·通过等级): 位置已在分类栏下方设好（见上面那处），
		//这里不再重复设置 —— 否则会把它又拽回底部的按钮那一行。

		//END(分页): 页码文字与按钮可用状态（页数变化后要刷新）
		updatePageLabel();

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
	/**
	 * END(修复·前置弹窗没出现): 这条规则是否"可以点开前置弹窗"。
	 *
	 * <p>判据：它**只差前置**（其它条件都满足），且当前还没被勾选。
	 *
	 * <p>为什么要单独一个方法：{@code canToggle()} 是"能不能直接勾选"，
	 * 它把"缺前置"也判成 false；而玩家点这一类条目时我们要**弹窗询问**，
	 * 所以需要一条独立的、更宽松的判据。
	 */
	private boolean canAskPrereq( ChallengeDef d ) {
		if (d == null) return false;
		if (mask.has( d.id )) return false;          //已勾选 → 不是"缺前置"
		if (!d.isImplemented()) return false;        //未实装 → 点了也没用
		if (!d.hasPrerequisite()) return false;      //压根没有前置
		if (d.prerequisitesMet( mask )) return false;//前置已满足 → 正常勾选路径
		if (!d.conflictingWith( mask ).isEmpty()) return false;  //互斥 → 补不了
		return true;
	}

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

	/**
	 * END(文案走 properties): 取一条挑战的效果说明。
	 *
	 * <h3>两处来源，优先用 properties</h3>
	 * <ol>
	 *   <li>{@code challenges.<key>_desc} —— properties 里的版本</li>
	 *   <li>{@code d.effect} —— 注册表里写死的 Java 字符串</li>
	 * </ol>
	 *
	 * <p>文档所有者要求文案改在 properties 里（**不用重新编译**），
	 * 所以这里先查 key；查不到才退回注册表。
	 *
	 * <p>这样新写的文案放 properties 就生效，而还没改写的条目
	 * 仍然显示注册表里那份 —— 可以**逐条迁移**，不会出现空白。
	 */
	private String effectTextOf( ChallengeDef d ) {
		String key = "challenges." + d.key + "_desc";
		if ( Messages.exists( null, key ) ) {
			return Messages.get( key );
		}
		return d.effect;
	}

	private String describe( ChallengeDef d ) {
		StringBuilder sb = new StringBuilder();

		sb.append( d.name );
		if (!d.isImplemented()) {
			sb.append( Messages.get( this, "not_implemented" ) );
		}
		sb.append( "\n\n" );
		//==== END(修订·详情里不显示挑战等级) ====
		//文档所有者要求："不需要在选挑战界面加挑战等级。"
		//
		//所以这里只显示分类与倾向，不再显示 Lv。
		//（等级仍然存在 ChallengeDef 里，用于计算"通过等级"，
		//  只是不再暴露给玩家 —— 那是个内部配平用的数值。）
		sb.append( d.group ).append( "   " ).append( d.tendencyName() );
		if (!d.effect.isEmpty()) {
			sb.append( "\n\n" ).append( effectTextOf( d ) );
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
