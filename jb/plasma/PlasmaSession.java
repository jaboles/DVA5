package jb.plasma;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.List;
import com.innahema.collections.query.queriables.Queryable;
import jb.plasma.data.DepartureData;
import jb.plasma.data.IDepartureDataSource;
import jb.plasma.ui.PlasmaWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Runs while the indicator is displayed. Manages de-queuing the first departure from the list
// once its due-out has passed, and playing periodic announcements.
public class PlasmaSession
{
    final static Logger logger = LoggerFactory.getLogger(PlasmaSession.class);
    // List of departures to display
    private IDepartureDataSource dataSource;
    // All active renderers, so they can be notified when a departure has been popped off the front of the list
    private List<Drawer> drawers;
    // All open plasma windows, so they can be all closed when the session is stopped
    private List<PlasmaWindow> windows;
    // Timer to manage de-queueing and playing announcements
    private javax.swing.Timer timer;
    // When announcements should be played (in minutes) before the due-out time
    private int[] announcementTimes;
    // Functor to play the announcement
    private Runnable announce;
    
    public PlasmaSession(List<PlasmaWindow> windows, final Runnable announce, IDepartureDataSource dataSource, List<Drawer> drawers, int[] announcementTimes)
    {
        this.announce = announce;
        this.dataSource = dataSource;
        this.drawers = drawers;
        this.windows = windows;
        this.announcementTimes = announcementTimes;

        timer = new javax.swing.Timer(1000, timerAction);
        timer.start();
    }

    // Stop session, close all windows, stop the timer.
    public void stop() {
        timer.stop();
        Queryable.from(windows).forEachR(PlasmaWindow::close);
        windows.clear();
    }

    public void dataChanged()
    {
        for (Drawer d : drawers)
            d.dataChanged(dataSource.getDepartureData());
    }

    // Determine whether two times are within half a second of each other.
    private boolean withinOneSecondOf(Calendar time1, Calendar time2) {
        long diff = Math.abs(time1.getTime().getTime() - time2.getTime().getTime());
        return diff <= 500;
    }

    ActionListener timerAction = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            // Only need to do something if there's at least one departure in the list
            List<DepartureData> data = dataSource.getDepartureData();
            if (data.size() > 0)
            {
                Calendar now = Calendar.getInstance();
                // If due-out time has passed, pop off the first entry in the list and notify the renderers
                if (data.get(0).DueOut.compareTo(now) < 0)
                {
                    logger.info("Reached the departure time -- dequeuing first DepartureData");
                    dataSource.notifyDeparture();
                    dataChanged();
                }

                // If a sound library has been specified, and it's time to play an announcement, play it
                // using the given sound library.
                if (announcementTimes != null && announce != null) {
                    for (int announcementTime : announcementTimes) {
                        Calendar c = Calendar.getInstance();
                        c.add(Calendar.MINUTE, announcementTime);
                        c.add(Calendar.SECOND, 30);
                        if (withinOneSecondOf(c, data.get(0).DueOut))
                        {
                            announce.run();
                            break;
                        }
                    }
                }
            }
        }
    };
}