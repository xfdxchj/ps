/*
 * 破碎的地牢 (End fork) — 挑战选择界面（注册表驱动）
 *
 * 本文件基于原版 Shattered Pixel Dungeon 的 WndChallenges 重写，
 * 改为读取 ChallengeRegistry，支持全部 118 条规则、分组、互斥置灰与前置解锁。
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeDef;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeMask;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRegistry;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge.ChallengeRandomizer;
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
 * END(挑战框架): 挑战规则选择窗口。
 *
 * <h3>与原版 WndChallenges 的区别</h3>
 * <ul>
 *   <li>数据来自 {@link ChallengeRegistry}（118 条），而不是硬编码的 12 条数组。</li>
 *   <li>**分组展示**（地图/战斗/怪物/经济/…），每组一个小标题。</li>
 *   <li>**未实装项置灰**并标注「未实装」——避免玩家勾了一堆没有效果的开关。</li>
 *   <li>**互斥项自动置灰**（勾了 32 通货膨胀，37 高价回收就点不动）。</li>
 *   <li>**前置解锁**（132 黑暗之魂需 125~131 全选）。</li>
 *   <li>底部实时显示**通过等级**（∑ 已启用规则的等级）。</li>
 *   <li>内容可滚动 —— 118 条装不下一屏。</li>
 * </ul>
 *
 * <p>构造函数保持与原版一致的签名，这样现有调用点无需改动。
 */
public class WndChallenges extends Window {

	private static final int WIDTH		= 120;
	private static final int TTL_HEIGHT = 16;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 1;
	/** 滚动区最大高度（超出则滚动）。 */
	private static final int MAX_LIST_H = 168;

	private boolean editable;

	/** 当前选择状态（唯一权威）。 */
	private ChallengeMask mask;

	private ArrayList<CheckBox> boxes = new ArrayList<>();
	/** 与 boxes 一一对应的规则定义。 */
	private ArrayList<ChallengeDef> defs = new ArrayList<>();

	private ScrollPane pane;
	private Component content;

	private RenderedTextBlock passLevelText;

	//==== END(随机挑战) ====
	/** 随机功能条高度。 */
	private static final int RANDOM_BAR_H = 18;
	private Component randomBar;
	/** 目标通过等级（玩家可调）。 */
	private int targetLevel = 6;
	/** 是否把未实装规则也纳入随机候选。 */
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

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos( (WIDTH - title.width()) / 2, (TTL_HEIGHT - title.height()) / 2 );
		PixelScene.align(title);
		add( title );

		//底部固定：通过等级 + 已选条数
		passLevelText = PixelScene.renderTextBlock( "", 7 );
		passLevelText.hardlight( 0xCCCCCC );
		add( passLevelText );

		content = new Component();
		pane = new ScrollPane( content );
		add( pane );

		//==== END(随机挑战): 目标分选择 + 随机按钮（仅开局可选时显示）====
		float top = TTL_HEIGHT;
		if (editable) {
			buildRandomBar();
			top = TTL_HEIGHT + RANDOM_BAR_H;
		}

		buildList();

		//布局：标题 → (随机器) → 滚动区 → 通过等级
		float listH = Math.min( content.height(), MAX_LIST_H );
		float bottomH = 14;
		if (editable) {
			randomBar.setRect( 0, TTL_HEIGHT, WIDTH, RANDOM_BAR_H );
		}
		pane.setRect( 0, top, WIDTH, listH );
		resize( WIDTH, (int)(top + listH + bottomH) );

