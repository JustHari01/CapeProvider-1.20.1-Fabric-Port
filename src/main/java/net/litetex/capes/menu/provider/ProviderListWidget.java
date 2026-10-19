package net.litetex.capes.menu.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import net.litetex.capes.Capes;
import net.litetex.capes.menu.TickBoxWidget;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.DefaultMinecraftCapeProvider;
import net.litetex.capes.provider.antifeature.AntiFeature;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


@SuppressWarnings("checkstyle:MagicNumber")
public class ProviderListWidget extends ObjectSelectionList<ProviderListWidget.ProviderListEntry>
{
	private static final int ITEM_HEIGHT = 21;
	
	private final Screen parent;
	
	public ProviderListWidget(
		final Minecraft client,
		final int width,
		final int height,
		final int top,
		final int bottom,
		final Screen parent)
	{
		super(client, width, height, top, bottom, ITEM_HEIGHT);
		this.parent = parent;
		
		this.load();
	}
	
	private void load()
	{
		final Capes capes = Capes.instance();
		
		final List<String> activeProviderIds = new ArrayList<>(capes.config().getActiveProviderIds());
		
		this.replaceEntries(
			Stream.concat(
				capes.getAllProviders().values()
					.stream()
					.filter(Capes.EXCLUDE_DEFAULT_MINECRAFT_CP)
					.sorted(Comparator.comparing(cp -> {
						final int index = activeProviderIds.indexOf(cp.id());
						return index != -1 ? index : Integer.MAX_VALUE;
					}))
					.map(cp -> this.createEntry(
						cp,
						activeProviderIds.contains(cp.id()))),
				Stream.of(this.createEntry(
					DefaultMinecraftCapeProvider.INSTANCE,
					capes.isUseDefaultProvider()
				))
			).toList()
		);
		
		final List<ProviderListEntry> children = this.children();
		children.get(0).upVisible(false);
		children.get(children.size() - 2).downVisible(false);
		
		// Default Provider (usually Minecraft) is always last and can't be moved
		final ProviderListEntry last = children.get(children.size() - 1);
		last.upVisible(false);
		last.downVisible(false);
	}
	
	private ProviderListEntry createEntry(
		final CapeProvider capeProvider,
		final boolean active)
	{
		return new ProviderListEntry(
			capeProvider,
			active,
			this.parent,
			(self, a) -> this.save(),
			this::onPositionChanged);
	}
	
	private void save()
	{
		final Capes capes = Capes.instance();
		
		final List<CapeProvider> capeProviders = this.children().stream()
			.filter(ProviderListEntry::isActive)
			.map(ProviderListEntry::capeProvider)
			.toList();
		final List<String> idsWithoutDefault = capeProviders.stream()
			.filter(Capes.EXCLUDE_DEFAULT_MINECRAFT_CP)
			.map(CapeProvider::id)
			.toList();
		
		capes.config().setActiveProviderIds(idsWithoutDefault);
		capes.config().setUseDefaultProvider(idsWithoutDefault.size() != capeProviders.size());
		
		capes.saveConfigAndMarkRefresh();
	}
	
	private void onPositionChanged(final ProviderListEntry entry, final boolean up)
	{
		final List<ProviderListEntry> children = this.children();
		final int selfIndex = children.indexOf(entry);
		final int otherIndex = selfIndex + (up ? -1 : 1);
		final ProviderListEntry other = children.get(otherIndex);
		
		Collections.swap(children, selfIndex, otherIndex);
		
		final ProviderListEntry higherEntry = up ? entry : other;
		final ProviderListEntry lowerEntry = up ? other : entry;
		
		if(selfIndex == 0 || otherIndex == 0)
		{
			higherEntry.upVisible(false);
			higherEntry.downVisible(true);
			
			lowerEntry.upVisible(true);
		}
		final int lastIndex = children.size() - 2; // MinecraftCape provider is always there
		if(selfIndex == lastIndex || otherIndex == lastIndex)
		{
			higherEntry.downVisible(true);
			
			lowerEntry.upVisible(true);
			lowerEntry.downVisible(false);
		}
		
		this.save();
	}
	
	@Override
	public int getRowWidth()
	{
		return this.width - 4;
	}
	
