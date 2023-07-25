package net.minecraft.world.level;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicLike;
import java.util.Comparator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class GameRules {
   public static final int DEFAULT_RANDOM_TICK_SPEED = 3;
   static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<GameRules.Key<?>, GameRules.Type<?>> GAME_RULE_TYPES = Maps.newTreeMap(Comparator.comparing((gamerules_key) -> gamerules_key.id));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOFIRETICK = register("doFireTick", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_MOBGRIEFING = register("mobGriefing", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_KEEPINVENTORY = register("keepInventory", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBSPAWNING = register("doMobSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOMOBLOOT = register("doMobLoot", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOBLOCKDROPS = register("doTileDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOENTITYDROPS = register("doEntityDrops", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_COMMANDBLOCKOUTPUT = register("commandBlockOutput", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_NATURAL_REGENERATION = register("naturalRegeneration", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DAYLIGHT = register("doDaylightCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_LOGADMINCOMMANDS = register("logAdminCommands", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_SHOWDEATHMESSAGES = register("showDeathMessages", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_RANDOMTICKING = register("randomTickSpeed", GameRules.Category.UPDATES, GameRules.IntegerValue.create(3));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_SENDCOMMANDFEEDBACK = register("sendCommandFeedback", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_REDUCEDDEBUGINFO = register("reducedDebugInfo", GameRules.Category.MISC, GameRules.BooleanValue.create(false, (minecraftserver, gamerules_booleanvalue) -> {
      byte b0 = (byte)(gamerules_booleanvalue.get() ? 22 : 23);

      for(ServerPlayer serverplayer : minecraftserver.getPlayerList().getPlayers()) {
         serverplayer.connection.send(new ClientboundEntityEventPacket(serverplayer, b0));
      }

   }));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_SPECTATORSGENERATECHUNKS = register("spectatorsGenerateChunks", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_SPAWN_RADIUS = register("spawnRadius", GameRules.Category.PLAYER, GameRules.IntegerValue.create(10));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_ELYTRA_MOVEMENT_CHECK = register("disableElytraMovementCheck", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_ENTITY_CRAMMING = register("maxEntityCramming", GameRules.Category.MOBS, GameRules.IntegerValue.create(24));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_WEATHER_CYCLE = register("doWeatherCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_LIMITED_CRAFTING = register("doLimitedCrafting", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_COMMAND_CHAIN_LENGTH = register("maxCommandChainLength", GameRules.Category.MISC, GameRules.IntegerValue.create(65536));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_COMMAND_MODIFICATION_BLOCK_LIMIT = register("commandModificationBlockLimit", GameRules.Category.MISC, GameRules.IntegerValue.create(32768));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_ANNOUNCE_ADVANCEMENTS = register("announceAdvancements", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DISABLE_RAIDS = register("disableRaids", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DOINSOMNIA = register("doInsomnia", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_IMMEDIATE_RESPAWN = register("doImmediateRespawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (minecraftserver, gamerules_booleanvalue) -> {
      for(ServerPlayer serverplayer : minecraftserver.getPlayerList().getPlayers()) {
         serverplayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, gamerules_booleanvalue.get() ? 1.0F : 0.0F));
      }

   }));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DROWNING_DAMAGE = register("drowningDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_FALL_DAMAGE = register("fallDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_FIRE_DAMAGE = register("fireDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_FREEZE_DAMAGE = register("freezeDamage", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_PATROL_SPAWNING = register("doPatrolSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_TRADER_SPAWNING = register("doTraderSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_WARDEN_SPAWNING = register("doWardenSpawning", GameRules.Category.SPAWNING, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_FORGIVE_DEAD_PLAYERS = register("forgiveDeadPlayers", GameRules.Category.MOBS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_UNIVERSAL_ANGER = register("universalAnger", GameRules.Category.MOBS, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_PLAYERS_SLEEPING_PERCENTAGE = register("playersSleepingPercentage", GameRules.Category.PLAYER, GameRules.IntegerValue.create(100));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_BLOCK_EXPLOSION_DROP_DECAY = register("blockExplosionDropDecay", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_MOB_EXPLOSION_DROP_DECAY = register("mobExplosionDropDecay", GameRules.Category.DROPS, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_TNT_EXPLOSION_DROP_DECAY = register("tntExplosionDropDecay", GameRules.Category.DROPS, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.IntegerValue> RULE_SNOW_ACCUMULATION_HEIGHT = register("snowAccumulationHeight", GameRules.Category.UPDATES, GameRules.IntegerValue.create(1));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_WATER_SOURCE_CONVERSION = register("waterSourceConversion", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_LAVA_SOURCE_CONVERSION = register("lavaSourceConversion", GameRules.Category.UPDATES, GameRules.BooleanValue.create(false));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_GLOBAL_SOUND_EVENTS = register("globalSoundEvents", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
   public static final GameRules.Key<GameRules.BooleanValue> RULE_DO_VINES_SPREAD = register("doVinesSpread", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
   private final Map<GameRules.Key<?>, GameRules.Value<?>> rules;

   private static <T extends GameRules.Value<T>> GameRules.Key<T> register(String s, GameRules.Category gamerules_category, GameRules.Type<T> gamerules_type) {
      GameRules.Key<T> gamerules_key = new GameRules.Key<>(s, gamerules_category);
      GameRules.Type<?> gamerules_type1 = GAME_RULE_TYPES.put(gamerules_key, gamerules_type);
      if (gamerules_type1 != null) {
         throw new IllegalStateException("Duplicate game rule registration for " + s);
      } else {
         return gamerules_key;
      }
   }

   public GameRules(DynamicLike<?> dynamiclike) {
      this();
      this.loadFromTag(dynamiclike);
   }

   public GameRules() {
      this.rules = GAME_RULE_TYPES.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (map_entry) -> map_entry.getValue().createRule()));
   }

   private GameRules(Map<GameRules.Key<?>, GameRules.Value<?>> map) {
      this.rules = map;
   }

   public <T extends GameRules.Value<T>> T getRule(GameRules.Key<T> gamerules_key) {
      return (T)(this.rules.get(gamerules_key));
   }

   public CompoundTag createTag() {
      CompoundTag compoundtag = new CompoundTag();
      this.rules.forEach((gamerules_key, gamerules_value) -> compoundtag.putString(gamerules_key.id, gamerules_value.serialize()));
      return compoundtag;
   }

   private void loadFromTag(DynamicLike<?> dynamiclike) {
      this.rules.forEach((gamerules_key, gamerules_value) -> dynamiclike.get(gamerules_key.id).asString().result().ifPresent(gamerules_value::deserialize));
   }

   public GameRules copy() {
      return new GameRules(this.rules.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (map_entry) -> map_entry.getValue().copy())));
   }

   public static void visitGameRuleTypes(GameRules.GameRuleTypeVisitor gamerules_gameruletypevisitor) {
      GAME_RULE_TYPES.forEach((gamerules_key, gamerules_type) -> callVisitorCap(gamerules_gameruletypevisitor, gamerules_key, gamerules_type));
   }

   private static <T extends GameRules.Value<T>> void callVisitorCap(GameRules.GameRuleTypeVisitor gamerules_gameruletypevisitor, GameRules.Key<?> gamerules_key, GameRules.Type<?> gamerules_type) {
      gamerules_gameruletypevisitor.visit(gamerules_key, gamerules_type);
      gamerules_type.callVisitor(gamerules_gameruletypevisitor, gamerules_key);
   }

   public void assignFrom(GameRules gamerules, @Nullable MinecraftServer minecraftserver) {
      gamerules.rules.keySet().forEach((gamerules_key) -> this.assignCap(gamerules_key, gamerules, minecraftserver));
   }

   private <T extends GameRules.Value<T>> void assignCap(GameRules.Key<T> gamerules_key, GameRules gamerules, @Nullable MinecraftServer minecraftserver) {
      T gamerules_value = gamerules.getRule(gamerules_key);
      this.<T>getRule(gamerules_key).setFrom(gamerules_value, minecraftserver);
   }

   public boolean getBoolean(GameRules.Key<GameRules.BooleanValue> gamerules_key) {
      return this.getRule(gamerules_key).get();
   }

   public int getInt(GameRules.Key<GameRules.IntegerValue> gamerules_key) {
      return this.getRule(gamerules_key).get();
   }

   public static class BooleanValue extends GameRules.Value<GameRules.BooleanValue> {
      private boolean value;

      static GameRules.Type<GameRules.BooleanValue> create(boolean flag, BiConsumer<MinecraftServer, GameRules.BooleanValue> biconsumer) {
         return new GameRules.Type<>(BoolArgumentType::bool, (gamerules_type) -> new GameRules.BooleanValue(gamerules_type, flag), biconsumer, GameRules.GameRuleTypeVisitor::visitBoolean);
      }

      static GameRules.Type<GameRules.BooleanValue> create(boolean flag) {
         return create(flag, (minecraftserver, gamerules_booleanvalue) -> {
         });
      }

      public BooleanValue(GameRules.Type<GameRules.BooleanValue> gamerules_type, boolean flag) {
         super(gamerules_type);
         this.value = flag;
      }

      protected void updateFromArgument(CommandContext<CommandSourceStack> commandcontext, String s) {
         this.value = BoolArgumentType.getBool(commandcontext, s);
      }

      public boolean get() {
         return this.value;
      }

      public void set(boolean flag, @Nullable MinecraftServer minecraftserver) {
         this.value = flag;
         this.onChanged(minecraftserver);
      }

      public String serialize() {
         return Boolean.toString(this.value);
      }

      protected void deserialize(String s) {
         this.value = Boolean.parseBoolean(s);
      }

      public int getCommandResult() {
         return this.value ? 1 : 0;
      }

      protected GameRules.BooleanValue getSelf() {
         return this;
      }

      protected GameRules.BooleanValue copy() {
         return new GameRules.BooleanValue(this.type, this.value);
      }

      public void setFrom(GameRules.BooleanValue gamerules_booleanvalue, @Nullable MinecraftServer minecraftserver) {
         this.value = gamerules_booleanvalue.value;
         this.onChanged(minecraftserver);
      }
   }

   public static enum Category {
      PLAYER("gamerule.category.player"),
      MOBS("gamerule.category.mobs"),
      SPAWNING("gamerule.category.spawning"),
      DROPS("gamerule.category.drops"),
      UPDATES("gamerule.category.updates"),
      CHAT("gamerule.category.chat"),
      MISC("gamerule.category.misc");

      private final String descriptionId;

      private Category(String s) {
         this.descriptionId = s;
      }

      public String getDescriptionId() {
         return this.descriptionId;
      }
   }

   public interface GameRuleTypeVisitor {
      default <T extends GameRules.Value<T>> void visit(GameRules.Key<T> gamerules_key, GameRules.Type<T> gamerules_type) {
      }

      default void visitBoolean(GameRules.Key<GameRules.BooleanValue> gamerules_key, GameRules.Type<GameRules.BooleanValue> gamerules_type) {
      }

      default void visitInteger(GameRules.Key<GameRules.IntegerValue> gamerules_key, GameRules.Type<GameRules.IntegerValue> gamerules_type) {
      }
   }

   public static class IntegerValue extends GameRules.Value<GameRules.IntegerValue> {
      private int value;

      private static GameRules.Type<GameRules.IntegerValue> create(int i, BiConsumer<MinecraftServer, GameRules.IntegerValue> biconsumer) {
         return new GameRules.Type<>(IntegerArgumentType::integer, (gamerules_type) -> new GameRules.IntegerValue(gamerules_type, i), biconsumer, GameRules.GameRuleTypeVisitor::visitInteger);
      }

      static GameRules.Type<GameRules.IntegerValue> create(int i) {
         return create(i, (minecraftserver, gamerules_integervalue) -> {
         });
      }

      public IntegerValue(GameRules.Type<GameRules.IntegerValue> gamerules_type, int i) {
         super(gamerules_type);
         this.value = i;
      }

      protected void updateFromArgument(CommandContext<CommandSourceStack> commandcontext, String s) {
         this.value = IntegerArgumentType.getInteger(commandcontext, s);
      }

      public int get() {
         return this.value;
      }

      public void set(int i, @Nullable MinecraftServer minecraftserver) {
         this.value = i;
         this.onChanged(minecraftserver);
      }

      public String serialize() {
         return Integer.toString(this.value);
      }

      protected void deserialize(String s) {
         this.value = safeParse(s);
      }

      public boolean tryDeserialize(String s) {
         try {
            this.value = Integer.parseInt(s);
            return true;
         } catch (NumberFormatException var3) {
            return false;
         }
      }

      private static int safeParse(String s) {
         if (!s.isEmpty()) {
            try {
               return Integer.parseInt(s);
            } catch (NumberFormatException var2) {
               GameRules.LOGGER.warn("Failed to parse integer {}", (Object)s);
            }
         }

         return 0;
      }

      public int getCommandResult() {
         return this.value;
      }

      protected GameRules.IntegerValue getSelf() {
         return this;
      }

      protected GameRules.IntegerValue copy() {
         return new GameRules.IntegerValue(this.type, this.value);
      }

      public void setFrom(GameRules.IntegerValue gamerules_integervalue, @Nullable MinecraftServer minecraftserver) {
         this.value = gamerules_integervalue.value;
         this.onChanged(minecraftserver);
      }
   }

   public static final class Key<T extends GameRules.Value<T>> {
      final String id;
      private final GameRules.Category category;

      public Key(String s, GameRules.Category gamerules_category) {
         this.id = s;
         this.category = gamerules_category;
      }

      public String toString() {
         return this.id;
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else {
            return object instanceof GameRules.Key && ((GameRules.Key)object).id.equals(this.id);
         }
      }

      public int hashCode() {
         return this.id.hashCode();
      }

      public String getId() {
         return this.id;
      }

      public String getDescriptionId() {
         return "gamerule." + this.id;
      }

      public GameRules.Category getCategory() {
         return this.category;
      }
   }

   public static class Type<T extends GameRules.Value<T>> {
      private final Supplier<ArgumentType<?>> argument;
      private final Function<GameRules.Type<T>, T> constructor;
      final BiConsumer<MinecraftServer, T> callback;
      private final GameRules.VisitorCaller<T> visitorCaller;

      Type(Supplier<ArgumentType<?>> supplier, Function<GameRules.Type<T>, T> function, BiConsumer<MinecraftServer, T> biconsumer, GameRules.VisitorCaller<T> gamerules_visitorcaller) {
         this.argument = supplier;
         this.constructor = function;
         this.callback = biconsumer;
         this.visitorCaller = gamerules_visitorcaller;
      }

      public RequiredArgumentBuilder<CommandSourceStack, ?> createArgument(String s) {
         return Commands.argument(s, this.argument.get());
      }

      public T createRule() {
         return this.constructor.apply(this);
      }

      public void callVisitor(GameRules.GameRuleTypeVisitor gamerules_gameruletypevisitor, GameRules.Key<T> gamerules_key) {
         this.visitorCaller.call(gamerules_gameruletypevisitor, gamerules_key, this);
      }
   }

   public abstract static class Value<T extends GameRules.Value<T>> {
      protected final GameRules.Type<T> type;

      public Value(GameRules.Type<T> gamerules_type) {
         this.type = gamerules_type;
      }

      protected abstract void updateFromArgument(CommandContext<CommandSourceStack> commandcontext, String s);

      public void setFromArgument(CommandContext<CommandSourceStack> commandcontext, String s) {
         this.updateFromArgument(commandcontext, s);
         this.onChanged(commandcontext.getSource().getServer());
      }

      protected void onChanged(@Nullable MinecraftServer minecraftserver) {
         if (minecraftserver != null) {
            this.type.callback.accept(minecraftserver, this.getSelf());
         }

      }

      protected abstract void deserialize(String s);

      public abstract String serialize();

      public String toString() {
         return this.serialize();
      }

      public abstract int getCommandResult();

      protected abstract T getSelf();

      protected abstract T copy();

      public abstract void setFrom(T gamerules_value, @Nullable MinecraftServer minecraftserver);
   }

   interface VisitorCaller<T extends GameRules.Value<T>> {
      void call(GameRules.GameRuleTypeVisitor gamerules_gameruletypevisitor, GameRules.Key<T> gamerules_key, GameRules.Type<T> gamerules_type);
   }
}
