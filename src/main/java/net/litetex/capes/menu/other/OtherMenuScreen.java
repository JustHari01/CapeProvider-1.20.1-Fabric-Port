package net.litetex.capes.menu.other;

import net.litetex.capes.Capes;
import net.litetex.capes.config.AnimatedCapesHandling;
import net.litetex.capes.menu.MainMenuScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;


public class OtherMenuScreen extends MainMenuScreen
{
	public OtherMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions);
	}
	
	@Override
	protected void addOptions()
	{
		final Capes capes = Capes.instance();
		final int buttonWidth = Math.min(200, (this.width - 40) / 2);
		final int gap = 10;
		final int leftX = (this.width - (buttonWidth * 2 + gap)) / 2;
		final int rightX = leftX + buttonWidth + gap;
		final int rowHeight = 28;
		final int firstY = 82;
		
		this.addRenderableWidget(CycleButton.onOffBuilder(capes.config().isOnlyLoadForSelf())
			.create(leftX, firstY, buttonWidth, 20, Component.literal("Only load your cape"), (btn, enabled) -> {
				this.config().setOnlyLoadForSelf(enabled);
				capes.saveConfigAndMarkRefresh();
			}));
		this.addRenderableWidget(CycleButton.<AnimatedCapesHandling>builder(handling ->
				switch(handling)
				{
					case ON -> CommonComponents.OPTION_ON;
					case FROZEN -> Component.literal("Frozen");
					case OFF -> CommonComponents.OPTION_OFF;
				})
			.withInitialValue(this.config().getAnimatedCapesHandling())
			.withValues(AnimatedCapesHandling.values())
			.create(rightX, firstY, buttonWidth, 20, Component.literal("Animated textures"), (btn, value) -> {
				this.config().setAnimatedCapesHandling(value);
				capes.saveConfigAndMarkRefresh();
			}));
		this.addRenderableWidget(CycleButton.onOffBuilder(capes.config().isEnableElytraTexture())
			.create(leftX, firstY + rowHeight, buttonWidth, 20, Component.literal("Elytra texture"), (btn, enabled) -> {
				this.config().setEnableElytraTexture(enabled);
				capes.saveConfig();
			}));
		this.addRenderableWidget(CycleButton.onOffBuilder(capes.config().isActivateExternalProvidersOnInitialLoad())
			.withTooltip(value -> Tooltip.create(Component.empty()
				.append(Component.literal("Should external providers (mods/local) be automatically "
					+ "activated/enabled when initially loaded?\n"))
				.append(this.requiresRestartComponent())))
			.create(rightX, firstY + rowHeight, buttonWidth, 20,
				Component.literal("Activate external providers"), (btn, enabled) -> {
					this.config().setActivateExternalProvidersOnInitialLoad(enabled);
					capes.saveConfig();
				}));
		this.addRenderableWidget(CycleButton.onOffBuilder(capes.config().isLoadProvidersFromMods())
			.withTooltip(value -> Tooltip.create(Component.empty()
				.append(Component.literal("Should providers be loaded from other mods?\n"))
				.append(this.requiresRestartComponent())))
			.create(leftX, firstY + rowHeight * 2, buttonWidth, 20, Component.literal("Load Mod providers"),
				(btn, enabled) -> {
					this.config().setLoadProvidersFromMods(enabled);
					capes.saveConfig();
				}));
		this.addRenderableWidget(CycleButton.onOffBuilder(capes.config().isLoadSimpleLocalProvidersFromFilesystem())
			.withTooltip(value -> Tooltip.create(Component.empty()
				.append(Component.literal("Should simple-local providers be loaded from "
					+ "config/cape-provider or config/cape-provider/simple-custom/...?\n"))
				.append(this.requiresRestartComponent())))
			.create(rightX, firstY + rowHeight * 2, buttonWidth, 20, Component.literal("Load local providers"),
				(btn, enabled) -> {
					this.config().setLoadSimpleLocalProvidersFromFilesystem(enabled);
					capes.saveConfig();
				}));
		
		this.addRenderableWidget(Button.builder(
				Component.translatable("controls.reset"), btn -> {
					capes.reset();
					this.minecraft.setScreen(new OtherMenuScreen(this.lastScreen, this.options));
				})
			.pos((this.width - 100) / 2, firstY + rowHeight * 3)
			.size(100, 20)
			.build());
	}
	
	private Component requiresRestartComponent()
	{
		return Component.literal("\nRequires restart to take effect")
			.withStyle(ChatFormatting.ITALIC);
	}
}
