package dev.emi.chime.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@Mixin(ModelPredicateProviderRegistry.class)
public abstract class ModelPredicateProviderRegistryMixin {

	@Shadow
	private static ModelPredicateProvider register(Identifier id, ModelPredicateProvider provider) {
		return null;
	}
	
	@Inject(at = @At("TAIL"), method = "<clinit>")
	private static void clinit(CallbackInfo info) {
		register(new Identifier("count"), (stack, world, entity) -> {
			return stack.getCount();
		});
	}
}
