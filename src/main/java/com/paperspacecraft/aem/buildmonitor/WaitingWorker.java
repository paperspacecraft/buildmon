package com.paperspacecraft.aem.buildmonitor;

import com.paperspacecraft.aem.buildmonitor.http.HttpTester;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.logging.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Builder(builderClassName = "Builder")
class WaitingWorker {

    private static final String PRE_WAITING_MESSAGE = "Checks will proceed after %d sec.";
    private static final String WAITING_MESSAGE = "Waiting for the response from %s (max %d sec. remaining)...";
    private static final String SUCCESS_MESSAGE = "Succeeded after %d sec.";
    private static final String ERROR_MESSAGE = "After %s sec., we haven't received the expected response. The waiting limit is reached.";
    private static final String INTERRUPTED_MESSAGE = "Waiting is interrupted.";

    private final HttpTester tester;
    private final int pollAfter;
    private final int pollingInterval;
    private final int maxWaiting;
    private final Log logger;

    public boolean doWaiting() {

        long startMoment = System.currentTimeMillis();

        CountDownLatch completion = new CountDownLatch(1);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(() -> {
            if (tester.test()) {
                completion.countDown();
            }
        }, pollAfter, pollingInterval, TimeUnit.MILLISECONDS);

        Timer timer = new Timer();
        TimerUpdatingTask task = new TimerUpdatingTask(startMoment);
        timer.scheduleAtFixedRate(task, pollAfter, pollingInterval);

        if (pollAfter > 0) {
            logger.info(String.format(PRE_WAITING_MESSAGE, pollAfter / 1000));
        }

        boolean success = false;
        try {
            success = completion.await((long) pollAfter + maxWaiting, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.debug(INTERRUPTED_MESSAGE);
            Thread.currentThread().interrupt();
        }

        executor.shutdown();
        timer.cancel();
        if (success) {
            logger.info(String.format(SUCCESS_MESSAGE, task.getDuration()));
        } else {
            logger.error(String.format(ERROR_MESSAGE, maxWaiting / 1000));
        }
        return success;
    }

    @RequiredArgsConstructor
    private class TimerUpdatingTask extends TimerTask {

        private final long startingMoment;
        private long currentMoment;
        private int runCount;

        @Override
        public void run() {
            if (runCount++ == 0) {
                logger.info(String.format(WAITING_MESSAGE, tester.getEndpoint(), maxWaiting / 1000));
            }
            currentMoment = System.currentTimeMillis();
            long timePassed = currentMoment - startingMoment;
            long timeRemaining = maxWaiting + pollAfter - timePassed;
            if (timeRemaining >= 1000) {
                logger.info(String.format(WAITING_MESSAGE, tester.getEndpoint(), timeRemaining / 1000));
            }
        }

        public long getDuration() {
            return (currentMoment - startingMoment) / 1000;
        }
    }
}
