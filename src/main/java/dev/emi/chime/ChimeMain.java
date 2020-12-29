package dev.emi.chime;

import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.BoundType;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.AbstractNumberTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.registry.Registry;

// TODO move this to client
@SuppressWarnings({"unchecked", "rawtypes"})
public class ChimeMain implements ModInitializer {
	public static final Map<String, CustomModelPredicate> CUSTOM_MODEL_PREDICATES = Maps.newHashMap();

	@Override
	public void onInitialize() {
	}

	static {
		register("nbt", JsonObject.class, (ItemStack stack, ClientWorld world, LivingEntity entity, JsonObject value) -> {
			if (stack.hasTag()) {
				return matchesJsonObject(value, stack.getTag());
			}
			return false;
		});
		register("name", Pattern.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Pattern value) -> {
			return value.matcher(stack.getName().asString()).matches();
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
		register("entity/nbt", JsonObject.class, (ItemStack stack, ClientWorld world, LivingEntity entity, JsonObject value) -> {
			if (entity != null) {
				return matchesJsonObject(value, entity.toTag(new CompoundTag()));
			}
			return false;
		});
		register("entity/x", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			return entity != null && ((Range<Float>) value).contains((float) entity.getZ());
		});
		register("entity/y", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			return entity != null && ((Range<Float>) value).contains((float) entity.getY());
		});
		register("entity/z", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			return entity != null && ((Range<Float>) value).contains((float) entity.getZ());
		});
		register("entity/hand", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			if (entity == null) {
				return false;
			}
			boolean main = entity.getMainHandStack() == stack;
			boolean off = entity.getOffHandStack() == stack;
			switch (value) {
				case "main":
					return main;
				case "off":
					return off;
				case "either":
					return main || off;
				case "neither":
				case "none":
					return !(main || off);
				default:
					return false;
			}
		});
		register("entity/target", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			String s = "none";
			if (entity != null) {
				MinecraftClient client = MinecraftClient.getInstance();
				if (entity == client.player) {
					HitResult result = client.crosshairTarget;
					if (result.getType() == HitResult.Type.BLOCK) {
						s = "block";
					} else if (result.getType() == HitResult.Type.ENTITY) {
						s = "entity";
					} else if (result.getType() == HitResult.Type.MISS) {
						s = "miss";
					}
				}
			}
			return value.equals(s);
		});
		register("entity/target_block/can_mine", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			BlockState state = raycastBlockState(world, entity);
			return value == stack.isEffectiveOn(state);
		});
		register("entity/target_block/id", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			BlockState state = raycastBlockState(world, entity);
			if (value.startsWith("#")) {
				MinecraftClient client = MinecraftClient.getInstance();
				Identifier id = new Identifier(value.substring(1));
				net.minecraft.tag.Tag<Block> tag = client.world.getTagManager().getBlocks().getTag(id);
				if (tag != null && tag.contains(state.getBlock())) {
					return true;
				}
			} else {
				if (Registry.BLOCK.getId(state.getBlock()).equals(new Identifier(value))) {
					return true;
				}
			}
			return false;
		});
		register("entity/target_entity/id", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			Entity hit = raycastEntity(world, entity);
			if (hit != null) {
				if (value.startsWith("#")) {
					Identifier id = new Identifier(value.substring(1));
					net.minecraft.tag.Tag<EntityType<?>> tag = client.world.getTagManager().getEntityTypes().getTag(id);
					if (tag != null && tag.contains(hit.getType())) {
						return true;
					}
				} else {
					if (EntityType.getId(hit.getType()).equals(new Identifier(value))) {
						return true;
					}
				}
			}
			return false;
		});
		register("entity/target_entity/nbt", JsonObject.class, (ItemStack stack, ClientWorld world, LivingEntity entity, JsonObject value) -> {
			Entity hit = raycastEntity(world, entity);
			if (hit != null) {
				return matchesJsonObject(value, hit.toTag(new CompoundTag()));
			}
			return false;
		});
	}

	private static BlockState raycastBlockState(ClientWorld world, LivingEntity entity) {
		if (entity != null) {
			MinecraftClient client = MinecraftClient.getInstance();
			if (entity == client.player) {
				HitResult result = client.crosshairTarget;
				if (result.getType() == HitResult.Type.BLOCK) {
					BlockHitResult blockResult = (BlockHitResult) result;
					return world.getBlockState(blockResult.getBlockPos());
				}
			}
		}
		return Blocks.AIR.getDefaultState();
	}

	private static Entity raycastEntity(ClientWorld world, LivingEntity entity) {
		if (entity != null) {
			MinecraftClient client = MinecraftClient.getInstance();
			if (entity == client.player) {
				HitResult result = client.crosshairTarget;
				if (result.getType() == HitResult.Type.ENTITY) {
					EntityHitResult entityHitResult = (EntityHitResult) result;
					return entityHitResult.getEntity();
				}
			}
		}
		return null;
	}

	private static <T> void register(String key, Class<T> clazz, CustomModelPredicateFunction<T> func) {
		if (clazz == Float.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new FloatCustomModelPredicate((CustomModelPredicateFunction<Float>) func));
		} else if (clazz == Integer.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new IntegerCustomModelPredicate((CustomModelPredicateFunction<Integer>) func));
		} else if (clazz == Boolean.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new BooleanCustomModelPredicate((CustomModelPredicateFunction<Boolean>) func));
		} else if (clazz == String.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new StringCustomModelPredicate((CustomModelPredicateFunction<String>) func));
		} else if (clazz == Pattern.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new PatternCustomModelPredicate((CustomModelPredicateFunction<Pattern>) func));
		} else if (clazz == CompoundTag.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new CompoundTagCustomModelPredicate((CustomModelPredicateFunction<CompoundTag>) func));
		} else if (clazz == Identifier.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new IdentifierCustomModelPredicate((CustomModelPredicateFunction<Identifier>) func));
		} else if (clazz == Range.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new FloatRangeCustomModelPredicate((CustomModelPredicateFunction<Range<Float>>) func));
		} else if (clazz == JsonObject.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new JsonObjectCustomModelPredicate((CustomModelPredicateFunction<JsonObject>) func));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private static boolean matchesJsonObject(JsonObject object, CompoundTag tag) {
		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			String key = entry.getKey();
			JsonElement element = entry.getValue();
			if (element.isJsonNull()) {
				if (tag.contains(key)) {
					return false;
				}
			} else {
				if (!tag.contains(key) || !matchesJsonElement(element, tag.get(key))) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean matchesJsonArray(JsonArray array, AbstractListTag list) {
		outer:
		for (JsonElement element : array) {
			for (Object object : list) {
				if (object instanceof Tag) {
					Tag tag = (Tag) object;
					if (matchesJsonElement(element, tag)) {
						continue outer;
					}
				}
			}
			return false;
		}
		return true;
	}

	private static boolean matchesJsonElement(JsonElement element, Tag tag) {
		if (element.isJsonObject()) {
			return tag.getType() == 10 && matchesJsonObject(element.getAsJsonObject(), (CompoundTag) tag);
		} else if (element.isJsonArray()) {
			return tag instanceof AbstractListTag && matchesJsonArray(element.getAsJsonArray(), (AbstractListTag) tag);
		} else {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (tag instanceof AbstractNumberTag) {
				AbstractNumberTag number = (AbstractNumberTag) tag;
				boolean isInt = false;
				if (tag instanceof ByteTag || tag instanceof ShortTag || tag instanceof IntTag || tag instanceof LongTag) {
					isInt = true;
				}
				if (primitive.isBoolean()) {
					return isInt && primitive.getAsBoolean() == (number.getInt() == 1);
				} else if (primitive.isNumber()) {
					if (isInt) {
						return primitive.getAsLong() == number.getLong();
					} else {
						return primitive.getAsDouble() == number.getDouble();
					}
				} else if (primitive.isString()) {
					if (isInt) {
						Range r = parseRange(Long.class, primitive.getAsString());
						return r != null && r.contains(number.getLong());
					} else {
						Range r = parseRange(Double.class, primitive.getAsString());
						return r != null && r.contains(number.getDouble());
					}
				}
			} else if (tag instanceof StringTag) {
				if (primitive.isString()) {
					return primitive.getAsString().equals(((StringTag) tag).asString());
				}
			}
		}
		return false;
	}

	private static Range parseRange(Class clazz, String s) {
		try {
			if (s.startsWith("<=")) {
				return Range.upTo(parseNumber(clazz, s.substring(2)), BoundType.CLOSED);
			} else if (s.startsWith("<")) {
				return Range.upTo(parseNumber(clazz, s.substring(1)), BoundType.OPEN);
			} else if (s.startsWith(">=")) {
				return Range.downTo(parseNumber(clazz, s.substring(2)), BoundType.CLOSED);
			} else if (s.startsWith(">")) {
				return Range.downTo(parseNumber(clazz, s.substring(1)), BoundType.CLOSED);
			} else if (s.startsWith("[") || s.startsWith("(")) {
				String[] parts = s.split("\\.\\.");
				if (parts.length == 2) {
					BoundType lt = parts[0].startsWith("[") ? BoundType.CLOSED : BoundType.OPEN;
					BoundType rt = parts[1].endsWith("[") ? BoundType.CLOSED : BoundType.OPEN;
					return Range.range(parseNumber(clazz, parts[0].substring(1)), lt, parseNumber(clazz, parts[1].substring(0, parts[1].length() - 1)), rt);
				}
			} else {
				String[] parts = s.split("\\.\\.");
				if (parts.length == 2) {
					return Range.closed(parseNumber(clazz, parts[0]), parseNumber(clazz, parts[1]));
				}
			}
			return Range.singleton(parseNumber(clazz, s));
		} catch (Exception e) {
		}
		return null;
	}

	private static <T extends Comparable> T parseNumber(Class<T> clazz, String s) {
		if (clazz == Double.class) {
			return (T) Double.valueOf(s);
		} else if (clazz == Float.class) {
			return (T) Float.valueOf(s);
		} else if (clazz == Long.class) {
			return (T) Long.valueOf(s);
		}
		throw new UnsupportedOperationException();
	}
/*
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
	}*/

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

	public static class FloatRangeCustomModelPredicate extends CustomModelPredicate<Range<Float>> {

		public FloatRangeCustomModelPredicate(CustomModelPredicateFunction<Range<Float>> function) {
			super(function);
		}

		@Override
		public Range<Float> parseType(JsonElement element) {
			String s = element.getAsString();
			if (s.startsWith("<=")) {
				return Range.upTo(Float.parseFloat(s.substring(2)), BoundType.CLOSED);
			} else if (s.startsWith("<")) {
				return Range.upTo(Float.parseFloat(s.substring(1)), BoundType.OPEN);
			} else if (s.startsWith(">=")) {
				return Range.downTo(Float.parseFloat(s.substring(2)), BoundType.CLOSED);
			} else if (s.startsWith(">")) {
				return Range.downTo(Float.parseFloat(s.substring(1)), BoundType.CLOSED);
			} else if (s.startsWith("[") || s.startsWith("(")) {
				String[] parts = s.split("\\.\\.");
				if (parts.length == 2) {
					BoundType lt = parts[0].startsWith("[") ? BoundType.CLOSED : BoundType.OPEN;
					BoundType rt = parts[1].endsWith("[") ? BoundType.CLOSED : BoundType.OPEN;
					return Range.range(Float.parseFloat(parts[0].substring(1)), lt, Float.parseFloat(parts[1].substring(0, parts[1].length() - 1)), rt);
				}
			} else {
				String[] parts = s.split("\\.\\.");
				if (parts.length == 2) {
					return Range.closed(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
				}
			}
			return Range.singleton(Float.parseFloat(s));
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

	public static class PatternCustomModelPredicate extends CustomModelPredicate<Pattern> {

		public PatternCustomModelPredicate(CustomModelPredicateFunction<Pattern> function) {
			super(function);
		}

		@Override
		public Pattern parseType(JsonElement element) {
			return Pattern.compile(element.getAsString());
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

	public static class JsonObjectCustomModelPredicate extends CustomModelPredicate<JsonObject> {

		public JsonObjectCustomModelPredicate(CustomModelPredicateFunction<JsonObject> function) {
			super(function);
		}

		@Override
		public JsonObject parseType(JsonElement element) {
			return element.getAsJsonObject();
		}
	}

	public interface CustomModelPredicateFunction<T> {
		public boolean matches(ItemStack stack, ClientWorld world, LivingEntity entity, T value);
	}
}