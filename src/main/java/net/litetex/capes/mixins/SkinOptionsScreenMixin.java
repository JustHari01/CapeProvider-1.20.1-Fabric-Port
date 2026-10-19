package net.litetex.capes.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.litetex.capes.menu.preview.PreviewMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;


@Mixin(SkinCustomizationScreen.class)
public abstract class SkinOptionsScreenMixin
{
	@Shadow
	protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

	@Inject(method = "init", at = @At("TAIL"))
	private void addCapeProviderButton(final CallbackInfo ci)
	{
		final SkinCustomizationScreen screen = (SkinCustomizationScreen)(Object)this;
		final Minecraft client = Minecraft.getInstance();
		this.addRenderableWidget(Button.builder(Component.literal("Capes"), ignored ->
			client.setScreen(new PreviewMenuScreen(screen, client.options)))
			.pos(20, screen.height - 38)
			.size(64, 20)
			.build());
	}
}
