//END(port from Arknights): Certificate
package com.shatteredpixel.shatteredpixeldungeon.items.NewGameItem;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Certificate
extends Item {
    private static final String TXT_VALUE = "%+d";
    private static final String VALUE = "value";

    public Certificate() {
        this.image = ItemSpriteSheet.INFO_CERTI;
        this.quantity = 1;
    }

    public Certificate(int value) {
        this.image = ItemSpriteSheet.INFO_CERTI;
        this.quantity = value;
        if (Challenges.activeChallenges() > 7) {
            this.quantity += 50;
        } else if (Challenges.activeChallenges() > 5) {
            this.quantity += 40;
        } else if (Challenges.activeChallenges() > 2) {
            this.quantity += 20;
        } else if (Challenges.activeChallenges() > 0) {
            this.quantity += 10;
        }
        if (Dungeon.eazymode == 1 || Dungeon.isChallenged(4096) || !Dungeon.customSeedText.isEmpty()) {
            this.quantity = 0;
        }
    }

    @Override
    public boolean doPickUp(Hero hero, int pos) {
        SPDSettings.addSpecialcoin(this.quantity);
        GameScene.pickUp(this, hero.pos);
        hero.sprite.showStatus(0xFFFF00, TXT_VALUE, this.quantity);
        hero.spendAndNext(1.0f);
        Sample.INSTANCE.play("sounds/evoke.mp3", 1.0f, 1.0f, Random.Float(1.35f, 1.45f));
        Badges.validateCertificate();
        return true;
    }

    public static void specialEndingBouns() {
        if (Dungeon.eazymode != 1 && !Dungeon.isChallenged(4096) && Dungeon.customSeedText.isEmpty()) {
            int bouns = 0;
            if (Challenges.activeChallenges() > 7) {
                bouns += 50;
            } else if (Challenges.activeChallenges() > 5) {
                bouns += 15;
            } else if (Challenges.activeChallenges() > 2) {
                bouns += 10;
            } else if (Challenges.activeChallenges() > 0) {
                bouns += 5;
            }
            SPDSettings.addSpecialcoin(10 + bouns);
        }
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(VALUE, this.quantity);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        this.quantity = bundle.getInt(VALUE);
    }
}
