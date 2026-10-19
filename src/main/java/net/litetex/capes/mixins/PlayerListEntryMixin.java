package net.litetex.capes.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.ResourceLocation;


@Mixin(PlayerInfo.class)
public abstract class PlayerListEntryMixin
{
	@Shadow
	@Final
	private GameProfile profile;

	@Inject(method = "registerTextures", at = @At("TAIL"))
	private void capeProvider$loadTextures(final CallbackInfo ci)
	{
		final Capes capes = Capes.instance();
		if(capes != null && (!capes.config().isOnlyLoadForSelf()
			|| Minecraft.getInstance().getUser().getProfileId().equals(this.profile.getId())))
		{
			capes.textureLoadThrottler().loadIfRequired(this.profile);
		}
	}

	@Inject(method = "getCapeLocation", at = @At("RETURN"), order = 1001, cancellable = true)
	private void capeProvider$applyCape(final CallbackInfoReturnable<ResourceLocation> cir)
	{
		final Capes capes = Capes.instance();
		if(capes == null)
		{
			return;
		}
		final ResourceLocation cape = capes.capeTexture(this.profile);
		if(cape != null)
		{
			cir.setReturnValue(cape);
		}
		else if(!capes.isUseDefaultProvider())
		{
			cir.setReturnValue(null);
		}
	}

	@Inject(method = "getElytraLocation", at = @At("RETURN"), order = 1001, cancellable = true)
	private void capeProvider$applyElytra(final CallbackInfoReturnable<ResourceLocation> cir)
	{
		final Capes capes = Capes.instance();
		if(capes == null)
		{
			return;
		}
		final ResourceLocation elytra = capes.elytraTexture(this.profile);
		if(elytra != null)
		{
			cir.setReturnValue(elytra);
		}
		else if(!capes.isUseDefaultProvider())
		{
			cir.setReturnValue(null);
		}
	}
}
