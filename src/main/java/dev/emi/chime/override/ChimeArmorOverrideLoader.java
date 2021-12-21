package dev.emi.chime.override;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.logging.log4j.LogManager;

import dev.emi.chime.armor.ChimeArmor;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class ChimeArmorOverrideLoader implements SimpleResourceReloadListener<Map<Identifier, List<ModelOverride>>> {
	private static final Identifier ID = new Identifier("chime", "armor");
	private static final Gson GSON = new Gson();

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	@Override
	public CompletableFuture<Map<Identifier, List<ModelOverride>>> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			return loadOverrides(manager);
		});
	}

	@Override
	public CompletableFuture<Void> apply(Map<Identifier, List<ModelOverride>> map, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			applyOverrides(map);
		});
	}
	
	public static void firstLoad() {
		applyOverrides(loadOverrides(MinecraftClient.getInstance().getResourceManager()));
	}

	private static Map<Identifier, List<ModelOverride>> loadOverrides(ResourceManager manager) {
		ModelOverride.Deserializer deserializer = new Deserializer2();

		Map<Identifier, List<ModelOverride>> map = new HashMap<>();
		for (Identifier id : manager.findResources("overrides/armor", path -> path.endsWith(".json"))) {
			String[] parts = id.getPath().split("/");
			String name = parts[parts.length - 1];
			name = name.substring(0, name.length() - 5);
			List<ModelOverride> list = new ArrayList<>();
			try {
				for (Resource r : manager.getAllResources(id)) {
					try (InputStreamReader reader = new InputStreamReader(r.getInputStream())) {
						JsonObject object = GSON.fromJson(reader, JsonObject.class);
						JsonArray arr = object.getAsJsonArray("overrides");
						for (JsonElement el : arr) {
							JsonObject obj = el.getAsJsonObject();
							if (obj.has("texture")) {
								obj.getAsJsonObject().addProperty("model", obj.getAsJsonPrimitive("texture").getAsString());
							}
							if (obj.has("model")) {
								obj.getAsJsonObject().addProperty("model", obj.getAsJsonPrimitive("model").getAsString() + ".png");
							}
							// What harm could passing null ever have?
							ModelOverride override = deserializer.deserialize(el, null, null);
							list.add(override);
						}
					} catch (Exception e) {
						LogManager.getLogger("chime").warn("[chime] Malformed json for armor override: " + r.getId(), e);
					}
				}
			} catch (Exception e) {
				LogManager.getLogger("chime").warn("[chime] IO error reading armor overrides: " + name, e);
			}
			// Abide by minecraft override standard and match bottom up
			Collections.reverse(list);
			map.put(new Identifier(id.getNamespace(), "textures/models/armor/" + name + ".png"), list);
		}
		return map;
	}

	private static void applyOverrides(Map<Identifier, List<ModelOverride>> map) {
		ChimeArmor.armorOverrides = map;
	}

	public static class Deserializer2 extends ModelOverride.Deserializer {

		public Deserializer2() {
		}
	}
}
