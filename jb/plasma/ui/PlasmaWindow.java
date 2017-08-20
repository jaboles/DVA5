package jb.plasma.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import org.swixml.SwingEngine;
import jb.common.ui.ProportionalPanel;
import jb.plasma.DepartureData;
import jb.plasma.Drawer;
import jb.plasma.renderers.CityrailV3Secondary;

// Custom window to display indicator in
public class PlasmaWindow extends JFrame
{
    public static class Mode
    {
        public static final Mode WINDOWED =                        new Mode() {{ IsFullScreen = false; IsUndecorated = false; CloseOnEvent = false; TerminateOnClose = false; LimitToOneScreen = false; }};
        public static final Mode FULLSCREEN =                      new Mode() {{ IsFullScreen = true;  IsUndecorated = true;  CloseOnEvent = false; TerminateOnClose = false; LimitToOneScreen = false; }};
        public static final Mode SCREENSAVER =                     new Mode() {{ IsFullScreen = true;  IsUndecorated = true;  CloseOnEvent = true;  TerminateOnClose = true;  LimitToOneScreen = false; }};
        public static final Mode SCREENSAVER_PREVIEW =             new Mode() {{ IsFullScreen = true;  IsUndecorated = true;  CloseOnEvent = true;  TerminateOnClose = false; LimitToOneScreen = false; }};
        public static final Mode SCREENSAVER_PREVIEW_MINI_WINDOW = new Mode() {{ IsFullScreen = false; IsUndecorated = true;  CloseOnEvent = false; TerminateOnClose = true;  LimitToOneScreen = true; }};
        
        public boolean IsFullScreen;
        public boolean IsUndecorated;
        public boolean CloseOnEvent;
        public boolean TerminateOnClose;
        public boolean LimitToOneScreen;
    }
    
    private static final long serialVersionUID = 1L;
    
    public Action announceAction;
    
    @SuppressWarnings("serial")
    public PlasmaWindow(final PlasmaUI controller, final Mode mode, int index, String title, Dimension size, Dimension aspectRatio, JPanel plasmaPanel) {
        super(mode.IsFullScreen ? (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[index]).getDefaultConfiguration() : null);

        announceAction = controller.announceAction;

        SwingEngine renderer = new SwingEngine(this);
        if (!mode.IsFullScreen)
        {
            try
            {
                setJMenuBar((JMenuBar) renderer.render(PlasmaWindow.class.getResource("/jb/plasma/ui/resources/plasmawindowmenu.xml")));
            } catch (Exception e) {
                jb.common.ExceptionReporter.reportException(e);
            }
        }
        
        // Set some window properties (icon, title, background)
        setDefaultCloseOperation(mode.TerminateOnClose ? WindowConstants.EXIT_ON_CLOSE : WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(getToolkit().getImage(PlasmaWindow.class.getResource("/resources/pse16.png")));
        setTitle(title);
        getContentPane().setBackground(Color.black);
        getContentPane().add(plasmaPanel);

        boolean closeOnMouseClick = mode.CloseOnEvent;
        boolean closeonMouseMove = mode.CloseOnEvent;
        boolean closeOnKey = mode.IsFullScreen;
        boolean closeOnEsc = true;

        // If set to close on Esc key
        if (closeOnEsc)
        {
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel"); //$NON-NLS-1$
            getRootPane().getActionMap().put("Cancel", new AbstractAction(){ //$NON-NLS-1$
                public void actionPerformed(ActionEvent e)
                {
                    if (controller != null)
                        controller.stopSession();
                }
            });
        }

        // If set to close on mouse movement (used for screen saver mode)
        if (closeonMouseMove)
        {
            MouseMotionListener mml = new MouseMotionListener() {
                Point lastPosition = null;
                public void mouseDragged(MouseEvent e) { }
                public void mouseMoved(MouseEvent e) {
                    Point newPosition = e.getLocationOnScreen();
                    if (lastPosition != null && !lastPosition.equals(newPosition))
                    {
                        if (controller != null)
                            controller.stopSession();
                        if (mode.TerminateOnClose)
                            System.exit(0);
                    }
                    lastPosition = newPosition;
                }
            };
            plasmaPanel.addMouseMotionListener(mml);
            addMouseMotionListener(mml);
        }

        // If set to close on key press (used for screen saver mode)
        if (closeOnKey)
        {
            KeyListener kl = new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (controller != null)
                        controller.stopSession();
                    if (mode.TerminateOnClose)
                        System.exit(0);
                }
            };
            plasmaPanel.addKeyListener(kl);
            addKeyListener(kl);
        }