	static class ProviderListEntry extends ObjectSelectionList.Entry<ProviderListEntry>
	{
		private static final int BTN_EDIT_CAPE_WIDTH = 96;
		
		private final CapeProvider capeProvider;
		
		private final Supplier<MutableComponent> nameTextSupplier;
		private boolean isNameHovering;
		
		private final Runnable onTxtClick;
		
		private final TickBoxWidget chbxActive;
		private final StringWidget txtName;
		private final Button icoWarn;
		private final Button btnEditCape;
		private final Button icoMoveUp;
		private final Button icoMoveDown;
		private final Runnable moveUpAction;
		private final Runnable moveDownAction;
		
		public ProviderListEntry(
			final CapeProvider capeProvider,
			final boolean activated,
			final Screen parentScreen,
			final BiConsumer<ProviderListEntry, Boolean> onActiveChanged,
			final BiConsumer<ProviderListEntry, Boolean> onPositionChange)
		{
			this.capeProvider = capeProvider;
			
			final Minecraft client = Minecraft.getInstance();
			
			this.chbxActive = new TickBoxWidget(
				13,
				activated,
				false,
				(w, ticked) -> onActiveChanged.accept(this, ticked));
			
			final String homepageUrl = capeProvider.homepageUrl();
			final boolean hasHomePageUrl = homepageUrl != null;
			
			this.nameTextSupplier = () ->
				formatMutableTextIf(Component.literal(this.capeProvider.name()), hasHomePageUrl, ChatFormatting.BLUE);
			final BiFunction<Component, Font, StringWidget> widgetFunc = StringWidget::new;
			this.txtName = widgetFunc.apply(
				this.nameTextSupplier.get(),
				Minecraft.getInstance().font);
			
			this.txtName.active = hasHomePageUrl;
			this.onTxtClick = hasHomePageUrl
				? () -> client.setScreen(new ConfirmLinkScreen(
				open -> {
					if(open)
					{
						Util.getPlatform().openUri(homepageUrl);
					}
					client.setScreen(parentScreen);
				}, homepageUrl, true))
				: null;
			
			final List<AntiFeature> antiFeatures = capeProvider.antiFeatures();
			this.icoWarn = !antiFeatures.isEmpty()
				? Button.builder(Component.literal("!"), btn -> { })
					.size(ITEM_HEIGHT - 4, ITEM_HEIGHT - 4)
					.build()
				: null;
			if(this.icoWarn != null)
			{
				this.icoWarn.setTooltip(Tooltip.create(antiFeatures.stream()
					.map(AntiFeature::message)
					.map(t -> Component.literal("- ").append(t))
					.reduce((t1, t2) -> t1.append("\n").append(t2))
					.orElseThrow()));
			}
			
			this.btnEditCape = capeProvider.hasChangeCapeUrl()
				? Button
				.builder(
					Component.literal("Edit cape"), btn ->
					{
						final String link = capeProvider.changeCapeUrl(client);
						client.setScreen(new ConfirmLinkScreen(
							open -> {
								if(open)
								{
									Util.getPlatform().openUri(link);
								}
								client.setScreen(parentScreen);
							}, link, true));
					})
				.size(BTN_EDIT_CAPE_WIDTH, ITEM_HEIGHT - 4)
				.build()
				: null;
			
			this.moveUpAction = () -> onPositionChange.accept(this, true);
			this.moveDownAction = () -> onPositionChange.accept(this, false);
			this.icoMoveUp = Button.builder(Component.literal("↑"), btn -> this.moveUpAction.run())
				.size(15, 10)
				.build();
			this.icoMoveDown = Button.builder(Component.literal("↓"), btn -> this.moveDownAction.run())
				.size(15, 10)
				.build();
		}
		
		public CapeProvider capeProvider()
		{
			return this.capeProvider;
		}
		
		public boolean isActive()
		{
			return this.chbxActive.isTicked();
		}
		
		public void upVisible(final boolean visible)
		{
			this.icoMoveUp.visible = visible;
		}
		
		public void downVisible(final boolean visible)
		{
			this.icoMoveDown.visible = visible;
		}
		
