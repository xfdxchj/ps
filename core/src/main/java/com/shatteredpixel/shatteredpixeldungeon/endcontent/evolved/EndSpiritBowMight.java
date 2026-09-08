package com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon.Enchantment;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

/**
 * END 灵能弓· 成品 ①「附魔灵弓」（双模式）。
 *
 * 需求（最终要求）：
 *  ① 锻造时可在本弓详情里从【正面向附魔全池随机抽 5 个】选 1 个写成“本体附魔”(enchantment)。
 *  ② 双模式(背包-本弓窗口里一排按钮手动来回切、可存档)：
 *      - 模式 A「稳固」：触发【本体附魔】，并给这把弓一个“+50% 奥术戒”等价加成
 *        (在 Weapon.Enchantment.genericProcChanceMultiplier 对当局只为本模式叠加 +0.5)，
 *        本体大多数时候会必然触发、>100% 溢出再自然变强。
 *      - 模式 B「随机」：放弃本体、改为每击必触发一个全池【随机附魔】(含稀有)。
 */
public class EndSpiritBowMight extends SpiritBow implements EndModeWand {

	public static final String AC_SELECT = "END_MIGHT_SELECT";   // 在本弓窗口里弹 5选1(本体附魔)

	private static final String MODE_KEY      = "might_mode";
	private static final String INIT_CHOSEN   = "might_body_chosen"; //是否已完成过本体选择(按钮文案提示用)

	private int mode = 0;                 //0=稳固本体(A),1=随机(B)
	private boolean bodyChosen = false;   //锻造后是否已选定过本体(未定每次点击选本体都再弹)

	/** 处理“本弓正在命中附加/本体”期间置位，让 Weapon 里据此把触发抬满，保证每击必附魔。 */
	private static volatile boolean forcingDuringHit = false;
	/** 供 Weapon 询问当前是否正有本弓的命中正在进行。 */
	public static boolean forcingNow(){ return forcingDuringHit; }

	/** 稳固本体当前命中要追加的“加法触发率”(字面 +0.3、非乘)，命中期间置 0.3，平时 0。 */
	private static volatile float additiveDuringHit = 0f;
	public static void setHitAdditive( float v ){ additiveDuringHit = v; }
	/** 各正面向附魔判定时加进 chance(封顶在 1 由判定处自行处理)。 */
	public static float activeAdd(){ return additiveDuringHit; }

	/* ---------------- 元信息 / EndModeWand ---------------- */
	@Override public String name() { return "附魔灵弓"; }

	@Override
	public String desc() {
		return "进化·附魔灵弓：“随机附魔工匠”。(可选)先在锻造/背包中从全池正向附魔里 5 选 1 定出【本体附魔】(仅次一次)，随后在背包-弓窗口可切两种用法：\n\n"
				+ "▍稳固本体：每击按本体附魔本身正常强度触发（增强应轻微，不加爆）；\n"
				+ "▍随机附魔：每击打出一个全池(8 种中的 1)【随机】附魔并弹名，含稀有在内。\n\n"
				+ "伤害比原版灵能弓高 20%，随角色等级成长；无法用升级卷轴强化。";
	}

	/** 进阶弓整体伤害比原版灵能弓高 20%（仍随角色等级成长、不可被升级卷轴）。 */
	@Override
	public int damageRoll( Char owner ){
		return Math.round( super.damageRoll( owner ) * 1.2f );
	}

	@Override public int modeCount()          { return 2; }
	@Override public int modeIndex()          { return mode; }
	@Override
	public void setModeIndex( int index ){
		if (index < 0 || index >= modeCount()) index = 0;
		mode = index;
	}
	@Override
	public String modeName( int index ){
		switch (index){
			case 0:  return "稳固本体";
			case 1:  return "随机附魔";
			default: return "";
		}
	}

	/* ---------------- 动作 (供 WndUseItem 生成“选本体”按钮) ---------------- */
	@Override
	public ArrayList<String> actions( Hero hero ){
		ArrayList<String> actions = super.actions( hero );
		//仅未定过本体的第一回提供“选本体(5选1)”，选定后按钮消失，避免反复刷本体附魔
		if (!bodyChosen){
			actions.add( AC_SELECT );
		}
		actions.remove( AC_EQUIP );      //神弓本来就不走 AC_EQUIP
		return actions;
	}

	@Override
	public String actionName( String action, Hero hero ){
		if (action.equals( AC_SELECT )) return "选本体(5选项 1)";
		return super.actionName( action, hero );
	}

	@Override
	public void execute( Hero hero, String action ){
		if (action.equals( AC_SELECT )){
			chooseBodyEnchantment();
			return;
		}
		super.execute( hero, action );
	}

	/** 弹出 5 个候选(全池正向随机、可含稀有)，由玩家挑 1 写成本弓本体附魔。 */
	private void chooseBodyEnchantment(){
		final Enchantment[] sample = samplePositiveEnchantments( 5 );
		if (sample.length == 0){
			bodyChosen = true;     //极端空池就保持未定义，交由模式B随机
			return;
		}
		ArrayList<String> opts = new ArrayList<>();
		for (Enchantment e : sample){
			opts.add( cnEnchantName( e ) );   //汉化：显示中文附魔名而非英文类名
		}
		GameScene.show( new WndOptions(
				Messages.titleCase( name() ),
				"请为本弓挑一个本体附魔(候选含 13 种正面，含恒动 Kinetic 等)：\n(模式<稳固本体>会让本体触发的当次判机 +0.3(加法、不乘)；切到<随机附魔>则每击掷 13 中 1 个)。",
				opts.toArray( new String[0] ) ) {
			@Override
			protected void onSelect( int index ){
				if (index >= 0 && index < sample.length){
					enchantment = sample[index];            //写为本体附魔（随本弓 bundle 自动持久化）
					bodyChosen = true;
					Item.updateQuickslot();
				}
			}
		});
	}

