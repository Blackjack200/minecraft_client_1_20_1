package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener implements GameTestListener {
   private final GameTestInfo originalTestInfo;
   private final GameTestTicker testTicker;
   private final BlockPos structurePos;
   int attempts;
   int successes;

   public ReportGameListener(GameTestInfo gametestinfo, GameTestTicker gametestticker, BlockPos blockpos) {
      this.originalTestInfo = gametestinfo;
      this.testTicker = gametestticker;
      this.structurePos = blockpos;
      this.attempts = 0;
      this.successes = 0;
   }

   public void testStructureLoaded(GameTestInfo gametestinfo) {
      spawnBeacon(this.originalTestInfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
      ++this.attempts;
   }

   public void testPassed(GameTestInfo gametestinfo) {
      ++this.successes;
      if (!gametestinfo.isFlaky()) {
         reportPassed(gametestinfo, gametestinfo.getTestName() + " passed! (" + gametestinfo.getRunTime() + "ms)");
      } else {
         if (this.successes >= gametestinfo.requiredSuccesses()) {
            reportPassed(gametestinfo, gametestinfo + " passed " + this.successes + " times of " + this.attempts + " attempts.");
         } else {
            say(this.originalTestInfo.getLevel(), ChatFormatting.GREEN, "Flaky test " + this.originalTestInfo + " succeeded, attempt: " + this.attempts + " successes: " + this.successes);
            this.rerunTest();
         }

      }
   }

   public void testFailed(GameTestInfo gametestinfo) {
      if (!gametestinfo.isFlaky()) {
         reportFailure(gametestinfo, gametestinfo.getError());
      } else {
         TestFunction testfunction = this.originalTestInfo.getTestFunction();
         String s = "Flaky test " + this.originalTestInfo + " failed, attempt: " + this.attempts + "/" + testfunction.getMaxAttempts();
         if (testfunction.getRequiredSuccesses() > 1) {
            s = s + ", successes: " + this.successes + " (" + testfunction.getRequiredSuccesses() + " required)";
         }

         say(this.originalTestInfo.getLevel(), ChatFormatting.YELLOW, s);
         if (gametestinfo.maxAttempts() - this.attempts + this.successes >= gametestinfo.requiredSuccesses()) {
            this.rerunTest();
         } else {
            reportFailure(gametestinfo, new ExhaustedAttemptsException(this.attempts, this.successes, gametestinfo));
         }

      }
   }

   public static void reportPassed(GameTestInfo gametestinfo, String s) {
      spawnBeacon(gametestinfo, Blocks.LIME_STAINED_GLASS);
      visualizePassedTest(gametestinfo, s);
   }

   private static void visualizePassedTest(GameTestInfo gametestinfo, String s) {
      say(gametestinfo.getLevel(), ChatFormatting.GREEN, s);
      GlobalTestReporter.onTestSuccess(gametestinfo);
   }

   protected static void reportFailure(GameTestInfo gametestinfo, Throwable throwable) {
      spawnBeacon(gametestinfo, gametestinfo.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
      spawnLectern(gametestinfo, Util.describeError(throwable));
      visualizeFailedTest(gametestinfo, throwable);
   }

   protected static void visualizeFailedTest(GameTestInfo gametestinfo, Throwable throwable) {
      String s = throwable.getMessage() + (throwable.getCause() == null ? "" : " cause: " + Util.describeError(throwable.getCause()));
      String s1 = (gametestinfo.isRequired() ? "" : "(optional) ") + gametestinfo.getTestName() + " failed! " + s;
      say(gametestinfo.getLevel(), gametestinfo.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, s1);
      Throwable throwable1 = MoreObjects.firstNonNull(ExceptionUtils.getRootCause(throwable), throwable);
      if (throwable1 instanceof GameTestAssertPosException gametestassertposexception) {
         showRedBox(gametestinfo.getLevel(), gametestassertposexception.getAbsolutePos(), gametestassertposexception.getMessageToShowAtBlock());
      }

      GlobalTestReporter.onTestFailed(gametestinfo);
   }

   private void rerunTest() {
      this.originalTestInfo.clearStructure();
      GameTestInfo gametestinfo = new GameTestInfo(this.originalTestInfo.getTestFunction(), this.originalTestInfo.getRotation(), this.originalTestInfo.getLevel());
      gametestinfo.startExecution();
      this.testTicker.add(gametestinfo);
      gametestinfo.addListener(this);
      gametestinfo.spawnStructure(this.structurePos, 2);
   }

   protected static void spawnBeacon(GameTestInfo gametestinfo, Block block) {
      ServerLevel serverlevel = gametestinfo.getLevel();
      BlockPos blockpos = gametestinfo.getStructureBlockPos();
      BlockPos blockpos1 = new BlockPos(-1, -1, -1);
      BlockPos blockpos2 = StructureTemplate.transform(blockpos.offset(blockpos1), Mirror.NONE, gametestinfo.getRotation(), blockpos);
      serverlevel.setBlockAndUpdate(blockpos2, Blocks.BEACON.defaultBlockState().rotate(gametestinfo.getRotation()));
      BlockPos blockpos3 = blockpos2.offset(0, 1, 0);
      serverlevel.setBlockAndUpdate(blockpos3, block.defaultBlockState());

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            BlockPos blockpos4 = blockpos2.offset(i, -1, j);
            serverlevel.setBlockAndUpdate(blockpos4, Blocks.IRON_BLOCK.defaultBlockState());
         }
      }

   }

   private static void spawnLectern(GameTestInfo gametestinfo, String s) {
      ServerLevel serverlevel = gametestinfo.getLevel();
      BlockPos blockpos = gametestinfo.getStructureBlockPos();
      BlockPos blockpos1 = new BlockPos(-1, 1, -1);
      BlockPos blockpos2 = StructureTemplate.transform(blockpos.offset(blockpos1), Mirror.NONE, gametestinfo.getRotation(), blockpos);
      serverlevel.setBlockAndUpdate(blockpos2, Blocks.LECTERN.defaultBlockState().rotate(gametestinfo.getRotation()));
      BlockState blockstate = serverlevel.getBlockState(blockpos2);
      ItemStack itemstack = createBook(gametestinfo.getTestName(), gametestinfo.isRequired(), s);
      LecternBlock.tryPlaceBook((Entity)null, serverlevel, blockpos2, blockstate, itemstack);
   }

   private static ItemStack createBook(String s, boolean flag, String s1) {
      ItemStack itemstack = new ItemStack(Items.WRITABLE_BOOK);
      ListTag listtag = new ListTag();
      StringBuffer stringbuffer = new StringBuffer();
      Arrays.stream(s.split("\\.")).forEach((s2) -> stringbuffer.append(s2).append('\n'));
      if (!flag) {
         stringbuffer.append("(optional)\n");
      }

      stringbuffer.append("-------------------\n");
      listtag.add(StringTag.valueOf(stringbuffer + s1));
      itemstack.addTagElement("pages", listtag);
      return itemstack;
   }

   protected static void say(ServerLevel serverlevel, ChatFormatting chatformatting, String s) {
      serverlevel.getPlayers((serverplayer1) -> true).forEach((serverplayer) -> serverplayer.sendSystemMessage(Component.literal(s).withStyle(chatformatting)));
   }

   private static void showRedBox(ServerLevel serverlevel, BlockPos blockpos, String s) {
      DebugPackets.sendGameTestAddMarker(serverlevel, blockpos, s, -2130771968, Integer.MAX_VALUE);
   }
}
