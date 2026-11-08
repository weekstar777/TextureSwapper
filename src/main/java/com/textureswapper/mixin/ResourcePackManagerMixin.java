package com.textureswapper.mixin;

import com.google.common.collect.ImmutableSet;
import com.textureswapper.SwapperPackProvider;
import net.minecraft.client.resource.DefaultClientResourcePackProvider;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ResourcePackManager.class)
public abstract class ResourcePackManagerMixin {
	@Mutable
	@Shadow
	@Final
	private Set<ResourcePackProvider> providers;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void textureswapper$addProvider(ResourcePackProvider[] providers, CallbackInfo ci) {
		for (ResourcePackProvider provider : this.providers) {
			if (provider instanceof DefaultClientResourcePackProvider) {
				this.providers = ImmutableSet.<ResourcePackProvider>builder()
						.addAll(this.providers)
						.add(new SwapperPackProvider())
						.build();
				return;
			}
		}
	}
}