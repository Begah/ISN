package com.client.core;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;

public class Dialog extends Window {
	private float WindowWidth;
	private Skin skin;
	
	private ArrayList<Row> rows = new ArrayList<>();
	private Row currentRow;
	
	float nexwWindowWidth;
	
	private class Row {
		private ArrayList<Label> labels = new ArrayList<>();
		private ArrayList<Button> buttons = new ArrayList<>();
		
		public void add(Label label) {
			labels.add(label);
			Dialog.this.add(label);
		}
		
		public void add(Button button) {
			buttons.add(button);
			Dialog.this.add(button);
		}
		
		public void calculate() {
			if(labels.size() == 0 && buttons.size() == 0) return;
			float totalWidth = 0;
			for(int i = 0; i < labels.size(); i++) {
				Label label = labels.get(i);
				if(label.isVisible() == false)
					continue;
				totalWidth += Dialog.this.getPadLeft();
				totalWidth += label.getPrefWidth();
				totalWidth += Dialog.this.getPadRight();
			}
			for(int i = 0; i < buttons.size(); i++) {
				Button button = buttons.get(i);
				if(button.isVisible() == false)
					continue;
				totalWidth += Dialog.this.getPadLeft();
				totalWidth += button.getPrefWidth();
				totalWidth += Dialog.this.getPadRight();
			}
			
			if(totalWidth > WindowWidth) {
				//Duh
				nexwWindowWidth = totalWidth;
			}
			float x = WindowWidth / 2 - totalWidth / 2;
			
			for(int i = 0; i < labels.size(); i++) {
				Label label = labels.get(i);
				if(label.isVisible() == false) {
					label.setX(x);
					continue;
				}
				x += Dialog.this.getPadLeft();
				label.setX(x);
				x += label.getPrefWidth() + Dialog.this.getPadRight();
			}
			for(int i = 0; i < buttons.size(); i++) {
				Button button = buttons.get(i);
				if(button.isVisible() == false) {
					button.setX(x);
					continue;
				}
				x += Dialog.this.getPadLeft();
				button.setX(x);
				x += button.getPrefWidth() + Dialog.this.getPadRight();
			}
		}
	}
	
	public Dialog(String title, Skin skin) {
		super(title, skin);
		this.skin = skin;
		
		currentRow = new Row();
		rows.add(currentRow);
	}
	
	@Override
	public Cell<?> row() {
		super.row();
		currentRow = new Row();
		rows.add(currentRow);
		
		return null;
	}
	
	public void show(Stage stage, float x, float y) {
		setPosition(x, y);
		for(int i = 0; i < rows.size(); i++) {
			rows.get(i).calculate();
		}
		setSize(getPrefWidth(), getPrefHeight());
		WindowWidth = this.getWidth();
		stage.addActor(this);
	}
	
	public void show(Stage stage) {
		show(stage, Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
	}
	
	public TextButton addButton(String text) {
		TextButton button = new TextButton(text, skin);
		addButton(button);
		return button;
	}
	
	public void addButton(TextButton button) {
		currentRow.add(button);
	}
	
	public void addButton(TextButton... buttons) {
		for(int i = 0; i < buttons.length; i++)
			currentRow.add(buttons[i]);
	}
	
	public Label addText(String txt) {
		Label label = new Label(txt, skin) {
			
			@Override
			public void invalidateHierarchy () {
				invalidate();
			}
			
			@Override
			public void layout () {
				BitmapFont font = getBitmapFontCache().getFont();
				BitmapFontCache cache = getBitmapFontCache();
				
				float oldScaleX = font.getScaleX();
				float oldScaleY = font.getScaleY();
				if (getFontScaleX() != 1 || getFontScaleY() != 1) font.getData().setScale(getFontScaleX(), getFontScaleY());

				boolean wrap = false;
				if (wrap) {
					float prefHeight = getPrefHeight();
					if (prefHeight != getPrefHeight()) {
						invalidateHierarchy();
					}
				}
				
				GlyphLayout prefSizeLayout = new GlyphLayout();
				prefSizeLayout.setText(cache.getFont(), getText());

				float width = prefSizeLayout.width, height = getHeight();
				float x = 0, y = 0;
				GlyphLayout layout = this.getGlyphLayout();
				
				float textWidth = width, textHeight = font.getData().capHeight;

				if ((getLabelAlign() & Align.top) != 0) {
					y += cache.getFont().isFlipped() ? 0 : height - textHeight;
					y += getStyle().font.getDescent();
				} else if ((getLabelAlign() & Align.bottom) != 0) {
					y += cache.getFont().isFlipped() ? height - textHeight : 0;
					y -= getStyle().font.getDescent();
				} else {
					y += (height - textHeight) / 2;
				}
				if (!cache.getFont().isFlipped()) y += textHeight;

				layout.setText(font, getText(), 0, getText().length(), Color.WHITE, textWidth, getLineAlign(), wrap, null);
				cache.setText(layout, x, y);

				if (getFontScaleX() != 1 || getFontScaleY() != 1) font.getData().setScale(oldScaleX, oldScaleY);
			}

			@Override
			public void setText(CharSequence newText) {
				super.setText(newText);
				for(int i = 0; i < rows.size(); i++) {
					rows.get(i).calculate();
				}
			}
		};
		currentRow.add(label);
		return label;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {		
		nexwWindowWidth = getWidth();

		super.draw(batch, parentAlpha);
		for(int i = 0; i < rows.size(); i++) {
			rows.get(i).calculate();
		}
		
		setWidth(nexwWindowWidth);
	}
}
