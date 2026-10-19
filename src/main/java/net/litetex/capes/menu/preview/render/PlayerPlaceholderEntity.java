package net.litetex.capes.menu.preview.render;

import java.util.Collection;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.handler.PlayerCapeHandler;
import net.litetex.capes.handler.PlayerCapeHandlerManager;
import net.litetex.capes.provider.CapeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;


@SuppressWarnings("checkstyle:MagicNumber")
public class PlayerPlaceholderEntity
{
	final PlayerCapeHandlerManager playerCapeHandlerManager;
	final GameProfile gameProfile;
	Collection<CapeProvider> capeProviders;
	ResourceLocation skinTexture;
	ResourceLocation defaultCapeTexture;
	boolean slim;
	boolean showBody = true;
	boolean showElytra;
	boolean capeLoading;
	boolean capeLoaded;
	float limbDistance;
	float lastLimbDistance;
	float limbAngle;
	float yaw;
	float preYaw;
	double x;
	double prevX;
	
	public PlayerPlaceholderEntity(
		final Collection<CapeProvider> capeProviders,
		final PlayerCapeHandlerManager playerCapeHandlerManager)
	{
		this.playerCapeHandlerManager = playerCapeHandlerManager;
		this.gameProfile = Minecraft.getInstance().getUser().getGameProfile();
		this.skinTexture = DefaultPlayerSkin.getDefaultSkin(this.gameProfile.getId());
		this.slim = "slim".equals(DefaultPlayerSkin.getSkinModelName(this.gameProfile.getId()));
		Minecraft.getInstance().getSkinManager().registerSkins(
			this.gameProfile,
			(type, location, texture) -> {
				if(type == MinecraftProfileTexture.Type.SKIN)
				{
					this.skinTexture = location;
					this.slim = "slim".equals(texture.getMetadata("model"));
				}
				else if(type == MinecraftProfileTexture.Type.CAPE)
				{
					this.defaultCapeTexture = location;
				}
			},
			false);
		this.forceCapeRefresh(capeProviders);
	}
	
	public void updateLimbs()
	{
		this.lastLimbDistance = this.limbDistance;
		float g = (float)(this.x - this.prevX) * 4.0f;
		if(g > 1.0f)
		{
			g = 1.0f;
		}
		this.limbDistance += (g - this.limbDistance) * 0.4f;
		this.limbAngle += this.limbDistance;
	}
	
	public void loadCapeTextureIfRequired()
	{
		if(!this.capeLoading)
		{
			this.capeLoading = true;
			this.playerCapeHandlerManager.onLoadTexture(
				this.gameProfile,
				false,
				this.capeProviders,
				() -> this.capeLoaded = true);
		}
	}
	
	public ResourceLocation getCapeTexture()
	{
		final PlayerCapeHandler handler = this.playerCapeHandlerManager.getProfile(this.gameProfile);
		return handler != null && handler.hasElytraTexture() ? handler.getCape() : this.defaultCapeTexture;
	}
	
	public ResourceLocation getElytraTexture()
	{
		final PlayerCapeHandler handler = this.playerCapeHandlerManager.getProfile(this.gameProfile);
		final ResourceLocation capeTexture = this.getCapeTexture();
		return handler == null
			||
			handler.hasElytraTexture()
				&& Capes.instance().config().isEnableElytraTexture()
				&& capeTexture != null
			? capeTexture
			: Capes.DEFAULT_ELYTRA_TEXTURE;
	}
	
	public ResourceLocation getSkinTexture()
	{
		return this.skinTexture;
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	public void updatePrevX()
	{
		this.prevX = this.x + 0.025;
	}
	
	public boolean isSlim()
	{
		return this.slim;
	}
	
	public void updateYawDueToMouseDrag(final float deltaX)
	{
		this.preYaw = this.yaw;
		this.yaw -= deltaX;
	}
	
	public void toggleShowBody()
	{
		this.showBody = !this.showBody;
	}
	
	public void toggleShowElytra()
	{
		this.showElytra = !this.showElytra;
	}
	
	public void forceCapeRefresh(final Collection<CapeProvider> capeProviders)
	{
		this.capeProviders = capeProviders;
		this.capeLoaded = false;
		this.capeLoading = false;
	}
}
