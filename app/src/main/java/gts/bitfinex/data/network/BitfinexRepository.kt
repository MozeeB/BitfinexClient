package gts.bitfinex.data.network

import android.annotation.SuppressLint

import com.tinder.scarlet.WebSocket

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

import gts.bitfinex.data.model.toSubcribeTickerRequest
import gts.bitfinex.data.model.toSubcribeOrderBookrRequest

import gts.bitfinex.domain.BitfinexService
import gts.bitfinex.domain.model.TickerData
import gts.bitfinex.domain.model.toTickerData
import gts.bitfinex.domain.model.OrderBookData
import gts.bitfinex.domain.model.toOrderBookData
import gts.bitfinex.domain.entities.SubscribeTickerEntity
import gts.bitfinex.domain.entities.SubscribeOrderBookEntity

import timber.log.Timber

@SuppressLint("CheckResult")
class BitfinexRepository(private val bitfinexApi: BitfinexApi) : BitfinexService {

    override fun subscribeAndObserveTicker(subscribe: SubscribeTickerEntity): Flowable<TickerData> {
        Timber.d("===> subscribeAndObserveTicker")
        bitfinexApi.openWebSocketEvent()
            .filter {
                it is WebSocket.Event.OnConnectionOpened<*>
            }
            .subscribe({
                bitfinexApi.sendTickerRequest(subscribe.toSubcribeTickerRequest())
                Timber.d("subscribeAndObserveTicker <===")
            }, { e ->
                Timber.e(e)
            })

        return bitfinexApi.observeTicker()
            .subscribeOn(Schedulers.io())
            .map {
                    t -> t.toTickerData()
            }
    }

    override fun subscribeAndObserveOrderBook(subscribe: SubscribeOrderBookEntity): Flowable<OrderBookData> {
        Timber.d("===> subscribeAndObserveOrderBook")
        bitfinexApi.openWebSocketEvent()
            .filter {
                it is WebSocket.Event.OnConnectionOpened<*>
            }
            .subscribe({
                bitfinexApi.sendOrderBookRequest(subscribe.toSubcribeOrderBookrRequest())
                Timber.d("subscribeAndObserveOrderBook <===")
            }, { e ->
                Timber.e(e)
            })

        return bitfinexApi.observeOrderBook()
            .subscribeOn(Schedulers.io())
            .filter{
                it[2].toInt() != 0 // COUNT=0 means that you have to remove the price level from your book.
            }
            .map { t -> t.toOrderBookData() }
    }
}