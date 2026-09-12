//END(port from Arknights): GunScope_II
package com.shatteredpixel.shatteredpixeldungeon.items.Gunaccessories;

import com.shatteredpixel.shatteredpixeldungeon.items.Gunaccessories.Accessories;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class GunScope_II
extends Accessories {
    public GunScope_II() {
        this.image = ItemSpriteSheet.TELESCOPE;
        this.ACCcorrectionvalue = 1.1f;
        this.DLYcorrectionvalue = 2.0f;
        this.DMGcorrectionvalue = 1.7f;
        this.CONEcorrectionvalue = 0.85f;
        this.SavingChancevalue = 0;
    }
}
