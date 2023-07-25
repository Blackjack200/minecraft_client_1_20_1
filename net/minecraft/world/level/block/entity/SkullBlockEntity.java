package net.minecraft.world.level.block.entity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SkullBlockEntity extends BlockEntity {
   public static final String TAG_SKULL_OWNER = "SkullOwner";
   public static final String TAG_NOTE_BLOCK_SOUND = "note_block_sound";
   @Nullable
   private static GameProfileCache profileCache;
   @Nullable
   private static MinecraftSessionService sessionService;
   @Nullable
   private static Executor mainThreadExecutor;
   @Nullable
   private GameProfile owner;
   @Nullable
   private ResourceLocation noteBlockSound;
   private int animationTickCount;
   private boolean isAnimating;

   public SkullBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.SKULL, blockpos, blockstate);
   }

   public static void setup(Services services, Executor executor) {
      profileCache = services.profileCache();
      sessionService = services.sessionService();
      mainThreadExecutor = executor;
   }

   public static void clear() {
      profileCache = null;
      sessionService = null;
      mainThreadExecutor = null;
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      if (this.owner != null) {
         CompoundTag compoundtag1 = new CompoundTag();
         NbtUtils.writeGameProfile(compoundtag1, this.owner);
         compoundtag.put("SkullOwner", compoundtag1);
      }

      if (this.noteBlockSound != null) {
         compoundtag.putString("note_block_sound", this.noteBlockSound.toString());
      }

   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      if (compoundtag.contains("SkullOwner", 10)) {
         this.setOwner(NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner")));
      } else if (compoundtag.contains("ExtraType", 8)) {
         String s = compoundtag.getString("ExtraType");
         if (!StringUtil.isNullOrEmpty(s)) {
            this.setOwner(new GameProfile((UUID)null, s));
         }
      }

      if (compoundtag.contains("note_block_sound", 8)) {
         this.noteBlockSound = ResourceLocation.tryParse(compoundtag.getString("note_block_sound"));
      }

   }

   public static void animation(Level level, BlockPos blockpos, BlockState blockstate, SkullBlockEntity skullblockentity) {
      if (level.hasNeighborSignal(blockpos)) {
         skullblockentity.isAnimating = true;
         ++skullblockentity.animationTickCount;
      } else {
         skullblockentity.isAnimating = false;
      }

   }

   public float getAnimation(float f) {
      return this.isAnimating ? (float)this.animationTickCount + f : (float)this.animationTickCount;
   }

   @Nullable
   public GameProfile getOwnerProfile() {
      return this.owner;
   }

   @Nullable
   public ResourceLocation getNoteBlockSound() {
      return this.noteBlockSound;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public void setOwner(@Nullable GameProfile gameprofile) {
      synchronized(this) {
         this.owner = gameprofile;
      }

      this.updateOwnerProfile();
   }

   private void updateOwnerProfile() {
      updateGameprofile(this.owner, (gameprofile) -> {
         this.owner = gameprofile;
         this.setChanged();
      });
   }

   public static void updateGameprofile(@Nullable GameProfile gameprofile, Consumer<GameProfile> consumer) {
      if (gameprofile != null && !StringUtil.isNullOrEmpty(gameprofile.getName()) && (!gameprofile.isComplete() || !gameprofile.getProperties().containsKey("textures")) && profileCache != null && sessionService != null) {
         profileCache.getAsync(gameprofile.getName(), (optional) -> Util.backgroundExecutor().execute(() -> Util.ifElse(optional, (gameprofile5) -> {
                  Property property = Iterables.getFirst(gameprofile5.getProperties().get("textures"), (Property)null);
                  if (property == null) {
                     MinecraftSessionService minecraftsessionservice = sessionService;
                     if (minecraftsessionservice == null) {
                        return;
                     }

                     gameprofile5 = minecraftsessionservice.fillProfileProperties(gameprofile5, true);
                  }

                  GameProfile gameprofile6 = gameprofile5;
                  Executor executor1 = mainThreadExecutor;
                  if (executor1 != null) {
                     executor1.execute(() -> {
                        GameProfileCache gameprofilecache = profileCache;
                        if (gameprofilecache != null) {
                           gameprofilecache.add(gameprofile6);
                           consumer.accept(gameprofile6);
                        }

                     });
                  }

               }, () -> {
                  Executor executor = mainThreadExecutor;
                  if (executor != null) {
                     executor.execute(() -> consumer.accept(gameprofile));
                  }

               })));
      } else {
         consumer.accept(gameprofile);
      }
   }
}
