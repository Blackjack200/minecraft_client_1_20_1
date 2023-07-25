package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Unit;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BehaviorBuilder<E extends LivingEntity, M> implements App<BehaviorBuilder.Mu<E>, M> {
   private final BehaviorBuilder.TriggerWithResult<E, M> trigger;

   public static <E extends LivingEntity, M> BehaviorBuilder<E, M> unbox(App<BehaviorBuilder.Mu<E>, M> app) {
      return (BehaviorBuilder)app;
   }

   public static <E extends LivingEntity> BehaviorBuilder.Instance<E> instance() {
      return new BehaviorBuilder.Instance<>();
   }

   public static <E extends LivingEntity> OneShot<E> create(Function<BehaviorBuilder.Instance<E>, ? extends App<BehaviorBuilder.Mu<E>, Trigger<E>>> function) {
      final BehaviorBuilder.TriggerWithResult<E, Trigger<E>> behaviorbuilder_triggerwithresult = get(function.apply(instance()));
      return new OneShot<E>() {
         public boolean trigger(ServerLevel serverlevel, E livingentity, long i) {
            Trigger<E> trigger = behaviorbuilder_triggerwithresult.tryTrigger(serverlevel, livingentity, i);
            return trigger == null ? false : trigger.trigger(serverlevel, livingentity, i);
         }

         public String debugString() {
            return "OneShot[" + behaviorbuilder_triggerwithresult.debugString() + "]";
         }

         public String toString() {
            return this.debugString();
         }
      };
   }

   public static <E extends LivingEntity> OneShot<E> sequence(Trigger<? super E> trigger, Trigger<? super E> trigger1) {
      return create((behaviorbuilder_instance) -> behaviorbuilder_instance.<Unit>group(behaviorbuilder_instance.ifTriggered(trigger)).apply(behaviorbuilder_instance, (unit) -> trigger1::trigger));
   }

   public static <E extends LivingEntity> OneShot<E> triggerIf(Predicate<E> predicate, OneShot<? super E> oneshot) {
      return sequence(triggerIf(predicate), oneshot);
   }

   public static <E extends LivingEntity> OneShot<E> triggerIf(Predicate<E> predicate) {
      return create((behaviorbuilder_instance) -> behaviorbuilder_instance.point((serverlevel, livingentity, i) -> predicate.test(livingentity)));
   }

   public static <E extends LivingEntity> OneShot<E> triggerIf(BiPredicate<ServerLevel, E> bipredicate) {
      return create((behaviorbuilder_instance) -> behaviorbuilder_instance.point((serverlevel, livingentity, i) -> bipredicate.test(serverlevel, livingentity)));
   }

   static <E extends LivingEntity, M> BehaviorBuilder.TriggerWithResult<E, M> get(App<BehaviorBuilder.Mu<E>, M> app) {
      return unbox(app).trigger;
   }

   BehaviorBuilder(BehaviorBuilder.TriggerWithResult<E, M> behaviorbuilder_triggerwithresult) {
      this.trigger = behaviorbuilder_triggerwithresult;
   }

   static <E extends LivingEntity, M> BehaviorBuilder<E, M> create(BehaviorBuilder.TriggerWithResult<E, M> behaviorbuilder_triggerwithresult) {
      return new BehaviorBuilder<>(behaviorbuilder_triggerwithresult);
   }

   static final class Constant<E extends LivingEntity, A> extends BehaviorBuilder<E, A> {
      Constant(A object) {
         this(object, () -> "C[" + object + "]");
      }

      Constant(final A object, final Supplier<String> supplier) {
         super(new BehaviorBuilder.TriggerWithResult<E, A>() {
            public A tryTrigger(ServerLevel serverlevel, E livingentity, long i) {
               return object;
            }

            public String debugString() {
               return supplier.get();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }
   }

   public static final class Instance<E extends LivingEntity> implements Applicative<BehaviorBuilder.Mu<E>, BehaviorBuilder.Instance.Mu<E>> {
      public <Value> Optional<Value> tryGet(MemoryAccessor<OptionalBox.Mu, Value> memoryaccessor) {
         return OptionalBox.unbox(memoryaccessor.value());
      }

      public <Value> Value get(MemoryAccessor<IdF.Mu, Value> memoryaccessor) {
         return IdF.get(memoryaccessor.value());
      }

      public <Value> BehaviorBuilder<E, MemoryAccessor<OptionalBox.Mu, Value>> registered(MemoryModuleType<Value> memorymoduletype) {
         return new BehaviorBuilder.PureMemory<>(new MemoryCondition.Registered<>(memorymoduletype));
      }

      public <Value> BehaviorBuilder<E, MemoryAccessor<IdF.Mu, Value>> present(MemoryModuleType<Value> memorymoduletype) {
         return new BehaviorBuilder.PureMemory<>(new MemoryCondition.Present<>(memorymoduletype));
      }

      public <Value> BehaviorBuilder<E, MemoryAccessor<Const.Mu<Unit>, Value>> absent(MemoryModuleType<Value> memorymoduletype) {
         return new BehaviorBuilder.PureMemory<>(new MemoryCondition.Absent<>(memorymoduletype));
      }

      public BehaviorBuilder<E, Unit> ifTriggered(Trigger<? super E> trigger) {
         return new BehaviorBuilder.TriggerWrapper<>(trigger);
      }

      public <A> BehaviorBuilder<E, A> point(A object) {
         return new BehaviorBuilder.Constant<>(object);
      }

      public <A> BehaviorBuilder<E, A> point(Supplier<String> supplier, A object) {
         return new BehaviorBuilder.Constant<>(object, supplier);
      }

      public <A, R> Function<App<BehaviorBuilder.Mu<E>, A>, App<BehaviorBuilder.Mu<E>, R>> lift1(App<BehaviorBuilder.Mu<E>, Function<A, R>> app) {
         return (app2) -> {
            final BehaviorBuilder.TriggerWithResult<E, A> behaviorbuilder_triggerwithresult = BehaviorBuilder.get(app2);
            final BehaviorBuilder.TriggerWithResult<E, Function<A, R>> behaviorbuilder_triggerwithresult1 = BehaviorBuilder.get(app);
            return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
               public R tryTrigger(ServerLevel serverlevel, E livingentity, long i) {
                  A object = (A)behaviorbuilder_triggerwithresult.tryTrigger(serverlevel, livingentity, i);
                  if (object == null) {
                     return (R)null;
                  } else {
                     Function<A, R> function = (Function)behaviorbuilder_triggerwithresult1.tryTrigger(serverlevel, livingentity, i);
                     return (R)(function == null ? null : function.apply(object));
                  }
               }

               public String debugString() {
                  return behaviorbuilder_triggerwithresult1.debugString() + " * " + behaviorbuilder_triggerwithresult.debugString();
               }

               public String toString() {
                  return this.debugString();
               }
            });
         };
      }

      public <T, R> BehaviorBuilder<E, R> map(final Function<? super T, ? extends R> function, App<BehaviorBuilder.Mu<E>, T> app) {
         final BehaviorBuilder.TriggerWithResult<E, T> behaviorbuilder_triggerwithresult = BehaviorBuilder.get(app);
         return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
            public R tryTrigger(ServerLevel serverlevel, E livingentity, long i) {
               T object = behaviorbuilder_triggerwithresult.tryTrigger(serverlevel, livingentity, i);
               return (R)(object == null ? null : function.apply(object));
            }

            public String debugString() {
               return behaviorbuilder_triggerwithresult.debugString() + ".map[" + function + "]";
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      public <A, B, R> BehaviorBuilder<E, R> ap2(App<BehaviorBuilder.Mu<E>, BiFunction<A, B, R>> app, App<BehaviorBuilder.Mu<E>, A> app1, App<BehaviorBuilder.Mu<E>, B> app2) {
         final BehaviorBuilder.TriggerWithResult<E, A> behaviorbuilder_triggerwithresult = BehaviorBuilder.get(app1);
         final BehaviorBuilder.TriggerWithResult<E, B> behaviorbuilder_triggerwithresult1 = BehaviorBuilder.get(app2);
         final BehaviorBuilder.TriggerWithResult<E, BiFunction<A, B, R>> behaviorbuilder_triggerwithresult2 = BehaviorBuilder.get(app);
         return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
            public R tryTrigger(ServerLevel serverlevel, E livingentity, long i) {
               A object = behaviorbuilder_triggerwithresult.tryTrigger(serverlevel, livingentity, i);
               if (object == null) {
                  return (R)null;
               } else {
                  B object1 = behaviorbuilder_triggerwithresult1.tryTrigger(serverlevel, livingentity, i);
                  if (object1 == null) {
                     return (R)null;
                  } else {
                     BiFunction<A, B, R> bifunction = behaviorbuilder_triggerwithresult2.tryTrigger(serverlevel, livingentity, i);
                     return (R)(bifunction == null ? null : bifunction.apply(object, object1));
                  }
               }
            }

            public String debugString() {
               return behaviorbuilder_triggerwithresult2.debugString() + " * " + behaviorbuilder_triggerwithresult.debugString() + " * " + behaviorbuilder_triggerwithresult1.debugString();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      public <T1, T2, T3, R> BehaviorBuilder<E, R> ap3(App<BehaviorBuilder.Mu<E>, Function3<T1, T2, T3, R>> app, App<BehaviorBuilder.Mu<E>, T1> app1, App<BehaviorBuilder.Mu<E>, T2> app2, App<BehaviorBuilder.Mu<E>, T3> app3) {
         final BehaviorBuilder.TriggerWithResult<E, T1> behaviorbuilder_triggerwithresult = BehaviorBuilder.get(app1);
         final BehaviorBuilder.TriggerWithResult<E, T2> behaviorbuilder_triggerwithresult1 = BehaviorBuilder.get(app2);
         final BehaviorBuilder.TriggerWithResult<E, T3> behaviorbuilder_triggerwithresult2 = BehaviorBuilder.get(app3);
         final BehaviorBuilder.TriggerWithResult<E, Function3<T1, T2, T3, R>> behaviorbuilder_triggerwithresult3 = BehaviorBuilder.get(app);
         return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
            public R tryTrigger(ServerLevel serverlevel, E livingentity, long i) {
               T1 object = behaviorbuilder_triggerwithresult.tryTrigger(serverlevel, livingentity, i);
               if (object == null) {
                  return (R)null;
               } else {
                  T2 object1 = behaviorbuilder_triggerwithresult1.tryTrigger(serverlevel, livingentity, i);
                  if (object1 == null) {
                     return (R)null;
                  } else {
                     T3 object2 = behaviorbuilder_triggerwithresult2.tryTrigger(serverlevel, livingentity, i);
                     if (object2 == null) {
                        return (R)null;
                     } else {
                        Function3<T1, T2, T3, R> function3 = behaviorbuilder_triggerwithresult3.tryTrigger(serverlevel, livingentity, i);
                        return (R)(function3 == null ? null : function3.apply(object, object1, object2));
                     }
                  }
               }
            }

            public String debugString() {
               return behaviorbuilder_triggerwithresult3.debugString() + " * " + behaviorbuilder_triggerwithresult.debugString() + " * " + behaviorbuilder_triggerwithresult1.debugString() + " * " + behaviorbuilder_triggerwithresult2.debugString();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      public <T1, T2, T3, T4, R> BehaviorBuilder<E, R> ap4(App<BehaviorBuilder.Mu<E>, Function4<T1, T2, T3, T4, R>> app, App<BehaviorBuilder.Mu<E>, T1> app1, App<BehaviorBuilder.Mu<E>, T2> app2, App<BehaviorBuilder.Mu<E>, T3> app3, App<BehaviorBuilder.Mu<E>, T4> app4) {
         final BehaviorBuilder.TriggerWithResult<E, T1> behaviorbuilder_triggerwithresult = BehaviorBuilder.get(app1);
         final BehaviorBuilder.TriggerWithResult<E, T2> behaviorbuilder_triggerwithresult1 = BehaviorBuilder.get(app2);
         final BehaviorBuilder.TriggerWithResult<E, T3> behaviorbuilder_triggerwithresult2 = BehaviorBuilder.get(app3);
         final BehaviorBuilder.TriggerWithResult<E, T4> behaviorbuilder_triggerwithresult3 = BehaviorBuilder.get(app4);
         final BehaviorBuilder.TriggerWithResult<E, Function4<T1, T2, T3, T4, R>> behaviorbuilder_triggerwithresult4 = BehaviorBuilder.get(app);
         return BehaviorBuilder.create(new BehaviorBuilder.TriggerWithResult<E, R>() {
            public R tryTrigger(ServerLevel serverlevel, E livingentity, long i) {
               T1 object = behaviorbuilder_triggerwithresult.tryTrigger(serverlevel, livingentity, i);
               if (object == null) {
                  return (R)null;
               } else {
                  T2 object1 = behaviorbuilder_triggerwithresult1.tryTrigger(serverlevel, livingentity, i);
                  if (object1 == null) {
                     return (R)null;
                  } else {
                     T3 object2 = behaviorbuilder_triggerwithresult2.tryTrigger(serverlevel, livingentity, i);
                     if (object2 == null) {
                        return (R)null;
                     } else {
                        T4 object3 = behaviorbuilder_triggerwithresult3.tryTrigger(serverlevel, livingentity, i);
                        if (object3 == null) {
                           return (R)null;
                        } else {
                           Function4<T1, T2, T3, T4, R> function4 = behaviorbuilder_triggerwithresult4.tryTrigger(serverlevel, livingentity, i);
                           return (R)(function4 == null ? null : function4.apply(object, object1, object2, object3));
                        }
                     }
                  }
               }
            }

            public String debugString() {
               return behaviorbuilder_triggerwithresult4.debugString() + " * " + behaviorbuilder_triggerwithresult.debugString() + " * " + behaviorbuilder_triggerwithresult1.debugString() + " * " + behaviorbuilder_triggerwithresult2.debugString() + " * " + behaviorbuilder_triggerwithresult3.debugString();
            }

            public String toString() {
               return this.debugString();
            }
         });
      }

      static final class Mu<E extends LivingEntity> implements Applicative.Mu {
         private Mu() {
         }
      }
   }

   public static final class Mu<E extends LivingEntity> implements K1 {
   }

   static final class PureMemory<E extends LivingEntity, F extends K1, Value> extends BehaviorBuilder<E, MemoryAccessor<F, Value>> {
      PureMemory(final MemoryCondition<F, Value> memorycondition) {
         super(new BehaviorBuilder.TriggerWithResult<E, MemoryAccessor<F, Value>>() {
            public MemoryAccessor<F, Value> tryTrigger(ServerLevel serverlevel, E livingentity, long i) {
               Brain<?> brain = livingentity.getBrain();
               Optional<Value> optional = brain.getMemoryInternal(memorycondition.memory());
               return optional == null ? null : memorycondition.createAccessor(brain, optional);
            }

            public String debugString() {
               return "M[" + memorycondition + "]";
            }

            public String toString() {
               return this.debugString();
            }
         });
      }
   }

   interface TriggerWithResult<E extends LivingEntity, R> {
      @Nullable
      R tryTrigger(ServerLevel serverlevel, E livingentity, long i);

      String debugString();
   }

   static final class TriggerWrapper<E extends LivingEntity> extends BehaviorBuilder<E, Unit> {
      TriggerWrapper(final Trigger<? super E> trigger) {
         super(new BehaviorBuilder.TriggerWithResult<E, Unit>() {
            @Nullable
            public Unit tryTrigger(ServerLevel serverlevel, E livingentity, long i) {
               return trigger.trigger(serverlevel, livingentity, i) ? Unit.INSTANCE : null;
            }

            public String debugString() {
               return "T[" + trigger + "]";
            }
         });
      }
   }
}
