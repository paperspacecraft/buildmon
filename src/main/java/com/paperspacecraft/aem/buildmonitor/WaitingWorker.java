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

    private static final String WAITING_MESSAGE = "Waiting for the response from %s (max %d sec. remaining)...";
    private static final String SUCCESS_MESSAGE = "Succeeded after %d sec.";
    private static final String ERROR_MESSAGE = "After %s sec., we haven't received the expected response. The waiting limit is reached.";
    private static final String INTERRUPTED_MESSAGE = "Waiting is interrupted.";

    private final HttpTester tester;
    private final int pollingInterval;
    private final int maxWaiting;
    private final Log logger;

    public boolean doWaiting() {

        logger.info(String.format(WAITING_MESSAGE, tester.getEndpoint(), maxWaiting / 1000));
        long startMoment = System.currentTimeMillis();

        CountDownLatch completion = new CountDownLatch(1);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(() -> {
            if (tester.test()) {
                completion.countDown();
            }
        }, 0, pollingInterval, TimeUnit.MILLISECONDS);

        Timer timer = new Timer();
        TimerUpdatingTask task = new TimerUpdatingTask(startMoment);
        timer.scheduleAtFixedRate(task, 0, pollingInterval);

        boolean success = false;
        try {
            success = completion.await(maxWaiting, TimeUnit.MILLISECONDS);
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

        @Override
        public void run() {
            currentMoment = System.currentTimeMillis();
            long timePassed = currentMoment - startingMoment;
            long timeRemaining = maxWaiting - timePassed;
            if (timeRemaining >= 1000) {
                logger.info(String.format(WAITING_MESSAGE, tester.getEndpoint(), timeRemaining / 1000));
            }
        }

        public long getDuration() {
            return (currentMoment - startingMoment) / 1000;
        }
    }
}