	/** 附魔英文类名 → 原版官方汉化前缀名(对齐 items_zh enchantments.*.name，不自造词)。 */
	private static String cnEnchantName( Enchantment e ){
		if (e == null) return "？";
		String sn = e.getClass().getSimpleName();
		switch (sn){
			case "Blazing":      return "烈焰";  //items.weapon.enchantments.blazing.name=烈焰%s
			case "Shocking":     return "电击";
			case "Chilling":     return "寒霜";
			case "Kinetic":      return "恒动";
			case "Blocking":     return "招架";
			case "Blooming":     return "繁茂";
			case "Elastic":      return "弹性";
			case "Lucky":        return "幸运";
			case "Projecting":   return "索敌";
			case "Unstable":     return "紊乱";
			case "Corrupting":   return "腐化";
			case "Grim":         return "死神";
			case "Vampiric":     return "血饮";
			default:             return sn; //未收录则退回类名
		}
	}

	/** 在目标头上弹出“本次触发哪个附魔”的小字，像伤害字带提示。 */
	private void popEnchantTrigger( Char defender, Enchantment roll ){
		if (defender == null || defender.sprite == null) return;
		try {
			defender.sprite.showStatus(
					com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.WARNING,
					cnEnchantName( roll ) );
		} catch (Exception ignore){ /* 显示失败忽略 */ }
	}

	/** 本弓可选/随机掷的候选(共 13 种，含恒动 Kinetic、索敌 Projecting、紊乱 Unstable 等；
	 *  不再额外做数值加成——只有带真实触发判定的正面才在稳固命中吃到 activeAdd(=0.3)，非乘)。 */
	private static final String[] CURATED_ENCHANTS = {
			"Blazing","Chilling","Shocking",
			"Blocking","Blooming","Corrupting","Elastic","Lucky",
			"Vampiric","Grim","Unstable","Kinetic","Projecting"
	};

	private Enchantment curatedEnchantRoll(){
		try {
			String id = CURATED_ENCHANTS[ com.watabou.utils.Random.Int( CURATED_ENCHANTS.length ) ];
			Class<?> c = Class.forName(
					"com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments." + id );
			return (Enchantment) com.watabou.utils.Reflection.newInstance( c );
		} catch (Throwable t){
			return null;
		}
	}

	/** 从 8 种“可感、支援加法触发”的附魔中抽样(类不重复)，最多 n 个——保证选中本体必然被加法口覆盖。 */
	private Enchantment[] samplePositiveEnchantments( int n ){
		ArrayList<Enchantment> got = new ArrayList<>();
		int guard = 0;
		while (got.size() < n && guard < 60 && got.size() < CURATED_ENCHANTS.length){
			guard++;
			Enchantment e = null;
			try {
				String id = CURATED_ENCHANTS[ com.watabou.utils.Random.Int( CURATED_ENCHANTS.length ) ];
				Class<?> c = Class.forName(
						"com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments." + id );
				e = (Enchantment) com.watabou.utils.Reflection.newInstance( c );
			} catch (Exception ignore){ continue; }
			if (e == null) continue;
			boolean dup = false;
			for (Enchantment have : got){
				if (have.getClass().equals(e.getClass())){ dup = true; break; }
			}
			if (!dup) got.add( e );
		}
		return got.toArray( new Enchantment[0] );
	}

	/* ---------------- 命中行为（双模式分流） ---------------- */
	@Override
	public int proc( Char attacker, Char defender, int damage ){

		if (defender != null){
			System.out.println("[MIGHT] mode="+mode+" body="+(enchantment!=null?enchantment.getClass().getSimpleName():"none")+" dmg="+damage); //临调试
		}

		if ( mode == 1 ){
			//模式 B(随机)：每击都掷一个 8 种可感随机附魔并(force)保证该发成功，弹出名字。
			Enchantment carried = enchantment;
			if (carried != null) enchantment = null;
			try {
				damage = super.proc( attacker, defender, damage );
			} finally {
				if (carried != null) enchantment = carried;
			}

			if (defender != null && defender.isAlive()){
				Enchantment roll = null;
				try { roll = curatedEnchantRoll(); } catch (Exception ignore){}
				if (roll != null){
					forceHit();
					try { damage = roll.proc( this, attacker, defender, damage ); }
					finally { forceEnd(); }
					popEnchantTrigger( defender, roll );
				}
			}
		} else {
			//模式 A(稳固)：仅给本体触发概率+0.5(加法、不乘、不加幅)后走本体正常一次。
			setHitAdditive( 0.3f );   //稳固：加法触发 +0.3(百分点)
			try { damage = super.proc( attacker, defender, damage ); }
			finally { setHitAdditive( 0f ); }
		}
		return damage;
	}

	private static void forceHit(){ forcingDuringHit = true; }
	private static void forceEnd(){ forcingDuringHit = false; }

	/* ---------------- 持久化 mode / bodyChosen ---------------- */
	@Override
	public void storeInBundle( Bundle bundle ){
		super.storeInBundle( bundle );
		bundle.put( MODE_KEY, mode );
		bundle.put( INIT_CHOSEN, bodyChosen );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ){
		super.restoreFromBundle( bundle );
		if (bundle.contains( MODE_KEY ))   mode = bundle.getInt( MODE_KEY );
		if (bundle.contains( INIT_CHOSEN )) bodyChosen = bundle.getBoolean( INIT_CHOSEN );
	}
}
