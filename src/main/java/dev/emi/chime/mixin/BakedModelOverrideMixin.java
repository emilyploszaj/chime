package dev.emi.chime.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.chime.ChimeClient;
import dev.emi.chime.ModelOverrideWrapper;
import net.minecraft.client.render.model.json.ModelOverrideList;

@Mixin(ModelOverrideList.BakedOverride.class)
public class BakedModelOverrideMixin implements ModelOverrideWrapper {
	@Unique
	private Map<String, Object> customPredicates;

	@Inject(at = @At("RETURN"), method = "test", cancellable = true)
	private void test(float[] fs, CallbackInfoReturnable<Boolean> info) {
		if (info.getReturnValue()) {
			info.setReturnValue(ChimeClient.checkOverrides(customPredicates));
		}
	}

	@Override
	public void setCustomPredicates(Map<String, Object> map) {
		customPredicates = map;
	}

	@Override
	public Map<String, Object> getCustomPredicates() {
		return customPredicates;
	}

	@Override
	public Object getCustomPredicate(String key) {
		return customPredicates.get(key);
	}
}
