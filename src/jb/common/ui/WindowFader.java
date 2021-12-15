package jb.common.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class WindowFader
{
    private Timer t;
    private final float fadeDelta;
    private float opacity;
    private final Window frame;
    private final int delayMs;

    public WindowFader(Window frame, int durationMs, int fps)
    {
        this.frame = frame;
        delayMs = 1000 / fps;
        fadeDelta = 1.0f / ((float) durationMs / delayMs);
    }

    public void fadeIn()
    {
        opacity = 0;
        frame.setOpacity(opacity);
        frame.setVisible(true);
        t = new Timer(delayMs, fadeInEvent);
        t.start();
    }

    public void fadeOut()
    {
        opacity = 1;
        frame.setOpacity(opacity);
        t = new Timer(delayMs, fadeOutEvent);
        t.start();
    }

    private final ActionListener fadeInEvent = new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            opacity += fadeDelta;
            if (opacity <= 1)
            {
                frame.setOpacity(opacity);
                return;
            }

            frame.setOpacity(1f);
            t.setRepeats(false);
        }
    };

    private final ActionListener fadeOutEvent = new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            opacity -= fadeDelta;
            if (opacity >= 0)
            {
                frame.setOpacity(opacity);
                return;
            }

            frame.setOpacity(0f);
            t.setRepeats(false);

            frame.dispose();
        }
    };
}