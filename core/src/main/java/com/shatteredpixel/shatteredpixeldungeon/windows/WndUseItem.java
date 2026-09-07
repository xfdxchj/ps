/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.endcontent.evolved.EndModeWand;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.ui.InventoryPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemJournalButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

import java.util.ArrayList;

public class WndUseItem extends WndInfoItem {

	private static final float BUTTON_HEIGHT	= 16;
	
	private static final float GAP	= 2;

	public Window owner;
	public Item item;

	public WndUseItem( final Window owner, final Item item ) {
		
		super(item);

		this.owner = owner;
		this.item = item;

		float y = height;
		
		if (Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(item)) {
			y += GAP;
			ArrayList<RedButton> buttons = new ArrayList<>();
			for (final String action : item.actions(Dungeon.hero)) {

				RedButton btn = new RedButton(item.actionName(action, Dungeon.hero), 8) {
					@Override
					protected void onClick() {
						hide();
						if (owner != null && owner.parent != null) owner.hide();
						if (Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(item)) {
							item.execute(Dungeon.hero, action);
						}
						Item.updateQuickslot();
						if (action.equals(item.defaultAction()) && item.usesTargeting && owner == null) {
							InventoryPane.useTargeting();
						}
					}
				};
				btn.setSize(btn.reqWidth(), BUTTON_HEIGHT);
				buttons.add(btn);
				add(btn);

				if (action.equals(item.defaultAction())) {
					btn.textColor(TITLE_COLOR);
				}

			}
			y = layoutButtons(buttons, width, y);

			//终焉扩展:进化法杖在“使用它”的详情窗口里提供发射形态手动选择
			if (item instanceof EndModeWand) {
				final EndModeWand emw = (EndModeWand) item;
				y += GAP;
				ArrayList<RedButton> modeBtns = new ArrayList<>();
				for (int i = 0; i < emw.modeCount(); i++) {
					final int idx = i;
					RedButton mb = new RedButton(emw.modeName(idx), 8) {
						@Override
						protected void onClick() {
							//切换形态并即时高亮当前选中,不必关窗
							emw.setModeIndex( idx );
							Item.updateQuickslot();
							//重开本窗口刷新选中态
							hide();
							if (Game.scene() != null && Dungeon.hero.isAlive()
									&& Dungeon.hero.belongings.contains( item )){
								Game.scene().addToFront( new WndUseItem( owner, item ) );
							}
						}
					};
					mb.setSize( mb.reqWidth(), BUTTON_HEIGHT );
					if (i == emw.modeIndex()){
						mb.textColor( TITLE_COLOR ); //高亮当前形态
					}
					add( mb ); //必须挂到窗口,否则 layoutButtons 只排版不出现在窗口里
					modeBtns.add( mb );
				}
				y = layoutButtons( modeBtns, width, y );
			}

			ItemJournalButton btn = new ItemJournalButton(item, this);
			btn.setRect(width - 16, 0, 16, 16);
			add(btn);
		}

		resize( width, (int)(y) );
	}

	private static float layoutButtons(ArrayList<RedButton> buttons, float width, float y){
		ArrayList<RedButton> curRow = new ArrayList<>();
		float widthLeftThisRow = width;
		
		while( !buttons.isEmpty() ){
			RedButton btn = buttons.get(0);
			
			widthLeftThisRow -= btn.width();
			if (curRow.isEmpty()) {
				curRow.add(btn);
				buttons.remove(btn);
			} else {
				widthLeftThisRow -= 1;
				if (widthLeftThisRow >= 0) {
					curRow.add(btn);
					buttons.remove(btn);
				}
			}
			
			//layout current row. Currently forces a max of 3 buttons but can work with more
			if (buttons.isEmpty() || widthLeftThisRow <= 0 || curRow.size() >= 3){
				
				//re-use this variable for laying out the buttons
				widthLeftThisRow = width - (curRow.size()-1);
				for (RedButton b : curRow){
					widthLeftThisRow -= b.width();
				}
				
				//while we still have space in this row, find the shortest button(s) and extend them
				while (widthLeftThisRow > 0){
					
					ArrayList<RedButton> shortest = new ArrayList<>();
					RedButton secondShortest = null;
					
					for (RedButton b : curRow) {
						if (shortest.isEmpty()) {
							shortest.add(b);
						} else {
							if (b.width() < shortest.get(0).width()) {
								secondShortest = shortest.get(0);
								shortest.clear();
								shortest.add(b);
							} else if (b.width() == shortest.get(0).width()) {
								shortest.add(b);
							} else if (secondShortest == null || secondShortest.width() > b.width()){
								secondShortest = b;
							}
						}
					}
					
					float widthToGrow;
					
					if (secondShortest == null){
						widthToGrow = widthLeftThisRow / shortest.size();
						widthLeftThisRow = 0;
					} else {
						widthToGrow = secondShortest.width() - shortest.get(0).width();
						if ((widthToGrow * shortest.size()) >= widthLeftThisRow){
							widthToGrow = widthLeftThisRow / shortest.size();
							widthLeftThisRow = 0;
						} else {
							widthLeftThisRow -= widthToGrow * shortest.size();
						}
					}
					
					for (RedButton toGrow : shortest){
						toGrow.setRect(0, 0, toGrow.width()+widthToGrow, toGrow.height());
					}
				}
				
				//finally set positions
				float x = 0;
				for (RedButton b : curRow){
					b.setRect(x, y, b.width(), b.height());
					x += b.width() + 1;
				}
				
				//move to next line and reset variables
				y += BUTTON_HEIGHT+1;
				widthLeftThisRow = width;
				curRow.clear();
				
			}
			
		}
		
		return y - 1;
	}

}
