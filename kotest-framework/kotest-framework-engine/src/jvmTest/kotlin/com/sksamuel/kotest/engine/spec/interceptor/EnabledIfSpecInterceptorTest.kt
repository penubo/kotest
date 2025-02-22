package com.sksamuel.kotest.engine.spec.interceptor

import io.kotest.common.ExperimentalKotest
import io.kotest.core.annotation.EnabledCondition
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.config.EmptyExtensionRegistry
import io.kotest.core.config.FixedExtensionRegistry
import io.kotest.core.listeners.IgnoredSpecListener
import io.kotest.core.spec.Spec
import io.kotest.core.spec.SpecRef
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.listener.NoopTestEngineListener
import io.kotest.engine.spec.interceptor.EnabledIfSpecInterceptor
import io.kotest.matchers.booleans.shouldBeTrue
import kotlin.reflect.KClass

@ExperimentalKotest
class EnabledIfSpecInterceptorTest : FunSpec({

   test("EnabledIfSpecInterceptor should proceed for any spec not annotated with @EnabledIf") {
      var fired = false
      EnabledIfSpecInterceptor(NoopTestEngineListener, EmptyExtensionRegistry)
         .intercept(SpecRef.Reference(MyUnannotatedSpec::class)) {
            fired = true
            Result.success(emptyMap())
         }
      fired.shouldBeTrue()
   }

   test("EnabledIfSpecInterceptor should proceed any spec annotated with @EnabledIf that passes predicate") {
      var fired = false
      EnabledIfSpecInterceptor(NoopTestEngineListener, EmptyExtensionRegistry)
         .intercept(SpecRef.Reference(MyEnabledSpec::class)) {
            fired = true
            Result.success(emptyMap())
         }
      fired.shouldBeTrue()
   }

   test("EnabledIfSpecInterceptor should skip any spec annotated with @EnabledIf that fails predicate") {
      EnabledIfSpecInterceptor(NoopTestEngineListener, EmptyExtensionRegistry)
         .intercept(SpecRef.Reference(MyDisabledSpec::class)) { error("boom") }
   }

   test("EnabledIfSpecInterceptor should fire listeners on skip") {
      var fired = false
      val ext = object : IgnoredSpecListener {
         override suspend fun ignoredSpec(kclass: KClass<*>, reason: String?) {
            fired = true
         }
      }
      EnabledIfSpecInterceptor(NoopTestEngineListener, FixedExtensionRegistry(ext))
         .intercept(SpecRef.Reference(MyDisabledSpec::class)) { error("boom") }
      fired.shouldBeTrue()
   }
})

class MyEnabledCondition : EnabledCondition {
   override fun enabled(kclass: KClass<out Spec>): Boolean = true
}

class MyDisabledCondition : EnabledCondition {
   override fun enabled(kclass: KClass<out Spec>): Boolean = false
}


@EnabledIf(MyEnabledCondition::class)
private class MyEnabledSpec : FunSpec()

@EnabledIf(MyDisabledCondition::class)
private class MyDisabledSpec : FunSpec()

private class MyUnannotatedSpec : FunSpec()
