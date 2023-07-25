package net.minecraft.gametest.framework;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import org.slf4j.Logger;

public class TeamcityTestReporter implements TestReporter {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Escaper ESCAPER = Escapers.builder().addEscape('\'', "|'").addEscape('\n', "|n").addEscape('\r', "|r").addEscape('|', "||").addEscape('[', "|[").addEscape(']', "|]").build();

   public void onTestFailed(GameTestInfo gametestinfo) {
      String s = ESCAPER.escape(gametestinfo.getTestName());
      String s1 = ESCAPER.escape(gametestinfo.getError().getMessage());
      String s2 = ESCAPER.escape(Util.describeError(gametestinfo.getError()));
      LOGGER.info("##teamcity[testStarted name='{}']", (Object)s);
      if (gametestinfo.isRequired()) {
         LOGGER.info("##teamcity[testFailed name='{}' message='{}' details='{}']", s, s1, s2);
      } else {
         LOGGER.info("##teamcity[testIgnored name='{}' message='{}' details='{}']", s, s1, s2);
      }

      LOGGER.info("##teamcity[testFinished name='{}' duration='{}']", s, gametestinfo.getRunTime());
   }

   public void onTestSuccess(GameTestInfo gametestinfo) {
      String s = ESCAPER.escape(gametestinfo.getTestName());
      LOGGER.info("##teamcity[testStarted name='{}']", (Object)s);
      LOGGER.info("##teamcity[testFinished name='{}' duration='{}']", s, gametestinfo.getRunTime());
   }
}
