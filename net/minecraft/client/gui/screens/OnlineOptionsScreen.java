package net.minecraft.client.gui.screens;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import org.apache.commons.compress.utils.Lists;

public class OnlineOptionsScreen extends SimpleOptionsSubScreen {
   @Nullable
   private final OptionInstance<Unit> difficultyDisplay;

   public static OnlineOptionsScreen createOnlineOptionsScreen(Minecraft minecraft, Screen screen, Options options) {
      List<OptionInstance<?>> list = Lists.newArrayList();
      list.add(options.realmsNotifications());
      list.add(options.allowServerListing());
      OptionInstance<Unit> optioninstance = Optionull.map(minecraft.level, (clientlevel) -> {
         Difficulty difficulty = clientlevel.getDifficulty();
         return new OptionInstance<>("options.difficulty.online", OptionInstance.noTooltip(), (component, unit1) -> difficulty.getDisplayName(), new OptionInstance.Enum<>(List.of(Unit.INSTANCE), Codec.EMPTY.codec()), Unit.INSTANCE, (unit) -> {
         });
      });
      if (optioninstance != null) {
         list.add(optioninstance);
      }

      return new OnlineOptionsScreen(screen, options, list.toArray(new OptionInstance[0]), optioninstance);
   }

   private OnlineOptionsScreen(Screen screen, Options options, OptionInstance<?>[] aoptioninstance, @Nullable OptionInstance<Unit> optioninstance) {
      super(screen, options, Component.translatable("options.online.title"), aoptioninstance);
      this.difficultyDisplay = optioninstance;
   }

   protected void init() {
      super.init();
      if (this.difficultyDisplay != null) {
         AbstractWidget abstractwidget = this.list.findOption(this.difficultyDisplay);
         if (abstractwidget != null) {
            abstractwidget.active = false;
         }
      }

      AbstractWidget abstractwidget1 = this.list.findOption(this.options.telemetryOptInExtra());
      if (abstractwidget1 != null) {
         abstractwidget1.active = this.minecraft.extraTelemetryAvailable();
      }

   }
}
