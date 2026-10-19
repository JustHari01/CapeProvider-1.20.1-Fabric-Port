package net.litetex.capes.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.litetex.capes.Capes;
import net.litetex.capes.config.Config;
import net.litetex.capes.menu.other.OtherMenuScreen;
import net.litetex.capes.menu.preview.PreviewMenuScreen;
import net.litetex.capes.menu.provider.ProviderMenuScreen;
import net.litetex.capes.util.CorrectHoverParentElement;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.network.chat.Component;


public abstract class MainMenuScreen extends OptionsSubScreen implements CorrectHoverParentElement
{
	private final List<GuiEventListener> selfManagedDrawableChilds = new ArrayList<>();
	
	protected MainMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions, Component.literal("Cape Options"));
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	@Override
	protected void init()
	{
		this.initSelfMangedDrawableChilds();
		this.addOptions();
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	protected void initSelfMangedDrawableChilds()
	{
		final int buttonW = 100;
		final int gap = 10;
		final int startX = (this.width - (buttonW * 3 + gap * 2)) / 2;
		
		record ButtonBuildData(
			String text,
			Supplier<Screen> screenSupplier,
			int positionDiff,
			Class<?> clazz
		)
		{
		}
		
		Stream.of(
				new ButtonBuildData(
					"Preview",
					() -> new PreviewMenuScreen(this.lastScreen, this.options),
					startX,
					PreviewMenuScreen.class
				),
				new ButtonBuildData(
					"Manage Providers",
					() -> new ProviderMenuScreen(this.lastScreen, this.options),
					startX + buttonW + gap,
					ProviderMenuScreen.class),
				new ButtonBuildData(
					"Other",
					() -> new OtherMenuScreen(this.lastScreen, this.options),
					startX + (buttonW + gap) * 2,
					OtherMenuScreen.class
				))
			.forEach(data -> {
				final Button buttonWidget = this.addSelfManagedDrawableChild(Button.builder(
						Component.literal(data.text()),
						b -> this.minecraft.setScreen(data.screenSupplier().get()))
					.pos(data.positionDiff(), 35)
					.size(buttonW, 20)
					.build());
				buttonWidget.active = !(data.clazz().isInstance(this));
			});
	}
	
	protected <T extends GuiEventListener & Renderable & NarratableEntry> T addSelfManagedDrawableChild(
		final T drawableElement)
	{
		this.selfManagedDrawableChilds.add(drawableElement);
		return this.addRenderableWidget(drawableElement);
	}
	
	@Override
	protected void clearWidgets()
	{
		this.selfManagedDrawableChilds.clear();
		super.clearWidgets();
	}
	
	protected void addOptions()
	{
		// Most screens contain no additional controls.
	}
	
	protected Capes capes()
	{
		return Capes.instance();
	}
	
	protected Config config()
	{
		return this.capes().config();
	}
	
	@Override
	public void onClose()
	{
		super.onClose();
		this.capes().refreshIfMarked();
	}
}
