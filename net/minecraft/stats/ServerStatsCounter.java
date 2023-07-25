package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class ServerStatsCounter extends StatsCounter {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final MinecraftServer server;
   private final File file;
   private final Set<Stat<?>> dirty = Sets.newHashSet();

   public ServerStatsCounter(MinecraftServer minecraftserver, File file) {
      this.server = minecraftserver;
      this.file = file;
      if (file.isFile()) {
         try {
            this.parseLocal(minecraftserver.getFixerUpper(), FileUtils.readFileToString(file));
         } catch (IOException var4) {
            LOGGER.error("Couldn't read statistics file {}", file, var4);
         } catch (JsonParseException var5) {
            LOGGER.error("Couldn't parse statistics file {}", file, var5);
         }
      }

   }

   public void save() {
      try {
         FileUtils.writeStringToFile(this.file, this.toJson());
      } catch (IOException var2) {
         LOGGER.error("Couldn't save stats", (Throwable)var2);
      }

   }

   public void setValue(Player player, Stat<?> stat, int i) {
      super.setValue(player, stat, i);
      this.dirty.add(stat);
   }

   private Set<Stat<?>> getDirty() {
      Set<Stat<?>> set = Sets.newHashSet(this.dirty);
      this.dirty.clear();
      return set;
   }

   public void parseLocal(DataFixer datafixer, String s) {
      try {
         JsonReader jsonreader = new JsonReader(new StringReader(s));

         label47: {
            try {
               jsonreader.setLenient(false);
               JsonElement jsonelement = Streams.parse(jsonreader);
               if (!jsonelement.isJsonNull()) {
                  CompoundTag compoundtag = fromJson(jsonelement.getAsJsonObject());
                  compoundtag = DataFixTypes.STATS.updateToCurrentVersion(datafixer, compoundtag, NbtUtils.getDataVersion(compoundtag, 1343));
                  if (!compoundtag.contains("stats", 10)) {
                     break label47;
                  }

                  CompoundTag compoundtag1 = compoundtag.getCompound("stats");
                  Iterator var7 = compoundtag1.getAllKeys().iterator();

                  while(true) {
                     if (!var7.hasNext()) {
                        break label47;
                     }

                     String s1 = (String)var7.next();
                     if (compoundtag1.contains(s1, 10)) {
                        Util.ifElse(BuiltInRegistries.STAT_TYPE.getOptional(new ResourceLocation(s1)), (stattype) -> {
                           CompoundTag compoundtag3 = compoundtag1.getCompound(s1);

                           for(String s4 : compoundtag3.getAllKeys()) {
                              if (compoundtag3.contains(s4, 99)) {
                                 Util.ifElse(this.getStat(stattype, s4), (stat) -> this.stats.put(stat, compoundtag3.getInt(s4)), () -> LOGGER.warn("Invalid statistic in {}: Don't know what {} is", this.file, s4));
                              } else {
                                 LOGGER.warn("Invalid statistic value in {}: Don't know what {} is for key {}", this.file, compoundtag3.get(s4), s4);
                              }
                           }

                        }, () -> LOGGER.warn("Invalid statistic type in {}: Don't know what {} is", this.file, s1));
                     }
                  }
               }

               LOGGER.error("Unable to parse Stat data from {}", (Object)this.file);
            } catch (Throwable var10) {
               try {
                  jsonreader.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            jsonreader.close();
            return;
         }

         jsonreader.close();
      } catch (IOException | JsonParseException var11) {
         LOGGER.error("Unable to parse Stat data from {}", this.file, var11);
      }

   }

   private <T> Optional<Stat<T>> getStat(StatType<T> stattype, String s) {
      return Optional.ofNullable(ResourceLocation.tryParse(s)).flatMap(stattype.getRegistry()::getOptional).map(stattype::get);
   }

   private static CompoundTag fromJson(JsonObject jsonobject) {
      CompoundTag compoundtag = new CompoundTag();

      for(Map.Entry<String, JsonElement> map_entry : jsonobject.entrySet()) {
         JsonElement jsonelement = map_entry.getValue();
         if (jsonelement.isJsonObject()) {
            compoundtag.put(map_entry.getKey(), fromJson(jsonelement.getAsJsonObject()));
         } else if (jsonelement.isJsonPrimitive()) {
            JsonPrimitive jsonprimitive = jsonelement.getAsJsonPrimitive();
            if (jsonprimitive.isNumber()) {
               compoundtag.putInt(map_entry.getKey(), jsonprimitive.getAsInt());
            }
         }
      }

      return compoundtag;
   }

   protected String toJson() {
      Map<StatType<?>, JsonObject> map = Maps.newHashMap();

      for(Object2IntMap.Entry<Stat<?>> object2intmap_entry : this.stats.object2IntEntrySet()) {
         Stat<?> stat = object2intmap_entry.getKey();
         map.computeIfAbsent(stat.getType(), (stattype) -> new JsonObject()).addProperty(getKey(stat).toString(), object2intmap_entry.getIntValue());
      }

      JsonObject jsonobject = new JsonObject();

      for(Map.Entry<StatType<?>, JsonObject> map_entry : map.entrySet()) {
         jsonobject.add(BuiltInRegistries.STAT_TYPE.getKey(map_entry.getKey()).toString(), map_entry.getValue());
      }

      JsonObject jsonobject1 = new JsonObject();
      jsonobject1.add("stats", jsonobject);
      jsonobject1.addProperty("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
      return jsonobject1.toString();
   }

   private static <T> ResourceLocation getKey(Stat<T> stat) {
      return stat.getType().getRegistry().getKey(stat.getValue());
   }

   public void markAllDirty() {
      this.dirty.addAll(this.stats.keySet());
   }

   public void sendStats(ServerPlayer serverplayer) {
      Object2IntMap<Stat<?>> object2intmap = new Object2IntOpenHashMap<>();

      for(Stat<?> stat : this.getDirty()) {
         object2intmap.put(stat, this.getValue(stat));
      }

      serverplayer.connection.send(new ClientboundAwardStatsPacket(object2intmap));
   }
}
