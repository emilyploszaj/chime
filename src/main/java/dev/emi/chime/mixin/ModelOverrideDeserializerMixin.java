package dev.emi.chime.mixin;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.chime.ChimeClient;
import dev.emi.chime.ModelOverrideWrapper;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.util.Identifier;

@Mixin(ModelOverride.Deserializer.class)
public class ModelOverrideDeserializerMixin {
	private Map<String, Object> customPredicates;
	
	@Inject(at = @At("RETURN"), method = "deserialize", cancellable = true)
	public void deserialize(JsonElement element, Type type, JsonDeserializationContext context, CallbackInfoReturnable<ModelOverride> info) throws JsonParseException {
		((ModelOverrideWrapper) info.getReturnValue()).setCustomPredicates(customPredicates);
	}

	@Inject(at = @At("HEAD"), method = "deserializeMinPropertyValues")
	private void deserializeMinPropertyValues(JsonObject object, CallbackInfoReturnable<Map<Identifier, Float>> info) {
		customPredicates = Maps.newHashMap();
		JsonObject pred = object.getAsJsonObject("predicate");
		parseCustomPredicates(pred, "");
	}

	private void parseCustomPredicates(JsonObject pred, String path) {
		List<String> toRemove = Lists.newArrayList();
		for (Map.Entry<String, JsonElement> entry : pred.entrySet()) {
			String newPath = entry.getKey();
			if (path.length() > 0) {
				newPath = path + "/" + newPath;
			}
			if (entry.getValue().isJsonObject() && !entry.getKey().equals("nbt")) {
				parseCustomPredicates(entry.getValue().getAsJsonObject(), newPath);
				if (path.length() == 0) {
					toRemove.add(entry.getKey());
				}
			} else {
				if (ChimeClient.CUSTOM_MODEL_PREDICATES.containsKey(newPath)) {
					customPredicates.put(newPath, ChimeClient.CUSTOM_MODEL_PREDICATES.get(newPath).parseType(entry.getValue()));
					if (path.length() == 0) {
						toRemove.add(entry.getKey());
					}
				}
			}
		}
		for (String s : toRemove) {
			pred.remove(s);
		}
	}
}
