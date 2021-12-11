package jb.dvacommon;

import javax.swing.SwingUtilities;
import jb.common.ui.ProgressWindow;

public class ProgressAdapter
{
    private final ProgressWindow pw;
    private long lastProgressUpdate;
    private final boolean download;

    public ProgressAdapter(ProgressWindow pw)
    {
        this(pw, true);
    }

    public ProgressAdapter(ProgressWindow pw, boolean download) {
        this.pw = pw;
        this.download = download;
        lastProgressUpdate = 0;
        if (download) {
            SwingUtilities.invokeLater(() -> pw.setProgressText("Downloading 0K of 0K"));
        }
    }

    public void show() {
        pw.show();
    }
    
    public void dispose() { pw.dispose(); }
    
    public void updateProgress(final int bytesRead, final int maxBytes, String currentTask, String currentSubTask)
    {
        SwingUtilities.invokeLater(() -> {
            if (pw.getProgressBarMaximum() != maxBytes)
            {
                pw.setProgressBarMaximum(maxBytes);
            }
            pw.setValue(bytesRead);
        });
        if (System.currentTimeMillis() - lastProgressUpdate > 200)
        {
            if (download) {
                SwingUtilities.invokeLater(() -> pw.setProgressText(currentTask + " " +
                        String.format("%.1f", (double) bytesRead / 1048576) + "M of " + String.format("%.1f", (double) maxBytes / 1048576) + "M" +
                        (currentSubTask != null ? " (" + currentSubTask + ")." : ".")));
            } else {
                SwingUtilities.invokeLater(() -> pw.setProgressText(currentTask + "\r\n" + currentSubTask));
            }
            lastProgressUpdate = System.currentTimeMillis();
        }
    }
    
    public void updateProgressComplete(String task)
    {
        SwingUtilities.invokeLater(() -> {
            pw.setProgressText(task);
            pw.setValue(pw.getProgressBarMaximum());
            pw.repaint();
        });
    }
    
    public void enableCancel(Thread threadToInterrupt) {
        pw.setCancelAction(threadToInterrupt::interrupt);
    }
    
    public void disableCancel() {
        pw.setCancelAction(null);
    }
}
