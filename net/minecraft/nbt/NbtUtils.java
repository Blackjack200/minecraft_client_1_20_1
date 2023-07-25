package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

public final class NbtUtils {
   private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt((listtag) -> listtag.getInt(1)).thenComparingInt((listtag) -> listtag.getInt(0)).thenComparingInt((listtag) -> listtag.getInt(2));
   private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble((listtag) -> listtag.getDouble(1)).thenComparingDouble((listtag) -> listtag.getDouble(0)).thenComparingDouble((listtag) -> listtag.getDouble(2));
   public static final String SNBT_DATA_TAG = "data";
   private static final char PROPERTIES_START = '{';
   private static final char PROPERTIES_END = '}';
   private static final String ELEMENT_SEPARATOR = ",";
   private static final char KEY_VALUE_SEPARATOR = ':';
   private static final Splitter COMMA_SPLITTER = Splitter.on(",");
   private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int INDENT = 2;
   private static final int NOT_FOUND = -1;

   private NbtUtils() {
   }

   @Nullable
   public static GameProfile readGameProfile(CompoundTag compoundtag) {
      String s = null;
      UUID uuid = null;
      if (compoundtag.contains("Name", 8)) {
         s = compoundtag.getString("Name");
      }

      if (compoundtag.hasUUID("Id")) {
         uuid = compoundtag.getUUID("Id");
      }

      try {
         GameProfile gameprofile = new GameProfile(uuid, s);
         if (compoundtag.contains("Properties", 10)) {
            CompoundTag compoundtag1 = compoundtag.getCompound("Properties");

            for(String s1 : compoundtag1.getAllKeys()) {
               ListTag listtag = compoundtag1.getList(s1, 10);

               for(int i = 0; i < listtag.size(); ++i) {
                  CompoundTag compoundtag2 = listtag.getCompound(i);
                  String s2 = compoundtag2.getString("Value");
                  if (compoundtag2.contains("Signature", 8)) {
                     gameprofile.getProperties().put(s1, new com.mojang.authlib.properties.Property(s1, s2, compoundtag2.getString("Signature")));
                  } else {
                     gameprofile.getProperties().put(s1, new com.mojang.authlib.properties.Property(s1, s2));
                  }
               }
            }
         }

         return gameprofile;
      } catch (Throwable var11) {
         return null;
      }
   }

   public static CompoundTag writeGameProfile(CompoundTag compoundtag, GameProfile gameprofile) {
      if (!StringUtil.isNullOrEmpty(gameprofile.getName())) {
         compoundtag.putString("Name", gameprofile.getName());
      }

      if (gameprofile.getId() != null) {
         compoundtag.putUUID("Id", gameprofile.getId());
      }

      if (!gameprofile.getProperties().isEmpty()) {
         CompoundTag compoundtag1 = new CompoundTag();

         for(String s : gameprofile.getProperties().keySet()) {
            ListTag listtag = new ListTag();

            for(com.mojang.authlib.properties.Property property : gameprofile.getProperties().get(s)) {
               CompoundTag compoundtag2 = new CompoundTag();
               compoundtag2.putString("Value", property.getValue());
               if (property.hasSignature()) {
                  compoundtag2.putString("Signature", property.getSignature());
               }

               listtag.add(compoundtag2);
            }

            compoundtag1.put(s, listtag);
         }

         compoundtag.put("Properties", compoundtag1);
      }

      return compoundtag;
   }

