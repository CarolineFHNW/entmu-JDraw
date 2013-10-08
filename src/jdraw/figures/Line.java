/*
 * Copyright (c) 2000-2013 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package jdraw.figures;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.awt.geom.Line2D;

import jdraw.framework.Figure;
import jdraw.framework.FigureEvent;
import jdraw.framework.FigureHandle;
import jdraw.framework.FigureListener;

/**
 * Represents rectangles in JDraw.
 * 
 * @author Christoph Denzler
 *
 */
public class Line implements Figure {
	/**
	 * Use the java.awt.Rectangle in order to save/reuse code.
	 */
	private Line2D.Double line;

	/** Register all DrawModelListeners in a list */
	private final List<FigureListener> fListeners = new LinkedList<FigureListener>();

	/**
	 * Create a new rectangle of the given dimension.
	 * @param x1 the x-coordinate of the upper left corner of the rectangle
	 * @param y1 the y-coordinate of the upper left corner of the rectangle
	 * @param x2 the rectangle's width
	 * @param y2 the rectangle's height
	 */
	public Line(int x1, int y1, int x2, int y2) {
		line = new Line2D.Double(x1, y1, x2, y2);
	}

	/**
	 * Draw the line to the given graphics context.
	 * @param g the graphics context to use for drawing.
	 */
	public void draw(Graphics g) {
		Graphics2D g2D;
		g2D = (Graphics2D) g;
		g2D.setColor(Color.BLACK);
		g2D.draw(line);
	}

	@Override
	public void setBounds(Point origin, Point corner) {
		// Only notify real changes */
		Line2D.Double original = new Line2D.Double(line.getP1(), line.getP2());
		line.setLine(origin, corner);
		if(!original.equals(line)) {
			propagateFigureEvent(new FigureEvent(this));
		}
	}

	/**
	 * 		java.awt.Rectangle original = new java.awt.Rectangle(rectangle);
		rectangle.setFrameFromDiagonal(origin, corner);
		if(!original.equals(rectangle)) {
			propagateFigureEvent(new FigureEvent(this));
		}
	 * 
	 */


	@Override
	public void move(int dx, int dy) {
		// only notify real changes
		if (dx != 0 || dy != 0) {
			line.setLine(line.x1 + dx, line.y1 + dy, line.x2 + dx, line.y2 + dy);
			propagateFigureEvent(new FigureEvent(this));		// notification of change
		}
	}

	@Override
	public boolean contains(int x, int y) {
		return line.contains(x, y);
	}

	@Override
	public Rectangle getBounds() {
		return line.getBounds();
	}

	/**
	 * Returns a list of 8 handles for this Rectangle.
	 * @return all handles that are attached to the targeted figure.
	 * @see jdraw.framework.Figure#getHandles()
	 */	
	public List<FigureHandle> getHandles() {
		return null;
	}

	@Override
	public void addFigureListener(FigureListener listener) {
		fListeners.add(listener);
	}

	@Override
	public void removeFigureListener(FigureListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Notification method 
	 * This class is being observed by DrawModel implementing classes 
	 * This method notifies all observers on changes
	 * figureChanged is implemented in DrawModel Ctor
	 */
	public void propagateFigureEvent(FigureEvent e){
		FigureListener[] copy = fListeners.toArray(new FigureListener[fListeners.size()]);
		for (FigureListener fl : copy) {
			fl.figureChanged(e);
		}
	}

	@Override
	public Object clone() {
		return null;
	}

}
