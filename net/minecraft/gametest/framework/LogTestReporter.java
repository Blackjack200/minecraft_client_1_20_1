package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import org.slf4j.Logger;

public class LogTestReporter implements TestReporter {
   private static final Logger LOGGER = LogUtils.getLogger();

   public void onTestFailed(GameTestInfo gametestinfo) {
      if (gametestinfo.isRequired()) {
         LOGGER.error("{} failed! {}", gametestinfo.getTestName(), Util.describeError(gametestinfo.getError()));
      } else {
         LOGGER.warn("(optional) {} failed. {}", gametestinfo.getTestName(), Util.describeError(gametestinfo.getError()));
      }

   }

   public void onTestSuccess(GameTestInfo gametestinfo) {
   }
}
