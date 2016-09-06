package jb.common.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JFrame;
import javax.swing.Timer;

public class WindowFader
{
    private Timer t;
    private float fadeDelta;
    private float opacity;
    private JFrame frame;
    private int delayMs;
    
    public WindowFader(JFrame frame, int durationMs, int fps)
    {
        this.frame = frame;
        delayMs = 1000 / fps;
        fadeDelta = 1.0f / ((float) durationMs / delayMs);
    }
    
    public void fadeIn()
    {
        opacity = 0;
        AWTUtilities_setWindowOpacity(frame,opacity); 
        frame.setVisible(true);
        t = new Timer(delayMs, fadeInEvent);
        t.start();
    }
    
    public void fadeOut()
    {
        opacity = 1;
        AWTUtilities_setWindowOpacity(frame,opacity);
        t = new Timer(delayMs, fadeOutEvent);
        t.start();
    }
    
    public ActionListener fadeInEvent = new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            opacity += fadeDelta;
            if (opacity <= 1)
            {
                AWTUtilities_setWindowOpacity(frame,opacity);
                return;
            }

            AWTUtilities_setWindowOpacity(frame,1f);
            t.setRepeats(false);
        }
    };

    public ActionListener fadeOutEvent = new ActionListener() {
        public void actionPerformed(ActionEvent e)
        {
            opacity -= fadeDelta;
            if (opacity >= 0)
            {
                AWTUtilities_setWindowOpacity(frame,opacity);
                return;
            }

            AWTUtilities_setWindowOpacity(frame,0f);
            t.setRepeats(false);
            
            frame.dispose();
        }
    };
    
    private static Method AWTUtilities_setWindowOpacity;
    private static boolean AWTUtilities_setWindowOpacity_loaded;
    private static void AWTUtilities_setWindowOpacity(Window w, float f) {
        if (!AWTUtilities_setWindowOpacity_loaded)
        {
            try
            {
                Class<?> awtUtilsClass = Class.forName("com.sun.awt.AWTUtilities");
                if (awtUtilsClass != null)
                {
                    AWTUtilities_setWindowOpacity = awtUtilsClass.getMethod("setWindowOpacity", Window.class, boolean.class);
                }
            }
            catch (ClassNotFoundException | NoSuchMethodException ignored) {}
            AWTUtilities_setWindowOpacity_loaded = true;
        }
        if (AWTUtilities_setWindowOpacity != null)
        {
            try
            {
                AWTUtilities_setWindowOpacity.invoke(null, w, f);
            }
            catch (IllegalAccessException | InvocationTargetException ignored) {}
        }        
    }
}
