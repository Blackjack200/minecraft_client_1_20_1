package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class MapItemSavedData extends SavedData {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int MAP_SIZE = 128;
   private static final int HALF_MAP_SIZE = 64;
   public static final int MAX_SCALE = 4;
   public static final int TRACKED_DECORATION_LIMIT = 256;
   public final int centerX;
   public final int centerZ;
   public final ResourceKey<Level> dimension;
   private final boolean trackingPosition;
   private final boolean unlimitedTracking;
   public final byte scale;
   public byte[] colors = new byte[16384];
   public final boolean locked;
   private final List<MapItemSavedData.HoldingPlayer> carriedBy = Lists.newArrayList();
   private final Map<Player, MapItemSavedData.HoldingPlayer> carriedByPlayers = Maps.newHashMap();
   private final Map<String, MapBanner> bannerMarkers = Maps.newHashMap();
   final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
   private final Map<String, MapFrame> frameMarkers = Maps.newHashMap();
   private int trackedDecorationCount;

   private MapItemSavedData(int i, int j, byte b0, boolean flag, boolean flag1, boolean flag2, ResourceKey<Level> resourcekey) {
      this.scale = b0;
      this.centerX = i;
      this.centerZ = j;
      this.dimension = resourcekey;
      this.trackingPosition = flag;
      this.unlimitedTracking = flag1;
      this.locked = flag2;
      this.setDirty();
   }

   public static MapItemSavedData createFresh(double d0, double d1, byte b0, boolean flag, boolean flag1, ResourceKey<Level> resourcekey) {
      int i = 128 * (1 << b0);
      int j = Mth.floor((d0 + 64.0D) / (double)i);
      int k = Mth.floor((d1 + 64.0D) / (double)i);
      int l = j * i + i / 2 - 64;
      int i1 = k * i + i / 2 - 64;
      return new MapItemSavedData(l, i1, b0, flag, flag1, false, resourcekey);
   }

   public static MapItemSavedData createForClient(byte b0, boolean flag, ResourceKey<Level> resourcekey) {
      return new MapItemSavedData(0, 0, b0, false, false, flag, resourcekey);
   }

   public static MapItemSavedData load(CompoundTag compoundtag) {
      ResourceKey<Level> resourcekey = DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, compoundtag.get("dimension"))).resultOrPartial(LOGGER::error).orElseThrow(() -> new IllegalArgumentException("Invalid map dimension: " + compoundtag.get("dimension")));
      int i = compoundtag.getInt("xCenter");
      int j = compoundtag.getInt("zCenter");
      byte b0 = (byte)Mth.clamp(compoundtag.getByte("scale"), 0, 4);
      boolean flag = !compoundtag.contains("trackingPosition", 1) || compoundtag.getBoolean("trackingPosition");
      boolean flag1 = compoundtag.getBoolean("unlimitedTracking");
      boolean flag2 = compoundtag.getBoolean("locked");
      MapItemSavedData mapitemsaveddata = new MapItemSavedData(i, j, b0, flag, flag1, flag2, resourcekey);
      byte[] abyte = compoundtag.getByteArray("colors");
      if (abyte.length == 16384) {
         mapitemsaveddata.colors = abyte;
      }

      ListTag listtag = compoundtag.getList("banners", 10);

      for(int k = 0; k < listtag.size(); ++k) {
         MapBanner mapbanner = MapBanner.load(listtag.getCompound(k));
         mapitemsaveddata.bannerMarkers.put(mapbanner.getId(), mapbanner);
         mapitemsaveddata.addDecoration(mapbanner.getDecoration(), (LevelAccessor)null, mapbanner.getId(), (double)mapbanner.getPos().getX(), (double)mapbanner.getPos().getZ(), 180.0D, mapbanner.getName());
      }

      ListTag listtag1 = compoundtag.getList("frames", 10);

      for(int l = 0; l < listtag1.size(); ++l) {
         MapFrame mapframe = MapFrame.load(listtag1.getCompound(l));
         mapitemsaveddata.frameMarkers.put(mapframe.getId(), mapframe);
         mapitemsaveddata.addDecoration(MapDecoration.Type.FRAME, (LevelAccessor)null, "frame-" + mapframe.getEntityId(), (double)mapframe.getPos().getX(), (double)mapframe.getPos().getZ(), (double)mapframe.getRotation(), (Component)null);
      }

      return mapitemsaveddata;
   }

   public CompoundTag save(CompoundTag compoundtag) {
      ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, this.dimension.location()).resultOrPartial(LOGGER::error).ifPresent((tag) -> compoundtag.put("dimension", tag));
      compoundtag.putInt("xCenter", this.centerX);
      compoundtag.putInt("zCenter", this.centerZ);
      compoundtag.putByte("scale", this.scale);
      compoundtag.putByteArray("colors", this.colors);
      compoundtag.putBoolean("trackingPosition", this.trackingPosition);
      compoundtag.putBoolean("unlimitedTracking", this.unlimitedTracking);
      compoundtag.putBoolean("locked", this.locked);
      ListTag listtag = new ListTag();

      for(MapBanner mapbanner : this.bannerMarkers.values()) {
         listtag.add(mapbanner.save());
      }

      compoundtag.put("banners", listtag);
      ListTag listtag1 = new ListTag();

      for(MapFrame mapframe : this.frameMarkers.values()) {
         listtag1.add(mapframe.save());
      }

      compoundtag.put("frames", listtag1);
      return compoundtag;
   }

   public MapItemSavedData locked() {
      MapItemSavedData mapitemsaveddata = new MapItemSavedData(this.centerX, this.centerZ, this.scale, this.trackingPosition, this.unlimitedTracking, true, this.dimension);
      mapitemsaveddata.bannerMarkers.putAll(this.bannerMarkers);
      mapitemsaveddata.decorations.putAll(this.decorations);
      mapitemsaveddata.trackedDecorationCount = this.trackedDecorationCount;
      System.arraycopy(this.colors, 0, mapitemsaveddata.colors, 0, this.colors.length);
      mapitemsaveddata.setDirty();
      return mapitemsaveddata;
   }

   public MapItemSavedData scaled(int i) {
      return createFresh((double)this.centerX, (double)this.centerZ, (byte)Mth.clamp(this.scale + i, 0, 4), this.trackingPosition, this.unlimitedTracking, this.dimension);
   }

   public void tickCarriedBy(Player player, ItemStack itemstack) {
      if (!this.carriedByPlayers.containsKey(player)) {
         MapItemSavedData.HoldingPlayer mapitemsaveddata_holdingplayer = new MapItemSavedData.HoldingPlayer(player);
         this.carriedByPlayers.put(player, mapitemsaveddata_holdingplayer);
         this.carriedBy.add(mapitemsaveddata_holdingplayer);
      }

      if (!player.getInventory().contains(itemstack)) {
         this.removeDecoration(player.getName().getString());
      }

      for(int i = 0; i < this.carriedBy.size(); ++i) {
         MapItemSavedData.HoldingPlayer mapitemsaveddata_holdingplayer1 = this.carriedBy.get(i);
         String s = mapitemsaveddata_holdingplayer1.player.getName().getString();
         if (!mapitemsaveddata_holdingplayer1.player.isRemoved() && (mapitemsaveddata_holdingplayer1.player.getInventory().contains(itemstack) || itemstack.isFramed())) {
            if (!itemstack.isFramed() && mapitemsaveddata_holdingplayer1.player.level().dimension() == this.dimension && this.trackingPosition) {
               this.addDecoration(MapDecoration.Type.PLAYER, mapitemsaveddata_holdingplayer1.player.level(), s, mapitemsaveddata_holdingplayer1.player.getX(), mapitemsaveddata_holdingplayer1.player.getZ(), (double)mapitemsaveddata_holdingplayer1.player.getYRot(), (Component)null);
            }
         } else {
            this.carriedByPlayers.remove(mapitemsaveddata_holdingplayer1.player);
            this.carriedBy.remove(mapitemsaveddata_holdingplayer1);
            this.removeDecoration(s);
         }
      }

      if (itemstack.isFramed() && this.trackingPosition) {
         ItemFrame itemframe = itemstack.getFrame();
         BlockPos blockpos = itemframe.getPos();
         MapFrame mapframe = this.frameMarkers.get(MapFrame.frameId(blockpos));
         if (mapframe != null && itemframe.getId() != mapframe.getEntityId() && this.frameMarkers.containsKey(mapframe.getId())) {
            this.removeDecoration("frame-" + mapframe.getEntityId());
         }

         MapFrame mapframe1 = new MapFrame(blockpos, itemframe.getDirection().get2DDataValue() * 90, itemframe.getId());
         this.addDecoration(MapDecoration.Type.FRAME, player.level(), "frame-" + itemframe.getId(), (double)blockpos.getX(), (double)blockpos.getZ(), (double)(itemframe.getDirection().get2DDataValue() * 90), (Component)null);
         this.frameMarkers.put(mapframe1.getId(), mapframe1);
      }

      CompoundTag compoundtag = itemstack.getTag();
      if (compoundtag != null && compoundtag.contains("Decorations", 9)) {
         ListTag listtag = compoundtag.getList("Decorations", 10);

         for(int j = 0; j < listtag.size(); ++j) {
            CompoundTag compoundtag1 = listtag.getCompound(j);
            if (!this.decorations.containsKey(compoundtag1.getString("id"))) {
               this.addDecoration(MapDecoration.Type.byIcon(compoundtag1.getByte("type")), player.level(), compoundtag1.getString("id"), compoundtag1.getDouble("x"), compoundtag1.getDouble("z"), compoundtag1.getDouble("rot"), (Component)null);
            }
         }
      }

   }

   private void removeDecoration(String s) {
      MapDecoration mapdecoration = this.decorations.remove(s);
      if (mapdecoration != null && mapdecoration.getType().shouldTrackCount()) {
         --this.trackedDecorationCount;
      }

      this.setDecorationsDirty();
   }

   public static void addTargetDecoration(ItemStack itemstack, BlockPos blockpos, String s, MapDecoration.Type mapdecoration_type) {
      ListTag listtag;
      if (itemstack.hasTag() && itemstack.getTag().contains("Decorations", 9)) {
         listtag = itemstack.getTag().getList("Decorations", 10);
      } else {
         listtag = new ListTag();
         itemstack.addTagElement("Decorations", listtag);
      }

      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putByte("type", mapdecoration_type.getIcon());
      compoundtag.putString("id", s);
      compoundtag.putDouble("x", (double)blockpos.getX());
      compoundtag.putDouble("z", (double)blockpos.getZ());
      compoundtag.putDouble("rot", 180.0D);
      listtag.add(compoundtag);
      if (mapdecoration_type.hasMapColor()) {
         CompoundTag compoundtag1 = itemstack.getOrCreateTagElement("display");
         compoundtag1.putInt("MapColor", mapdecoration_type.getMapColor());
      }

   }

   private void addDecoration(MapDecoration.Type mapdecoration_type, @Nullable LevelAccessor levelaccessor, String s, double d0, double d1, double d2, @Nullable Component component) {
      int i = 1 << this.scale;
      float f = (float)(d0 - (double)this.centerX) / (float)i;
      float f1 = (float)(d1 - (double)this.centerZ) / (float)i;
      byte b0 = (byte)((int)((double)(f * 2.0F) + 0.5D));
      byte b1 = (byte)((int)((double)(f1 * 2.0F) + 0.5D));
      int j = 63;
      byte b2;
      if (f >= -63.0F && f1 >= -63.0F && f <= 63.0F && f1 <= 63.0F) {
         d2 += d2 < 0.0D ? -8.0D : 8.0D;
         b2 = (byte)((int)(d2 * 16.0D / 360.0D));
         if (this.dimension == Level.NETHER && levelaccessor != null) {
            int k = (int)(levelaccessor.getLevelData().getDayTime() / 10L);
            b2 = (byte)(k * k * 34187121 + k * 121 >> 15 & 15);
         }
      } else {
         if (mapdecoration_type != MapDecoration.Type.PLAYER) {
            this.removeDecoration(s);
            return;
         }

         int l = 320;
         if (Math.abs(f) < 320.0F && Math.abs(f1) < 320.0F) {
            mapdecoration_type = MapDecoration.Type.PLAYER_OFF_MAP;
         } else {
            if (!this.unlimitedTracking) {
               this.removeDecoration(s);
               return;
            }

            mapdecoration_type = MapDecoration.Type.PLAYER_OFF_LIMITS;
         }

         b2 = 0;
         if (f <= -63.0F) {
            b0 = -128;
         }

         if (f1 <= -63.0F) {
            b1 = -128;
         }

         if (f >= 63.0F) {
            b0 = 127;
         }

         if (f1 >= 63.0F) {
            b1 = 127;
         }
      }

      MapDecoration mapdecoration = new MapDecoration(mapdecoration_type, b0, b1, b2, component);
      MapDecoration mapdecoration1 = this.decorations.put(s, mapdecoration);
      if (!mapdecoration.equals(mapdecoration1)) {
         if (mapdecoration1 != null && mapdecoration1.getType().shouldTrackCount()) {
            --this.trackedDecorationCount;
         }

         if (mapdecoration_type.shouldTrackCount()) {
            ++this.trackedDecorationCount;
         }

         this.setDecorationsDirty();
      }

   }

   @Nullable
   public Packet<?> getUpdatePacket(int i, Player player) {
      MapItemSavedData.HoldingPlayer mapitemsaveddata_holdingplayer = this.carriedByPlayers.get(player);
      return mapitemsaveddata_holdingplayer == null ? null : mapitemsaveddata_holdingplayer.nextUpdatePacket(i);
   }

   private void setColorsDirty(int i, int j) {
      this.setDirty();

      for(MapItemSavedData.HoldingPlayer mapitemsaveddata_holdingplayer : this.carriedBy) {
         mapitemsaveddata_holdingplayer.markColorsDirty(i, j);
      }

   }

   private void setDecorationsDirty() {
      this.setDirty();
      this.carriedBy.forEach(MapItemSavedData.HoldingPlayer::markDecorationsDirty);
   }

   public MapItemSavedData.HoldingPlayer getHoldingPlayer(Player player) {
      MapItemSavedData.HoldingPlayer mapitemsaveddata_holdingplayer = this.carriedByPlayers.get(player);
      if (mapitemsaveddata_holdingplayer == null) {
         mapitemsaveddata_holdingplayer = new MapItemSavedData.HoldingPlayer(player);
         this.carriedByPlayers.put(player, mapitemsaveddata_holdingplayer);
         this.carriedBy.add(mapitemsaveddata_holdingplayer);
      }

      return mapitemsaveddata_holdingplayer;
   }

   public boolean toggleBanner(LevelAccessor levelaccessor, BlockPos blockpos) {
      double d0 = (double)blockpos.getX() + 0.5D;
      double d1 = (double)blockpos.getZ() + 0.5D;
      int i = 1 << this.scale;
      double d2 = (d0 - (double)this.centerX) / (double)i;
      double d3 = (d1 - (double)this.centerZ) / (double)i;
      int j = 63;
      if (d2 >= -63.0D && d3 >= -63.0D && d2 <= 63.0D && d3 <= 63.0D) {
         MapBanner mapbanner = MapBanner.fromWorld(levelaccessor, blockpos);
         if (mapbanner == null) {
            return false;
         }

         if (this.bannerMarkers.remove(mapbanner.getId(), mapbanner)) {
            this.removeDecoration(mapbanner.getId());
            return true;
         }

         if (!this.isTrackedCountOverLimit(256)) {
            this.bannerMarkers.put(mapbanner.getId(), mapbanner);
            this.addDecoration(mapbanner.getDecoration(), levelaccessor, mapbanner.getId(), d0, d1, 180.0D, mapbanner.getName());
            return true;
         }
      }

      return false;
   }

   public void checkBanners(BlockGetter blockgetter, int i, int j) {
      Iterator<MapBanner> iterator = this.bannerMarkers.values().iterator();

      while(iterator.hasNext()) {
         MapBanner mapbanner = iterator.next();
         if (mapbanner.getPos().getX() == i && mapbanner.getPos().getZ() == j) {
            MapBanner mapbanner1 = MapBanner.fromWorld(blockgetter, mapbanner.getPos());
            if (!mapbanner.equals(mapbanner1)) {
               iterator.remove();
               this.removeDecoration(mapbanner.getId());
            }
         }
      }

   }

   public Collection<MapBanner> getBanners() {
      return this.bannerMarkers.values();
   }

   public void removedFromFrame(BlockPos blockpos, int i) {
      this.removeDecoration("frame-" + i);
      this.frameMarkers.remove(MapFrame.frameId(blockpos));
   }

   public boolean updateColor(int i, int j, byte b0) {
      byte b1 = this.colors[i + j * 128];
      if (b1 != b0) {
         this.setColor(i, j, b0);
         return true;
      } else {
         return false;
      }
   }

   public void setColor(int i, int j, byte b0) {
      this.colors[i + j * 128] = b0;
      this.setColorsDirty(i, j);
   }

   public boolean isExplorationMap() {
      for(MapDecoration mapdecoration : this.decorations.values()) {
         if (mapdecoration.getType() == MapDecoration.Type.MANSION || mapdecoration.getType() == MapDecoration.Type.MONUMENT) {
            return true;
         }
      }

      return false;
   }

   public void addClientSideDecorations(List<MapDecoration> list) {
      this.decorations.clear();
      this.trackedDecorationCount = 0;

      for(int i = 0; i < list.size(); ++i) {
         MapDecoration mapdecoration = list.get(i);
         this.decorations.put("icon-" + i, mapdecoration);
         if (mapdecoration.getType().shouldTrackCount()) {
            ++this.trackedDecorationCount;
         }
      }

   }

   public Iterable<MapDecoration> getDecorations() {
      return this.decorations.values();
   }

   public boolean isTrackedCountOverLimit(int i) {
      return this.trackedDecorationCount >= i;
   }

   public class HoldingPlayer {
      public final Player player;
      private boolean dirtyData = true;
      private int minDirtyX;
      private int minDirtyY;
      private int maxDirtyX = 127;
      private int maxDirtyY = 127;
      private boolean dirtyDecorations = true;
      private int tick;
      public int step;

      HoldingPlayer(Player player) {
         this.player = player;
      }

      private MapItemSavedData.MapPatch createPatch() {
         int i = this.minDirtyX;
         int j = this.minDirtyY;
         int k = this.maxDirtyX + 1 - this.minDirtyX;
         int l = this.maxDirtyY + 1 - this.minDirtyY;
         byte[] abyte = new byte[k * l];

         for(int i1 = 0; i1 < k; ++i1) {
            for(int j1 = 0; j1 < l; ++j1) {
               abyte[i1 + j1 * k] = MapItemSavedData.this.colors[i + i1 + (j + j1) * 128];
            }
         }

         return new MapItemSavedData.MapPatch(i, j, k, l, abyte);
      }

      @Nullable
      Packet<?> nextUpdatePacket(int i) {
         MapItemSavedData.MapPatch mapitemsaveddata_mappatch;
         if (this.dirtyData) {
            this.dirtyData = false;
            mapitemsaveddata_mappatch = this.createPatch();
         } else {
            mapitemsaveddata_mappatch = null;
         }

         Collection<MapDecoration> collection;
         if (this.dirtyDecorations && this.tick++ % 5 == 0) {
            this.dirtyDecorations = false;
            collection = MapItemSavedData.this.decorations.values();
         } else {
            collection = null;
         }

         return collection == null && mapitemsaveddata_mappatch == null ? null : new ClientboundMapItemDataPacket(i, MapItemSavedData.this.scale, MapItemSavedData.this.locked, collection, mapitemsaveddata_mappatch);
      }

      void markColorsDirty(int i, int j) {
         if (this.dirtyData) {
            this.minDirtyX = Math.min(this.minDirtyX, i);
            this.minDirtyY = Math.min(this.minDirtyY, j);
            this.maxDirtyX = Math.max(this.maxDirtyX, i);
            this.maxDirtyY = Math.max(this.maxDirtyY, j);
         } else {
            this.dirtyData = true;
            this.minDirtyX = i;
            this.minDirtyY = j;
            this.maxDirtyX = i;
            this.maxDirtyY = j;
         }

      }

      private void markDecorationsDirty() {
         this.dirtyDecorations = true;
      }
   }

   public static class MapPatch {
      public final int startX;
      public final int startY;
      public final int width;
      public final int height;
      public final byte[] mapColors;

      public MapPatch(int i, int j, int k, int l, byte[] abyte) {
         this.startX = i;
         this.startY = j;
         this.width = k;
         this.height = l;
         this.mapColors = abyte;
      }

      public void applyToMap(MapItemSavedData mapitemsaveddata) {
         for(int i = 0; i < this.width; ++i) {
            for(int j = 0; j < this.height; ++j) {
               mapitemsaveddata.setColor(this.startX + i, this.startY + j, this.mapColors[i + j * this.width]);
            }
         }

      }
   }
}
