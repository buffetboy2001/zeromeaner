package org.zeromeaner.gui.net;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class WindowSnapper extends ComponentAdapter {

    public WindowSnapper(int snap_distance) {
        this.sd = snap_distance;
    }
	
    private boolean locked = false;
    private int sd = 50;
	
    public void componentMoved(ComponentEvent evt) {
        if(locked) return;
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        int nx = evt.getComponent().getX();
        int ny = evt.getComponent().getY();
        if(nx < 0) {
            nx = 0;
        }
        if(ny < 0) {
            ny = 0;
        }
        if(nx < 0+sd) {
            nx = 0;
        }
        if(nx > size.getWidth()-evt.getComponent().getWidth()-sd) {
            nx = (int)size.getWidth()-evt.getComponent().getWidth();
        }

        if(ny > size.getHeight()-evt.getComponent().getHeight()-sd) {
            ny = (int)size.getHeight()-evt.getComponent().getHeight();
        }
		// make sure we don't get into a recursive loop when the
		// set location generates more events
        locked = true;
        evt.getComponent().setLocation(nx,ny);
        locked = false;
    }
}