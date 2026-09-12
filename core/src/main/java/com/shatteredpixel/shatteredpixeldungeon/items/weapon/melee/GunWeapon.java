//END(port from Arknights): GunWeapon
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Camouflage;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChenShooterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CloserangeShot;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Momentum;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.SnipersMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Bonk;
import com.shatteredpixel.shatteredpixeldungeon.items.Gunaccessories.Accessories;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.C1_9mm;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Thunderbolt;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.UpMagazine;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class GunWeapon
extends MeleeWeapon {
    public static final String AC_ZAP = "ZAP";
    public static final String AC_RELOAD = "RELOAD";
    public static final String AC_REMOVE = "REMOVE";
    protected static final int RELOAD_AMOUNT = 31;
    protected int bulletTier = 3;
    protected int bulletMax = 25;
    protected int bullet = 0;
    protected int specialBullet = 0;
    protected boolean specialFire = false;
    protected boolean gamza = false;
    protected float FIRE_DELAY_MULT = 1.0f;
    protected int MIN_RANGE = 1;
    protected int MAX_RANGE = 4;
    protected float RELOAD_DELAY = 2.0f;
    public Accessories gunAccessories;
    protected static CellSelector.Listener zapper = new CellSelector.Listener(){

        @Override
        public void onSelect(Integer target) {
            if (target != null && curItem instanceof GunWeapon) {
                final GunWeapon ss = (GunWeapon)curItem;
                final Ballistica shot = new Ballistica(curUser.pos, target, 7);
                int cell = shot.collisionPos;
                if (target == curUser.pos || cell == curUser.pos) {
                    GLog.i(Messages.get(GunWeapon.class, "self_target"));
                    return;
                }
                curUser.sprite.zap(cell);
                if (Actor.findChar(target) != null) {
                    QuickSlotButton.target(Actor.findChar(target));
                } else {
                    QuickSlotButton.target(Actor.findChar(cell));
                }
                if (ss.tryToZap(curUser, target)) {
                    curUser.busy();
                    ss.fx(shot, new Callback(){

                        @Override
                        public void call() {
                            ss.onZap(shot);
                        }
                    });
                }
            }
        }

        @Override
        public String prompt() {
            return Messages.get(GunWeapon.class, "prompt");
        }
    };
    private final WndBag.Listener itemSelector = new WndBag.Listener(){

        @Override
        public void onSelect(Item item) {
            if (item != null) {
                //END(修复·关键): 必须判类型再强转。
                //之前只要 item 不是 UpMagazine 就走 else 分支强转 MissileWeapon，
                //玩家若点到非弹药物品就会 ClassCastException 崩溃。
                if (!(item instanceof MissileWeapon)) {
                    GLog.w(Messages.get(GunWeapon.class, "need_ammo"));
                    return;
                }
                if (item instanceof Thunderbolt) {
                    GunWeapon.this.bulletMax += 3;
                    GunWeapon.this.gamza = true;
                }
                if (item instanceof UpMagazine) {
                    GunWeapon.this.reload(((MissileWeapon)item).tier, true);
                } else {
                    GunWeapon.this.reload(((MissileWeapon)item).tier, false);
                }
                item.detach(Dungeon.hero.belongings.backpack);
            }
        }
    };
    private static final String BULLET = "bullet";
    private static final String BULLET_CAP = "bulletCap";
    private static final String GAMZA = "gamza";
    private static final String TIER = "bullettier";
    private static final String SP = "spshot";
    private static final String SP_BULLET_COUNT = "spBulletCount";
    private static final String ACCESSORIES = "GunAccessories";

    @Override
    public int max(int lvl) {
        //END(修复): 图鉴(WndJournal)会在 Dungeon.hero == null 时调用本方法，
        //原代码直接访问 hero.buff()/hero.belongings 会 NPE 崩溃。
        if (Dungeon.hero == null) {
            return 0;
        }

        return 3 * this.tier + lvl * (this.tier - 2);
    }

    public int fireMin() {
        return this.tier - 1 + this.bulletTier + this.level() + RingOfSharpshooting.levelDamageBonus(Dungeon.hero);
    }

    public int fireMax() {
        return 4 + this.tier * 2 + this.bulletTier * 3 + this.level() * this.tier + RingOfSharpshooting.levelDamageBonus(Dungeon.hero) * 2;
    }

    public int getDistance(int from, int to) {
        return Dungeon.level.distance(from, to);
    }

    public int getMaxRange() {
        boolean projecting = this.hasEnchant(Projecting.class, Dungeon.hero);
        int range = projecting ? this.MAX_RANGE + 1 : this.MAX_RANGE;
        return range;
    }

    public int getMinRange() {
        return this.MIN_RANGE;
    }

    public boolean isWithinRange(int distance) {
        return this.getMinRange() <= distance && distance <= this.getMaxRange();
    }

    public float getFireAcc(int from, int to) {
        int distance = this.getDistance(from, to);
        return this.getMaxRange() >= distance && distance >= this.getMinRange() ? 1.0f : 0.5f;
    }

    public int fireDamageRoll() {
        return Random.Int(this.fireMin(), this.fireMax());
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (attacker instanceof Hero && Dungeon.hero.subClass == HeroSubClass.GLADIATOR && Random.Int(4) < 1) {
            this.bullet = Math.min(this.bullet + 1, this.bulletMax);
            GunWeapon.updateQuickslot();
        }
        return super.proc(attacker, defender, damage);
    }

    protected void specialFire(Char ch) {
    }

    protected float fireAccuracyFactor(float acc) {
        CloserangeShot closerrange;
        if (this.gunAccessories != null) {
            acc *= this.gunAccessories.GetACCcorrectionvalue();
            if (Dungeon.hero.hasTalent(Talent.SHARPSHOOTER)) {
                acc += (float)Dungeon.hero.pointsInTalent(Talent.SHARPSHOOTER) * 0.2f;
            }
        }
        if (Dungeon.hero.hasTalent(Talent.BLITZKRIEG)) {
            acc += (float)Dungeon.hero.pointsInTalent(Talent.BLITZKRIEG) * 0.1f;
        }
        if ((closerrange = Dungeon.hero.buff(CloserangeShot.class)) != null && Dungeon.hero.hasTalent(Talent.PINPOINT)) {
            acc += (float)Dungeon.hero.pointsInTalent(Talent.PINPOINT) * 0.2f;
        }
        return acc;
    }

    protected float fireDelayFactor(Char owner, float dly) {
        if (this.gunAccessories != null) {
            dly *= this.gunAccessories.GetDLYcorrectionvalue();
        }
        return dly;
    }

    protected int fireDamageFactor(int dmg) {
        CloserangeShot closerRange;
        float accessoriesBonus = 1.0f;
        if (this.gunAccessories != null) {
            accessoriesBonus = this.gunAccessories.GetDMGcorrectionvalue();
        }
        float talentBonus = 1.0f;
        if (Dungeon.hero.hasTalent(Talent.PROJECTILE_MOMENTUM) && Dungeon.hero.buff(Momentum.class) != null && Dungeon.hero.buff(Momentum.class).freerunning()) {
            talentBonus += (float)Dungeon.hero.pointsInTalent(Talent.PROJECTILE_MOMENTUM) * 0.1f;
        }
        if (Dungeon.hero.hasTalent(Talent.BLITZKRIEG)) {
            talentBonus += (float)Dungeon.hero.pointsInTalent(Talent.BLITZKRIEG) * 0.1f;
        }
        if ((closerRange = Dungeon.hero.buff(CloserangeShot.class)) != null && closerRange.state()) {
            talentBonus += 0.5f;
            if (Dungeon.hero.hasTalent(Talent.ZERO_RANGE_SHOT)) {
                talentBonus += (float)Dungeon.hero.pointsInTalent(Talent.ZERO_RANGE_SHOT) * 0.1f;
            }
        }
        dmg = (int)((float)dmg * (accessoriesBonus * talentBonus));
        return dmg;
    }

    public boolean affixAccessories(Accessories accessories) {
        if (this.gunAccessories != null) {
            return false;
        }
        this.gunAccessories = accessories;
        return true;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ZAP);
        actions.add(AC_RELOAD);
        if (this.gunAccessories != null) {
            actions.add(AC_REMOVE);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero) {
        if (this.bullet <= 0) {
            this.execute(hero, AC_RELOAD);
        } else {
            this.execute(hero, this.defaultAction);
        }
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_ZAP)) {
            this.usesTargeting = false;
            if (Dungeon.hero.belongings.weapon != this) {
                GLog.n(Messages.get(this, "not_equipped"));
                QuickSlotButton.cancel();
            } else if (this.cursed) {
                Buff.affect(Dungeon.hero, Burning.class).reignite(Dungeon.hero, 4.0f);
                this.cursedKnown = true;
                this.bullet = Math.max(0, this.bullet - 1);
            } else {
                this.usesTargeting = true;
                curUser = hero;
                curItem = this;
                this.cursedKnown = true;
                GameScene.selectCell(zapper);
            }
        }
        if (action.equals(AC_RELOAD)) {
            curUser = hero;
            GameScene.selectItem(this.itemSelector, WndBag.Mode.MISSILEWEAPON, Messages.get(this, "reload_prompt"));
            QuickSlotButton.cancel();
        }
        if (action.equals(AC_REMOVE)) {
            curUser = hero;
            Accessories ac = this.gunAccessories;
            if (ac.doPickUp(Dungeon.hero)) {
                GLog.i(Messages.get(Dungeon.hero, "you_now_have", ac.name()));
            } else {
                Dungeon.level.drop((Item)ac, (int)GunWeapon.curUser.pos).sprite.drop();
            }
            this.gunAccessories = null;
            curUser.spendAndNext(1.0f);
        }
    }

    public void reload(int tier, boolean sp) {
        this.bulletTier = tier;
        this.bullet = Math.min(this.bullet + 31, this.bulletMax);
        int n = this.specialBullet = sp ? Math.min(this.specialBullet + 31, this.bulletMax) : this.specialBullet;
        if (Dungeon.hero.subClass == HeroSubClass.FREERUNNER) {
            Dungeon.hero.spendAndNext(this.RELOAD_DELAY / 2.0f);
        } else {
            Dungeon.hero.spendAndNext(this.RELOAD_DELAY);
        }
        Dungeon.hero.sprite.operate(Dungeon.hero.pos);
    }

    protected void fx(Ballistica bolt, Callback callback) {
        MagicMissile.boltFromChar(GunWeapon.curUser.sprite.parent, 102, GunWeapon.curUser.sprite, bolt.collisionPos, callback);
        Sample.INSTANCE.play(this.hitSound);
    }

    public boolean tryToZap(Hero owner, int target) {
        if (owner.STR() < this.STRReq()) {
            GLog.w(Messages.get(this, "weak"));
            return false;
        }
        if (this.bullet >= 1) {
            this.specialFire = this.specialBullet > 0 || Random.Int(12 - this.bulletTier) == 0;
            return true;
        }
        GLog.w(Messages.get(this, "fizzles"));
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void onZap(Ballistica bolt) {
        CloserangeShot closerRange = Dungeon.hero.buff(CloserangeShot.class);
        float oldacc = this.ACC;
        boolean anyKill = false;
        try {
            Char ch = Actor.findChar(bolt.collisionPos);
            if (ch != null) {
                Buff.affect(Dungeon.hero, RangedAttackTracker.class);
                this.processGunHit(ch, 1.0f, true);
                if (!ch.isAlive()) {
                    anyKill = true;
                }
            } else {
                /*END(移植调整): 本 fork 无 pressCellGunfire，改为普通地图刷新*/
                com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.updateMap(bolt.collisionPos);
            }
            this.postShotCleanup(closerRange, false, anyKill);
        }
        finally {
            this.ACC = oldacc;
        }
    }

    protected boolean processGunHit(Char ch, float dmgMult, boolean triggerTalentProcs) {
        return this.processGunHit(ch, dmgMult, triggerTalentProcs, -1);
    }

    protected boolean processGunHit(Char ch, float dmgMult, boolean triggerTalentProcs, int preRolledDr) {
        float dmg = (float)this.fireDamageFactor(this.fireDamageRoll()) * dmgMult;
        int trueDmg = 0;
        if (ch.buff(Blindness.class) != null && Dungeon.hero.hasTalent(Talent.FLASH_SPEAR)) {
            trueDmg += (int)(dmg * ((float)Dungeon.hero.pointsInTalent(Talent.FLASH_SPEAR) * 0.075f));
        }
        this.ACC = this.fireAccuracyFactor(this.getFireAcc(Dungeon.hero.pos, ch.pos));
        if (this.ACC <= 0.0f) {
            String missed = Messages.get(ch, "missed");
            ch.sprite.showStatus(0xFFFF00, missed);
            Sample.INSTANCE.play("sounds/miss.mp3");
            return false;
        }
        if (Char.hit(Dungeon.hero, ch, false)) {
            if (Dungeon.hero.hasTalent(Talent.TARGET_FOCUSING) && Random.Int(3) < Dungeon.hero.pointsInTalent(Talent.TARGET_FOCUSING)) {
                Buff.detach(ch, Camouflage.class);
            }
            int dr = preRolledDr >= 0 ? preRolledDr : ch.drRoll();
            int effectiveDamage = ch.defenseProc(Dungeon.hero, (int)dmg);
            if (Dungeon.hero.subClass == HeroSubClass.SNIPER) {
                dr /= 2;
            }
            effectiveDamage = Math.max(effectiveDamage - dr, 0);
            if (ch.buff(Vulnerable.class) != null) {
                effectiveDamage = (int)((float)effectiveDamage * 1.33f);
            }
            if (triggerTalentProcs) {
                effectiveDamage = Dungeon.hero.attackProc(ch, effectiveDamage);
            }
            if (!ch.isAlive()) {
                return true;
            }
            ch.damage(effectiveDamage, Dungeon.hero);
            if (ch.isAlive() && trueDmg > 0) {
                ch.damage(trueDmg, this);
            }
            Sample.INSTANCE.play("sounds/hit.mp3", 1.0f, Random.Float(0.87f, 1.15f));
            if (triggerTalentProcs && this.specialFire) {
                this.specialFire(ch);
            }
            ch.sprite.burst(-1, this.buffedLvl() / 2 + 2);
            if (triggerTalentProcs) {
                CloserangeShot closerRange;
                int bonusTurns;
                if (this instanceof C1_9mm && Random.Int(8) == 0) {
                    Buff.affect(ch, Chill.class, 2.0f);
                }
                int n = bonusTurns = Dungeon.hero.hasTalent(Talent.SHARED_UPGRADES) ? this.buffedLvl() : 0;
                if (Dungeon.hero.subClass == HeroSubClass.SNIPER) {
                    Buff.prolong(Dungeon.hero, SnipersMark.class, 4.0f).set(ch.id(), bonusTurns);
                }
                if (Dungeon.hero.subClass == HeroSubClass.GLADIATOR) {
                    Buff.affect(Dungeon.hero, Combo.class).hit(ch);
                    if (Dungeon.hero.hasTalent(Talent.CLEAVE) && Random.Int(10) < Dungeon.hero.pointsInTalent(Talent.CLEAVE)) {
                        Buff.affect(Dungeon.hero, Combo.class).hit(ch);
                    }
                }
                if (Dungeon.hero.hasTalent(Talent.SPARKOFLIFE) && 1 + Dungeon.hero.pointsInTalent(Talent.SPARKOFLIFE) > Random.Int(33)) {
                    Dungeon.hero.HP = Math.min(Dungeon.hero.HP + Dungeon.hero.HT / 20, Dungeon.hero.HT);
                }
                if (false /*END(移植调整): 本 fork 无 SPSHOOTER 子职业*/ && ch.isAlive() && Dungeon.hero.buff(ChenShooterBuff.TACMoveCooldown.class) == null) {
                    Buff.prolong(Dungeon.hero, ChenShooterBuff.class, 5.0f).set(ch.id());
                }
                if ((closerRange = Dungeon.hero.buff(CloserangeShot.class)) != null && ch.isAlive() && closerRange.state()) {
                    if (Dungeon.hero.hasTalent(Talent.WATER_PLAY) && Random.Int(5) < Dungeon.hero.pointsInTalent(Talent.WATER_PLAY)) {
                        Buff.affect(ch, Blindness.class, 1.0f);
                    }
                    if (Dungeon.hero.hasTalent(Talent.TAC_SHOT) && Dungeon.hero.buff(ChenShooterBuff.TACMove_tacshot.class) != null) {
                        int min = Dungeon.hero.pointsInTalent(Talent.TAC_SHOT) / 2;
                        int max = 1 + Dungeon.hero.pointsInTalent(Talent.TAC_SHOT) / 3;
                        Ballistica trajectory = new Ballistica(GunWeapon.curUser.pos, ch.pos, 1);
                        trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), 7);
                        WandOfBlastWave.throwChar(ch, trajectory, Random.IntRange(min, max), true, true, null);
                        Buff.detach(Dungeon.hero, ChenShooterBuff.TACMove_tacshot.class);
                    }
                }
            }
            return true;
        }
        String defense = ch.defenseVerb();
        ch.sprite.showStatus(0xFFFF00, defense);
        Sample.INSTANCE.play("sounds/miss.mp3");
        return false;
    }

    protected void postShotCleanup(CloserangeShot closerRange, boolean pala, boolean anyTargetKilled) {
        Buff buff = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
        if (buff != null) {
            buff.detach();
        }
        if ((buff = Dungeon.hero.buff(Swiftthistle.TimeBubble.class)) != null) {
            buff.detach();
        }
        if ((buff = Dungeon.hero.buff(RangedAttackTracker.class)) != null) {
            buff.detach();
        }
        if (Dungeon.hero.buff(Bonk.BonkBuff.class) != null) {
            Buff.detach(Dungeon.hero, Bonk.BonkBuff.class);
        }
        Invisibility.dispel();
        boolean savedBullet = false;
        if (this.gunAccessories != null && Random.Int(100) < this.gunAccessories.GetSavingChance()) {
            savedBullet = true;
        }
        if (closerRange != null && closerRange.state() && Dungeon.hero.hasTalent(Talent.FRUGALITY) && Random.Int(100) < Dungeon.hero.pointsInTalent(Talent.FRUGALITY) * 15) {
            savedBullet = true;
        }
        if (Random.Float() < 1f) {
            savedBullet = true;
        }
        if (!savedBullet) {
            this.bullet = Math.max(0, this.bullet - 1);
            this.specialBullet = Math.max(0, this.specialBullet - 1);
        }
        GunWeapon.updateQuickslot();
        if (pala) {
            curUser.spendAndNext(this.fireDelayFactor(curUser, this.FIRE_DELAY_MULT / 4.0f));
        } else {
            curUser.spendAndNext(this.fireDelayFactor(curUser, this.FIRE_DELAY_MULT));
        }
        if (anyTargetKilled && Dungeon.hero.hasTalent(Talent.BF_RULL) && Random.Int(5) < Dungeon.hero.pointsInTalent(Talent.BF_RULL)) {
            /*END(移植调整): 本 fork 的 TimeBubble 没有公开的时长延长 API（原方舟有 bufftime()），
                //此处不再额外延长时间，仅保留已施加效果。*/
        }
    }

    @Override
    public String status() {
        return "" + this.bullet;
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", this.bulletTier);
    }

    @Override
    public String info() {
        Object info = super.info();
        if (this.gunAccessories != null) {
            info = (String)info + "\n\n" + Messages.get(this.gunAccessories, "desc");
        }
        return String.valueOf(info);
    }

    @Override
    public String name() {
        if (this.gamza) {
            return Messages.get(this, "gamza_name");
        }
        return super.name();
    }

    @Override
    public String statsInfo() {
        if (this.specialBullet > 0) {
            return Messages.get(this, "stats_desc_sp", this.fireMin(), this.fireMax(), this.specialBullet, this.getMinRange(), this.getMaxRange());
        }
        return Messages.get(this, "stats_desc", this.fireMin(), this.fireMax(), this.getMinRange(), this.getMaxRange());
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(BULLET, this.bullet);
        bundle.put(BULLET_CAP, this.bulletMax);
        bundle.put(GAMZA, this.gamza);
        bundle.put(TIER, this.bulletTier);
        bundle.put(SP_BULLET_COUNT, this.specialBullet);
        bundle.put(ACCESSORIES, this.gunAccessories);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.bulletMax = bundle.getInt(BULLET_CAP);
        this.bullet = this.bulletMax > 0 ? Math.min(this.bulletMax, bundle.getInt(BULLET)) : bundle.getInt(BULLET);
        this.bulletTier = bundle.getInt(TIER);
        this.specialBullet = bundle.getInt(SP_BULLET_COUNT);
        this.gamza = bundle.getBoolean(GAMZA);
        this.gunAccessories = (Accessories)bundle.get(ACCESSORIES);
    }

    public static class RangedAttackTracker
    extends Buff {
    }
}
