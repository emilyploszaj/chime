package dev.emi.chime.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.emi.chime.ModelOverrideWrapper;
import net.minecraft.client.render.model.json.ModelOverride;

@Mixin(ModelOverride.class)
public class ModelOverrideMixin implements ModelOverrideWrapper {
	@Unique
	private Map<String, Object> customPredicates;

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
