package com.mojang.blaze3d.audio;

import com.mojang.logging.LogUtils;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.slf4j.Logger;

public class OpenAlUtil {
   private static final Logger LOGGER = LogUtils.getLogger();

   private static String alErrorToString(int i) {
      switch (i) {
         case 40961:
            return "Invalid name parameter.";
         case 40962:
            return "Invalid enumerated parameter value.";
         case 40963:
            return "Invalid parameter parameter value.";
         case 40964:
            return "Invalid operation.";
         case 40965:
            return "Unable to allocate memory.";
         default:
            return "An unrecognized error occurred.";
      }
   }

   static boolean checkALError(String s) {
      int i = AL10.alGetError();
      if (i != 0) {
         LOGGER.error("{}: {}", s, alErrorToString(i));
         return true;
      } else {
         return false;
      }
   }

   private static String alcErrorToString(int i) {
      switch (i) {
         case 40961:
            return "Invalid device.";
         case 40962:
            return "Invalid context.";
         case 40963:
            return "Illegal enum.";
         case 40964:
            return "Invalid value.";
         case 40965:
            return "Unable to allocate memory.";
         default:
            return "An unrecognized error occurred.";
      }
   }

   static boolean checkALCError(long i, String s) {
      int j = ALC10.alcGetError(i);
      if (j != 0) {
         LOGGER.error("{}{}: {}", s, i, alcErrorToString(j));
         return true;
      } else {
         return false;
      }
   }

   static int audioFormatToOpenAl(AudioFormat audioformat) {
      AudioFormat.Encoding audioformat_encoding = audioformat.getEncoding();
      int i = audioformat.getChannels();
      int j = audioformat.getSampleSizeInBits();
      if (audioformat_encoding.equals(Encoding.PCM_UNSIGNED) || audioformat_encoding.equals(Encoding.PCM_SIGNED)) {
         if (i == 1) {
            if (j == 8) {
               return 4352;
            }

            if (j == 16) {
               return 4353;
            }
         } else if (i == 2) {
            if (j == 8) {
               return 4354;
            }

            if (j == 16) {
               return 4355;
            }
         }
      }

      throw new IllegalArgumentException("Invalid audio format: " + audioformat);
   }
}
