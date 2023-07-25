package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class KeyboardHandler {
   public static final int DEBUG_CRASH_TIME = 10000;
   private final Minecraft minecraft;
   private final ClipboardManager clipboardManager = new ClipboardManager();
   private long debugCrashKeyTime = -1L;
   private long debugCrashKeyReportedTime = -1L;
   private long debugCrashKeyReportedCount = -1L;
   private boolean handledDebugKey;

   public KeyboardHandler(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   private boolean handleChunkDebugKeys(int i) {
      switch (i) {
         case 69:
            this.minecraft.chunkPath = !this.minecraft.chunkPath;
            this.debugFeedback("ChunkPath: {0}", this.minecraft.chunkPath ? "shown" : "hidden");
            return true;
         case 76:
            this.minecraft.smartCull = !this.minecraft.smartCull;
            this.debugFeedback("SmartCull: {0}", this.minecraft.smartCull ? "enabled" : "disabled");
            return true;
         case 85:
            if (Screen.hasShiftDown()) {
               this.minecraft.levelRenderer.killFrustum();
               this.debugFeedback("Killed frustum");
            } else {
               this.minecraft.levelRenderer.captureFrustum();
               this.debugFeedback("Captured frustum");
            }

            return true;
         case 86:
            this.minecraft.chunkVisibility = !this.minecraft.chunkVisibility;
            this.debugFeedback("ChunkVisibility: {0}", this.minecraft.chunkVisibility ? "enabled" : "disabled");
            return true;
         case 87:
            this.minecraft.wireframe = !this.minecraft.wireframe;
            this.debugFeedback("WireFrame: {0}", this.minecraft.wireframe ? "enabled" : "disabled");
            return true;
         default:
            return false;
      }
   }

   private void debugComponent(ChatFormatting chatformatting, Component component) {
      this.minecraft.gui.getChat().addMessage(Component.empty().append(Component.translatable("debug.prefix").withStyle(chatformatting, ChatFormatting.BOLD)).append(CommonComponents.SPACE).append(component));
   }

   private void debugFeedbackComponent(Component component) {
      this.debugComponent(ChatFormatting.YELLOW, component);
   }

   private void debugFeedbackTranslated(String s, Object... aobject) {
      this.debugFeedbackComponent(Component.translatable(s, aobject));
   }

   private void debugWarningTranslated(String s, Object... aobject) {
      this.debugComponent(ChatFormatting.RED, Component.translatable(s, aobject));
   }

   private void debugFeedback(String s, Object... aobject) {
      this.debugFeedbackComponent(Component.literal(MessageFormat.format(s, aobject)));
   }

   private boolean handleDebugKeys(int i) {
      if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
         return true;
      } else {
         switch (i) {
            case 65:
               this.minecraft.levelRenderer.allChanged();
               this.debugFeedbackTranslated("debug.reload_chunks.message");
               return true;
            case 66:
               boolean flag = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
               this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(flag);
               this.debugFeedbackTranslated(flag ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
               return true;
            case 67:
               if (this.minecraft.player.isReducedDebugInfo()) {
                  return false;
               } else {
                  ClientPacketListener clientpacketlistener = this.minecraft.player.connection;
                  if (clientpacketlistener == null) {
                     return false;
                  }

                  this.debugFeedbackTranslated("debug.copy_location.message");
                  this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.minecraft.player.level().dimension().location(), this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(), this.minecraft.player.getYRot(), this.minecraft.player.getXRot()));
                  return true;
               }
            case 68:
               if (this.minecraft.gui != null) {
                  this.minecraft.gui.getChat().clearMessages(false);
               }

               return true;
            case 71:
               boolean flag1 = this.minecraft.debugRenderer.switchRenderChunkborder();
               this.debugFeedbackTranslated(flag1 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
               return true;
            case 72:
               this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
               this.debugFeedbackTranslated(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
               this.minecraft.options.save();
               return true;
            case 73:
               if (!this.minecraft.player.isReducedDebugInfo()) {
                  this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
               }

               return true;
            case 76:
               if (this.minecraft.debugClientMetricsStart(this::debugFeedbackComponent)) {
                  this.debugFeedbackTranslated("debug.profiling.start", 10);
               }

               return true;
            case 78:
               if (!this.minecraft.player.hasPermissions(2)) {
                  this.debugFeedbackTranslated("debug.creative_spectator.error");
               } else if (!this.minecraft.player.isSpectator()) {
                  this.minecraft.player.connection.sendUnsignedCommand("gamemode spectator");
               } else {
                  this.minecraft.player.connection.sendUnsignedCommand("gamemode " + MoreObjects.firstNonNull(this.minecraft.gameMode.getPreviousPlayerMode(), GameType.CREATIVE).getName());
               }

               return true;
            case 80:
               this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
               this.minecraft.options.save();
               this.debugFeedbackTranslated(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
               return true;
            case 81:
               this.debugFeedbackTranslated("debug.help.message");
               ChatComponent chatcomponent = this.minecraft.gui.getChat();
               chatcomponent.addMessage(Component.translatable("debug.reload_chunks.help"));
               chatcomponent.addMessage(Component.translatable("debug.show_hitboxes.help"));
               chatcomponent.addMessage(Component.translatable("debug.copy_location.help"));
               chatcomponent.addMessage(Component.translatable("debug.clear_chat.help"));
               chatcomponent.addMessage(Component.translatable("debug.chunk_boundaries.help"));
               chatcomponent.addMessage(Component.translatable("debug.advanced_tooltips.help"));
               chatcomponent.addMessage(Component.translatable("debug.inspect.help"));
               chatcomponent.addMessage(Component.translatable("debug.profiling.help"));
               chatcomponent.addMessage(Component.translatable("debug.creative_spectator.help"));
               chatcomponent.addMessage(Component.translatable("debug.pause_focus.help"));
               chatcomponent.addMessage(Component.translatable("debug.help.help"));
               chatcomponent.addMessage(Component.translatable("debug.dump_dynamic_textures.help"));
               chatcomponent.addMessage(Component.translatable("debug.reload_resourcepacks.help"));
               chatcomponent.addMessage(Component.translatable("debug.pause.help"));
               chatcomponent.addMessage(Component.translatable("debug.gamemodes.help"));
               return true;
            case 83:
               Path path = this.minecraft.gameDirectory.toPath().toAbsolutePath();
               Path path1 = TextureUtil.getDebugTexturePath(path);
               this.minecraft.getTextureManager().dumpAllSheets(path1);
               Component component = Component.literal(path.relativize(path1).toString()).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path1.toFile().toString())));
               this.debugFeedbackTranslated("debug.dump_dynamic_textures", component);
               return true;
            case 84:
               this.debugFeedbackTranslated("debug.reload_resourcepacks.message");
               this.minecraft.reloadResourcePacks();
               return true;
            case 293:
               if (!this.minecraft.player.hasPermissions(2)) {
                  this.debugFeedbackTranslated("debug.gamemodes.error");
               } else {
                  this.minecraft.setScreen(new GameModeSwitcherScreen());
               }

               return true;
            default:
               return false;
         }
      }
   }

   private void copyRecreateCommand(boolean flag, boolean flag1) {
      HitResult hitresult = this.minecraft.hitResult;
      if (hitresult != null) {
         switch (hitresult.getType()) {
            case BLOCK:
               BlockPos blockpos = ((BlockHitResult)hitresult).getBlockPos();
               BlockState blockstate = this.minecraft.player.level().getBlockState(blockpos);
               if (flag) {
                  if (flag1) {
                     this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(blockpos, (compoundtag3) -> {
                        this.copyCreateBlockCommand(blockstate, blockpos, compoundtag3);
                        this.debugFeedbackTranslated("debug.inspect.server.block");
                     });
                  } else {
                     BlockEntity blockentity = this.minecraft.player.level().getBlockEntity(blockpos);
                     CompoundTag compoundtag = blockentity != null ? blockentity.saveWithoutMetadata() : null;
                     this.copyCreateBlockCommand(blockstate, blockpos, compoundtag);
                     this.debugFeedbackTranslated("debug.inspect.client.block");
                  }
               } else {
                  this.copyCreateBlockCommand(blockstate, blockpos, (CompoundTag)null);
                  this.debugFeedbackTranslated("debug.inspect.client.block");
               }
               break;
            case ENTITY:
               Entity entity = ((EntityHitResult)hitresult).getEntity();
               ResourceLocation resourcelocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
               if (flag) {
                  if (flag1) {
                     this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), (compoundtag2) -> {
                        this.copyCreateEntityCommand(resourcelocation, entity.position(), compoundtag2);
                        this.debugFeedbackTranslated("debug.inspect.server.entity");
                     });
                  } else {
                     CompoundTag compoundtag1 = entity.saveWithoutId(new CompoundTag());
                     this.copyCreateEntityCommand(resourcelocation, entity.position(), compoundtag1);
                     this.debugFeedbackTranslated("debug.inspect.client.entity");
                  }
               } else {
                  this.copyCreateEntityCommand(resourcelocation, entity.position(), (CompoundTag)null);
                  this.debugFeedbackTranslated("debug.inspect.client.entity");
               }
         }

      }
   }

   private void copyCreateBlockCommand(BlockState blockstate, BlockPos blockpos, @Nullable CompoundTag compoundtag) {
      StringBuilder stringbuilder = new StringBuilder(BlockStateParser.serialize(blockstate));
      if (compoundtag != null) {
         stringbuilder.append((Object)compoundtag);
      }

      String s = String.format(Locale.ROOT, "/setblock %d %d %d %s", blockpos.getX(), blockpos.getY(), blockpos.getZ(), stringbuilder);
      this.setClipboard(s);
   }

   private void copyCreateEntityCommand(ResourceLocation resourcelocation, Vec3 vec3, @Nullable CompoundTag compoundtag) {
      String s1;
      if (compoundtag != null) {
         compoundtag.remove("UUID");
         compoundtag.remove("Pos");
         compoundtag.remove("Dimension");
         String s = NbtUtils.toPrettyComponent(compoundtag).getString();
         s1 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", resourcelocation.toString(), vec3.x, vec3.y, vec3.z, s);
      } else {
         s1 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", resourcelocation.toString(), vec3.x, vec3.y, vec3.z);
      }

      this.setClipboard(s1);
   }

   public void keyPress(long i, int j, int k, int l, int i1) {
      if (i == this.minecraft.getWindow().getWindow()) {
         if (this.debugCrashKeyTime > 0L) {
            if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) || !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
               this.debugCrashKeyTime = -1L;
            }
         } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
            this.handledDebugKey = true;
            this.debugCrashKeyTime = Util.getMillis();
            this.debugCrashKeyReportedTime = Util.getMillis();
            this.debugCrashKeyReportedCount = 0L;
         }

         Screen screen = this.minecraft.screen;
         if (screen != null) {
            switch (j) {
               case 258:
                  this.minecraft.setLastInputType(InputType.KEYBOARD_TAB);
               case 259:
               case 260:
               case 261:
               default:
                  break;
               case 262:
               case 263:
               case 264:
               case 265:
                  this.minecraft.setLastInputType(InputType.KEYBOARD_ARROW);
            }
         }

         if (l == 1 && (!(this.minecraft.screen instanceof KeyBindsScreen) || ((KeyBindsScreen)screen).lastKeySelection <= Util.getMillis() - 20L)) {
            if (this.minecraft.options.keyFullscreen.matches(j, k)) {
               this.minecraft.getWindow().toggleFullScreen();
               this.minecraft.options.fullscreen().set(this.minecraft.getWindow().isFullscreen());
               return;
            }

            if (this.minecraft.options.keyScreenshot.matches(j, k)) {
               if (Screen.hasControlDown()) {
               }

               Screenshot.grab(this.minecraft.gameDirectory, this.minecraft.getMainRenderTarget(), (component) -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(component)));
               return;
            }
         }

         if (this.minecraft.getNarrator().isActive()) {
            boolean flag = screen == null || !(screen.getFocused() instanceof EditBox) || !((EditBox)screen.getFocused()).canConsumeInput();
            if (l != 0 && j == 66 && Screen.hasControlDown() && flag) {
               boolean flag1 = this.minecraft.options.narrator().get() == NarratorStatus.OFF;
               this.minecraft.options.narrator().set(NarratorStatus.byId(this.minecraft.options.narrator().get().getId() + 1));
               if (screen instanceof SimpleOptionsSubScreen) {
                  ((SimpleOptionsSubScreen)screen).updateNarratorButton();
               }

               if (flag1 && screen != null) {
                  screen.narrationEnabled();
               }
            }
         }

         if (screen != null) {
            boolean[] aboolean = new boolean[]{false};
            Screen.wrapScreenError(() -> {
               if (l != 1 && l != 2) {
                  if (l == 0) {
                     aboolean[0] = screen.keyReleased(j, k, i1);
                  }
               } else {
                  screen.afterKeyboardAction();
                  aboolean[0] = screen.keyPressed(j, k, i1);
               }

            }, "keyPressed event handler", screen.getClass().getCanonicalName());
            if (aboolean[0]) {
               return;
            }
         }

         if (this.minecraft.screen == null) {
            InputConstants.Key inputconstants_key = InputConstants.getKey(j, k);
            if (l == 0) {
               KeyMapping.set(inputconstants_key, false);
               if (j == 292) {
                  if (this.handledDebugKey) {
                     this.handledDebugKey = false;
                  } else {
                     this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
                     this.minecraft.options.renderDebugCharts = this.minecraft.options.renderDebug && Screen.hasShiftDown();
                     this.minecraft.options.renderFpsChart = this.minecraft.options.renderDebug && Screen.hasAltDown();
                  }
               }
            } else {
               if (j == 293 && this.minecraft.gameRenderer != null) {
                  this.minecraft.gameRenderer.togglePostEffect();
               }

               boolean flag2 = false;
               if (j == 256) {
                  boolean flag3 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
                  this.minecraft.pauseGame(flag3);
               }

               flag2 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292) && this.handleDebugKeys(j);
               this.handledDebugKey |= flag2;
               if (j == 290) {
                  this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
               }

               if (flag2) {
                  KeyMapping.set(inputconstants_key, false);
               } else {
                  KeyMapping.set(inputconstants_key, true);
                  KeyMapping.click(inputconstants_key);
               }

               if (this.minecraft.options.renderDebugCharts && j >= 48 && j <= 57) {
                  this.minecraft.debugFpsMeterKeyPress(j - 48);
               }
            }
         }

      }
   }

   private void charTyped(long i, int j, int k) {
      if (i == this.minecraft.getWindow().getWindow()) {
         GuiEventListener guieventlistener = this.minecraft.screen;
         if (guieventlistener != null && this.minecraft.getOverlay() == null) {
            if (Character.charCount(j) == 1) {
               Screen.wrapScreenError(() -> guieventlistener.charTyped((char)j, k), "charTyped event handler", guieventlistener.getClass().getCanonicalName());
            } else {
               for(char c0 : Character.toChars(j)) {
                  Screen.wrapScreenError(() -> guieventlistener.charTyped(c0, k), "charTyped event handler", guieventlistener.getClass().getCanonicalName());
               }
            }

         }
      }
   }

   public void setup(long i) {
      InputConstants.setupKeyboardCallbacks(i, (l1, i2, j2, k2, l2) -> this.minecraft.execute(() -> this.keyPress(l1, i2, j2, k2, l2)), (j, k, l) -> this.minecraft.execute(() -> this.charTyped(j, k, l)));
   }

   public String getClipboard() {
      return this.clipboardManager.getClipboard(this.minecraft.getWindow().getWindow(), (i, j) -> {
         if (i != 65545) {
            this.minecraft.getWindow().defaultErrorCallback(i, j);
         }

      });
   }

   public void setClipboard(String s) {
      if (!s.isEmpty()) {
         this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), s);
      }

   }

   public void tick() {
      if (this.debugCrashKeyTime > 0L) {
         long i = Util.getMillis();
         long j = 10000L - (i - this.debugCrashKeyTime);
         long k = i - this.debugCrashKeyReportedTime;
         if (j < 0L) {
            if (Screen.hasControlDown()) {
               Blaze3D.youJustLostTheGame();
            }

            String s = "Manually triggered debug crash";
            CrashReport crashreport = new CrashReport("Manually triggered debug crash", new Throwable("Manually triggered debug crash"));
            CrashReportCategory crashreportcategory = crashreport.addCategory("Manual crash details");
            NativeModuleLister.addCrashSection(crashreportcategory);
            throw new ReportedException(crashreport);
         }

         if (k >= 1000L) {
            if (this.debugCrashKeyReportedCount == 0L) {
               this.debugFeedbackTranslated("debug.crash.message");
            } else {
               this.debugWarningTranslated("debug.crash.warning", Mth.ceil((float)j / 1000.0F));
            }

            this.debugCrashKeyReportedTime = i;
            ++this.debugCrashKeyReportedCount;
         }
      }

   }
}