		passLevelText.setPos( 4, top + listH + 2 );
		updatePassLevel();
	}

	/**
	 * END(随机挑战): 构建顶部功能条 —— 目标分调整 + 「随机」按钮。
	 *
	 * <p>交互：−/+ 调整目标通过等级，点「随机」就抽一组刚好凑到该分的规则。
	 * 目标分上限由 {@link ChallengeRandomizer#maxTarget} 动态决定
	 * （只算已实装时全部加起来才 22 分，填更高永远凑不满）。
	 */
	private void buildRandomBar() {

		randomBar = new Component();
		add( randomBar );

		final int maxT = ChallengeRandomizer.maxTarget( includePending );
		if (targetLevel > maxT) targetLevel = maxT;
		if (targetLevel < ChallengeRandomizer.MIN_TARGET) {
			targetLevel = ChallengeRandomizer.MIN_TARGET;
		}

		//---- − 按钮 ----
		RedButton minus = new RedButton( "−" ) {
			@Override
			protected void onClick() {
				super.onClick();
				if (targetLevel > ChallengeRandomizer.MIN_TARGET) targetLevel--;
				updateTargetText();
			}
		};
		minus.setRect( 0, 0, 14, 16 );
		randomBar.add( minus );

		//---- 目标分显示 ----
		targetText = PixelScene.renderTextBlock( "", 7 );
		targetText.hardlight( 0xFFFF88 );
		randomBar.add( targetText );

		//---- + 按钮 ----
		RedButton plus = new RedButton( "+" ) {
			@Override
			protected void onClick() {
				super.onClick();
				if (targetLevel < ChallengeRandomizer.maxTarget( includePending )) targetLevel++;
				updateTargetText();
			}
		};
		plus.setRect( 16, 0, 14, 16 );
		randomBar.add( plus );

		//---- 随机按钮 ----
		RedButton roll = new RedButton( Messages.get( this, "roll" ) ) {
			@Override
			protected void onClick() {
				super.onClick();
				mask = ChallengeRandomizer.roll( targetLevel, includePending );
				//抽完之后整表重建，勾选状态与置灰一起刷新
				rebuild();
			}
		};
		roll.setRect( 32, 0, WIDTH - 32, 16 );
		randomBar.add( roll );

		updateTargetText();
	}

	/** 刷新目标分显示。 */
	private void updateTargetText() {
		if (targetText == null) return;
		targetText.text( Messages.get( this, "target", targetLevel ) );
		targetText.setPos( 17 + (12 - targetText.width())/2, (16 - targetText.height())/2 );
		PixelScene.align( targetText );
	}

	/** 按分组构建整个列表。 */
	private void buildList() {

		float pos = 0;

		for (String group : ChallengeRegistry.groups()) {

			List<ChallengeDef> inGroup = ChallengeRegistry.inGroup( group );

			//---- 分组标题 ----
			RenderedTextBlock header = PixelScene.renderTextBlock( group, 8 );
			header.hardlight( 0xFFFF88 );
			header.setPos( 2, pos );
			PixelScene.align( header );
			content.add( header );
			pos += header.height() + GAP;

			//---- 该组内的规则 ----
			for (ChallengeDef def : inGroup) {

				final ChallengeDef d = def;

				String label = d.name;
				if (!d.isImplemented()) {
					label = label + Messages.get( this, "not_implemented" );
				}
				//END(限制提示): 与已启用规则存在"功能失效"关系时标个警告符号。
				//注意这里**不置灰** —— 限制允许共存，只是其中一条不生效。
				if (!d.restrictedBy( mask ).isEmpty()) {
					label = label + Messages.get( this, "restricted_mark" );
				}

				CheckBox cb = new CheckBox( Messages.titleCase( label ) ) {
					@Override
					protected void onClick() {
						boolean on = !checked();
						checked( on );
						mask = on ? mask.with( d.id ) : mask.without( d.id );
						//互斥 / 前置会随选择变化，重建整张列表
						rebuild();
					}
				};
				cb.setRect( 0, pos, WIDTH - 16, BTN_HEIGHT );
				cb.checked( mask.has( d.id ) );

				//只有「可编辑 + 已实装 + 没有互斥冲突 + 前置满足」才能勾
				cb.active = editable && canToggle( d );

				content.add( cb );
				boxes.add( cb );
				defs.add( d );

				//---- 问号按钮：查看详情 ----
				IconButton info = new IconButton( Icons.get( Icons.INFO ) ) {
					@Override
					protected void onClick() {
						super.onClick();
						ShatteredPixelDungeon.scene().add(
								new WndMessage( describe( d ) ) );
					}
				};
				info.setRect( cb.right(), pos, 16, BTN_HEIGHT );
				content.add( info );

				pos = cb.bottom() + GAP;
			}

			pos += 2;   // 组间留白
		}

		//ScrollPane 需要知道内容总高
		content.setSize( WIDTH, pos );
		content.setPos( 0, 0 );
	}

	/** 重建列表（选择变化后刷新置灰状态）。 */
	private void rebuild() {
		content.clear();
		boxes.clear();
		defs.clear();
		buildList();
		updatePassLevel();
	}

	/**
	 * 该条规则当前是否允许勾选。
	 *
	 * <p>三种情况不允许：
	 * <ol>
	 *   <li>未实装 —— 勾了也没有效果</li>
	 *   <li>与已启用规则互斥</li>
	 *   <li>前置条件未满足（如 132 需 125~131 全选）</li>
	 * </ol>
	 * <p>注意：**已勾选的项永远允许取消**，否则玩家会被卡死
	 * （例如先勾了 132 再想取消 125，如果 125 因前置关系被禁用就退不出来了）。
	 */
	private boolean canToggle( ChallengeDef d ) {
		if (mask.has( d.id )) return true;                // 已选的永远能取消
		if (!d.isImplemented()) return false;             // 未实装
		if (!d.prerequisitesMet( mask )) return false;    // 前置不满足
		if (!d.conflictingWith( mask ).isEmpty()) return false; // 互斥冲突
		return true;
	}

	/** 刷新底部「通过等级」。 */
	private void updatePassLevel() {
		int level = mask.passLevel();
		int count = mask.activeCount();
		passLevelText.text( Messages.get( this, "pass_level", level, count ) );
	}

	/** 组装详情文本：效果 + 关系 + 状态。 */
	private String describe( ChallengeDef d ) {
		StringBuilder sb = new StringBuilder();

		sb.append( d.name );
		if (!d.isImplemented()) {
			sb.append( Messages.get( this, "not_implemented" ) );
		}
		sb.append( "\n\n" );
		sb.append( d.tendencyName() ).append( "   Lv" ).append( d.level );
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

		//END(限制提示): 实时告知"此刻这条是否真的失效了"。
		//声明层面说"与谁冲突"是一回事，当前选择下是否真的失效是另一回事。
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
