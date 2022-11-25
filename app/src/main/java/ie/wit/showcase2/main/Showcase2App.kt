package ie.wit.showcase2.main

import android.app.Application
import timber.log.Timber
//import ie.wit.showcase2.models.PortfolioJSONStore
import ie.wit.showcase2.models.PortfolioStore
import timber.log.Timber.i

class Showcase2App : Application() {

    lateinit var portfolios: PortfolioStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        //portfolios = PortfolioJSONStore(applicationContext)
        i("Showcase started")
    }
}