package dev.emi.chime.mixin;

import java.util.List;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.chime.ChimeClient;
import dev.emi.chime.ModelOverrideWrapper;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelOverrideList.BakedOverride;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Mixin(ModelOverrideList.class)
public class ModelOverrideListMixin {
	@Shadow @Final
    private BakedOverride[] overrides;

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/client/render/model/ModelLoader;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Ljava/util/List;)V")
	private void init(ModelLoader modelLoader, JsonUnbakedModel parent, Function<Identifier, UnbakedModel> unbakedModelGetter,
			List<ModelOverride> overrides, CallbackInfo info) {
		for (int i = 0; i < this.overrides.length; i++) {
			((ModelOverrideWrapper) this.overrides[i]).setCustomPredicates(
				((ModelOverrideWrapper) overrides.get(overrides.size() - i - 1)).getCustomPredicates());
		}
	}
	
	@Inject(at = @At("HEAD"), method = "apply")
	private void apply(BakedModel model, ItemStack stack, ClientWorld world, LivingEntity entity, int seed,
			CallbackInfoReturnable<BakedModel> info) {
		if (world == null && entity != null && entity.getEntityWorld() instanceof ClientWorld) {
			world = (ClientWorld) entity.getEntityWorld();
		}
		if (world == null && stack.getHolder() != null && stack.getHolder().getEntityWorld() instanceof ClientWorld) {
			world = (ClientWorld) stack.getHolder().getEntityWorld();
		}
		ChimeClient.cachedStack = stack;
		ChimeClient.cachedWorld = world;
		ChimeClient.cachedLivingEntity = entity;
	}
}
