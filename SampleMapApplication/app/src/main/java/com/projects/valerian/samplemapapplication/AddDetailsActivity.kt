package com.projects.valerian.samplemapapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.projects.valerian.samplemapapplication.api.SuggestionsAPI
import com.projects.valerian.samplemapapplication.api.SuggestionsAPI.InfrastructureType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_details.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AddDetailsActivity : Activity() {

    private var addSuggestionDisposable: Disposable? = null
    private var selectedItems = mutableListOf<InfrastructureType>()
    private lateinit var suggestionsApi: SuggestionsAPI


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        intent?.extras?.getStringArray(EXTRA_SUGGESTIONS)?.let {
            selectedItems.addAll(it.map { InfrastructureType.valueOf(it) })
        }

        val lat: Double = intent?.extras?.getDouble(EXTRA_LATITUDE) ?: 0.0
        val lon: Double = intent?.extras?.getDouble(EXTRA_LONGITUDE) ?: 0.0

        val coords = LatLng(lat,lon)

        suggestionsApi = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(SuggestionsAPI::class.java)

        setupButton(coords)
    }

    private fun setupButton(coords: LatLng) =
            btn_submit.setOnClickListener {
                if (addSuggestionDisposable == null && selectedItems.isNotEmpty()) {
                    addSuggestionDisposable =
                            suggestionsApi.postSuggestion(SuggestionsAPI.Suggestion(coords.latitude, coords.longitude, selectedItems.toList()))
                                    .timeout(5000, TimeUnit.MILLISECONDS)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        setResult(Activity.RESULT_OK)
                                        this.finish()
                                    }, {
                                        showSnackbar("Check your network connection")
                                        addSuggestionDisposable = null
                                    })
                }
            }

    private fun showSnackbar(msg: String) {
        Snackbar.make(main, msg, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        private const val EXTRA_LATITUDE = "extra_latitude"
        private const val EXTRA_LONGITUDE = "extra_longitude"
        private const val EXTRA_SUGGESTIONS = "extra_suggestions"

        private const val BASE_URL = "https://hibuaduiiqwerudnba.serveo.net/"

        fun createIntent(context: Context, lat: Double, lon: Double, suggestions: Array<String>) =
                Intent(context, AddDetailsActivity::class.java).apply {
                    putExtra(EXTRA_LATITUDE, lat)
                    putExtra(EXTRA_LONGITUDE, lon)
                    putExtra(EXTRA_SUGGESTIONS, suggestions)
                    flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
                }
    }
}