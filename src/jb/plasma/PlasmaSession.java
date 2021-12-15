package jb.plasma;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import jb.plasma.ui.PlasmaWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Runs while the indicator is displayed. Manages de-queuing the first departure from the list
// once its due-out has passed, and playing periodic announcements.
public class PlasmaSession
{
    final static Logger logger = LogManager.getLogger(PlasmaSession.class);
    // List of departures to display
    private final List<DepartureData> data;
    // All active renderers, so they can be notified when a departure has been popped off the front of the list
    private final List<Drawer> drawers;
    // All open plasma windows, so they can be all closed when the session is stopped
    private final List<PlasmaWindow> windows;
    // Timer to manage de-queueing and playing announcements
    private final javax.swing.Timer timer;
    // When announcements should be played (in minutes) before the due-out time
    private final int[] announcementTimes;
    // Functor to play the announcement
    private final Runnable announce;

    public PlasmaSession(List<PlasmaWindow> windows, final Runnable announce, List<DepartureData> data, List<Drawer> drawers, int[] announcementTimes)
    {
        this.announce = announce;
        this.data = data;
        this.drawers = drawers;
        this.windows = windows;
        this.announcementTimes = announcementTimes;

        timer = new javax.swing.Timer(1000, timerAction);
        timer.start();
    }

    // Stop session, close all windows, stop the timer.
    public void stop() {
        timer.stop();
        windows.forEach(PlasmaWindow::close);
        windows.clear();
    }

    // Determine whether two times are within half a second of each other.
    private boolean withinOneSecondOf(LocalDateTime time1, LocalDateTime time2) {
        long diff = Math.abs(ChronoUnit.MILLIS.between(time1, time2));
        return diff <= 500;
    }

    public void trainDeparted()
    {
        logger.debug("Reached the departure time -- dequeuing first DepartureData");
        if (data.size() > 0)
        {
            data.remove(0);
            if (data.size() > 0)
            {
                data.get(0).logDetails();
            }
        }

        for (Drawer d : drawers)
        {
            d.dataChanged(data);
        }
    }

    private final ActionListener timerAction = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            // Only need to do something if there's at least one departure in the list
            if (data.size() > 0)
            {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime dueOut = data.get(0).DueOut;
                if (dueOut != null)
                {
                    // If due-out time has passed, pop off the first entry in the list and notify the renderers
                    if (now.isAfter(dueOut))
                    {
                        trainDeparted();
                    }
                    // If a sound library has been specified, and it's time to play an announcement, play it
                    // using the given sound library.
                    else if (announcementTimes != null && announce != null) {
                        for (int announcementTime : announcementTimes) {
                            LocalDateTime announceAt = now
                                    .plusMinutes(announcementTime)
                                    .plusSeconds(30);
                            if (withinOneSecondOf(announceAt, dueOut))
                            {
                                announce.run();
                                break;
                            }
                        }
                    }
                }
            }
        }
    };
}