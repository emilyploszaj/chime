package dev.emi.cim;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class CIMMain implements ModInitializer {
	@SuppressWarnings("rawtypes")
	public static final Map<String, CustomModelPredicate> CUSTOM_MODEL_PREDICATES = Maps.newHashMap();

	@Override
	public void onInitialize() {
	}

	static {
		register("nbt", CompoundTag.class, (ItemStack stack, ClientWorld world, LivingEntity entity, CompoundTag value) -> {
			if (stack.hasTag()) {
				try {
					return objectMatches(value, stack.getTag());
				} catch (Exception e) {
					return false;
				}
			}
			return false;
		});
		register("dimension/id", Identifier.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Identifier value) -> {
			return world != null && world.getRegistryKey().getValue().equals(value);
		});
		register("dimension/has_sky_light", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().hasSkyLight() == value;
		});
		register("dimension/has_ceiling", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().hasCeiling() == value;
		});
		register("dimension/ultrawarm", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().isUltrawarm() == value;
		});
		register("dimension/natural", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().isNatural() == value;
		});
		register("dimension/has_ender_dragon_fight", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().hasEnderDragonFight() == value;
		});
		register("dimension/piglin_safe", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().isPiglinSafe() == value;
		});
		register("dimension/bed_works", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().isBedWorking() == value;
		});
		register("dimension/respawn_anchor_works", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().isRespawnAnchorWorking() == value;
		});
		register("dimension/has_raids", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().hasRaids() == value;
		});
		register("dimension/natural", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.getDimension().isNatural() == value;
		});
		register("world/raining", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.isRaining() == value.booleanValue();
		});
		register("world/thundering", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.isThundering() == value;
		});
		register("entity/x", Float.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Float value) -> {
			return entity != null && entity.getX() >= value;
		});
		register("entity/y", Float.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Float value) -> {
			return entity != null && entity.getY() >= value;
		});
		register("entity/z", Float.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Float value) -> {
			return entity != null && entity.getZ() >= value;
		});
		register("entity/target_entity", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			if (entity != null) {
				MinecraftClient client = MinecraftClient.getInstance();
				if (entity == client.player) {
					HitResult result = client.crosshairTarget;
					if (result.getType() == HitResult.Type.ENTITY) {
						double d = client.interactionManager.getReachDistance();
						Vec3d vec3d = entity.getCameraPosVec(client.getTickDelta());
						Vec3d vec3d2 = entity.getRotationVec(1.0F);
						Vec3d vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
						Box box = entity.getBoundingBox().stretch(vec3d2.multiply(d)).expand(1.0D, 1.0D, 1.0D);
						EntityHitResult entityHitResult = ProjectileUtil.raycast(entity, vec3d, vec3d3, box, (entityx) -> {
						   return !entityx.isSpectator() && entityx.collides();
						}, d * d);
						if (entityHitResult != null) {
							Entity hit = entityHitResult.getEntity();
							if (hit != null) {
								Identifier id;
								if (value.startsWith("#")) {
									id = new Identifier(value.substring(1));
									net.minecraft.tag.Tag<EntityType<?>> tag = client.world.getTagManager().getEntityTypes().getTag(id);
									if (tag != null && tag.contains(hit.getType())) {
										return true;
									}
								} else {
									id = new Identifier(value);
									if (EntityType.getId(hit.getType()).equals(id)) {
										return true;
									}
								}
							}
						}
					}
				}
			}
			return false;
		});
	}

	@SuppressWarnings("unchecked")
	private static <T> void register(String key, Class<T> clazz, CustomModelPredicateFunction<T> func) {
		if (clazz == Float.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new FloatCustomModelPredicate((CustomModelPredicateFunction<Float>) func));
		} else if (clazz == Integer.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new IntegerCustomModelPredicate((CustomModelPredicateFunction<Integer>) func));
		} else if (clazz == Boolean.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new BooleanCustomModelPredicate((CustomModelPredicateFunction<Boolean>) func));
		} else if (clazz == String.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new StringCustomModelPredicate((CustomModelPredicateFunction<String>) func));
		} else if (clazz == CompoundTag.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new CompoundTagCustomModelPredicate((CustomModelPredicateFunction<CompoundTag>) func));
		} else if (clazz == Identifier.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new IdentifierCustomModelPredicate((CustomModelPredicateFunction<Identifier>) func));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private static boolean objectMatches(CompoundTag base, CompoundTag comp) {
		for (String key : base.getKeys()) {
			Tag t = base.get(key);
			if (!comp.contains(key, t.getType())) {
				return false;
			}
			if (t.getType() == 10) {
				if (!objectMatches((CompoundTag) t, comp.getCompound(key))) {
					return false;
				}
			} else if (t.getType() == 9) {
				if (!listMatches((ListTag) t, (ListTag) comp.get(key))) {
					return false;
				}
			} else {
				if (!t.equals(comp.get(key))) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean listMatches(ListTag base, ListTag comp) {
		outer:
		for (Tag b : base) {
			for (Tag c : comp) {
				if (b.getType() != c.getType()) {
					return false;
				}
				if (b.getType() == 10) {
					if (objectMatches((CompoundTag) b, (CompoundTag) c)) {
						continue outer;
					}
				} else if (b.getType() == 9) {
					if (listMatches((ListTag) b, (ListTag) c)) {
						continue outer;
					}
				} else {
					if (b.equals(c)) {
						continue outer;
					}
				}
			}
			return false;
		}
		return true;
	}

	public static abstract class CustomModelPredicate<T> {
		private CustomModelPredicateFunction<T> function;

		public CustomModelPredicate(CustomModelPredicateFunction<T> function) {
			this.function = function;
		}

		public boolean matches(ItemStack stack, ClientWorld world, LivingEntity entity, T value) {
			return function.matches(stack, world, entity, value);
		}

		public abstract T parseType(JsonElement element);
	}

	public static class FloatCustomModelPredicate extends CustomModelPredicate<Float> {

		public FloatCustomModelPredicate(CustomModelPredicateFunction<Float> function) {
			super(function);
		}

		@Override
		public Float parseType(JsonElement element) {
			return element.getAsFloat();
		}
	}

	public static class IntegerCustomModelPredicate extends CustomModelPredicate<Integer> {

		public IntegerCustomModelPredicate(CustomModelPredicateFunction<Integer> function) {
			super(function);
		}

		@Override
		public Integer parseType(JsonElement element) {
			return element.getAsInt();
		}
	}

	public static class BooleanCustomModelPredicate extends CustomModelPredicate<Boolean> {

		public BooleanCustomModelPredicate(CustomModelPredicateFunction<Boolean> function) {
			super(function);
		}

		@Override
		public Boolean parseType(JsonElement element) {
			return element.getAsBoolean();
		}
	}

	public static class StringCustomModelPredicate extends CustomModelPredicate<String> {

		public StringCustomModelPredicate(CustomModelPredicateFunction<String> function) {
			super(function);
		}

		@Override
		public String parseType(JsonElement element) {
			return element.getAsString();
		}
	}

	public static class CompoundTagCustomModelPredicate extends CustomModelPredicate<CompoundTag> {

		public CompoundTagCustomModelPredicate(CustomModelPredicateFunction<CompoundTag> function) {
			super(function);
		}

		@Override
		public CompoundTag parseType(JsonElement element) {
			try {
				return StringNbtReader.parse(element.getAsString());
			} catch(Exception e) {
				return null;
			}
		}
	}

	public static class IdentifierCustomModelPredicate extends CustomModelPredicate<Identifier> {

		public IdentifierCustomModelPredicate(CustomModelPredicateFunction<Identifier> function) {
			super(function);
		}

		@Override
		public Identifier parseType(JsonElement element) {
			return new Identifier(element.getAsString());
		}
	}

	public interface CustomModelPredicateFunction<T> {
		public boolean matches(ItemStack stack, ClientWorld world, LivingEntity entity, T value);
	}
}