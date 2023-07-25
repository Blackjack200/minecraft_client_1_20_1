package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TextFilter {
   TextFilter DUMMY = new TextFilter() {
      public void join() {
      }

      public void leave() {
      }

      public CompletableFuture<FilteredText> processStreamMessage(String s) {
         return CompletableFuture.completedFuture(FilteredText.passThrough(s));
      }

      public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list) {
         return CompletableFuture.completedFuture(list.stream().map(FilteredText::passThrough).collect(ImmutableList.toImmutableList()));
      }
   };

   void join();

   void leave();

   CompletableFuture<FilteredText> processStreamMessage(String s);

   CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list);
}
