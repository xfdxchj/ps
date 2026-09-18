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
	private static final int CAT_H      = 14;
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

	/** 分类按钮行（可横向滚动）。 */
	private ScrollPane catPane;
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
		catContent = new Component();
		catPane = new ScrollPane( catContent );
		add( catPane );
		buildCategoryBar();

		//列表区
		content = new Component();
		pane = new ScrollPane( content );
		add( pane );

		//随机条（仅开局可选时）
		float top = TTL_HEIGHT;
		if (editable) {
			buildRandomBar();
			randomBar.setRect( 0, TTL_HEIGHT, WIDTH, RANDOM_BAR_H );
			top = TTL_HEIGHT + RANDOM_BAR_H;
		}

		//布局
		catPane.setRect( 0, top, WIDTH, CAT_H );
		top += CAT_H + 1;

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
		} catch (Throwable t) {
			System.out.println("  诊断失败: " + t);
		}
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

		float x = 0;
		for (final String g : groups) {

			int count = 0;
			for (ChallengeDef d : ChallengeRegistry.inGroup(g)) if (d.isImplemented()) count++;

			String label = g + " " + count;
			RedButton btn = new RedButton( label, 6 ) {
				@Override
				protected void onClick() {
					super.onClick();
					switchGroup( g );
				}
			};
			//宽度按文字自适应，最小 30
			int w = Math.max(30, (int)btn.reqWidth() + 4);
			btn.setRect( x, 0, w, CAT_H );

			//当前分类高亮
			btn.textColor( g.equals(currentGroup) ? 0xFFFF88 : 0xCCCCCC );

			catContent.add( btn );
			catButtons.add( btn );
			x += w + 1;
		}

		catContent.setSize( Math.max(WIDTH, x), CAT_H );
		catContent.setPos( 0, 0 );
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
		buildList();

		//列表高度可能变化，重算窗口
		float top = TTL_HEIGHT + (editable ? RANDOM_BAR_H : 0) + CAT_H + 1;
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
					boolean on = !checked();
					checked( on );
					mask = on ? mask.with( d.id ) : mask.without( d.id );
					//互斥/前置会随选择变化，重建整表以刷新置灰
					rebuildAll();
				}
			};
			cb.setRect( 0, pos, WIDTH - 16, BTN_HEIGHT );
			cb.checked( mask.has( d.id ) );
			cb.active = editable && canToggle( d );

			content.add( cb );
			boxes.add( cb );

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

	/** 选择变化后重建（分类高亮 + 列表 + 通过等级）。 */
	private void rebuildAll() {
		buildCategoryBar();
		content.clear();
		boxes.clear();
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
