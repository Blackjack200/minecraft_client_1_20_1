package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

public class Raids extends SavedData {
   private static final String RAID_FILE_ID = "raids";
   private final Map<Integer, Raid> raidMap = Maps.newHashMap();
   private final ServerLevel level;
   private int nextAvailableID;
   private int tick;

   public Raids(ServerLevel serverlevel) {
      this.level = serverlevel;
      this.nextAvailableID = 1;
      this.setDirty();
   }

   public Raid get(int i) {
      return this.raidMap.get(i);
   }

   public void tick() {
      ++this.tick;
      Iterator<Raid> iterator = this.raidMap.values().iterator();

      while(iterator.hasNext()) {
         Raid raid = iterator.next();
         if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
            raid.stop();
         }

         if (raid.isStopped()) {
            iterator.remove();
            this.setDirty();
         } else {
            raid.tick();
         }
      }

      if (this.tick % 200 == 0) {
         this.setDirty();
      }

      DebugPackets.sendRaids(this.level, this.raidMap.values());
   }

   public static boolean canJoinRaid(Raider raider, Raid raid) {
      if (raider != null && raid != null && raid.getLevel() != null) {
         return raider.isAlive() && raider.canJoinRaid() && raider.getNoActionTime() <= 2400 && raider.level().dimensionType() == raid.getLevel().dimensionType();
      } else {
         return false;
      }
   }

   @Nullable
   public Raid createOrExtendRaid(ServerPlayer serverplayer) {
      if (serverplayer.isSpectator()) {
         return null;
      } else if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
         return null;
      } else {
         DimensionType dimensiontype = serverplayer.level().dimensionType();
         if (!dimensiontype.hasRaids()) {
            return null;
         } else {
            BlockPos blockpos = serverplayer.blockPosition();
            List<PoiRecord> list = this.level.getPoiManager().getInRange((holder) -> holder.is(PoiTypeTags.VILLAGE), blockpos, 64, PoiManager.Occupancy.IS_OCCUPIED).toList();
            int i = 0;
            Vec3 vec3 = Vec3.ZERO;

            for(PoiRecord poirecord : list) {
               BlockPos blockpos1 = poirecord.getPos();
               vec3 = vec3.add((double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ());
               ++i;
            }

            BlockPos blockpos2;
            if (i > 0) {
               vec3 = vec3.scale(1.0D / (double)i);
               blockpos2 = BlockPos.containing(vec3);
            } else {
               blockpos2 = blockpos;
            }

            Raid raid = this.getOrCreateRaid(serverplayer.serverLevel(), blockpos2);
            boolean flag = false;
            if (!raid.isStarted()) {
               if (!this.raidMap.containsKey(raid.getId())) {
                  this.raidMap.put(raid.getId(), raid);
               }

               flag = true;
            } else if (raid.getBadOmenLevel() < raid.getMaxBadOmenLevel()) {
               flag = true;
            } else {
               serverplayer.removeEffect(MobEffects.BAD_OMEN);
               serverplayer.connection.send(new ClientboundEntityEventPacket(serverplayer, (byte)43));
            }

            if (flag) {
               raid.absorbBadOmen(serverplayer);
               serverplayer.connection.send(new ClientboundEntityEventPacket(serverplayer, (byte)43));
               if (!raid.hasFirstWaveSpawned()) {
                  serverplayer.awardStat(Stats.RAID_TRIGGER);
                  CriteriaTriggers.BAD_OMEN.trigger(serverplayer);
               }
            }

            this.setDirty();
            return raid;
         }
      }
   }

   private Raid getOrCreateRaid(ServerLevel serverlevel, BlockPos blockpos) {
      Raid raid = serverlevel.getRaidAt(blockpos);
      return raid != null ? raid : new Raid(this.getUniqueId(), serverlevel, blockpos);
   }

   public static Raids load(ServerLevel serverlevel, CompoundTag compoundtag) {
      Raids raids = new Raids(serverlevel);
      raids.nextAvailableID = compoundtag.getInt("NextAvailableID");
      raids.tick = compoundtag.getInt("Tick");
      ListTag listtag = compoundtag.getList("Raids", 10);

      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag1 = listtag.getCompound(i);
         Raid raid = new Raid(serverlevel, compoundtag1);
         raids.raidMap.put(raid.getId(), raid);
      }

      return raids;
   }

   public CompoundTag save(CompoundTag compoundtag) {
      compoundtag.putInt("NextAvailableID", this.nextAvailableID);
      compoundtag.putInt("Tick", this.tick);
      ListTag listtag = new ListTag();

      for(Raid raid : this.raidMap.values()) {
         CompoundTag compoundtag1 = new CompoundTag();
         raid.save(compoundtag1);
         listtag.add(compoundtag1);
      }

      compoundtag.put("Raids", listtag);
      return compoundtag;
   }

   public static String getFileId(Holder<DimensionType> holder) {
      return holder.is(BuiltinDimensionTypes.END) ? "raids_end" : "raids";
   }

   private int getUniqueId() {
      return ++this.nextAvailableID;
   }

   @Nullable
   public Raid getNearbyRaid(BlockPos blockpos, int i) {
      Raid raid = null;
      double d0 = (double)i;

      for(Raid raid1 : this.raidMap.values()) {
         double d1 = raid1.getCenter().distSqr(blockpos);
         if (raid1.isActive() && d1 < d0) {
            raid = raid1;
            d0 = d1;
         }
      }

      return raid;
   }
}
