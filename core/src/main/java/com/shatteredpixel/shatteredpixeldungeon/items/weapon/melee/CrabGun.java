//END(port from Arknights): CrabGun
package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hallucination;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Silence;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CustomeSet;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.Rock_CrabSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import java.util.ArrayList;

public class CrabGun
extends MeleeWeapon {
    public CrabGun() {
        this.image = ItemSpriteSheet.BEENS;
        this.hitSound = "sounds/atk_spiritbow.mp3";
        this.hitSoundPitch = 1.0f;
        this.tier = 4;
        this.RCH = 2;
    }

    @Override
    public int max(int lvl) {
        return 3 * (this.tier + 1) + lvl * (this.tier - 1);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (attacker instanceof Hero || attacker instanceof DriedRose.GhostHero) {
            if (this.charge >= this.chargeCap) {
                ArrayList<Integer> respawnPoints = new ArrayList<Integer>();
                for (int i = 0; i < PathFinder.NEIGHBOURS8.length; ++i) {
                    int p = defender.pos + PathFinder.NEIGHBOURS8[i];
                    if (Actor.findChar(p) != null || !Dungeon.level.passable[p]) continue;
                    respawnPoints.add(p);
                }
                for (int spawnd = 0; respawnPoints.size() > 0 && spawnd == 0; ++spawnd) {
                    int index = Random.index(respawnPoints);
                    MetalCrab crab = new MetalCrab();
                    crab.setting(this.buffedLvl());
                    GameScene.add(crab);
                    ScrollOfTeleportation.appear(crab, (Integer)respawnPoints.get(index));
                    if (this.setbouns()) {
                        for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                            if (mob.alignment == Char.Alignment.ALLY || !Dungeon.level.heroFOV[mob.pos]) continue;
                            Buff.affect(mob, Hallucination.class).set(5.0f);
                        }
                    }
                    respawnPoints.remove(index);
                }
                this.charge = 0;
            } else {
                /*END(移植调整): SPCharge 未移植，改为直接回充*/
        this.charge = Math.min(this.charge + 5, this.chargeCap);
            }
        }
        CrabGun.updateQuickslot();
        return super.proc(attacker, defender, damage);
    }

    public void SpawnCrab(int lvl, int pos) {
        MetalCrab crab = new MetalCrab();
        crab.setting(lvl);
        GameScene.add(crab);
        ScrollOfTeleportation.appear(crab, pos);
    }

    @Override
    public String desc() {
        Object info = Messages.get(this, "desc");
        if (this.setbouns()) {
            info = (String)info + "\n\n" + Messages.get(CrabGun.class, "setbouns");
        }
        return String.valueOf(info);
    }

    private boolean setbouns() {
        //END(修复): 图鉴(WndJournal)会调用 Item.info() → desc()，
        //那时 Dungeon.hero 为 null → 原来直接访问 belongings 会 NPE 崩溃。
        if (Dungeon.hero == null || Dungeon.hero.belongings == null) {
            return false;
        }
        return Dungeon.hero.belongings.getItem(RingOfWealth.class) != null && Dungeon.hero.belongings.getItem(CustomeSet.class) != null && Dungeon.hero.belongings.getItem(RingOfWealth.class).isEquipped(Dungeon.hero) && Dungeon.hero.belongings.getItem(CustomeSet.class).isEquipped(Dungeon.hero);
    }

    @Override
    public String status() {
        if (!this.isIdentified() || this.cursed) {
            return null;
        }
        if (this.chargeCap == 100) {
            return Messages.format("%d%%", this.charge);
        }
        return null;
    }

    public static class MetalCrab
    extends NPC {
        public MetalCrab() {
            this.spriteClass = Rock_CrabSprite.class;
            this.baseSpeed = 3.0f;
            this.immunities.add(Silence.class);
            this.alignment = Char.Alignment.ALLY;
            this.state = this.WANDERING;
        }

        @Override
        public int damageRoll() {
            return Random.NormalIntRange(2 + Dungeon.depth / 2, 6 + Dungeon.depth / 2 + this.maxLvl * 2);
        }

        @Override
        public int attackSkill(Char target) {
            return 10 + Dungeon.depth / 2 + this.maxLvl;
        }

        @Override
        public int drRoll() {
            return Random.NormalIntRange(0, 3 + this.maxLvl / 2);
        }

        public void setting(int setlvl) {
            CustomeSet.CustomSetBuff setBuff = Dungeon.hero.buff(CustomeSet.CustomSetBuff.class);
            int itembuff = 0;
            if (setBuff != null) {
                itembuff = setBuff.itemLevel();
            }
            this.HP = this.HT = 30 + setlvl * 6 + itembuff * 5;
            this.defenseSkill = 1 + setlvl + itembuff;
            this.maxLvl = setlvl + itembuff / 2;
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            this.enemySeen = true;
        }
    }
}
