package dev.emi.chime.mixin;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
	@Unique
	private int currentIndex;
	@Unique
	private List<ModelOverride> unbakedOverrides;

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/client/render/model/ModelLoader;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Ljava/util/List;)V")
	public void init(ModelLoader modelLoader, JsonUnbakedModel parent, Function<Identifier, UnbakedModel> unbakedModelGetter, List<ModelOverride> overrides, CallbackInfo info) {
		unbakedOverrides = overrides;
	}

	@Inject(at = @At("HEAD"), method = "apply")
	private void apply(CallbackInfoReturnable<BakedModel> info) {
		if (unbakedOverrides != null) {
			currentIndex = unbakedOverrides.size();
		}
	}

	@SuppressWarnings("unchecked")
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/ModelOverrideList$BakedOverride;test([F)Z"), method = "apply")
	private boolean redirect(BakedOverride override, float[] arr, BakedModel model, ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
		currentIndex--;
		if (override.test(arr)) {
			if (unbakedOverrides == null) {
				return true;
			}
			if (world == null && entity != null) {
				world = (ClientWorld) entity.getEntityWorld();
			}
			if (world == null && stack.getHolder() != null) {
				world = (ClientWorld) stack.getHolder().getEntityWorld();
			}
			Map<String, Object> customPredicates = ((ModelOverrideWrapper) unbakedOverrides.get(currentIndex)).getCustomPredicates();
			if (customPredicates.size() > 0) {
				for (Map.Entry<String, Object> entry : customPredicates.entrySet()) {
					if (ChimeClient.CUSTOM_MODEL_PREDICATES.containsKey(entry.getKey())) {
						if (!ChimeClient.CUSTOM_MODEL_PREDICATES.get(entry.getKey()).matches(stack, world, entity, entry.getValue())) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
}
