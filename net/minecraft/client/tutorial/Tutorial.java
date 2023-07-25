package net.minecraft.client.tutorial;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class Tutorial {
   private final Minecraft minecraft;
   @Nullable
   private TutorialStepInstance instance;
   private final List<Tutorial.TimedToast> timedToasts = Lists.newArrayList();
   private final BundleTutorial bundleTutorial;

   public Tutorial(Minecraft minecraft, Options options) {
      this.minecraft = minecraft;
      this.bundleTutorial = new BundleTutorial(this, options);
   }

   public void onInput(Input input) {
      if (this.instance != null) {
         this.instance.onInput(input);
      }

   }

   public void onMouse(double d0, double d1) {
      if (this.instance != null) {
         this.instance.onMouse(d0, d1);
      }

   }

   public void onLookAt(@Nullable ClientLevel clientlevel, @Nullable HitResult hitresult) {
      if (this.instance != null && hitresult != null && clientlevel != null) {
         this.instance.onLookAt(clientlevel, hitresult);
      }

   }

   public void onDestroyBlock(ClientLevel clientlevel, BlockPos blockpos, BlockState blockstate, float f) {
      if (this.instance != null) {
         this.instance.onDestroyBlock(clientlevel, blockpos, blockstate, f);
      }

   }

   public void onOpenInventory() {
      if (this.instance != null) {
         this.instance.onOpenInventory();
      }

   }

   public void onGetItem(ItemStack itemstack) {
      if (this.instance != null) {
         this.instance.onGetItem(itemstack);
      }

   }

   public void stop() {
      if (this.instance != null) {
         this.instance.clear();
         this.instance = null;
      }
   }

   public void start() {
      if (this.instance != null) {
         this.stop();
      }

      this.instance = this.minecraft.options.tutorialStep.create(this);
   }

   public void addTimedToast(TutorialToast tutorialtoast, int i) {
      this.timedToasts.add(new Tutorial.TimedToast(tutorialtoast, i));
      this.minecraft.getToasts().addToast(tutorialtoast);
   }

   public void removeTimedToast(TutorialToast tutorialtoast) {
      this.timedToasts.removeIf((tutorial_timedtoast) -> tutorial_timedtoast.toast == tutorialtoast);
      tutorialtoast.hide();
   }

   public void tick() {
      this.timedToasts.removeIf(Tutorial.TimedToast::updateProgress);
      if (this.instance != null) {
         if (this.minecraft.level != null) {
            this.instance.tick();
         } else {
            this.stop();
         }
      } else if (this.minecraft.level != null) {
         this.start();
      }

   }

   public void setStep(TutorialSteps tutorialsteps) {
      this.minecraft.options.tutorialStep = tutorialsteps;
      this.minecraft.options.save();
      if (this.instance != null) {
         this.instance.clear();
         this.instance = tutorialsteps.create(this);
      }

   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   public boolean isSurvival() {
      if (this.minecraft.gameMode == null) {
         return false;
      } else {
         return this.minecraft.gameMode.getPlayerMode() == GameType.SURVIVAL;
      }
   }

   public static Component key(String s) {
      return Component.keybind("key." + s).withStyle(ChatFormatting.BOLD);
   }

   public void onInventoryAction(ItemStack itemstack, ItemStack itemstack1, ClickAction clickaction) {
      this.bundleTutorial.onInventoryAction(itemstack, itemstack1, clickaction);
   }

   static final class TimedToast {
      final TutorialToast toast;
      private final int durationTicks;
      private int progress;

      TimedToast(TutorialToast tutorialtoast, int i) {
         this.toast = tutorialtoast;
         this.durationTicks = i;
      }

      private boolean updateProgress() {
         this.toast.updateProgress(Math.min((float)(++this.progress) / (float)this.durationTicks, 1.0F));
         if (this.progress > this.durationTicks) {
            this.toast.hide();
            return true;
         } else {
            return false;
         }
      }
   }
}
