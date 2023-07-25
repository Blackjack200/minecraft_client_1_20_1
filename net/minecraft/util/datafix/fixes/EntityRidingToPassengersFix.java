package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EntityRidingToPassengersFix extends DataFix {
   public EntityRidingToPassengersFix(Schema schema, boolean flag) {
      super(schema, flag);
   }

   public TypeRewriteRule makeRule() {
      Schema schema = this.getInputSchema();
      Schema schema1 = this.getOutputSchema();
      Type<?> type = schema.getTypeRaw(References.ENTITY_TREE);
      Type<?> type1 = schema1.getTypeRaw(References.ENTITY_TREE);
      Type<?> type2 = schema.getTypeRaw(References.ENTITY);
      return this.cap(schema, schema1, type, type1, type2);
   }

   private <OldEntityTree, NewEntityTree, Entity> TypeRewriteRule cap(Schema schema, Schema schema1, Type<OldEntityTree> type, Type<NewEntityTree> type1, Type<Entity> type2) {
      Type<Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>>> type3 = DSL.named(References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Riding", type)), type2));
      Type<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> type4 = DSL.named(References.ENTITY_TREE.typeName(), DSL.and(DSL.optional(DSL.field("Passengers", DSL.list(type1))), type2));
      Type<?> type5 = schema.getType(References.ENTITY_TREE);
      Type<?> type6 = schema1.getType(References.ENTITY_TREE);
      if (!Objects.equals(type5, type3)) {
         throw new IllegalStateException("Old entity type is not what was expected.");
      } else if (!type6.equals(type4, true, true)) {
         throw new IllegalStateException("New entity type is not what was expected.");
      } else {
         OpticFinder<Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>>> opticfinder = DSL.typeFinder(type3);
         OpticFinder<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> opticfinder1 = DSL.typeFinder(type4);
         OpticFinder<NewEntityTree> opticfinder2 = DSL.typeFinder(type1);
         Type<?> type7 = schema.getType(References.PLAYER);
         Type<?> type8 = schema1.getType(References.PLAYER);
         return TypeRewriteRule.seq(this.fixTypeEverywhere("EntityRidingToPassengerFix", type3, type4, (dynamicops) -> (pair) -> {
               Optional<Pair<String, Pair<Either<List<NewEntityTree>, Unit>, Entity>>> optional = Optional.empty();
               Pair<String, Pair<Either<OldEntityTree, Unit>, Entity>> pair1 = pair;

               while(true) {
                  Either<List<NewEntityTree>, Unit> either = DataFixUtils.orElse(optional.map((pair2) -> {
                     Typed<NewEntityTree> typed = type1.pointTyped(dynamicops).orElseThrow(() -> new IllegalStateException("Could not create new entity tree"));
                     NewEntityTree object = typed.set(opticfinder1, pair2).getOptional(opticfinder2).orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                     return Either.left(ImmutableList.of(object));
                  }), Either.right(DSL.unit()));
                  optional = Optional.of(Pair.of(References.ENTITY_TREE.typeName(), Pair.of(either, pair1.getSecond().getSecond())));
                  Optional<OldEntityTree> optional1 = pair1.getSecond().getFirst().left();
                  if (!optional1.isPresent()) {
                     return optional.orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                  }

                  pair1 = (new Typed<>(type, dynamicops, optional1.get())).getOptional(opticfinder).orElseThrow(() -> new IllegalStateException("Should always have an entity here"));
               }
            }), this.writeAndRead("player RootVehicle injecter", type7, type8));
      }
   }
}
