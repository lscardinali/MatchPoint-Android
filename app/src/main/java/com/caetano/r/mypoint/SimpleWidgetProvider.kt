package com.caetano.r.mypoint

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.caetano.r.mypoint.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SimpleWidgetProvider : AppWidgetProvider() {

    private val ACTION_REGISTER = "ACTION_REGISTER"
    private val CLIENT_ID = "client_id"
    private val TOKEN = "token"
    private val EMAIL = "email"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val intent = Intent(context, SimpleWidgetProvider::class.java)
            intent.action = ACTION_REGISTER
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)

            views.setOnClickPendingIntent(R.id.btn_register, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (ACTION_REGISTER == intent.action) {
            register(context)
        }
    }

    private fun register(context: Context) {
        val sharedPref = context.getSharedPreferences(context.getString(R.string.prefs), Context.MODE_PRIVATE)
        val register = generateRegisterData()
        val token = sharedPref.getString(TOKEN, "")
        val client = sharedPref.getString(CLIENT_ID, "")
        val email = sharedPref.getString(EMAIL, "")

        Api.service.register(register, token, client, email).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, response.body().success, Toast.LENGTH_LONG).show()
                    updateLastRegisterText(context)
                } else {
                    Toast.makeText(context, "Failed: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(context, "Failed: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun generateRegisterData(): Register {
        val timeCard = generateTimeCard()
        val device = generateDevice()
        return Register(timeCard, "/meu_ponto/registro_de_ponto", device,
                "0.10.21")
    }

    private fun generateDevice() = Device("4.1.0", "unknown", "Chrome", "browser", null, "61.0.3163.79")

    private fun generateTimeCard(): TimeCard {
        return TimeCard(-23.6015042, -46.694538,
                "Av. das Nações Unidas, 11541 - Cidade Monções, São Paulo - SP, Brasil",
                null, -23.6015042, -46.694538,
                "Av. das Nações Unidas, 11541 - Cidade Monções, São Paulo - SP, Brasil",
                false, 30)
    }

    private fun updateLastRegisterText(context: Context) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val min = calendar.get(Calendar.MINUTE)

        val timeToPrint = "Last register\n$hour:$min"

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setTextViewText(R.id.txt_last_register, timeToPrint)
        val appWidget = ComponentName(context, SimpleWidgetProvider::class.java)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidget, views)
    }
}