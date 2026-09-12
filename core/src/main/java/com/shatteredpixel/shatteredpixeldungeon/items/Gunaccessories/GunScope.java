//END(port from Arknights): GunScope
package com.shatteredpixel.shatteredpixeldungeon.items.Gunaccessories;

import com.shatteredpixel.shatteredpixeldungeon.items.Gunaccessories.Accessories;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class GunScope
extends Accessories {
    public GunScope() {
        this.image = ItemSpriteSheet.TELESCOPE;
        this.ACCcorrectionvalue = 1.0f;
        this.DLYcorrectionvalue = 1.5f;
        this.DMGcorrectionvalue = 1.4f;
        this.CONEcorrectionvalue = 0.9f;
        this.SavingChancevalue = 0;
    }
}
