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

import dev.emi.chime.override.ChimeArmorOverrideLoader;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtElement;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ChimeClient implements ClientModInitializer {
	public static final Map<String, CustomModelPredicate> CUSTOM_MODEL_PREDICATES = Maps.newHashMap();

	@Override
	public void onInitializeClient() {
		MinecraftClient.getInstance().execute(() -> {
			ChimeArmorOverrideLoader.firstLoad();
			((ReloadableResourceManager) MinecraftClient.getInstance().getResourceManager()).registerReloader(new ChimeArmorOverrideLoader());
		});
	}

	static {
		register("count", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			return value.contains((float) stack.getCount());
		});
		register("durability", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			return value.contains((float) stack.getDamage());
		});
		register("nbt", JsonObject.class, (ItemStack stack, ClientWorld world, LivingEntity entity, JsonObject value) -> {
			if (stack.hasTag()) {
				return matchesJsonObject(value, stack.getTag());
			}
			return false;
		});
		register("name", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			if (value.startsWith("/") && value.endsWith("/")) {
				return Pattern.matches(value.substring(1, value.length() - 1), stack.getName().asString());
			} else {
				return value.equals(stack.getName().asString());
			}
		});
		register("hash", HashPredicate.class, (ItemStack stack, ClientWorld world, LivingEntity entity, HashPredicate value) -> {
			return value.matches(stack.getTag());
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
		register("world/raining", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.isRaining() == value.booleanValue();
		});
		register("world/thundering", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			return world != null && world.isThundering() == value;
		});
		register("world/biome/id", Identifier.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Identifier value) -> {
			Biome biome = getBiome(stack, world, entity);
			if (biome != null) {
				return world.getRegistryManager().get(Registry.BIOME_KEY).getId(biome).equals(value);
			}
			return false;
		});
		register("world/biome/precipitation", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			Biome biome = getBiome(stack, world, entity);
			if (biome != null) {
				return biome.getPrecipitation().getName().equals(value);
			}
			return false;
		});
		register("world/biome/temperature", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			Biome biome = getBiome(stack, world, entity);
			if (biome != null) {
				return value.contains(biome.getTemperature());
			}
			return false;
		});
		register("world/biome/downfall", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			Biome biome = getBiome(stack, world, entity);
			if (biome != null) {
				return value.contains(biome.getDownfall());
			}
			return false;
		});
		register("entity/nbt", JsonObject.class, (ItemStack stack, ClientWorld world, LivingEntity entity, JsonObject value) -> {
			if (entity != null) {
				return matchesJsonObject(value, entity.writeNbt(new NbtCompound()));
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
		register("entity/light", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			Entity e = entity;
			if (e == null) {
				e = stack.getHolder();
			}
			return e != null && value.contains((float) world.getLightLevel(new BlockPos(e.getX(), e.getEyeY(), e.getZ())));
		});
		register("entity/block_light", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			Entity e = entity;
			if (e == null) {
				e = stack.getHolder();
			}
			return e != null && value.contains((float) world.getLightLevel(LightType.BLOCK, new BlockPos(e.getX(), e.getEyeY(), e.getZ())));
		});
		register("entity/sky_light", Range.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Range value) -> {
			Entity e = entity;
			if (e == null) {
				e = stack.getHolder();
			}
			return e != null && value.contains((float) world.getLightLevel(LightType.SKY, new BlockPos(e.getX(), e.getEyeY(), e.getZ())));
		});
		register("entity/can_see_sky", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			Entity e = entity;
			if (e == null) {
				e = stack.getHolder();
			}
			if (e != null) {
				BlockPos pos = e.getBlockPos();
				return (world.getTopY(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= e.getEyeY()) == value;
			}
			return false;
		});
		register("entity/hand", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			if (entity == null) {
				return false;
			}
			boolean main = entity.getMainHandStack() == stack;
			boolean off = entity.getOffHandStack() == stack;
			return switch (value) {
				case "main" -> main;
				case "off" -> off;
				case "either" -> main || off;
				case "neither", "none" -> !(main || off);
				default -> false;
			};
		});
		register("entity/slot", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			if (entity == null) {
				return false;
			}
			return switch (value) {
				case "head" -> entity.getEquippedStack(EquipmentSlot.HEAD) == stack;
				case "chest" -> entity.getEquippedStack(EquipmentSlot.CHEST) == stack;
				case "legs" -> entity.getEquippedStack(EquipmentSlot.LEGS) == stack;
				case "feet" -> entity.getEquippedStack(EquipmentSlot.FEET) == stack;
				default -> false;
			};
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
		register("entity/target_block/id", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			BlockState state = raycastBlockState(world, entity);
			if (value.startsWith("#")) {
				Identifier id = new Identifier(value.substring(1));
				net.minecraft.tag.Tag<Block> tag = BlockTags.getTagGroup().getTag(id);
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
		register("entity/target_block/can_mine", Boolean.class, (ItemStack stack, ClientWorld world, LivingEntity entity, Boolean value) -> {
			BlockState state = raycastBlockState(world, entity);
			return value == stack.isSuitableFor(state);
		});
		register("entity/target_entity/id", String.class, (ItemStack stack, ClientWorld world, LivingEntity entity, String value) -> {
			Entity hit = raycastEntity(world, entity);
			if (hit != null) {
				if (value.startsWith("#")) {
					Identifier id = new Identifier(value.substring(1));
					net.minecraft.tag.Tag<EntityType<?>> tag = EntityTypeTags.getTagGroup().getTag(id);
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
				return matchesJsonObject(value, hit.writeNbt(new NbtCompound()));
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

	private static Biome getBiome(ItemStack stack, ClientWorld world, LivingEntity entity) {
		Entity e = entity;
		if (e == null) {
			e = stack.getHolder();
		}
		if (world != null && e != null) {
			return world.getBiome(e.getBlockPos());
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
		} else if (clazz == NbtCompound.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new NbtCompoundCustomModelPredicate((CustomModelPredicateFunction<NbtCompound>) func));
		} else if (clazz == Identifier.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new IdentifierCustomModelPredicate((CustomModelPredicateFunction<Identifier>) func));
		} else if (clazz == Range.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new FloatRangeCustomModelPredicate((CustomModelPredicateFunction<Range<Float>>) func));
		} else if (clazz == JsonObject.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new JsonObjectCustomModelPredicate((CustomModelPredicateFunction<JsonObject>) func));
		} else if (clazz == HashPredicate.class) {
			CUSTOM_MODEL_PREDICATES.put(key, new HashPredicateCustomModelPredicate((CustomModelPredicateFunction<HashPredicate>) func));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private static boolean matchesJsonObject(JsonObject object, NbtCompound tag) {
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

	private static boolean matchesJsonArray(JsonArray array, AbstractNbtList list) {
		// TODO lists can match the same element, but solving this would require permutations
		if (list.size() < array.size()) {
			return false;
		}
		outer:
		for (JsonElement element : array) {
			for (Object object : list) {
				if (object instanceof NbtElement tag) {
					if (matchesJsonElement(element, tag)) {
						continue outer;
					}
				}
			}
			return false;
		}
		return true;
	}

	private static boolean matchesJsonElement(JsonElement element, NbtElement tag) {
		if (element.isJsonObject()) {
			return tag.getType() == 10 && matchesJsonObject(element.getAsJsonObject(), (NbtCompound) tag);
		} else if (element.isJsonArray()) {
			return tag instanceof AbstractNbtList && matchesJsonArray(element.getAsJsonArray(), (AbstractNbtList) tag);
		} else {
			JsonPrimitive primitive = element.getAsJsonPrimitive();
			if (tag instanceof AbstractNbtNumber number) {
				boolean isInt = tag instanceof NbtByte || tag instanceof NbtShort || tag instanceof NbtInt || tag instanceof NbtLong;
				if (primitive.isBoolean()) {
					return isInt && primitive.getAsBoolean() == (number.intValue() == 1);
				} else if (primitive.isNumber()) {
					if (isInt) {
						return primitive.getAsLong() == number.longValue();
					} else {
						return primitive.getAsDouble() == number.doubleValue();
					}
				} else if (primitive.isString()) {
					if (isInt) {
						Range r = parseRange(Long.class, primitive.getAsString());
						return r != null && r.contains(number.longValue());
					} else {
						Range r = parseRange(Double.class, primitive.getAsString());
						return r != null && r.contains(number.doubleValue());
					}
				}
			} else if (tag instanceof NbtString) {
				if (primitive.isString()) {
					String prim = primitive.getAsString();
					String text = ((NbtString) tag).asString();
					if (prim.startsWith("/") && prim.endsWith("/")) {
						return Pattern.matches(prim.substring(1, prim.length() - 1), text);
					} else {
						return prim.equals(text);
					}
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

	public static abstract class CustomModelPredicate<T> {
		private final CustomModelPredicateFunction<T> function;

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
					BoundType rt = parts[1].endsWith("]") ? BoundType.CLOSED : BoundType.OPEN;
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

	public static class NbtCompoundCustomModelPredicate extends CustomModelPredicate<NbtCompound> {

		public NbtCompoundCustomModelPredicate(CustomModelPredicateFunction<NbtCompound> function) {
			super(function);
		}

		@Override
		public NbtCompound parseType(JsonElement element) {
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

	public static class HashPredicateCustomModelPredicate extends CustomModelPredicate<HashPredicate> {

		public HashPredicateCustomModelPredicate(CustomModelPredicateFunction<HashPredicate> function) {
			super(function);
		}

		@Override
		public HashPredicate parseType(JsonElement element) {
			JsonObject object = element.getAsJsonObject();
			int modulo = object.get("modulo").getAsInt();
			Range<Float> value = parseRange(Float.class, object.get("value").getAsString());
			String subTag = "";
			if (object.has("tag")) {
				subTag = object.get("tag").getAsString();
			}
			return new HashPredicate(subTag, modulo, value);
		}
	}

	public interface CustomModelPredicateFunction<T> {
		public boolean matches(ItemStack stack, ClientWorld world, LivingEntity entity, T value);
	}

	public static class HashPredicate {
		public String subTag;
		public int modulo;
		public Range<Float> value;

		public HashPredicate(String subTag, int modulo, Range<Float> value) {
			this.subTag = subTag;
			this.modulo = modulo;
			this.value = value;
		}

		public boolean matches(NbtElement tag) {
			try {
				String[] tags = subTag.split("/");
				for (String t : tags) {
					if (t.length() == 0) {
						continue;
					}
					tag = ((NbtCompound) tag).get(t);
				}
				int i = tag.toString().hashCode();
				i = i % modulo;
				if (i < 0) {
					i += modulo;
				}
				return value.contains((float) i);
			} catch (Exception e) {
			}
			return value.contains(-1f);
		}
	}
}