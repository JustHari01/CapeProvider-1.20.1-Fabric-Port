package net.litetex.capes.menu.provider;

import net.litetex.capes.menu.MainMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;


public class ProviderMenuScreen extends MainMenuScreen
{
	public ProviderMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions);
	}
	
	@Override
	protected void initSelfMangedDrawableChilds()
	{
		super.initSelfMangedDrawableChilds();
		final int listWidth = Math.max(180, Math.min(480, this.width - 40));
		final int top = 66;
		final int bottom = this.height - 18;
		final ProviderListWidget providerListWidget = this.addSelfManagedDrawableChild(new ProviderListWidget(
			Minecraft.getInstance(),
			listWidth,
			Math.max(0, bottom - top),
			top,
			bottom,
			this));
		providerListWidget.setLeftPos((this.width - listWidth) / 2);
	}
}
