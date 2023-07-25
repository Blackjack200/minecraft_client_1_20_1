package net.minecraft.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.TaskChainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandSourceStack implements SharedSuggestionProvider {
   public static final SimpleCommandExceptionType ERROR_NOT_PLAYER = new SimpleCommandExceptionType(Component.translatable("permissions.requires.player"));
   public static final SimpleCommandExceptionType ERROR_NOT_ENTITY = new SimpleCommandExceptionType(Component.translatable("permissions.requires.entity"));
   private final CommandSource source;
   private final Vec3 worldPosition;
   private final ServerLevel level;
   private final int permissionLevel;
   private final String textName;
   private final Component displayName;
   private final MinecraftServer server;
   private final boolean silent;
   @Nullable
   private final Entity entity;
   @Nullable
   private final ResultConsumer<CommandSourceStack> consumer;
   private final EntityAnchorArgument.Anchor anchor;
   private final Vec2 rotation;
   private final CommandSigningContext signingContext;
   private final TaskChainer chatMessageChainer;
   private final IntConsumer returnValueConsumer;

   public CommandSourceStack(CommandSource commandsource, Vec3 vec3, Vec2 vec2, ServerLevel serverlevel, int i, String s, Component component, MinecraftServer minecraftserver, @Nullable Entity entity) {
      this(commandsource, vec3, vec2, serverlevel, i, s, component, minecraftserver, entity, false, (commandcontext, flag, k) -> {
      }, EntityAnchorArgument.Anchor.FEET, CommandSigningContext.ANONYMOUS, TaskChainer.immediate(minecraftserver), (j) -> {
      });
   }

   protected CommandSourceStack(CommandSource commandsource, Vec3 vec3, Vec2 vec2, ServerLevel serverlevel, int i, String s, Component component, MinecraftServer minecraftserver, @Nullable Entity entity, boolean flag, @Nullable ResultConsumer<CommandSourceStack> resultconsumer, EntityAnchorArgument.Anchor entityanchorargument_anchor, CommandSigningContext commandsigningcontext, TaskChainer taskchainer, IntConsumer intconsumer) {
      this.source = commandsource;
      this.worldPosition = vec3;
      this.level = serverlevel;
      this.silent = flag;
      this.entity = entity;
      this.permissionLevel = i;
      this.textName = s;
      this.displayName = component;
      this.server = minecraftserver;
      this.consumer = resultconsumer;
      this.anchor = entityanchorargument_anchor;
      this.rotation = vec2;
      this.signingContext = commandsigningcontext;
      this.chatMessageChainer = taskchainer;
      this.returnValueConsumer = intconsumer;
   }

   public CommandSourceStack withSource(CommandSource commandsource) {
      return this.source == commandsource ? this : new CommandSourceStack(commandsource, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer);
   }

   public CommandSourceStack withEntity(Entity entity) {
      return this.entity == entity ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, entity.getName().getString(), entity.getDisplayName(), this.server, entity, this.silent, this.consumer, this.anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer);
   }

   public CommandSourceStack withPosition(Vec3 vec3) {
      return this.worldPosition.equals(vec3) ? this : new CommandSourceStack(this.source, vec3, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer);
   }

   public CommandSourceStack withRotation(Vec2 vec2) {
      return this.rotation.equals(vec2) ? this : new CommandSourceStack(this.source, this.worldPosition, vec2, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer);
   }

   public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> resultconsumer) {
      return Objects.equals(this.consumer, resultconsumer) ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, resultconsumer, this.anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer);
   }

   public CommandSourceStack withCallback(ResultConsumer<CommandSourceStack> resultconsumer, BinaryOperator<ResultConsumer<CommandSourceStack>> binaryoperator) {
      ResultConsumer<CommandSourceStack> resultconsumer1 = binaryoperator.apply(this.consumer, resultconsumer);
      return this.withCallback(resultconsumer1);
   }

   public CommandSourceStack withSuppressedOutput() {
      return !this.silent && !this.source.alwaysAccepts() ? new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, true, this.consumer, this.anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer) : this;
   }

   public CommandSourceStack withPermission(int i) {
      return i == this.permissionLevel ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, i, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer);
   }

   public CommandSourceStack withMaximumPermission(int i) {
      return i <= this.permissionLevel ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, i, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer);
   }

   public CommandSourceStack withAnchor(EntityAnchorArgument.Anchor entityanchorargument_anchor) {
      return entityanchorargument_anchor == this.anchor ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, entityanchorargument_anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer);
   }

   public CommandSourceStack withLevel(ServerLevel serverlevel) {
      if (serverlevel == this.level) {
         return this;
      } else {
         double d0 = DimensionType.getTeleportationScale(this.level.dimensionType(), serverlevel.dimensionType());
         Vec3 vec3 = new Vec3(this.worldPosition.x * d0, this.worldPosition.y, this.worldPosition.z * d0);
         return new CommandSourceStack(this.source, vec3, this.rotation, serverlevel, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor, this.signingContext, this.chatMessageChainer, this.returnValueConsumer);
      }
   }

   public CommandSourceStack facing(Entity entity, EntityAnchorArgument.Anchor entityanchorargument_anchor) {
      return this.facing(entityanchorargument_anchor.apply(entity));
   }

   public CommandSourceStack facing(Vec3 vec3) {
      Vec3 vec31 = this.anchor.apply(this);
      double d0 = vec3.x - vec31.x;
      double d1 = vec3.y - vec31.y;
      double d2 = vec3.z - vec31.z;
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      float f = Mth.wrapDegrees((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
      float f1 = Mth.wrapDegrees((float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
      return this.withRotation(new Vec2(f, f1));
   }

   public CommandSourceStack withSigningContext(CommandSigningContext commandsigningcontext) {
      return commandsigningcontext == this.signingContext ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor, commandsigningcontext, this.chatMessageChainer, this.returnValueConsumer);
   }

   public CommandSourceStack withChatMessageChainer(TaskChainer taskchainer) {
      return taskchainer == this.chatMessageChainer ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor, this.signingContext, taskchainer, this.returnValueConsumer);
   }

   public CommandSourceStack withReturnValueConsumer(IntConsumer intconsumer) {
      return intconsumer == this.returnValueConsumer ? this : new CommandSourceStack(this.source, this.worldPosition, this.rotation, this.level, this.permissionLevel, this.textName, this.displayName, this.server, this.entity, this.silent, this.consumer, this.anchor, this.signingContext, this.chatMessageChainer, intconsumer);
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   public String getTextName() {
      return this.textName;
   }

   public boolean hasPermission(int i) {
      return this.permissionLevel >= i;
   }

   public Vec3 getPosition() {
      return this.worldPosition;
   }

   public ServerLevel getLevel() {
      return this.level;
   }

   @Nullable
   public Entity getEntity() {
      return this.entity;
   }

   public Entity getEntityOrException() throws CommandSyntaxException {
      if (this.entity == null) {
         throw ERROR_NOT_ENTITY.create();
      } else {
         return this.entity;
      }
   }

   public ServerPlayer getPlayerOrException() throws CommandSyntaxException {
      Entity var2 = this.entity;
      if (var2 instanceof ServerPlayer) {
         return (ServerPlayer)var2;
      } else {
         throw ERROR_NOT_PLAYER.create();
      }
   }

   @Nullable
   public ServerPlayer getPlayer() {
      Entity var2 = this.entity;
      ServerPlayer var10000;
      if (var2 instanceof ServerPlayer serverplayer) {
         var10000 = serverplayer;
      } else {
         var10000 = null;
      }

      return var10000;
   }

   public boolean isPlayer() {
      return this.entity instanceof ServerPlayer;
   }

   public Vec2 getRotation() {
      return this.rotation;
   }

   public MinecraftServer getServer() {
      return this.server;
   }

   public EntityAnchorArgument.Anchor getAnchor() {
      return this.anchor;
   }

   public CommandSigningContext getSigningContext() {
      return this.signingContext;
   }

   public TaskChainer getChatMessageChainer() {
      return this.chatMessageChainer;
   }

   public IntConsumer getReturnValueConsumer() {
      return this.returnValueConsumer;
   }

   public boolean shouldFilterMessageTo(ServerPlayer serverplayer) {
      ServerPlayer serverplayer1 = this.getPlayer();
      if (serverplayer == serverplayer1) {
         return false;
      } else {
         return serverplayer1 != null && serverplayer1.isTextFilteringEnabled() || serverplayer.isTextFilteringEnabled();
      }
   }

   public void sendChatMessage(OutgoingChatMessage outgoingchatmessage, boolean flag, ChatType.Bound chattype_bound) {
      if (!this.silent) {
         ServerPlayer serverplayer = this.getPlayer();
         if (serverplayer != null) {
            serverplayer.sendChatMessage(outgoingchatmessage, flag, chattype_bound);
         } else {
            this.source.sendSystemMessage(chattype_bound.decorate(outgoingchatmessage.content()));
         }

      }
   }

   public void sendSystemMessage(Component component) {
      if (!this.silent) {
         ServerPlayer serverplayer = this.getPlayer();
         if (serverplayer != null) {
            serverplayer.sendSystemMessage(component);
         } else {
            this.source.sendSystemMessage(component);
         }

      }
   }

   public void sendSuccess(Supplier<Component> supplier, boolean flag) {
      boolean flag1 = this.source.acceptsSuccess() && !this.silent;
      boolean flag2 = flag && this.source.shouldInformAdmins() && !this.silent;
      if (flag1 || flag2) {
         Component component = supplier.get();
         if (flag1) {
            this.source.sendSystemMessage(component);
         }

         if (flag2) {
            this.broadcastToAdmins(component);
         }

      }
   }

   private void broadcastToAdmins(Component component) {
      Component component1 = Component.translatable("chat.type.admin", this.getDisplayName(), component).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
      if (this.server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
         for(ServerPlayer serverplayer : this.server.getPlayerList().getPlayers()) {
            if (serverplayer != this.source && this.server.getPlayerList().isOp(serverplayer.getGameProfile())) {
               serverplayer.sendSystemMessage(component1);
            }
         }
      }

      if (this.source != this.server && this.server.getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
         this.server.sendSystemMessage(component1);
      }

   }

   public void sendFailure(Component component) {
      if (this.source.acceptsFailure() && !this.silent) {
         this.source.sendSystemMessage(Component.empty().append(component).withStyle(ChatFormatting.RED));
      }

   }

   public void onCommandComplete(CommandContext<CommandSourceStack> commandcontext, boolean flag, int i) {
      if (this.consumer != null) {
         this.consumer.onCommandComplete(commandcontext, flag, i);
      }

   }

   public Collection<String> getOnlinePlayerNames() {
      return Lists.newArrayList(this.server.getPlayerNames());
   }

   public Collection<String> getAllTeams() {
      return this.server.getScoreboard().getTeamNames();
   }

   public Stream<ResourceLocation> getAvailableSounds() {
      return BuiltInRegistries.SOUND_EVENT.stream().map(SoundEvent::getLocation);
   }

   public Stream<ResourceLocation> getRecipeNames() {
      return this.server.getRecipeManager().getRecipeIds();
   }

   public CompletableFuture<Suggestions> customSuggestion(CommandContext<?> commandcontext) {
      return Suggestions.empty();
   }

   public CompletableFuture<Suggestions> suggestRegistryElements(ResourceKey<? extends Registry<?>> resourcekey, SharedSuggestionProvider.ElementSuggestionType sharedsuggestionprovider_elementsuggestiontype, SuggestionsBuilder suggestionsbuilder, CommandContext<?> commandcontext) {
      return this.registryAccess().registry(resourcekey).map((registry) -> {
         this.suggestRegistryElements(registry, sharedsuggestionprovider_elementsuggestiontype, suggestionsbuilder);
         return suggestionsbuilder.buildFuture();
      }).orElseGet(Suggestions::empty);
   }

   public Set<ResourceKey<Level>> levels() {
      return this.server.levelKeys();
   }

   public RegistryAccess registryAccess() {
      return this.server.registryAccess();
   }

   public FeatureFlagSet enabledFeatures() {
      return this.level.enabledFeatures();
   }
}
