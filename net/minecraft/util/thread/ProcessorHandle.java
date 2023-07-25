package net.minecraft.util.thread;

import com.mojang.datafixers.util.Either;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ProcessorHandle<Msg> extends AutoCloseable {
   String name();

   void tell(Msg object);

   default void close() {
   }

   default <Source> CompletableFuture<Source> ask(Function<? super ProcessorHandle<Source>, ? extends Msg> function) {
      CompletableFuture<Source> completablefuture = new CompletableFuture<>();
      Msg object = function.apply(of("ask future procesor handle", completablefuture::complete));
      this.tell(object);
      return completablefuture;
   }

   default <Source> CompletableFuture<Source> askEither(Function<? super ProcessorHandle<Either<Source, Exception>>, ? extends Msg> function) {
      CompletableFuture<Source> completablefuture = new CompletableFuture<>();
      Msg object = function.apply(of("ask future procesor handle", (either) -> {
         either.ifLeft(completablefuture::complete);
         either.ifRight(completablefuture::completeExceptionally);
      }));
      this.tell(object);
      return completablefuture;
   }

   static <Msg> ProcessorHandle<Msg> of(final String s, final Consumer<Msg> consumer) {
      return new ProcessorHandle<Msg>() {
         public String name() {
            return s;
         }

         public void tell(Msg object) {
            consumer.accept(object);
         }

         public String toString() {
            return s;
         }
      };
   }
}
