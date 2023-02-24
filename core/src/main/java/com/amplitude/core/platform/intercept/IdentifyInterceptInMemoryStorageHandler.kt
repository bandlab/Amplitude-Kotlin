package com.amplitude.core.platform.intercept

import com.amplitude.core.events.BaseEvent
import com.amplitude.core.events.IdentifyOperation
import com.amplitude.core.utilities.InMemoryStorage

class IdentifyInterceptInMemoryStorageHandler(
    private val storage: InMemoryStorage
) : IdentifyInterceptStorageHandler {
    override suspend fun getTransferIdentifyEvent(): BaseEvent? {
        val eventsData = storage.readEventsContent() as List<List<BaseEvent>>
        if (eventsData.isEmpty() || eventsData[0].isEmpty()) {
            return null
        }
        val events = eventsData[0]
        val identifyEvent = events[0]
        val identifyEventUserProperties = identifyEvent.userProperties!!.get(IdentifyOperation.SET.operationType) as MutableMap<String, Any?>
        val userProperties = IdentifyInterceptorUtil.mergeIdentifyList(events.subList(1, events.size))
        identifyEventUserProperties.putAll(userProperties)
        identifyEvent.userProperties!!.put(IdentifyOperation.SET.operationType, identifyEventUserProperties)
        return identifyEvent
    }

    override suspend fun fetchAndMergeToNormalEvent(event: BaseEvent): BaseEvent {
        val eventsData = storage.readEventsContent() as List<List<BaseEvent>>
        if (eventsData.isEmpty() || eventsData[0].isEmpty()) {
            return event
        }
        val events = eventsData[0]
        val userProperties = IdentifyInterceptorUtil.mergeIdentifyList(events)
        event.userProperties?.let {
            userProperties.putAll(it)
        }
        event.userProperties = userProperties
        return event
    }

    override suspend fun fetchAndMergeToIdentifyEvent(event: BaseEvent): BaseEvent {
        val eventsData = storage.readEventsContent() as List<List<BaseEvent>>
        if (eventsData.isEmpty() || eventsData[0].isEmpty()) {
            return event
        }
        val events = eventsData[0]
        val userProperties = IdentifyInterceptorUtil.mergeIdentifyList(events)
        if (event.userProperties?.contains(IdentifyOperation.SET.operationType) == true) {
            userProperties.putAll(event.userProperties!!.get(IdentifyOperation.SET.operationType) as MutableMap<String, Any?>)
        }
        event.userProperties?.put(IdentifyOperation.SET.operationType, userProperties)
        return event
    }

    override suspend fun clearIdentifyIntercepts() {
        // no-op for in memory storage
        storage.removeEvents()
    }
}
