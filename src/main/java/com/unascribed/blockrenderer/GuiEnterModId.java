package com.unascribed.blockrenderer;

import com.google.common.base.Strings;
//import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.OptionSlider;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;

import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiEnterModId extends Screen implements GuiResponder {
	private String prefill;
	private TextFieldWidget text;
	private OptionSlider size;
	private Screen old;
	
	public GuiEnterModId(ITextComponent titleIn, Screen old, String prefill) {
		super(titleIn);
		this.old = old;
		this.prefill = Strings.nullToEmpty(prefill);
	}
	
	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		String oldText = (text == null ? prefill : text.getText());
		
		float oldSize = (size == null ? 512 : size.getSliderValue());
		
		
		text = new GuiTextField(0, minecraft.fontRenderer, width/2 - 100, height/6 + 50, 200, 20);
		text.setText(oldText);
		
		//buttonList.add(new Button(2, width/2 - 100, height/6 + 120, 98, 20, I18n.format("gui.cancel")));
		addButton(new Button(2, width/2 - 100, height/6 + 120, 98, 20, I18n.format("gui.cancel")));
		
		Button render = new Button(1, width/2 + 2, height/6 + 120, 98, 20, I18n.format("gui.render"));
		//buttonList.add(render);
		addButton(render);
		int minSize = Math.min(minecraft.displayWidth, minecraft.displayHeight);
		size = new OptionSlider(this, 3, width/2 - 100, height/6 + 80, I18n.format("gui.rendersize"), 16, Math.min(2048, minSize), Math.min(oldSize, minSize), (id, name, value) -> {
			String px = Integer.toString(round(value));
			return name + ": "+px + "x" + px;
		});
		
		size.setWidth(200);
		//buttonList.add(size);
		addButton(size);
		
		text.setFocused(true);
		text.setCanLoseFocus(false);
		boolean enabled = minecraft.world != null;
		render.enabled = enabled;
		text.setEnabled(enabled);
		size.enabled = enabled;
	}

	private int round(float value) {
		int val = (int)value;
		// There's a more efficient method in MathHelper, but it rounds up. We want the nearest.
		int nearestPowerOfTwo = (int)Math.pow(2, Math.ceil(Math.log(val)/Math.log(2)));
		int minSize = Math.min(minecraft.displayHeight, minecraft.displayWidth);
		if (nearestPowerOfTwo < minSize && Math.abs(val-nearestPowerOfTwo) < 32) {
			val = nearestPowerOfTwo;
		}
		return Math.min(val, minSize);
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		drawCenteredString(minecraft.fontRenderer, I18n.format("gui.entermodid"), width/2, height/6, -1);
		if (minecraft.world == null) {
			drawCenteredString(minecraft.fontRenderer, I18n.format("gui.noworld"), width/2, height/6+30, 0xFF5555);
		} else {
			boolean widthCap = (minecraft.displayWidth < 2048);
			boolean heightCap = (minecraft.displayHeight < 2048);
			String str = null;
			if (widthCap && heightCap) {
				if (minecraft.displayWidth > minecraft.displayHeight) {
					str = "gui.cappedheight";
				} else if (minecraft.displayWidth == minecraft.displayHeight) {
					str = "gui.cappedboth";
				} else if (minecraft.displayHeight > minecraft.displayWidth) {
					str = "gui.cappedwidth";
				}
			} else if (widthCap) {
				str = "gui.cappedwidth";
			} else if (heightCap) {
				str = "gui.cappedheight";
			}
			if (str != null) {
				drawCenteredString(minecraft.fontRenderer, I18n.format(str, Math.min(minecraft.displayHeight, minecraft.displayWidth)), width/2, height/6+104, 0xFFFFFF);
			}
		}
		text.drawTextBox();
	}

	@Override
	protected void actionPerformed(Button button) throws IOException {
		super.actionPerformed(button);
		if (button.id == 1) {
			if (minecraft.world != null) {
				BlockRenderer.inst.pendingBulkRender = text.getText();
				BlockRenderer.inst.pendingBulkRenderSize = round(size.getSliderValue());
			}
			minecraft.displayGuiScreen(old);
		} else if (button.id == 2) {
			minecraft.displayGuiScreen(old);
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		text.updateCursorCounter();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		text.textboxKeyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		text.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void setEntryValue(int id, float value) {
		size.setSliderValue(round(value), false);
	}
	
	@Override
	public void setEntryValue(int id, boolean value) {
	}
	
	@Override
	public void setEntryValue(int id, String value) {
	}
}
