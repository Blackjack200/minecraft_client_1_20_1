package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;

public class BeeDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final boolean SHOW_GOAL_FOR_ALL_BEES = true;
   private static final boolean SHOW_NAME_FOR_ALL_BEES = true;
   private static final boolean SHOW_HIVE_FOR_ALL_BEES = true;
   private static final boolean SHOW_FLOWER_POS_FOR_ALL_BEES = true;
   private static final boolean SHOW_TRAVEL_TICKS_FOR_ALL_BEES = true;
   private static final boolean SHOW_PATH_FOR_ALL_BEES = false;
   private static final boolean SHOW_GOAL_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_NAME_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_HIVE_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_FLOWER_POS_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_TRAVEL_TICKS_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_PATH_FOR_SELECTED_BEE = true;
   private static final boolean SHOW_HIVE_MEMBERS = true;
   private static final boolean SHOW_BLACKLISTS = true;
   private static final int MAX_RENDER_DIST_FOR_HIVE_OVERLAY = 30;
   private static final int MAX_RENDER_DIST_FOR_BEE_OVERLAY = 30;
   private static final int MAX_TARGETING_DIST = 8;
   private static final int HIVE_TIMEOUT = 20;
   private static final float TEXT_SCALE = 0.02F;
   private static final int WHITE = -1;
   private static final int YELLOW = -256;
   private static final int ORANGE = -23296;
   private static final int GREEN = -16711936;
   private static final int GRAY = -3355444;
   private static final int PINK = -98404;
   private static final int RED = -65536;
   private final Minecraft minecraft;
   private final Map<BlockPos, BeeDebugRenderer.HiveInfo> hives = Maps.newHashMap();
   private final Map<UUID, BeeDebugRenderer.BeeInfo> beeInfosPerEntity = Maps.newHashMap();
   private UUID lastLookedAtUuid;

   public BeeDebugRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void clear() {
      this.hives.clear();
      this.beeInfosPerEntity.clear();
      this.lastLookedAtUuid = null;
   }

   public void addOrUpdateHiveInfo(BeeDebugRenderer.HiveInfo beedebugrenderer_hiveinfo) {
      this.hives.put(beedebugrenderer_hiveinfo.pos, beedebugrenderer_hiveinfo);
   }

   public void addOrUpdateBeeInfo(BeeDebugRenderer.BeeInfo beedebugrenderer_beeinfo) {
      this.beeInfosPerEntity.put(beedebugrenderer_beeinfo.uuid, beedebugrenderer_beeinfo);
   }

   public void removeBeeInfo(int i) {
      this.beeInfosPerEntity.values().removeIf((beedebugrenderer_beeinfo) -> beedebugrenderer_beeinfo.id == i);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      this.clearRemovedHives();
      this.clearRemovedBees();
      this.doRender(posestack, multibuffersource);
      if (!this.minecraft.player.isSpectator()) {
         this.updateLastLookedAtUuid();
      }

   }

   private void clearRemovedBees() {
      this.beeInfosPerEntity.entrySet().removeIf((map_entry) -> this.minecraft.level.getEntity((map_entry.getValue()).id) == null);
   }

   private void clearRemovedHives() {
      long i = this.minecraft.level.getGameTime() - 20L;
      this.hives.entrySet().removeIf((map_entry) -> (map_entry.getValue()).lastSeen < i);
   }

   private void doRender(PoseStack posestack, MultiBufferSource multibuffersource) {
      BlockPos blockpos = this.getCamera().getBlockPosition();
      this.beeInfosPerEntity.values().forEach((beedebugrenderer_beeinfo) -> {
         if (this.isPlayerCloseEnoughToMob(beedebugrenderer_beeinfo)) {
            this.renderBeeInfo(posestack, multibuffersource, beedebugrenderer_beeinfo);
         }

      });
      this.renderFlowerInfos(posestack, multibuffersource);

      for(BlockPos blockpos1 : this.hives.keySet()) {
         if (blockpos.closerThan(blockpos1, 30.0D)) {
            highlightHive(posestack, multibuffersource, blockpos1);
         }
      }

      Map<BlockPos, Set<UUID>> map = this.createHiveBlacklistMap();
      this.hives.values().forEach((beedebugrenderer_hiveinfo) -> {
         if (blockpos.closerThan(beedebugrenderer_hiveinfo.pos, 30.0D)) {
            Set<UUID> set = map.get(beedebugrenderer_hiveinfo.pos);
            this.renderHiveInfo(posestack, multibuffersource, beedebugrenderer_hiveinfo, (Collection<UUID>)(set == null ? Sets.newHashSet() : set));
         }

      });
      this.getGhostHives().forEach((blockpos3, list) -> {
         if (blockpos.closerThan(blockpos3, 30.0D)) {
            this.renderGhostHive(posestack, multibuffersource, blockpos3, list);
         }

      });
   }

   private Map<BlockPos, Set<UUID>> createHiveBlacklistMap() {
      Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
      this.beeInfosPerEntity.values().forEach((beedebugrenderer_beeinfo) -> beedebugrenderer_beeinfo.blacklistedHives.forEach((blockpos) -> map.computeIfAbsent(blockpos, (blockpos1) -> Sets.newHashSet()).add(beedebugrenderer_beeinfo.getUuid())));
      return map;
   }

   private void renderFlowerInfos(PoseStack posestack, MultiBufferSource multibuffersource) {
      Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
      this.beeInfosPerEntity.values().stream().filter(BeeDebugRenderer.BeeInfo::hasFlower).forEach((beedebugrenderer_beeinfo) -> map.computeIfAbsent(beedebugrenderer_beeinfo.flowerPos, (blockpos1) -> Sets.newHashSet()).add(beedebugrenderer_beeinfo.getUuid()));
      map.entrySet().forEach((map_entry) -> {
         BlockPos blockpos = map_entry.getKey();
         Set<UUID> set = map_entry.getValue();
         Set<String> set1 = set.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
         int i = 1;
         renderTextOverPos(posestack, multibuffersource, set1.toString(), blockpos, i++, -256);
         renderTextOverPos(posestack, multibuffersource, "Flower", blockpos, i++, -1);
         float f = 0.05F;
         DebugRenderer.renderFilledBox(posestack, multibuffersource, blockpos, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
      });
   }

   private static String getBeeUuidsAsString(Collection<UUID> collection) {
      if (collection.isEmpty()) {
         return "-";
      } else {
         return collection.size() > 3 ? collection.size() + " bees" : collection.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet()).toString();
      }
   }

   private static void highlightHive(PoseStack posestack, MultiBufferSource multibuffersource, BlockPos blockpos) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(posestack, multibuffersource, blockpos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void renderGhostHive(PoseStack posestack, MultiBufferSource multibuffersource, BlockPos blockpos, List<String> list) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(posestack, multibuffersource, blockpos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      renderTextOverPos(posestack, multibuffersource, "" + list, blockpos, 0, -256);
      renderTextOverPos(posestack, multibuffersource, "Ghost Hive", blockpos, 1, -65536);
   }

   private void renderHiveInfo(PoseStack posestack, MultiBufferSource multibuffersource, BeeDebugRenderer.HiveInfo beedebugrenderer_hiveinfo, Collection<UUID> collection) {
      int i = 0;
      if (!collection.isEmpty()) {
         renderTextOverHive(posestack, multibuffersource, "Blacklisted by " + getBeeUuidsAsString(collection), beedebugrenderer_hiveinfo, i++, -65536);
      }

      renderTextOverHive(posestack, multibuffersource, "Out: " + getBeeUuidsAsString(this.getHiveMembers(beedebugrenderer_hiveinfo.pos)), beedebugrenderer_hiveinfo, i++, -3355444);
      if (beedebugrenderer_hiveinfo.occupantCount == 0) {
         renderTextOverHive(posestack, multibuffersource, "In: -", beedebugrenderer_hiveinfo, i++, -256);
      } else if (beedebugrenderer_hiveinfo.occupantCount == 1) {
         renderTextOverHive(posestack, multibuffersource, "In: 1 bee", beedebugrenderer_hiveinfo, i++, -256);
      } else {
         renderTextOverHive(posestack, multibuffersource, "In: " + beedebugrenderer_hiveinfo.occupantCount + " bees", beedebugrenderer_hiveinfo, i++, -256);
      }

      renderTextOverHive(posestack, multibuffersource, "Honey: " + beedebugrenderer_hiveinfo.honeyLevel, beedebugrenderer_hiveinfo, i++, -23296);
      renderTextOverHive(posestack, multibuffersource, beedebugrenderer_hiveinfo.hiveType + (beedebugrenderer_hiveinfo.sedated ? " (sedated)" : ""), beedebugrenderer_hiveinfo, i++, -1);
   }

   private void renderPath(PoseStack posestack, MultiBufferSource multibuffersource, BeeDebugRenderer.BeeInfo beedebugrenderer_beeinfo) {
      if (beedebugrenderer_beeinfo.path != null) {
         PathfindingRenderer.renderPath(posestack, multibuffersource, beedebugrenderer_beeinfo.path, 0.5F, false, false, this.getCamera().getPosition().x(), this.getCamera().getPosition().y(), this.getCamera().getPosition().z());
      }

   }

   private void renderBeeInfo(PoseStack posestack, MultiBufferSource multibuffersource, BeeDebugRenderer.BeeInfo beedebugrenderer_beeinfo) {
      boolean flag = this.isBeeSelected(beedebugrenderer_beeinfo);
      int i = 0;
      renderTextOverMob(posestack, multibuffersource, beedebugrenderer_beeinfo.pos, i++, beedebugrenderer_beeinfo.toString(), -1, 0.03F);
      if (beedebugrenderer_beeinfo.hivePos == null) {
         renderTextOverMob(posestack, multibuffersource, beedebugrenderer_beeinfo.pos, i++, "No hive", -98404, 0.02F);
      } else {
         renderTextOverMob(posestack, multibuffersource, beedebugrenderer_beeinfo.pos, i++, "Hive: " + this.getPosDescription(beedebugrenderer_beeinfo, beedebugrenderer_beeinfo.hivePos), -256, 0.02F);
      }

      if (beedebugrenderer_beeinfo.flowerPos == null) {
         renderTextOverMob(posestack, multibuffersource, beedebugrenderer_beeinfo.pos, i++, "No flower", -98404, 0.02F);
      } else {
         renderTextOverMob(posestack, multibuffersource, beedebugrenderer_beeinfo.pos, i++, "Flower: " + this.getPosDescription(beedebugrenderer_beeinfo, beedebugrenderer_beeinfo.flowerPos), -256, 0.02F);
      }

      for(String s : beedebugrenderer_beeinfo.goals) {
         renderTextOverMob(posestack, multibuffersource, beedebugrenderer_beeinfo.pos, i++, s, -16711936, 0.02F);
      }

      if (flag) {
         this.renderPath(posestack, multibuffersource, beedebugrenderer_beeinfo);
      }

      if (beedebugrenderer_beeinfo.travelTicks > 0) {
         int j = beedebugrenderer_beeinfo.travelTicks < 600 ? -3355444 : -23296;
         renderTextOverMob(posestack, multibuffersource, beedebugrenderer_beeinfo.pos, i++, "Travelling: " + beedebugrenderer_beeinfo.travelTicks + " ticks", j, 0.02F);
      }

   }

   private static void renderTextOverHive(PoseStack posestack, MultiBufferSource multibuffersource, String s, BeeDebugRenderer.HiveInfo beedebugrenderer_hiveinfo, int i, int j) {
      BlockPos blockpos = beedebugrenderer_hiveinfo.pos;
      renderTextOverPos(posestack, multibuffersource, s, blockpos, i, j);
   }

   private static void renderTextOverPos(PoseStack posestack, MultiBufferSource multibuffersource, String s, BlockPos blockpos, int i, int j) {
      double d0 = 1.3D;
      double d1 = 0.2D;
      double d2 = (double)blockpos.getX() + 0.5D;
      double d3 = (double)blockpos.getY() + 1.3D + (double)i * 0.2D;
      double d4 = (double)blockpos.getZ() + 0.5D;
      DebugRenderer.renderFloatingText(posestack, multibuffersource, s, d2, d3, d4, j, 0.02F, true, 0.0F, true);
   }

   private static void renderTextOverMob(PoseStack posestack, MultiBufferSource multibuffersource, Position position, int i, String s, int j, float f) {
      double d0 = 2.4D;
      double d1 = 0.25D;
      BlockPos blockpos = BlockPos.containing(position);
      double d2 = (double)blockpos.getX() + 0.5D;
      double d3 = position.y() + 2.4D + (double)i * 0.25D;
      double d4 = (double)blockpos.getZ() + 0.5D;
      float f1 = 0.5F;
      DebugRenderer.renderFloatingText(posestack, multibuffersource, s, d2, d3, d4, j, f, false, 0.5F, true);
   }

   private Camera getCamera() {
      return this.minecraft.gameRenderer.getMainCamera();
   }

   private Set<String> getHiveMemberNames(BeeDebugRenderer.HiveInfo beedebugrenderer_hiveinfo) {
      return this.getHiveMembers(beedebugrenderer_hiveinfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
   }

   private String getPosDescription(BeeDebugRenderer.BeeInfo beedebugrenderer_beeinfo, BlockPos blockpos) {
      double d0 = Math.sqrt(blockpos.distToCenterSqr(beedebugrenderer_beeinfo.pos));
      double d1 = (double)Math.round(d0 * 10.0D) / 10.0D;
      return blockpos.toShortString() + " (dist " + d1 + ")";
   }

   private boolean isBeeSelected(BeeDebugRenderer.BeeInfo beedebugrenderer_beeinfo) {
      return Objects.equals(this.lastLookedAtUuid, beedebugrenderer_beeinfo.uuid);
   }

   private boolean isPlayerCloseEnoughToMob(BeeDebugRenderer.BeeInfo beedebugrenderer_beeinfo) {
      Player player = this.minecraft.player;
      BlockPos blockpos = BlockPos.containing(player.getX(), beedebugrenderer_beeinfo.pos.y(), player.getZ());
      BlockPos blockpos1 = BlockPos.containing(beedebugrenderer_beeinfo.pos);
      return blockpos.closerThan(blockpos1, 30.0D);
   }

   private Collection<UUID> getHiveMembers(BlockPos blockpos) {
      return this.beeInfosPerEntity.values().stream().filter((beedebugrenderer_beeinfo) -> beedebugrenderer_beeinfo.hasHive(blockpos)).map(BeeDebugRenderer.BeeInfo::getUuid).collect(Collectors.toSet());
   }

   private Map<BlockPos, List<String>> getGhostHives() {
      Map<BlockPos, List<String>> map = Maps.newHashMap();

      for(BeeDebugRenderer.BeeInfo beedebugrenderer_beeinfo : this.beeInfosPerEntity.values()) {
         if (beedebugrenderer_beeinfo.hivePos != null && !this.hives.containsKey(beedebugrenderer_beeinfo.hivePos)) {
            map.computeIfAbsent(beedebugrenderer_beeinfo.hivePos, (blockpos) -> Lists.newArrayList()).add(beedebugrenderer_beeinfo.getName());
         }
      }

      return map;
   }

   private void updateLastLookedAtUuid() {
      DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent((entity) -> this.lastLookedAtUuid = entity.getUUID());
   }

   public static class BeeInfo {
      public final UUID uuid;
      public final int id;
      public final Position pos;
      @Nullable
      public final Path path;
      @Nullable
      public final BlockPos hivePos;
      @Nullable
      public final BlockPos flowerPos;
      public final int travelTicks;
      public final List<String> goals = Lists.newArrayList();
      public final Set<BlockPos> blacklistedHives = Sets.newHashSet();

      public BeeInfo(UUID uuid, int i, Position position, @Nullable Path path, @Nullable BlockPos blockpos, @Nullable BlockPos blockpos1, int j) {
         this.uuid = uuid;
         this.id = i;
         this.pos = position;
         this.path = path;
         this.hivePos = blockpos;
         this.flowerPos = blockpos1;
         this.travelTicks = j;
      }

      public boolean hasHive(BlockPos blockpos) {
         return this.hivePos != null && this.hivePos.equals(blockpos);
      }

      public UUID getUuid() {
         return this.uuid;
      }

      public String getName() {
         return DebugEntityNameGenerator.getEntityName(this.uuid);
      }

      public String toString() {
         return this.getName();
      }

      public boolean hasFlower() {
         return this.flowerPos != null;
      }
   }

   public static class HiveInfo {
      public final BlockPos pos;
      public final String hiveType;
      public final int occupantCount;
      public final int honeyLevel;
      public final boolean sedated;
      public final long lastSeen;

      public HiveInfo(BlockPos blockpos, String s, int i, int j, boolean flag, long k) {
         this.pos = blockpos;
         this.hiveType = s;
         this.occupantCount = i;
         this.honeyLevel = j;
         this.sedated = flag;
         this.lastSeen = k;
      }
   }
}