		@Override
		public void render(
			final GuiGraphics context,
			final int index,
			final int y,
			final int x,
			final int entryWidth,
			final int entryHeight,
			final int mouseX,
			final int mouseY,
			final boolean hovered,
			final float tickDelta)
		{
			this.chbxActive.setPosition(x, y + (ITEM_HEIGHT - this.chbxActive.getHeight() - 4) / 2);
			this.chbxActive.render(context, mouseX, mouseY, tickDelta);
			
			if(this.onTxtClick != null)
			{
				final boolean isOverName = this.txtName.isHovered();
				if(this.isNameHovering != isOverName)
				{
					this.txtName.setMessage(formatMutableTextIf(
						this.nameTextSupplier.get(),
						isOverName,
						ChatFormatting.UNDERLINE));
					
					this.isNameHovering = isOverName;
				}
			}
			
			this.txtName.setPosition(
				x + this.chbxActive.getWidth() + 4,
				y + ((ITEM_HEIGHT - this.txtName.getHeight()) / 2) - 1);
			this.txtName.render(context, mouseX, mouseY, tickDelta);
			
			if(this.icoWarn != null)
			{
				this.icoWarn.setPosition(
					x + entryWidth
						- this.icoWarn.getWidth() - BTN_EDIT_CAPE_WIDTH - this.icoMoveUp.getWidth() - (4 * 3),
					y + ((ITEM_HEIGHT - this.icoWarn.getHeight()) / 2) - 2);
				this.icoWarn.render(context, mouseX, mouseY, tickDelta);
			}
			
			if(this.btnEditCape != null)
			{
				this.btnEditCape.setPosition(
					x + entryWidth - BTN_EDIT_CAPE_WIDTH - this.icoMoveUp.getWidth() - (4 * 2),
					y);
				this.btnEditCape.render(context, mouseX, mouseY, tickDelta);
			}
			
			this.icoMoveUp.setPosition(x + entryWidth - this.icoMoveUp.getWidth() - 4, y);
			this.icoMoveUp.render(context, mouseX, mouseY, tickDelta);
			
			this.icoMoveDown.setPosition(x + entryWidth - this.icoMoveDown.getWidth() - 4, y + (ITEM_HEIGHT / 2));
			this.icoMoveDown.render(context, mouseX, mouseY, tickDelta);
		}
		
		@Override
		public boolean mouseClicked(final double mouseX, final double mouseY, final int button)
		{
			if(this.chbxActive.isMouseOver(mouseX, mouseY))
			{
				return this.chbxActive.mouseClicked(mouseX, mouseY, button);
			}
			if(this.onTxtClick != null && this.txtName.isMouseOver(mouseX, mouseY))
			{
				this.onTxtClick.run();
				return true;
			}
			if(this.btnEditCape != null && this.btnEditCape.isMouseOver(mouseX, mouseY))
			{
				return this.btnEditCape.mouseClicked(mouseX, mouseY, button);
			}
			if(this.icoMoveUp.isMouseOver(mouseX, mouseY))
			{
				return this.icoMoveUp.mouseClicked(mouseX, mouseY, button);
			}
			else if(this.icoMoveDown.isMouseOver(mouseX, mouseY))
			{
				return this.icoMoveDown.mouseClicked(mouseX, mouseY, button);
			}
			return true; // Select
		}
		
		private static MutableComponent formatMutableTextIf(
			final MutableComponent text,
			final boolean condition,
			final ChatFormatting formatting)
		{
			return condition ? text.withStyle(formatting) : text;
		}
		
		@Override
		public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers)
		{
			if(GLFW.GLFW_KEY_SPACE == keyCode || GLFW.GLFW_KEY_ENTER == keyCode)
			{
				this.chbxActive.toggle();
				return true;
			}
			if(GLFW.GLFW_MOD_SHIFT == modifiers)
			{
				if(GLFW.GLFW_KEY_UP == keyCode && this.icoMoveUp.visible)
				{
					this.moveUpAction.run();
					return true;
				}
				else if(GLFW.GLFW_KEY_DOWN == keyCode && this.icoMoveDown.visible)
				{
					this.moveDownAction.run();
					return true;
				}
			}
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		
		@Override
		public Component getNarration()
		{
			return Component.literal(this.capeProvider().name());
		}
	}
}
