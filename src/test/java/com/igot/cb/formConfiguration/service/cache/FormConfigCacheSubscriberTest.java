package com.igot.cb.formConfiguration.service.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FormConfigCacheSubscriberTest {

    @Mock
    private JedisPool jedisPool;

    @Mock
    private FormConfigCache formConfigCache;

    private FormConfigCacheSubscriber subscriber;

    @AfterEach
    void tearDown() throws Exception {
        if (subscriber != null) {
            subscriber.stop();
            Thread listenerThread = listenerThreadOf(subscriber);
            if (listenerThread != null) {
                listenerThread.join(3000);
            }
        }
    }

    private Thread listenerThreadOf(FormConfigCacheSubscriber target) throws Exception {
        Field field = FormConfigCacheSubscriber.class.getDeclaredField("listenerThread");
        field.setAccessible(true);
        return (Thread) field.get(target);
    }

    @Test
    void stop_beforeStart_shouldNotThrowAndSkipJedisInteractions() {
        subscriber = new FormConfigCacheSubscriber(jedisPool, formConfigCache);

        subscriber.stop();

        verifyNoInteractions(jedisPool);
    }

    @Test
    void start_shouldSpawnDaemonListenerThread() throws Exception {
        when(jedisPool.getResource()).thenThrow(new RuntimeException("Redis unavailable"));
        subscriber = new FormConfigCacheSubscriber(jedisPool, formConfigCache);

        subscriber.start();

        Thread listenerThread = listenerThreadOf(subscriber);
        assertNotNull(listenerThread);
        assertTrue(listenerThread.isDaemon());
        assertTrue(listenerThread.isAlive());
    }

    @Test
    void start_thenStop_shouldTerminateListenerThreadWithinTimeout() throws Exception {
        when(jedisPool.getResource()).thenThrow(new RuntimeException("Redis unavailable"));
        subscriber = new FormConfigCacheSubscriber(jedisPool, formConfigCache);

        subscriber.start();
        Thread listenerThread = listenerThreadOf(subscriber);
        subscriber.stop();
        listenerThread.join(3000);

        assertFalse(listenerThread.isAlive());
    }
}
