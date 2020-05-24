package com.gas.city.dependencyinjection

import com.gas.city.GasCityApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [GasCityModule::class, AndroidInjectionModule::class])
interface ApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: GasCityApplication): Builder

        fun build(): ApplicationComponent
    }

    fun inject(application: GasCityApplication)
}