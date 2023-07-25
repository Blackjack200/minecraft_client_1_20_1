package net.minecraft.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class PlayerAdvancements {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer()).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).setPrettyPrinting().create();
   private static final TypeToken<Map<ResourceLocation, AdvancementProgress>> TYPE_TOKEN = new TypeToken<Map<ResourceLocation, AdvancementProgress>>() {
   };
   private final DataFixer dataFixer;
   private final PlayerList playerList;
   private final Path playerSavePath;
   private final Map<Advancement, AdvancementProgress> progress = new LinkedHashMap<>();
   private final Set<Advancement> visible = new HashSet<>();
   private final Set<Advancement> progressChanged = new HashSet<>();
   private final Set<Advancement> rootsToUpdate = new HashSet<>();
   private ServerPlayer player;
   @Nullable
   private Advancement lastSelectedTab;
   private boolean isFirstPacket = true;

   public PlayerAdvancements(DataFixer datafixer, PlayerList playerlist, ServerAdvancementManager serveradvancementmanager, Path path, ServerPlayer serverplayer) {
      this.dataFixer = datafixer;
      this.playerList = playerlist;
      this.playerSavePath = path;
      this.player = serverplayer;
      this.load(serveradvancementmanager);
   }

   public void setPlayer(ServerPlayer serverplayer) {
      this.player = serverplayer;
   }

   public void stopListening() {
      for(CriterionTrigger<?> criteriontrigger : CriteriaTriggers.all()) {
         criteriontrigger.removePlayerListeners(this);
      }

   }

   public void reload(ServerAdvancementManager serveradvancementmanager) {
      this.stopListening();
      this.progress.clear();
      this.visible.clear();
      this.rootsToUpdate.clear();
      this.progressChanged.clear();
      this.isFirstPacket = true;
      this.lastSelectedTab = null;
      this.load(serveradvancementmanager);
   }

   private void registerListeners(ServerAdvancementManager serveradvancementmanager) {
      for(Advancement advancement : serveradvancementmanager.getAllAdvancements()) {
         this.registerListeners(advancement);
      }

   }

   private void checkForAutomaticTriggers(ServerAdvancementManager serveradvancementmanager) {
      for(Advancement advancement : serveradvancementmanager.getAllAdvancements()) {
         if (advancement.getCriteria().isEmpty()) {
            this.award(advancement, "");
            advancement.getRewards().grant(this.player);
         }
      }

   }

   private void load(ServerAdvancementManager serveradvancementmanager) {
      if (Files.isRegularFile(this.playerSavePath)) {
         try {
            JsonReader jsonreader = new JsonReader(Files.newBufferedReader(this.playerSavePath, StandardCharsets.UTF_8));

            try {
               jsonreader.setLenient(false);
               Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, Streams.parse(jsonreader));
               int i = dynamic.get("DataVersion").asInt(1343);
               dynamic = dynamic.remove("DataVersion");
               dynamic = DataFixTypes.ADVANCEMENTS.updateToCurrentVersion(this.dataFixer, dynamic, i);
               Map<ResourceLocation, AdvancementProgress> map = GSON.getAdapter(TYPE_TOKEN).fromJsonTree(dynamic.getValue());
               if (map == null) {
                  throw new JsonParseException("Found null for advancements");
               }

               map.entrySet().stream().sorted(Entry.comparingByValue()).forEach((map_entry) -> {
                  Advancement advancement = serveradvancementmanager.getAdvancement(map_entry.getKey());
                  if (advancement == null) {
                     LOGGER.warn("Ignored advancement '{}' in progress file {} - it doesn't exist anymore?", map_entry.getKey(), this.playerSavePath);
                  } else {
                     this.startProgress(advancement, map_entry.getValue());
                     this.progressChanged.add(advancement);
                     this.markForVisibilityUpdate(advancement);
                  }
               });
            } catch (Throwable var7) {
               try {
                  jsonreader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            jsonreader.close();
         } catch (JsonParseException var8) {
            LOGGER.error("Couldn't parse player advancements in {}", this.playerSavePath, var8);
         } catch (IOException var9) {
            LOGGER.error("Couldn't access player advancements in {}", this.playerSavePath, var9);
         }
      }

      this.checkForAutomaticTriggers(serveradvancementmanager);
      this.registerListeners(serveradvancementmanager);
   }

   public void save() {
      Map<ResourceLocation, AdvancementProgress> map = new LinkedHashMap<>();

      for(Map.Entry<Advancement, AdvancementProgress> map_entry : this.progress.entrySet()) {
         AdvancementProgress advancementprogress = map_entry.getValue();
         if (advancementprogress.hasProgress()) {
            map.put(map_entry.getKey().getId(), advancementprogress);
         }
      }

      JsonElement jsonelement = GSON.toJsonTree(map);
      jsonelement.getAsJsonObject().addProperty("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());

      try {
         FileUtil.createDirectoriesSafe(this.playerSavePath.getParent());
         Writer writer = Files.newBufferedWriter(this.playerSavePath, StandardCharsets.UTF_8);

         try {
            GSON.toJson(jsonelement, writer);
         } catch (Throwable var7) {
            if (writer != null) {
               try {
                  writer.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException var8) {
         LOGGER.error("Couldn't save player advancements to {}", this.playerSavePath, var8);
      }

   }

   public boolean award(Advancement advancement, String s) {
      boolean flag = false;
      AdvancementProgress advancementprogress = this.getOrStartProgress(advancement);
      boolean flag1 = advancementprogress.isDone();
      if (advancementprogress.grantProgress(s)) {
         this.unregisterListeners(advancement);
         this.progressChanged.add(advancement);
         flag = true;
         if (!flag1 && advancementprogress.isDone()) {
            advancement.getRewards().grant(this.player);
            if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceChat() && this.player.level().getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
               this.playerList.broadcastSystemMessage(Component.translatable("chat.type.advancement." + advancement.getDisplay().getFrame().getName(), this.player.getDisplayName(), advancement.getChatComponent()), false);
            }
         }
      }

      if (!flag1 && advancementprogress.isDone()) {
         this.markForVisibilityUpdate(advancement);
      }

      return flag;
   }

   public boolean revoke(Advancement advancement, String s) {
      boolean flag = false;
      AdvancementProgress advancementprogress = this.getOrStartProgress(advancement);
      boolean flag1 = advancementprogress.isDone();
      if (advancementprogress.revokeProgress(s)) {
         this.registerListeners(advancement);
         this.progressChanged.add(advancement);
         flag = true;
      }

      if (flag1 && !advancementprogress.isDone()) {
         this.markForVisibilityUpdate(advancement);
      }

      return flag;
   }

   private void markForVisibilityUpdate(Advancement advancement) {
      this.rootsToUpdate.add(advancement.getRoot());
   }

   private void registerListeners(Advancement advancement) {
      AdvancementProgress advancementprogress = this.getOrStartProgress(advancement);
      if (!advancementprogress.isDone()) {
         for(Map.Entry<String, Criterion> map_entry : advancement.getCriteria().entrySet()) {
            CriterionProgress criterionprogress = advancementprogress.getCriterion(map_entry.getKey());
            if (criterionprogress != null && !criterionprogress.isDone()) {
               CriterionTriggerInstance criteriontriggerinstance = map_entry.getValue().getTrigger();
               if (criteriontriggerinstance != null) {
                  CriterionTrigger<CriterionTriggerInstance> criteriontrigger = CriteriaTriggers.getCriterion(criteriontriggerinstance.getCriterion());
                  if (criteriontrigger != null) {
                     criteriontrigger.addPlayerListener(this, new CriterionTrigger.Listener<>(criteriontriggerinstance, advancement, map_entry.getKey()));
                  }
               }
            }
         }

      }
   }

   private void unregisterListeners(Advancement advancement) {
      AdvancementProgress advancementprogress = this.getOrStartProgress(advancement);

      for(Map.Entry<String, Criterion> map_entry : advancement.getCriteria().entrySet()) {
         CriterionProgress criterionprogress = advancementprogress.getCriterion(map_entry.getKey());
         if (criterionprogress != null && (criterionprogress.isDone() || advancementprogress.isDone())) {
            CriterionTriggerInstance criteriontriggerinstance = map_entry.getValue().getTrigger();
            if (criteriontriggerinstance != null) {
               CriterionTrigger<CriterionTriggerInstance> criteriontrigger = CriteriaTriggers.getCriterion(criteriontriggerinstance.getCriterion());
               if (criteriontrigger != null) {
                  criteriontrigger.removePlayerListener(this, new CriterionTrigger.Listener<>(criteriontriggerinstance, advancement, map_entry.getKey()));
               }
            }
         }
      }

   }

   public void flushDirty(ServerPlayer serverplayer) {
      if (this.isFirstPacket || !this.rootsToUpdate.isEmpty() || !this.progressChanged.isEmpty()) {
         Map<ResourceLocation, AdvancementProgress> map = new HashMap<>();
         Set<Advancement> set = new HashSet<>();
         Set<ResourceLocation> set1 = new HashSet<>();

         for(Advancement advancement : this.rootsToUpdate) {
            this.updateTreeVisibility(advancement, set, set1);
         }

         this.rootsToUpdate.clear();

         for(Advancement advancement1 : this.progressChanged) {
            if (this.visible.contains(advancement1)) {
               map.put(advancement1.getId(), this.progress.get(advancement1));
            }
         }

         this.progressChanged.clear();
         if (!map.isEmpty() || !set.isEmpty() || !set1.isEmpty()) {
            serverplayer.connection.send(new ClientboundUpdateAdvancementsPacket(this.isFirstPacket, set, set1, map));
         }
      }

      this.isFirstPacket = false;
   }

   public void setSelectedTab(@Nullable Advancement advancement) {
      Advancement advancement1 = this.lastSelectedTab;
      if (advancement != null && advancement.getParent() == null && advancement.getDisplay() != null) {
         this.lastSelectedTab = advancement;
      } else {
         this.lastSelectedTab = null;
      }

      if (advancement1 != this.lastSelectedTab) {
         this.player.connection.send(new ClientboundSelectAdvancementsTabPacket(this.lastSelectedTab == null ? null : this.lastSelectedTab.getId()));
      }

   }

   public AdvancementProgress getOrStartProgress(Advancement advancement) {
      AdvancementProgress advancementprogress = this.progress.get(advancement);
      if (advancementprogress == null) {
         advancementprogress = new AdvancementProgress();
         this.startProgress(advancement, advancementprogress);
      }

      return advancementprogress;
   }

   private void startProgress(Advancement advancement, AdvancementProgress advancementprogress) {
      advancementprogress.update(advancement.getCriteria(), advancement.getRequirements());
      this.progress.put(advancement, advancementprogress);
   }

   private void updateTreeVisibility(Advancement advancement, Set<Advancement> set, Set<ResourceLocation> set1) {
      AdvancementVisibilityEvaluator.evaluateVisibility(advancement, (advancement2) -> this.getOrStartProgress(advancement2).isDone(), (advancement1, flag) -> {
         if (flag) {
            if (this.visible.add(advancement1)) {
               set.add(advancement1);
               if (this.progress.containsKey(advancement1)) {
                  this.progressChanged.add(advancement1);
               }
            }
         } else if (this.visible.remove(advancement1)) {
            set1.add(advancement1.getId());
         }

      });
   }
}
