package net.minecraft.client.gui.narration;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

public class ScreenNarrationCollector {
   int generation;
   final Map<ScreenNarrationCollector.EntryKey, ScreenNarrationCollector.NarrationEntry> entries = Maps.newTreeMap(Comparator.comparing((screennarrationcollector_entrykey1) -> screennarrationcollector_entrykey1.type).thenComparing((screennarrationcollector_entrykey) -> screennarrationcollector_entrykey.depth));

   public void update(Consumer<NarrationElementOutput> consumer) {
      ++this.generation;
      consumer.accept(new ScreenNarrationCollector.Output(0));
   }

   public String collectNarrationText(boolean flag) {
      final StringBuilder stringbuilder = new StringBuilder();
      Consumer<String> consumer = new Consumer<String>() {
         private boolean firstEntry = true;

         public void accept(String s) {
            if (!this.firstEntry) {
               stringbuilder.append(". ");
            }

            this.firstEntry = false;
            stringbuilder.append(s);
         }
      };
      this.entries.forEach((screennarrationcollector_entrykey, screennarrationcollector_narrationentry) -> {
         if (screennarrationcollector_narrationentry.generation == this.generation && (flag || !screennarrationcollector_narrationentry.alreadyNarrated)) {
            screennarrationcollector_narrationentry.contents.getText(consumer);
            screennarrationcollector_narrationentry.alreadyNarrated = true;
         }

      });
      return stringbuilder.toString();
   }

   static class EntryKey {
      final NarratedElementType type;
      final int depth;

      EntryKey(NarratedElementType narratedelementtype, int i) {
         this.type = narratedelementtype;
         this.depth = i;
      }
   }

   static class NarrationEntry {
      NarrationThunk<?> contents = NarrationThunk.EMPTY;
      int generation = -1;
      boolean alreadyNarrated;

      public ScreenNarrationCollector.NarrationEntry update(int i, NarrationThunk<?> narrationthunk) {
         if (!this.contents.equals(narrationthunk)) {
            this.contents = narrationthunk;
            this.alreadyNarrated = false;
         } else if (this.generation + 1 != i) {
            this.alreadyNarrated = false;
         }

         this.generation = i;
         return this;
      }
   }

   class Output implements NarrationElementOutput {
      private final int depth;

      Output(int i) {
         this.depth = i;
      }

      public void add(NarratedElementType narratedelementtype, NarrationThunk<?> narrationthunk) {
         ScreenNarrationCollector.this.entries.computeIfAbsent(new ScreenNarrationCollector.EntryKey(narratedelementtype, this.depth), (screennarrationcollector_entrykey) -> new ScreenNarrationCollector.NarrationEntry()).update(ScreenNarrationCollector.this.generation, narrationthunk);
      }

      public NarrationElementOutput nest() {
         return ScreenNarrationCollector.this.new Output(this.depth + 1);
      }
   }
}
