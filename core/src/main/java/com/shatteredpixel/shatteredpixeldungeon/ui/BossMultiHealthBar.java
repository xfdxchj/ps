//END(port from Arknights): BossMultiHealthBar
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BloodParticle;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Component;
import java.util.HashSet;
import java.util.Set;

public class BossMultiHealthBar
extends Component {
    private Image bar;
    private Image rawShielding;
    private Image shieldedHP;
    private Image hp;
    private static final Set<Mob> bosses = new HashSet<Mob>();
    private Image skull;
    private Emitter blood;
    private static String asset = "interfaces/boss_hp.png";
    private static BossMultiHealthBar instance;
    private static boolean bleeding;

    BossMultiHealthBar() {
        this.active = !bosses.isEmpty();
        this.visible = this.active;
        instance = this;
    }

    @Override
    protected void createChildren() {
        this.bar = new Image(asset, 0, 0, 64, 16);
        this.add(this.bar);
        this.width = this.bar.width;
        this.height = this.bar.height;
        this.rawShielding = new Image(asset, 15, 25, 47, 4);
        this.rawShielding.alpha(0.5f);
        this.add(this.rawShielding);
        this.shieldedHP = new Image(asset, 15, 25, 47, 4);
        this.add(this.shieldedHP);
        this.hp = new Image(asset, 15, 19, 47, 4);
        this.add(this.hp);
        this.skull = new Image(asset, 5, 18, 6, 6);
        this.add(this.skull);
        this.blood = new Emitter();
        this.blood.pos(this.skull);
        this.blood.pour(BloodParticle.FACTORY, 0.3f);
        this.blood.autoKill = false;
        this.blood.on = false;
        this.add(this.blood);
    }

    @Override
    protected void layout() {
        this.bar.x = this.x;
        this.bar.y = this.y;
        this.shieldedHP.x = this.rawShielding.x = this.bar.x + 15.0f;
        this.hp.x = this.rawShielding.x;
        this.shieldedHP.y = this.rawShielding.y = this.bar.y + 6.0f;
        this.hp.y = this.rawShielding.y;
        this.skull.x = this.bar.x + 5.0f;
        this.skull.y = this.bar.y + 5.0f;
    }

    @Override
    public void update() {
        super.update();
        if (!bosses.isEmpty()) {
            if (!this.atLeastOneBossAlive()) {
                bosses.clear();
                this.active = false;
                this.visible = false;
            } else {
                float health = 0.0f;
                float shield = 0.0f;
                float max = 0.0f;
                for (Mob boss : bosses) {
                    health += (float)boss.HP;
                    shield += (float)boss.shielding();
                    max += (float)boss.HT;
                }
                this.hp.scale.x = Math.max(0.0f, (health - shield) / max);
                this.shieldedHP.scale.x = health / max;
                this.rawShielding.scale.x = shield / max;
                float bleedThreshold = 1.0f / (float)bosses.size();
                if (this.hp.scale.x < bleedThreshold) {
                    BossMultiHealthBar.bleed(true);
                }
                if (bleeding != this.blood.on) {
                    if (bleeding) {
                        this.skull.tint(0xCC0000, 0.6f);
                    } else {
                        this.skull.resetColor();
                    }
                    this.blood.on = bleeding;
                }
            }
        }
    }

    private boolean atLeastOneBossAlive() {
        for (Mob boss : bosses) {
            if (!boss.isAlive() || !Dungeon.level.mobs.contains(boss)) continue;
            return true;
        }
        return false;
    }

    public static void assignBoss(Mob boss) {
        bosses.add(boss);
        BossMultiHealthBar.bleed(false);
        if (instance != null) {
            BossMultiHealthBar.instance.active = true;
            BossMultiHealthBar.instance.visible = true;
        }
    }

    public static void bleed(boolean value) {
        bleeding = value;
    }
}
