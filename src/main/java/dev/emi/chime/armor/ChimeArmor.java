package dev.emi.chime.armor;

import java.util.List;
import java.util.Map;

import dev.emi.chime.ChimeClient;
import dev.emi.chime.ModelOverrideWrapper;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ChimeArmor {
	public static Map<Identifier, List<ModelOverride>> armorOverrides;
	
	public static <T extends LivingEntity> Identifier getArmorIdentifier(Identifier id, ItemStack stack, T entity) {
		if (armorOverrides.containsKey(id)) {
			ChimeClient.cachedStack = stack;
			ChimeClient.cachedWorld = (ClientWorld) entity.getEntityWorld();
			ChimeClient.cachedLivingEntity = entity;
			for (ModelOverride override : armorOverrides.get(id)) {
				if (ChimeClient.checkOverrides(((ModelOverrideWrapper) override).getCustomPredicates())) {
					return override.getModelId();
				}
			}
		}
		return null;
	}
}