        // If set to close on mouse click (used for screen saver mode)
        if (closeOnMouseClick)
        {
            MouseListener ml = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (controller != null)
                        controller.stopSession();
                    if (mode.TerminateOnClose)
                        System.exit(0);
                }
            };
            plasmaPanel.addMouseListener(ml);
            addMouseListener(ml);
        }

        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                if (controller != null)
                    controller.stopSession();
                if (mode.TerminateOnClose)
                    System.exit(0);
            }
        });

        // Set window properties depending on whether full screen mode was requested or not.
        if (mode.IsFullScreen) {
            setUndecorated(true);
            setResizable(false);
            GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[index].setFullScreenWindow(this);
            validate();
        } else {
            setUndecorated(mode.IsUndecorated);
            setResizable(!mode.IsUndecorated);
            if (mode.IsUndecorated) setJMenuBar(null);

            if (size == null)
            {
                Dimension ar = aspectRatio;
                int width = 1000;
                int height = 700;
                if (ar.getWidth() > ar.getHeight()) {
                    height = (int)(width * ar.getHeight() / ar.getWidth());
                } else {
                    width = (int)(height * ar.getWidth() / ar.getHeight());
                }
                plasmaPanel.setPreferredSize(new Dimension(width, height));
                pack();
            }
            else
            {
                plasmaPanel.setPreferredSize(size);
            }
            
            super.setVisible(true);
        }
    }

    // Dispose on close
    public boolean close()
    {
        dispose();
        return true;
    }

    // Test entry point
    public static void main(String[] args) {
        DepartureData[] dd = new DepartureData[]
        {
            new DepartureData()
            {{
                Destination = "Nowra";
                Destination2 = "via Wollongong";
                Type = "Limited stops";
                Cars = 4;
                Platform = 2;
                DueOut = Calendar.getInstance();
                DueOut.add(Calendar.MINUTE, 5);
                Stops = new String[] { "Redfern", "Hurstville", "Sutherland", "Waterfall", "Thirroul", "North Wollongong", "Wollongong", "Coniston", "Unanderra", "Kembla Grange", "Dapto", "Kiama", "Berry", "Nowra" };
            }},
            new DepartureData()
            {{
                Destination = "Nowra";
                Destination2 = "via Wollongong";
                Type = "Limited stops";
                Cars = 4;
                Platform = 2;
                DueOut = Calendar.getInstance();
                DueOut.add(Calendar.MINUTE, 10);
                Stops = new String[] { "Redfern", "Hurstville", "Sutherland", "Waterfall", "Thirroul", "North Wollongong", "Wollongong", "Coniston", "Unanderra", "Kembla Grange", "Dapto", "Kiama", "Berry", "Nowra" };
            }},
            new DepartureData()
            {{
                Destination = "Nowra";
                Destination2 = "via Wollongong";
                Type = "Limited stops";
                Cars = 4;
                Platform = 2;
                DueOut = Calendar.getInstance();
                DueOut.add(Calendar.MINUTE, 15);
                Stops = new String[] { "Redfern", "Hurstville", "Sutherland", "Waterfall", "Thirroul", "North Wollongong", "Wollongong", "Coniston", "Unanderra", "Kembla Grange", "Dapto", "Kiama", "Berry", "Nowra" };
            }}
        };

        Drawer dr = new CityrailV3Secondary();
        PlasmaPanel p = new PlasmaPanel(dr, Arrays.asList(dd));
        new PlasmaWindow(null, PlasmaWindow.Mode.FULLSCREEN, 0, dr.toString(), null, dr.getAspectRatio(), new ProportionalPanel(dr.getAspectRatio(), p)).setVisible(true);
    }
}