   @VisibleForTesting
   public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag tag1, boolean flag) {
      if (tag == tag1) {
         return true;
      } else if (tag == null) {
         return true;
      } else if (tag1 == null) {
         return false;
      } else if (!tag.getClass().equals(tag1.getClass())) {
         return false;
      } else if (tag instanceof CompoundTag) {
         CompoundTag compoundtag = (CompoundTag)tag;
         CompoundTag compoundtag1 = (CompoundTag)tag1;

         for(String s : compoundtag.getAllKeys()) {
            Tag tag2 = compoundtag.get(s);
            if (!compareNbt(tag2, compoundtag1.get(s), flag)) {
               return false;
            }
         }

         return true;
      } else if (tag instanceof ListTag && flag) {
         ListTag listtag = (ListTag)tag;
         ListTag listtag1 = (ListTag)tag1;
         if (listtag.isEmpty()) {
            return listtag1.isEmpty();
         } else {
            for(int i = 0; i < listtag.size(); ++i) {
               Tag tag3 = listtag.get(i);
               boolean flag1 = false;

               for(int j = 0; j < listtag1.size(); ++j) {
                  if (compareNbt(tag3, listtag1.get(j), flag)) {
                     flag1 = true;
                     break;
                  }
               }

               if (!flag1) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return tag.equals(tag1);
      }
   }

   public static IntArrayTag createUUID(UUID uuid) {
      return new IntArrayTag(UUIDUtil.uuidToIntArray(uuid));
   }

   public static UUID loadUUID(Tag tag) {
      if (tag.getType() != IntArrayTag.TYPE) {
         throw new IllegalArgumentException("Expected UUID-Tag to be of type " + IntArrayTag.TYPE.getName() + ", but found " + tag.getType().getName() + ".");
      } else {
         int[] aint = ((IntArrayTag)tag).getAsIntArray();
         if (aint.length != 4) {
            throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + aint.length + ".");
         } else {
            return UUIDUtil.uuidFromIntArray(aint);
         }
      }
   }

   public static BlockPos readBlockPos(CompoundTag compoundtag) {
      return new BlockPos(compoundtag.getInt("X"), compoundtag.getInt("Y"), compoundtag.getInt("Z"));
   }

   public static CompoundTag writeBlockPos(BlockPos blockpos) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putInt("X", blockpos.getX());
      compoundtag.putInt("Y", blockpos.getY());
      compoundtag.putInt("Z", blockpos.getZ());
      return compoundtag;
   }

   public static BlockState readBlockState(HolderGetter<Block> holdergetter, CompoundTag compoundtag) {
      if (!compoundtag.contains("Name", 8)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         ResourceLocation resourcelocation = new ResourceLocation(compoundtag.getString("Name"));
         Optional<? extends Holder<Block>> optional = holdergetter.get(ResourceKey.create(Registries.BLOCK, resourcelocation));
         if (optional.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
         } else {
            Block block = optional.get().value();
            BlockState blockstate = block.defaultBlockState();
            if (compoundtag.contains("Properties", 10)) {
               CompoundTag compoundtag1 = compoundtag.getCompound("Properties");
               StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();

               for(String s : compoundtag1.getAllKeys()) {
                  Property<?> property = statedefinition.getProperty(s);
                  if (property != null) {
                     blockstate = setValueHelper(blockstate, property, s, compoundtag1, compoundtag);
                  }
               }
            }

            return blockstate;
         }
      }
   }

   private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S stateholder, Property<T> property, String s, CompoundTag compoundtag, CompoundTag compoundtag1) {
      Optional<T> optional = property.getValue(compoundtag.getString(s));
      if (optional.isPresent()) {
         return stateholder.setValue(property, optional.get());
      } else {
         LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", s, compoundtag.getString(s), compoundtag1.toString());
         return stateholder;
      }
   }

   public static CompoundTag writeBlockState(BlockState blockstate) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("Name", BuiltInRegistries.BLOCK.getKey(blockstate.getBlock()).toString());
      ImmutableMap<Property<?>, Comparable<?>> immutablemap = blockstate.getValues();
      if (!immutablemap.isEmpty()) {
         CompoundTag compoundtag1 = new CompoundTag();

         for(Map.Entry<Property<?>, Comparable<?>> map_entry : immutablemap.entrySet()) {
            Property<?> property = map_entry.getKey();
            compoundtag1.putString(property.getName(), getName(property, map_entry.getValue()));
         }

         compoundtag.put("Properties", compoundtag1);
      }

      return compoundtag;
   }

   public static CompoundTag writeFluidState(FluidState fluidstate) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("Name", BuiltInRegistries.FLUID.getKey(fluidstate.getType()).toString());
      ImmutableMap<Property<?>, Comparable<?>> immutablemap = fluidstate.getValues();
      if (!immutablemap.isEmpty()) {
         CompoundTag compoundtag1 = new CompoundTag();

         for(Map.Entry<Property<?>, Comparable<?>> map_entry : immutablemap.entrySet()) {
            Property<?> property = map_entry.getKey();
            compoundtag1.putString(property.getName(), getName(property, map_entry.getValue()));
         }

         compoundtag.put("Properties", compoundtag1);
      }

      return compoundtag;
   }

   private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
      return property.getName((T)comparable);
   }

   public static String prettyPrint(Tag tag) {
      return prettyPrint(tag, false);
   }

   public static String prettyPrint(Tag tag, boolean flag) {
      return prettyPrint(new StringBuilder(), tag, 0, flag).toString();
   }

   public static StringBuilder prettyPrint(StringBuilder stringbuilder, Tag tag, int i, boolean flag) {
      switch (tag.getId()) {
         case 0:
            break;
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 8:
            stringbuilder.append((Object)tag);
            break;
         case 7:
            ByteArrayTag bytearraytag = (ByteArrayTag)tag;
            byte[] abyte = bytearraytag.getAsByteArray();
            int j = abyte.length;
            indent(i, stringbuilder).append("byte[").append(j).append("] {\n");
            if (!flag) {
               indent(i + 1, stringbuilder).append(" // Skipped, supply withBinaryBlobs true");
            } else {
               indent(i + 1, stringbuilder);

               for(int k = 0; k < abyte.length; ++k) {
                  if (k != 0) {
                     stringbuilder.append(',');
                  }

                  if (k % 16 == 0 && k / 16 > 0) {
                     stringbuilder.append('\n');
                     if (k < abyte.length) {
                        indent(i + 1, stringbuilder);
                     }
                  } else if (k != 0) {
                     stringbuilder.append(' ');
                  }

                  stringbuilder.append(String.format(Locale.ROOT, "0x%02X", abyte[k] & 255));
               }
            }

            stringbuilder.append('\n');
            indent(i, stringbuilder).append('}');
            break;
         case 9:
            ListTag listtag = (ListTag)tag;
            int l = listtag.size();
            int i1 = listtag.getElementType();
            String s = i1 == 0 ? "undefined" : TagTypes.getType(i1).getPrettyName();
            indent(i, stringbuilder).append("list<").append(s).append(">[").append(l).append("] [");
            if (l != 0) {
               stringbuilder.append('\n');
            }

            for(int j1 = 0; j1 < l; ++j1) {
               if (j1 != 0) {
                  stringbuilder.append(",\n");
               }

               indent(i + 1, stringbuilder);
               prettyPrint(stringbuilder, listtag.get(j1), i + 1, flag);
            }

            if (l != 0) {
               stringbuilder.append('\n');
            }

            indent(i, stringbuilder).append(']');
            break;
         case 10:
            CompoundTag compoundtag = (CompoundTag)tag;
            List<String> list = Lists.newArrayList(compoundtag.getAllKeys());
            Collections.sort(list);
            indent(i, stringbuilder).append('{');
            if (stringbuilder.length() - stringbuilder.lastIndexOf("\n") > 2 * (i + 1)) {
               stringbuilder.append('\n');
               indent(i + 1, stringbuilder);
            }

            int k2 = list.stream().mapToInt(String::length).max().orElse(0);
            String s1 = Strings.repeat(" ", k2);

            for(int l2 = 0; l2 < list.size(); ++l2) {
               if (l2 != 0) {
                  stringbuilder.append(",\n");
               }

               String s2 = list.get(l2);
               indent(i + 1, stringbuilder).append('"').append(s2).append('"').append((CharSequence)s1, 0, s1.length() - s2.length()).append(": ");
               prettyPrint(stringbuilder, compoundtag.get(s2), i + 1, flag);
            }

            if (!list.isEmpty()) {
               stringbuilder.append('\n');
            }

            indent(i, stringbuilder).append('}');
            break;
         case 11:
            IntArrayTag intarraytag = (IntArrayTag)tag;
            int[] aint = intarraytag.getAsIntArray();
            int k1 = 0;

            for(int l1 : aint) {
               k1 = Math.max(k1, String.format(Locale.ROOT, "%X", l1).length());
            }

            int i2 = aint.length;
            indent(i, stringbuilder).append("int[").append(i2).append("] {\n");
            if (!flag) {
               indent(i + 1, stringbuilder).append(" // Skipped, supply withBinaryBlobs true");
            } else {
               indent(i + 1, stringbuilder);

               for(int j2 = 0; j2 < aint.length; ++j2) {
                  if (j2 != 0) {
                     stringbuilder.append(',');
                  }

                  if (j2 % 16 == 0 && j2 / 16 > 0) {
                     stringbuilder.append('\n');
                     if (j2 < aint.length) {
                        indent(i + 1, stringbuilder);
                     }
                  } else if (j2 != 0) {
                     stringbuilder.append(' ');
                  }

                  stringbuilder.append(String.format(Locale.ROOT, "0x%0" + k1 + "X", aint[j2]));
               }
            }

            stringbuilder.append('\n');
            indent(i, stringbuilder).append('}');
            break;
         case 12:
            LongArrayTag longarraytag = (LongArrayTag)tag;
            long[] along = longarraytag.getAsLongArray();
            long i3 = 0L;

            for(long j3 : along) {
               i3 = Math.max(i3, (long)String.format(Locale.ROOT, "%X", j3).length());
            }

            long k3 = (long)along.length;
            indent(i, stringbuilder).append("long[").append(k3).append("] {\n");
            if (!flag) {
               indent(i + 1, stringbuilder).append(" // Skipped, supply withBinaryBlobs true");
            } else {
               indent(i + 1, stringbuilder);

               for(int l3 = 0; l3 < along.length; ++l3) {
                  if (l3 != 0) {
                     stringbuilder.append(',');
                  }

                  if (l3 % 16 == 0 && l3 / 16 > 0) {
                     stringbuilder.append('\n');
                     if (l3 < along.length) {
                        indent(i + 1, stringbuilder);
                     }
                  } else if (l3 != 0) {
                     stringbuilder.append(' ');
                  }

                  stringbuilder.append(String.format(Locale.ROOT, "0x%0" + i3 + "X", along[l3]));
               }
            }

            stringbuilder.append('\n');
            indent(i, stringbuilder).append('}');
            break;
         default:
            stringbuilder.append("<UNKNOWN :(>");
      }

      return stringbuilder;
   }

   private static StringBuilder indent(int i, StringBuilder stringbuilder) {
      int j = stringbuilder.lastIndexOf("\n") + 1;
      int k = stringbuilder.length() - j;

      for(int l = 0; l < 2 * i - k; ++l) {
         stringbuilder.append(' ');
      }

      return stringbuilder;
   }

   public static Component toPrettyComponent(Tag tag) {
      return (new TextComponentTagVisitor("", 0)).visit(tag);
   }

   public static String structureToSnbt(CompoundTag compoundtag) {
      return (new SnbtPrinterTagVisitor()).visit(packStructureTemplate(compoundtag));
   }

   public static CompoundTag snbtToStructure(String s) throws CommandSyntaxException {
      return unpackStructureTemplate(TagParser.parseTag(s));
   }

   @VisibleForTesting
   static CompoundTag packStructureTemplate(CompoundTag compoundtag) {
      boolean flag = compoundtag.contains("palettes", 9);
      ListTag listtag;
      if (flag) {
         listtag = compoundtag.getList("palettes", 9).getList(0);
      } else {
         listtag = compoundtag.getList("palette", 10);
      }

      ListTag listtag2 = listtag.stream().map(CompoundTag.class::cast).map(NbtUtils::packBlockState).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new));
      compoundtag.put("palette", listtag2);
      if (flag) {
         ListTag listtag3 = new ListTag();
         ListTag listtag4 = compoundtag.getList("palettes", 9);
         listtag4.stream().map(ListTag.class::cast).forEach((listtag11) -> {
            CompoundTag compoundtag4 = new CompoundTag();

            for(int i = 0; i < listtag11.size(); ++i) {
               compoundtag4.putString(listtag2.getString(i), packBlockState(listtag11.getCompound(i)));
            }

            listtag3.add(compoundtag4);
         });
         compoundtag.put("palettes", listtag3);
      }

      if (compoundtag.contains("entities", 9)) {
         ListTag listtag5 = compoundtag.getList("entities", 10);
         ListTag listtag6 = listtag5.stream().map(CompoundTag.class::cast).sorted(Comparator.comparing((compoundtag3) -> compoundtag3.getList("pos", 6), YXZ_LISTTAG_DOUBLE_COMPARATOR)).collect(Collectors.toCollection(ListTag::new));
         compoundtag.put("entities", listtag6);
      }

      ListTag listtag7 = compoundtag.getList("blocks", 10).stream().map(CompoundTag.class::cast).sorted(Comparator.comparing((compoundtag2) -> compoundtag2.getList("pos", 3), YXZ_LISTTAG_INT_COMPARATOR)).peek((compoundtag1) -> compoundtag1.putString("state", listtag2.getString(compoundtag1.getInt("state")))).collect(Collectors.toCollection(ListTag::new));
      compoundtag.put("data", listtag7);
      compoundtag.remove("blocks");
      return compoundtag;
   }

   @VisibleForTesting
   static CompoundTag unpackStructureTemplate(CompoundTag compoundtag) {
      ListTag listtag = compoundtag.getList("palette", 8);
      Map<String, Tag> map = listtag.stream().map(StringTag.class::cast).map(StringTag::getAsString).collect(ImmutableMap.toImmutableMap(Function.identity(), NbtUtils::unpackBlockState));
      if (compoundtag.contains("palettes", 9)) {
         compoundtag.put("palettes", compoundtag.getList("palettes", 10).stream().map(CompoundTag.class::cast).map((compoundtag2) -> map.keySet().stream().map(compoundtag2::getString).map(NbtUtils::unpackBlockState).collect(Collectors.toCollection(ListTag::new))).collect(Collectors.toCollection(ListTag::new)));
         compoundtag.remove("palette");
      } else {
         compoundtag.put("palette", map.values().stream().collect(Collectors.toCollection(ListTag::new)));
      }

      if (compoundtag.contains("data", 9)) {
         Object2IntMap<String> object2intmap = new Object2IntOpenHashMap<>();
         object2intmap.defaultReturnValue(-1);

         for(int i = 0; i < listtag.size(); ++i) {
            object2intmap.put(listtag.getString(i), i);
         }

         ListTag listtag1 = compoundtag.getList("data", 10);

         for(int j = 0; j < listtag1.size(); ++j) {
            CompoundTag compoundtag1 = listtag1.getCompound(j);
            String s = compoundtag1.getString("state");
            int k = object2intmap.getInt(s);
            if (k == -1) {
               throw new IllegalStateException("Entry " + s + " missing from palette");
            }

            compoundtag1.putInt("state", k);
         }

         compoundtag.put("blocks", listtag1);
         compoundtag.remove("data");
      }

      return compoundtag;
   }

   @VisibleForTesting
   static String packBlockState(CompoundTag compoundtag) {
      StringBuilder stringbuilder = new StringBuilder(compoundtag.getString("Name"));
      if (compoundtag.contains("Properties", 10)) {
         CompoundTag compoundtag1 = compoundtag.getCompound("Properties");
         String s = compoundtag1.getAllKeys().stream().sorted().map((s1) -> s1 + ":" + compoundtag1.get(s1).getAsString()).collect(Collectors.joining(","));
         stringbuilder.append('{').append(s).append('}');
      }

      return stringbuilder.toString();
   }

   @VisibleForTesting
   static CompoundTag unpackBlockState(String s) {
      CompoundTag compoundtag = new CompoundTag();
      int i = s.indexOf(123);
      String s1;
      if (i >= 0) {
         s1 = s.substring(0, i);
         CompoundTag compoundtag1 = new CompoundTag();
         if (i + 2 <= s.length()) {
            String s2 = s.substring(i + 1, s.indexOf(125, i));
            COMMA_SPLITTER.split(s2).forEach((s5) -> {
               List<String> list = COLON_SPLITTER.splitToList(s5);
               if (list.size() == 2) {
                  compoundtag1.putString(list.get(0), list.get(1));
               } else {
                  LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", (Object)s);
               }

            });
            compoundtag.put("Properties", compoundtag1);
         }
      } else {
         s1 = s;
      }

      compoundtag.putString("Name", s1);
      return compoundtag;
   }

   public static CompoundTag addCurrentDataVersion(CompoundTag compoundtag) {
      int i = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
      return addDataVersion(compoundtag, i);
   }

   public static CompoundTag addDataVersion(CompoundTag compoundtag, int i) {
      compoundtag.putInt("DataVersion", i);
      return compoundtag;
   }

   public static int getDataVersion(CompoundTag compoundtag, int i) {
      return compoundtag.contains("DataVersion", 99) ? compoundtag.getInt("DataVersion") : i;
   }
}
