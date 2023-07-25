package net.minecraft.client.renderer.debug;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import org.slf4j.Logger;

public class BrainDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean SHOW_NAME_FOR_ALL = true;
   private static final boolean SHOW_PROFESSION_FOR_ALL = false;
   private static final boolean SHOW_BEHAVIORS_FOR_ALL = false;
   private static final boolean SHOW_ACTIVITIES_FOR_ALL = false;
   private static final boolean SHOW_INVENTORY_FOR_ALL = false;
   private static final boolean SHOW_GOSSIPS_FOR_ALL = false;
   private static final boolean SHOW_PATH_FOR_ALL = false;
   private static final boolean SHOW_HEALTH_FOR_ALL = false;
   private static final boolean SHOW_WANTS_GOLEM_FOR_ALL = true;
   private static final boolean SHOW_ANGER_LEVEL_FOR_ALL = false;
   private static final boolean SHOW_NAME_FOR_SELECTED = true;
   private static final boolean SHOW_PROFESSION_FOR_SELECTED = true;
   private static final boolean SHOW_BEHAVIORS_FOR_SELECTED = true;
   private static final boolean SHOW_ACTIVITIES_FOR_SELECTED = true;
   private static final boolean SHOW_MEMORIES_FOR_SELECTED = true;
   private static final boolean SHOW_INVENTORY_FOR_SELECTED = true;
   private static final boolean SHOW_GOSSIPS_FOR_SELECTED = true;
   private static final boolean SHOW_PATH_FOR_SELECTED = true;
   private static final boolean SHOW_HEALTH_FOR_SELECTED = true;
   private static final boolean SHOW_WANTS_GOLEM_FOR_SELECTED = true;
   private static final boolean SHOW_ANGER_LEVEL_FOR_SELECTED = true;
   private static final boolean SHOW_POI_INFO = true;
   private static final int MAX_RENDER_DIST_FOR_BRAIN_INFO = 30;
   private static final int MAX_RENDER_DIST_FOR_POI_INFO = 30;
   private static final int MAX_TARGETING_DIST = 8;
   private static final float TEXT_SCALE = 0.02F;
   private static final int WHITE = -1;
   private static final int YELLOW = -256;
   private static final int CYAN = -16711681;
   private static final int GREEN = -16711936;
   private static final int GRAY = -3355444;
   private static final int PINK = -98404;
   private static final int RED = -65536;
   private static final int ORANGE = -23296;
   private final Minecraft minecraft;
   private final Map<BlockPos, BrainDebugRenderer.PoiInfo> pois = Maps.newHashMap();
   private final Map<UUID, BrainDebugRenderer.BrainDump> brainDumpsPerEntity = Maps.newHashMap();
   @Nullable
   private UUID lastLookedAtUuid;

   public BrainDebugRenderer(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void clear() {
      this.pois.clear();
      this.brainDumpsPerEntity.clear();
      this.lastLookedAtUuid = null;
   }

   public void addPoi(BrainDebugRenderer.PoiInfo braindebugrenderer_poiinfo) {
      this.pois.put(braindebugrenderer_poiinfo.pos, braindebugrenderer_poiinfo);
   }

   public void removePoi(BlockPos blockpos) {
      this.pois.remove(blockpos);
   }

   public void setFreeTicketCount(BlockPos blockpos, int i) {
      BrainDebugRenderer.PoiInfo braindebugrenderer_poiinfo = this.pois.get(blockpos);
      if (braindebugrenderer_poiinfo == null) {
         LOGGER.warn("Strange, setFreeTicketCount was called for an unknown POI: {}", (Object)blockpos);
      } else {
         braindebugrenderer_poiinfo.freeTicketCount = i;
      }
   }

   public void addOrUpdateBrainDump(BrainDebugRenderer.BrainDump braindebugrenderer_braindump) {
      this.brainDumpsPerEntity.put(braindebugrenderer_braindump.uuid, braindebugrenderer_braindump);
   }

   public void removeBrainDump(int i) {
      this.brainDumpsPerEntity.values().removeIf((braindebugrenderer_braindump) -> braindebugrenderer_braindump.id == i);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      this.clearRemovedEntities();
      this.doRender(posestack, multibuffersource, d0, d1, d2);
      if (!this.minecraft.player.isSpectator()) {
         this.updateLastLookedAtUuid();
      }

   }

   private void clearRemovedEntities() {
      this.brainDumpsPerEntity.entrySet().removeIf((map_entry) -> {
         Entity entity = this.minecraft.level.getEntity((map_entry.getValue()).id);
         return entity == null || entity.isRemoved();
      });
   }

   private void doRender(PoseStack posestack, MultiBufferSource multibuffersource, double d0, double d1, double d2) {
      BlockPos blockpos = BlockPos.containing(d0, d1, d2);
      this.brainDumpsPerEntity.values().forEach((braindebugrenderer_braindump) -> {
         if (this.isPlayerCloseEnoughToMob(braindebugrenderer_braindump)) {
            this.renderBrainInfo(posestack, multibuffersource, braindebugrenderer_braindump, d0, d1, d2);
         }

      });

      for(BlockPos blockpos1 : this.pois.keySet()) {
         if (blockpos.closerThan(blockpos1, 30.0D)) {
            highlightPoi(posestack, multibuffersource, blockpos1);
         }
      }

      this.pois.values().forEach((braindebugrenderer_poiinfo) -> {
         if (blockpos.closerThan(braindebugrenderer_poiinfo.pos, 30.0D)) {
            this.renderPoiInfo(posestack, multibuffersource, braindebugrenderer_poiinfo);
         }

      });
      this.getGhostPois().forEach((blockpos3, list) -> {
         if (blockpos.closerThan(blockpos3, 30.0D)) {
            this.renderGhostPoi(posestack, multibuffersource, blockpos3, list);
         }

      });
   }

   private static void highlightPoi(PoseStack posestack, MultiBufferSource multibuffersource, BlockPos blockpos) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(posestack, multibuffersource, blockpos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
   }

   private void renderGhostPoi(PoseStack posestack, MultiBufferSource multibuffersource, BlockPos blockpos, List<String> list) {
      float f = 0.05F;
      DebugRenderer.renderFilledBox(posestack, multibuffersource, blockpos, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
      renderTextOverPos(posestack, multibuffersource, "" + list, blockpos, 0, -256);
      renderTextOverPos(posestack, multibuffersource, "Ghost POI", blockpos, 1, -65536);
   }

   private void renderPoiInfo(PoseStack posestack, MultiBufferSource multibuffersource, BrainDebugRenderer.PoiInfo braindebugrenderer_poiinfo) {
      int i = 0;
      Set<String> set = this.getTicketHolderNames(braindebugrenderer_poiinfo);
      if (set.size() < 4) {
         renderTextOverPoi(posestack, multibuffersource, "Owners: " + set, braindebugrenderer_poiinfo, i, -256);
      } else {
         renderTextOverPoi(posestack, multibuffersource, set.size() + " ticket holders", braindebugrenderer_poiinfo, i, -256);
      }

      ++i;
      Set<String> set1 = this.getPotentialTicketHolderNames(braindebugrenderer_poiinfo);
      if (set1.size() < 4) {
         renderTextOverPoi(posestack, multibuffersource, "Candidates: " + set1, braindebugrenderer_poiinfo, i, -23296);
      } else {
         renderTextOverPoi(posestack, multibuffersource, set1.size() + " potential owners", braindebugrenderer_poiinfo, i, -23296);
      }

      ++i;
      renderTextOverPoi(posestack, multibuffersource, "Free tickets: " + braindebugrenderer_poiinfo.freeTicketCount, braindebugrenderer_poiinfo, i, -256);
      ++i;
      renderTextOverPoi(posestack, multibuffersource, braindebugrenderer_poiinfo.type, braindebugrenderer_poiinfo, i, -1);
   }

   private void renderPath(PoseStack posestack, MultiBufferSource multibuffersource, BrainDebugRenderer.BrainDump braindebugrenderer_braindump, double d0, double d1, double d2) {
      if (braindebugrenderer_braindump.path != null) {
         PathfindingRenderer.renderPath(posestack, multibuffersource, braindebugrenderer_braindump.path, 0.5F, false, false, d0, d1, d2);
      }

   }

   private void renderBrainInfo(PoseStack posestack, MultiBufferSource multibuffersource, BrainDebugRenderer.BrainDump braindebugrenderer_braindump, double d0, double d1, double d2) {
      boolean flag = this.isMobSelected(braindebugrenderer_braindump);
      int i = 0;
      renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, braindebugrenderer_braindump.name, -1, 0.03F);
      ++i;
      if (flag) {
         renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, braindebugrenderer_braindump.profession + " " + braindebugrenderer_braindump.xp + " xp", -1, 0.02F);
         ++i;
      }

      if (flag) {
         int j = braindebugrenderer_braindump.health < braindebugrenderer_braindump.maxHealth ? -23296 : -1;
         renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, "health: " + String.format(Locale.ROOT, "%.1f", braindebugrenderer_braindump.health) + " / " + String.format(Locale.ROOT, "%.1f", braindebugrenderer_braindump.maxHealth), j, 0.02F);
         ++i;
      }

      if (flag && !braindebugrenderer_braindump.inventory.equals("")) {
         renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, braindebugrenderer_braindump.inventory, -98404, 0.02F);
         ++i;
      }

      if (flag) {
         for(String s : braindebugrenderer_braindump.behaviors) {
            renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, s, -16711681, 0.02F);
            ++i;
         }
      }

      if (flag) {
         for(String s1 : braindebugrenderer_braindump.activities) {
            renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, s1, -16711936, 0.02F);
            ++i;
         }
      }

      if (braindebugrenderer_braindump.wantsGolem) {
         renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, "Wants Golem", -23296, 0.02F);
         ++i;
      }

      if (flag && braindebugrenderer_braindump.angerLevel != -1) {
         renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, "Anger Level: " + braindebugrenderer_braindump.angerLevel, -98404, 0.02F);
         ++i;
      }

      if (flag) {
         for(String s2 : braindebugrenderer_braindump.gossips) {
            if (s2.startsWith(braindebugrenderer_braindump.name)) {
               renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, s2, -1, 0.02F);
            } else {
               renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, s2, -23296, 0.02F);
            }

            ++i;
         }
      }

      if (flag) {
         for(String s3 : Lists.reverse(braindebugrenderer_braindump.memories)) {
            renderTextOverMob(posestack, multibuffersource, braindebugrenderer_braindump.pos, i, s3, -3355444, 0.02F);
            ++i;
         }
      }

      if (flag) {
         this.renderPath(posestack, multibuffersource, braindebugrenderer_braindump, d0, d1, d2);
      }

   }

   private static void renderTextOverPoi(PoseStack posestack, MultiBufferSource multibuffersource, String s, BrainDebugRenderer.PoiInfo braindebugrenderer_poiinfo, int i, int j) {
      renderTextOverPos(posestack, multibuffersource, s, braindebugrenderer_poiinfo.pos, i, j);
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

   private Set<String> getTicketHolderNames(BrainDebugRenderer.PoiInfo braindebugrenderer_poiinfo) {
      return this.getTicketHolders(braindebugrenderer_poiinfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
   }

   private Set<String> getPotentialTicketHolderNames(BrainDebugRenderer.PoiInfo braindebugrenderer_poiinfo) {
      return this.getPotentialTicketHolders(braindebugrenderer_poiinfo.pos).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
   }

   private boolean isMobSelected(BrainDebugRenderer.BrainDump braindebugrenderer_braindump) {
      return Objects.equals(this.lastLookedAtUuid, braindebugrenderer_braindump.uuid);
   }

   private boolean isPlayerCloseEnoughToMob(BrainDebugRenderer.BrainDump braindebugrenderer_braindump) {
      Player player = this.minecraft.player;
      BlockPos blockpos = BlockPos.containing(player.getX(), braindebugrenderer_braindump.pos.y(), player.getZ());
      BlockPos blockpos1 = BlockPos.containing(braindebugrenderer_braindump.pos);
      return blockpos.closerThan(blockpos1, 30.0D);
   }

   private Collection<UUID> getTicketHolders(BlockPos blockpos) {
      return this.brainDumpsPerEntity.values().stream().filter((braindebugrenderer_braindump) -> braindebugrenderer_braindump.hasPoi(blockpos)).map(BrainDebugRenderer.BrainDump::getUuid).collect(Collectors.toSet());
   }

   private Collection<UUID> getPotentialTicketHolders(BlockPos blockpos) {
      return this.brainDumpsPerEntity.values().stream().filter((braindebugrenderer_braindump) -> braindebugrenderer_braindump.hasPotentialPoi(blockpos)).map(BrainDebugRenderer.BrainDump::getUuid).collect(Collectors.toSet());
   }

   private Map<BlockPos, List<String>> getGhostPois() {
      Map<BlockPos, List<String>> map = Maps.newHashMap();

      for(BrainDebugRenderer.BrainDump braindebugrenderer_braindump : this.brainDumpsPerEntity.values()) {
         for(BlockPos blockpos : Iterables.concat(braindebugrenderer_braindump.pois, braindebugrenderer_braindump.potentialPois)) {
            if (!this.pois.containsKey(blockpos)) {
               map.computeIfAbsent(blockpos, (blockpos1) -> Lists.newArrayList()).add(braindebugrenderer_braindump.name);
            }
         }
      }

      return map;
   }

   private void updateLastLookedAtUuid() {
      DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent((entity) -> this.lastLookedAtUuid = entity.getUUID());
   }

   public static class BrainDump {
      public final UUID uuid;
      public final int id;
      public final String name;
      public final String profession;
      public final int xp;
      public final float health;
      public final float maxHealth;
      public final Position pos;
      public final String inventory;
      public final Path path;
      public final boolean wantsGolem;
      public final int angerLevel;
      public final List<String> activities = Lists.newArrayList();
      public final List<String> behaviors = Lists.newArrayList();
      public final List<String> memories = Lists.newArrayList();
      public final List<String> gossips = Lists.newArrayList();
      public final Set<BlockPos> pois = Sets.newHashSet();
      public final Set<BlockPos> potentialPois = Sets.newHashSet();

      public BrainDump(UUID uuid, int i, String s, String s1, int j, float f, float f1, Position position, String s2, @Nullable Path path, boolean flag, int k) {
         this.uuid = uuid;
         this.id = i;
         this.name = s;
         this.profession = s1;
         this.xp = j;
         this.health = f;
         this.maxHealth = f1;
         this.pos = position;
         this.inventory = s2;
         this.path = path;
         this.wantsGolem = flag;
         this.angerLevel = k;
      }

      boolean hasPoi(BlockPos blockpos) {
         return this.pois.stream().anyMatch(blockpos::equals);
      }

      boolean hasPotentialPoi(BlockPos blockpos) {
         return this.potentialPois.contains(blockpos);
      }

      public UUID getUuid() {
         return this.uuid;
      }
   }

   public static class PoiInfo {
      public final BlockPos pos;
      public String type;
      public int freeTicketCount;

      public PoiInfo(BlockPos blockpos, String s, int i) {
         this.pos = blockpos;
         this.type = s;
         this.freeTicketCount = i;
      }
   }
}
