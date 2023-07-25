package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class DebugPackets {
   private static final Logger LOGGER = LogUtils.getLogger();

   public static void sendGameTestAddMarker(ServerLevel serverlevel, BlockPos blockpos, String s, int i, int j) {
      FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
      friendlybytebuf.writeBlockPos(blockpos);
      friendlybytebuf.writeInt(i);
      friendlybytebuf.writeUtf(s);
      friendlybytebuf.writeInt(j);
      sendPacketToAllPlayers(serverlevel, friendlybytebuf, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER);
   }

   public static void sendGameTestClearPacket(ServerLevel serverlevel) {
      FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.buffer());
      sendPacketToAllPlayers(serverlevel, friendlybytebuf, ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR);
   }

   public static void sendPoiPacketsForChunk(ServerLevel serverlevel, ChunkPos chunkpos) {
   }

   public static void sendPoiAddedPacket(ServerLevel serverlevel, BlockPos blockpos) {
      sendVillageSectionsPacket(serverlevel, blockpos);
   }

   public static void sendPoiRemovedPacket(ServerLevel serverlevel, BlockPos blockpos) {
      sendVillageSectionsPacket(serverlevel, blockpos);
   }

   public static void sendPoiTicketCountPacket(ServerLevel serverlevel, BlockPos blockpos) {
      sendVillageSectionsPacket(serverlevel, blockpos);
   }

   private static void sendVillageSectionsPacket(ServerLevel serverlevel, BlockPos blockpos) {
   }

   public static void sendPathFindingPacket(Level level, Mob mob, @Nullable Path path, float f) {
   }

   public static void sendNeighborsUpdatePacket(Level level, BlockPos blockpos) {
   }

   public static void sendStructurePacket(WorldGenLevel worldgenlevel, StructureStart structurestart) {
   }

   public static void sendGoalSelector(Level level, Mob mob, GoalSelector goalselector) {
      if (level instanceof ServerLevel) {
         ;
      }
   }

   public static void sendRaids(ServerLevel serverlevel, Collection<Raid> collection) {
   }

   public static void sendEntityBrain(LivingEntity livingentity) {
   }

   public static void sendBeeInfo(Bee bee) {
   }

   public static void sendGameEventInfo(Level level, GameEvent gameevent, Vec3 vec3) {
   }

   public static void sendGameEventListenerInfo(Level level, GameEventListener gameeventlistener) {
   }

   public static void sendHiveInfo(Level level, BlockPos blockpos, BlockState blockstate, BeehiveBlockEntity beehiveblockentity) {
   }

   private static void writeBrain(LivingEntity livingentity, FriendlyByteBuf friendlybytebuf) {
      Brain<?> brain = livingentity.getBrain();
      long i = livingentity.level().getGameTime();
      if (livingentity instanceof InventoryCarrier) {
         Container container = ((InventoryCarrier)livingentity).getInventory();
         friendlybytebuf.writeUtf(container.isEmpty() ? "" : container.toString());
      } else {
         friendlybytebuf.writeUtf("");
      }

      friendlybytebuf.writeOptional(brain.hasMemoryValue(MemoryModuleType.PATH) ? brain.getMemory(MemoryModuleType.PATH) : Optional.empty(), (friendlybytebuf3, path) -> path.writeToStream(friendlybytebuf3));
      if (livingentity instanceof Villager villager) {
         boolean flag = villager.wantsToSpawnGolem(i);
         friendlybytebuf.writeBoolean(flag);
      } else {
         friendlybytebuf.writeBoolean(false);
      }

      if (livingentity.getType() == EntityType.WARDEN) {
         Warden warden = (Warden)livingentity;
         friendlybytebuf.writeInt(warden.getClientAngerLevel());
      } else {
         friendlybytebuf.writeInt(-1);
      }

      friendlybytebuf.writeCollection(brain.getActiveActivities(), (friendlybytebuf2, activity) -> friendlybytebuf2.writeUtf(activity.getName()));
      Set<String> set = brain.getRunningBehaviors().stream().map(BehaviorControl::debugString).collect(Collectors.toSet());
      friendlybytebuf.writeCollection(set, FriendlyByteBuf::writeUtf);
      friendlybytebuf.writeCollection(getMemoryDescriptions(livingentity, i), (friendlybytebuf1, s2) -> {
         String s3 = StringUtil.truncateStringIfNecessary(s2, 255, true);
         friendlybytebuf1.writeUtf(s3);
      });
      if (livingentity instanceof Villager) {
         Set<BlockPos> set1 = Stream.of(MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT).map(brain::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
         friendlybytebuf.writeCollection(set1, FriendlyByteBuf::writeBlockPos);
      } else {
         friendlybytebuf.writeVarInt(0);
      }

      if (livingentity instanceof Villager) {
         Set<BlockPos> set2 = Stream.of(MemoryModuleType.POTENTIAL_JOB_SITE).map(brain::getMemory).flatMap(Optional::stream).map(GlobalPos::pos).collect(Collectors.toSet());
         friendlybytebuf.writeCollection(set2, FriendlyByteBuf::writeBlockPos);
      } else {
         friendlybytebuf.writeVarInt(0);
      }

      if (livingentity instanceof Villager) {
         Map<UUID, Object2IntMap<GossipType>> map = ((Villager)livingentity).getGossips().getGossipEntries();
         List<String> list = Lists.newArrayList();
         map.forEach((uuid, object2intmap) -> {
            String s = DebugEntityNameGenerator.getEntityName(uuid);
            object2intmap.forEach((gossiptype, integer) -> list.add(s + ": " + gossiptype + ": " + integer));
         });
         friendlybytebuf.writeCollection(list, FriendlyByteBuf::writeUtf);
      } else {
         friendlybytebuf.writeVarInt(0);
      }

   }

   private static List<String> getMemoryDescriptions(LivingEntity livingentity, long i) {
      Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> map = livingentity.getBrain().getMemories();
      List<String> list = Lists.newArrayList();

      for(Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> map_entry : map.entrySet()) {
         MemoryModuleType<?> memorymoduletype = map_entry.getKey();
         Optional<? extends ExpirableValue<?>> optional = map_entry.getValue();
         String s;
         if (optional.isPresent()) {
            ExpirableValue<?> expirablevalue = optional.get();
            Object object = expirablevalue.getValue();
            if (memorymoduletype == MemoryModuleType.HEARD_BELL_TIME) {
               long j = i - (Long)object;
               s = j + " ticks ago";
            } else if (expirablevalue.canExpire()) {
               s = getShortDescription((ServerLevel)livingentity.level(), object) + " (ttl: " + expirablevalue.getTimeToLive() + ")";
            } else {
               s = getShortDescription((ServerLevel)livingentity.level(), object);
            }
         } else {
            s = "-";
         }

         list.add(BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memorymoduletype).getPath() + ": " + s);
      }

      list.sort(String::compareTo);
      return list;
   }

   private static String getShortDescription(ServerLevel serverlevel, @Nullable Object object) {
      if (object == null) {
         return "-";
      } else if (object instanceof UUID) {
         return getShortDescription(serverlevel, serverlevel.getEntity((UUID)object));
      } else if (object instanceof LivingEntity) {
         Entity entity = (Entity)object;
         return DebugEntityNameGenerator.getEntityName(entity);
      } else if (object instanceof Nameable) {
         return ((Nameable)object).getName().getString();
      } else if (object instanceof WalkTarget) {
         return getShortDescription(serverlevel, ((WalkTarget)object).getTarget());
      } else if (object instanceof EntityTracker) {
         return getShortDescription(serverlevel, ((EntityTracker)object).getEntity());
      } else if (object instanceof GlobalPos) {
         return getShortDescription(serverlevel, ((GlobalPos)object).pos());
      } else if (object instanceof BlockPosTracker) {
         return getShortDescription(serverlevel, ((BlockPosTracker)object).currentBlockPosition());
      } else if (object instanceof DamageSource) {
         Entity entity1 = ((DamageSource)object).getEntity();
         return entity1 == null ? object.toString() : getShortDescription(serverlevel, entity1);
      } else if (!(object instanceof Collection)) {
         return object.toString();
      } else {
         List<String> list = Lists.newArrayList();

         for(Object object1 : (Iterable)object) {
            list.add(getShortDescription(serverlevel, object1));
         }

         return list.toString();
      }
   }

   private static void sendPacketToAllPlayers(ServerLevel serverlevel, FriendlyByteBuf friendlybytebuf, ResourceLocation resourcelocation) {
      Packet<?> packet = new ClientboundCustomPayloadPacket(resourcelocation, friendlybytebuf);

      for(ServerPlayer serverplayer : serverlevel.players()) {
         serverplayer.connection.send(packet);
      }

   }
}
