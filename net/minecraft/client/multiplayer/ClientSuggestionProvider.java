package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClientSuggestionProvider implements SharedSuggestionProvider {
   private final ClientPacketListener connection;
   private final Minecraft minecraft;
   private int pendingSuggestionsId = -1;
   @Nullable
   private CompletableFuture<Suggestions> pendingSuggestionsFuture;
   private final Set<String> customCompletionSuggestions = new HashSet<>();

   public ClientSuggestionProvider(ClientPacketListener clientpacketlistener, Minecraft minecraft) {
      this.connection = clientpacketlistener;
      this.minecraft = minecraft;
   }

   public Collection<String> getOnlinePlayerNames() {
      List<String> list = Lists.newArrayList();

      for(PlayerInfo playerinfo : this.connection.getOnlinePlayers()) {
         list.add(playerinfo.getProfile().getName());
      }

      return list;
   }

   public Collection<String> getCustomTabSugggestions() {
      if (this.customCompletionSuggestions.isEmpty()) {
         return this.getOnlinePlayerNames();
      } else {
         Set<String> set = new HashSet<>(this.getOnlinePlayerNames());
         set.addAll(this.customCompletionSuggestions);
         return set;
      }
   }

   public Collection<String> getSelectedEntities() {
      return (Collection<String>)(this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.ENTITY ? Collections.singleton(((EntityHitResult)this.minecraft.hitResult).getEntity().getStringUUID()) : Collections.emptyList());
   }

   public Collection<String> getAllTeams() {
      return this.connection.getLevel().getScoreboard().getTeamNames();
   }

   public Stream<ResourceLocation> getAvailableSounds() {
      return this.minecraft.getSoundManager().getAvailableSounds().stream();
   }

   public Stream<ResourceLocation> getRecipeNames() {
      return this.connection.getRecipeManager().getRecipeIds();
   }

   public boolean hasPermission(int i) {
      LocalPlayer localplayer = this.minecraft.player;
      return localplayer != null ? localplayer.hasPermissions(i) : i == 0;
   }

   public CompletableFuture<Suggestions> suggestRegistryElements(ResourceKey<? extends Registry<?>> resourcekey, SharedSuggestionProvider.ElementSuggestionType sharedsuggestionprovider_elementsuggestiontype, SuggestionsBuilder suggestionsbuilder, CommandContext<?> commandcontext) {
      return this.registryAccess().registry(resourcekey).map((registry) -> {
         this.suggestRegistryElements(registry, sharedsuggestionprovider_elementsuggestiontype, suggestionsbuilder);
         return suggestionsbuilder.buildFuture();
      }).orElseGet(() -> this.customSuggestion(commandcontext));
   }

   public CompletableFuture<Suggestions> customSuggestion(CommandContext<?> commandcontext) {
      if (this.pendingSuggestionsFuture != null) {
         this.pendingSuggestionsFuture.cancel(false);
      }

      this.pendingSuggestionsFuture = new CompletableFuture<>();
      int i = ++this.pendingSuggestionsId;
      this.connection.send(new ServerboundCommandSuggestionPacket(i, commandcontext.getInput()));
      return this.pendingSuggestionsFuture;
   }

   private static String prettyPrint(double d0) {
      return String.format(Locale.ROOT, "%.2f", d0);
   }

   private static String prettyPrint(int i) {
      return Integer.toString(i);
   }

   public Collection<SharedSuggestionProvider.TextCoordinates> getRelevantCoordinates() {
      HitResult hitresult = this.minecraft.hitResult;
      if (hitresult != null && hitresult.getType() == HitResult.Type.BLOCK) {
         BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
         return Collections.singleton(new SharedSuggestionProvider.TextCoordinates(prettyPrint(blockpos.getX()), prettyPrint(blockpos.getY()), prettyPrint(blockpos.getZ())));
      } else {
         return SharedSuggestionProvider.super.getRelevantCoordinates();
      }
   }

   public Collection<SharedSuggestionProvider.TextCoordinates> getAbsoluteCoordinates() {
      HitResult hitresult = this.minecraft.hitResult;
      if (hitresult != null && hitresult.getType() == HitResult.Type.BLOCK) {
         Vec3 vec3 = hitresult.getLocation();
         return Collections.singleton(new SharedSuggestionProvider.TextCoordinates(prettyPrint(vec3.x), prettyPrint(vec3.y), prettyPrint(vec3.z)));
      } else {
         return SharedSuggestionProvider.super.getAbsoluteCoordinates();
      }
   }

   public Set<ResourceKey<Level>> levels() {
      return this.connection.levels();
   }

   public RegistryAccess registryAccess() {
      return this.connection.registryAccess();
   }

   public FeatureFlagSet enabledFeatures() {
      return this.connection.enabledFeatures();
   }

   public void completeCustomSuggestions(int i, Suggestions suggestions) {
      if (i == this.pendingSuggestionsId) {
         this.pendingSuggestionsFuture.complete(suggestions);
         this.pendingSuggestionsFuture = null;
         this.pendingSuggestionsId = -1;
      }

   }

   public void modifyCustomCompletions(ClientboundCustomChatCompletionsPacket.Action clientboundcustomchatcompletionspacket_action, List<String> list) {
      switch (clientboundcustomchatcompletionspacket_action) {
         case ADD:
            this.customCompletionSuggestions.addAll(list);
            break;
         case REMOVE:
            list.forEach(this.customCompletionSuggestions::remove);
            break;
         case SET:
            this.customCompletionSuggestions.clear();
            this.customCompletionSuggestions.addAll(list);
      }

   }
}
