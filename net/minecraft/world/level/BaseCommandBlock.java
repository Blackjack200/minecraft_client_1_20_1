package net.minecraft.world.level;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public abstract class BaseCommandBlock implements CommandSource {
   private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
   private static final Component DEFAULT_NAME = Component.literal("@");
   private long lastExecution = -1L;
   private boolean updateLastExecution = true;
   private int successCount;
   private boolean trackOutput = true;
   @Nullable
   private Component lastOutput;
   private String command = "";
   private Component name = DEFAULT_NAME;

   public int getSuccessCount() {
      return this.successCount;
   }

   public void setSuccessCount(int i) {
      this.successCount = i;
   }

   public Component getLastOutput() {
      return this.lastOutput == null ? CommonComponents.EMPTY : this.lastOutput;
   }

   public CompoundTag save(CompoundTag compoundtag) {
      compoundtag.putString("Command", this.command);
      compoundtag.putInt("SuccessCount", this.successCount);
      compoundtag.putString("CustomName", Component.Serializer.toJson(this.name));
      compoundtag.putBoolean("TrackOutput", this.trackOutput);
      if (this.lastOutput != null && this.trackOutput) {
         compoundtag.putString("LastOutput", Component.Serializer.toJson(this.lastOutput));
      }

      compoundtag.putBoolean("UpdateLastExecution", this.updateLastExecution);
      if (this.updateLastExecution && this.lastExecution > 0L) {
         compoundtag.putLong("LastExecution", this.lastExecution);
      }

      return compoundtag;
   }

   public void load(CompoundTag compoundtag) {
      this.command = compoundtag.getString("Command");
      this.successCount = compoundtag.getInt("SuccessCount");
      if (compoundtag.contains("CustomName", 8)) {
         this.setName(Component.Serializer.fromJson(compoundtag.getString("CustomName")));
      }

      if (compoundtag.contains("TrackOutput", 1)) {
         this.trackOutput = compoundtag.getBoolean("TrackOutput");
      }

      if (compoundtag.contains("LastOutput", 8) && this.trackOutput) {
         try {
            this.lastOutput = Component.Serializer.fromJson(compoundtag.getString("LastOutput"));
         } catch (Throwable var3) {
            this.lastOutput = Component.literal(var3.getMessage());
         }
      } else {
         this.lastOutput = null;
      }

      if (compoundtag.contains("UpdateLastExecution")) {
         this.updateLastExecution = compoundtag.getBoolean("UpdateLastExecution");
      }

      if (this.updateLastExecution && compoundtag.contains("LastExecution")) {
         this.lastExecution = compoundtag.getLong("LastExecution");
      } else {
         this.lastExecution = -1L;
      }

   }

   public void setCommand(String s) {
      this.command = s;
      this.successCount = 0;
   }

   public String getCommand() {
      return this.command;
   }

   public boolean performCommand(Level level) {
      if (!level.isClientSide && level.getGameTime() != this.lastExecution) {
         if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = Component.literal("#itzlipofutzli");
            this.successCount = 1;
            return true;
         } else {
            this.successCount = 0;
            MinecraftServer minecraftserver = this.getLevel().getServer();
            if (minecraftserver.isCommandBlockEnabled() && !StringUtil.isNullOrEmpty(this.command)) {
               try {
                  this.lastOutput = null;
                  CommandSourceStack commandsourcestack = this.createCommandSourceStack().withCallback((commandcontext, flag, i) -> {
                     if (flag) {
                        ++this.successCount;
                     }

                  });
                  minecraftserver.getCommands().performPrefixedCommand(commandsourcestack, this.command);
               } catch (Throwable var6) {
                  CrashReport crashreport = CrashReport.forThrowable(var6, "Executing command block");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Command to be executed");
                  crashreportcategory.setDetail("Command", this::getCommand);
                  crashreportcategory.setDetail("Name", () -> this.getName().getString());
                  throw new ReportedException(crashreport);
               }
            }

            if (this.updateLastExecution) {
               this.lastExecution = level.getGameTime();
            } else {
               this.lastExecution = -1L;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public Component getName() {
      return this.name;
   }

   public void setName(@Nullable Component component) {
      if (component != null) {
         this.name = component;
      } else {
         this.name = DEFAULT_NAME;
      }

   }

   public void sendSystemMessage(Component component) {
      if (this.trackOutput) {
         this.lastOutput = Component.literal("[" + TIME_FORMAT.format(new Date()) + "] ").append(component);
         this.onUpdated();
      }

   }

   public abstract ServerLevel getLevel();

   public abstract void onUpdated();

   public void setLastOutput(@Nullable Component component) {
      this.lastOutput = component;
   }

   public void setTrackOutput(boolean flag) {
      this.trackOutput = flag;
   }

   public boolean isTrackOutput() {
      return this.trackOutput;
   }

   public InteractionResult usedBy(Player player) {
      if (!player.canUseGameMasterBlocks()) {
         return InteractionResult.PASS;
      } else {
         if (player.getCommandSenderWorld().isClientSide) {
            player.openMinecartCommandBlock(this);
         }

         return InteractionResult.sidedSuccess(player.level().isClientSide);
      }
   }

   public abstract Vec3 getPosition();

   public abstract CommandSourceStack createCommandSourceStack();

   public boolean acceptsSuccess() {
      return this.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK) && this.trackOutput;
   }

   public boolean acceptsFailure() {
      return this.trackOutput;
   }

   public boolean shouldInformAdmins() {
      return this.getLevel().getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
   }

   public abstract boolean isValid();
}
