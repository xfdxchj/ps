/*
 * 破碎的地牢 (End fork) — 挑战 100「镜像对决」的敌对镜像
 *
 * 本文件为框架新增，不属于原版 Shattered Pixel Dungeon。
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MirrorSprite;
import com.watabou.utils.Bundle;

/**
 * END(挑战 100 镜像对决): 敌对镜像。
 *
 * <h3>原表效果</h3>
 * "每层 13% 概率生成镜像，同玩家装备外观/攻击/生命，主动攻击，
 * 掉落随机复制品，不用道具和法杖"
 *
 * <h3>与 {@code MirrorImage} 的区别</h3>
 * 原版的 {@code MirrorImage} 是**盟友**（镜像卷轴召的），
 * 本类是**敌人** —— 其余表现（外观、跟随玩家护甲等级）完全照抄。
 *
 * <h3>"不用道具和法杖"怎么保证</h3>
 * 本类继承 {@code Mob} 而不是 {@code Hero}，所以**天生没有背包** ——
 * 它只能普通攻击，不可能掏道具或法杖。这是结构上的保证，不是靠判断。
 */
public class HostileMirror extends Mob {

	{
		spriteClass = MirrorSprite.class;

		//外观跟随玩家的护甲等级（与 MirrorImage 同一套精灵）
		alignment = Alignment.ENEMY;
		state = HUNTING;

		//镜像优先行动，压迫感更强
		actPriority = MOB_PRIO + 1;

		//它是"玩家的镜像"，不是不死生物
		properties.add(Property.DEMONIC);   //让 158 神圣之力也能对它生效
	}

	/** 记录玩家的 id —— 跨存档要用 id 而不是引用。 */
	private int heroID = -1;
	private Hero hero;

	/** 护甲外观等级（跟随玩家变化）。 */
	public int armTier = 0;

	/**
	 * END(100): 按玩家当前状态初始化。
	 *
	 * <p>生命与攻击都取自玩家 —— 这是"同玩家装备外观/攻击/生命"的落地。
	 * 但**不**直接复制玩家的 HP（那在高层会是几百点，太硬），
	 * 而是取一个按层数缩放的合理值：玩家 HT 的一半，并封顶。
	 */
	public static HostileMirror createFor(Hero hero) {
		HostileMirror m = new HostileMirror();
		if (hero == null) return m;

		m.hero = hero;
		m.heroID = hero.id();
		m.armTier = hero.tier();

		//生命：取玩家上限的一半，但至少 10、至多 120
		int hp = Math.max(10, Math.min(120, hero.HT / 2));
		m.HP = m.HT = hp;

		return m;
	}

	private static final String HEROID = "hero_id";
	private static final String ARMTIER = "arm_tier";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(HEROID, heroID);
		bundle.put(ARMTIER, armTier);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		heroID = bundle.getInt(HEROID);
		armTier = bundle.getInt(ARMTIER);
	}

	@Override
	protected boolean act() {
		//解析玩家引用（跨存档时是 id 查回来的）
		if (hero == null) {
			//注意：Actor.findById 返回的是 Actor，要自己判类型再转。
			//直接赋给 Char 会编译不过（Actor 不是 Char 的子类）。
			com.shatteredpixel.shatteredpixeldungeon.actors.Actor found =
					com.shatteredpixel.shatteredpixeldungeon.actors.Actor
							.findById(heroID);
			if (found instanceof Hero) {
				hero = (Hero) found;
			} else if (Dungeon.hero != null) {
				hero = Dungeon.hero;
				heroID = hero.id();
			} else {
				//找不到玩家 → 自行消失，避免留下一个不动的怪
				die(null);
				return true;
			}
		}

		//外观跟随玩家的护甲等级
		if (hero.tier() != armTier) {
			armTier = hero.tier();
			if (sprite instanceof MirrorSprite) {
				((MirrorSprite) sprite).updateArmor(armTier);
			}
		}

		return super.act();
	}

	/** END(100): 攻击力跟随玩家等级。 */
	@Override
	public int attackSkill(Char target) {
		if (hero != null) {
			//与玩家同级，但略低一点 —— 否则高层镜像会一刀秒人
			return Math.max(1, hero.attackSkill(target) - 2);
		}
		return super.attackSkill(target);
	}

	/** END(100): 伤害跟随玩家武器。 */
	@Override
	public int damageRoll() {
		if (hero != null) {
			com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon w =
					hero.belongings.attackingWeapon();
			if (w != null) {
				//打七折：完全照搬玩家伤害会过于致命
				return Math.max(1, Math.round(w.damageRoll(hero) * 0.7f));
			}
		}
		return super.damageRoll();
	}

	/** END(100): 防御力也跟随玩家（但不高）。 */
	@Override
	public int defenseSkill(Char enemy) {
		if (hero != null) {
			return Math.max(1, hero.defenseSkill(enemy) / 2);
		}
		return super.defenseSkill(enemy);
	}

	/** END(100): 掉落"一件同等级的普通装备"。 */
	@Override
	public void die(Object cause) {
		super.die(cause);

		try {
			com.shatteredpixel.shatteredpixeldungeon.items.Item drop =
					com.shatteredpixel.shatteredpixeldungeon.endcontent.challenge
							.ChallengeEffects.rollMirrorDrop();
			if (drop != null && Dungeon.level != null) {
				Dungeon.level.drop(drop, pos).sprite.drop();
			}
		} catch (Throwable t) {
			//掉落失败不影响击杀流程
		}
	}
}
