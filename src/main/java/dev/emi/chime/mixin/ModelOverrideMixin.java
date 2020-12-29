package dev.emi.chime.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.chime.ChimeMain;
import dev.emi.chime.ModelOverrideWrapper;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Mixin(ModelOverride.class)
public class ModelOverrideMixin implements ModelOverrideWrapper {
	private Map<String, Object> customPredicates;

	@SuppressWarnings("unchecked")
	@Inject(at = @At("RETURN"), method = "matches", cancellable = true)
	private void matches(ItemStack stack, ClientWorld world, LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
		if (world == null && entity != null) {
			world = (ClientWorld) entity.getEntityWorld();
		}
		if (world == null && stack.getHolder() != null) {
			world = (ClientWorld) stack.getHolder().getEntityWorld();
		}
		if (info.getReturnValue()) {
			if (customPredicates.size() > 0) {
				for (Map.Entry<String, Object> entry : customPredicates.entrySet()) {
					if (ChimeMain.CUSTOM_MODEL_PREDICATES.containsKey(entry.getKey())) {
						if (!ChimeMain.CUSTOM_MODEL_PREDICATES.get(entry.getKey()).matches(stack, world, entity, entry.getValue())) {
							info.setReturnValue(false);
							return;
						}
					} else {
						info.setReturnValue(false);
						return;
					}
				}
			}
			info.setReturnValue(true);
		}
	}

	@Override
	public void setCustomPredicates(Map<String, Object> map) {
		customPredicates = map;
	}

	@Override
	public Object getCustomPredicate(String key) {
		return customPredicates.get(key);
	}
}
