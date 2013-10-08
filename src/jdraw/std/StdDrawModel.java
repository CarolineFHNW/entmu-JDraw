/*
 * Copyright (c) 2000-2013 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package jdraw.std;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jdraw.framework.DrawCommandHandler;
import jdraw.framework.DrawModel;
import jdraw.framework.DrawModelEvent;
import jdraw.framework.DrawModelEvent.Type;
import jdraw.framework.DrawModelListener;
import jdraw.framework.Figure;
import jdraw.framework.FigureEvent;
import jdraw.framework.FigureListener;

/**
 * Provide a standard behavior for the drawing model. This class initially does not implement the methods
 * in a proper way.
 * It is part of the course assignments to do so.
 * @author Caroline Schnell, Marlene Wittwer
 *
 */
public class StdDrawModel implements DrawModel, FigureListener {

	/** C: The view's selection. */
	private List<Figure> figurelist = new LinkedList<Figure>();

	/** C: Send changes to this listener. */
	private FigureListener fl;

	/** M: Register all DrawModelListeners in a list. */
	private final List<DrawModelListener> mListeners = new LinkedList<DrawModelListener>();

	/** M: Only add FigureListener if f is being added to the list. */
	@Override
	public void addFigure(Figure f) {
		if(f != null && !figurelist.contains(f)) {
			figurelist.add(f);
			f.addFigureListener(this);
			notifyDrawModelChangeListener(f, Type.FIGURE_ADDED);
			//System.out.println(f + ", Index: " + figurelist.indexOf(f));
		}
	}

	/** M: Return a copy of the current figurelist to avoid concurrency problem. */
	@Override
	public Iterable<Figure> getFigures() {
		return Collections.unmodifiableList(figurelist);
	}

	/** M: Only remove FigureListener if f is contained in list.  
	 *  M: Remove Observer
	 *  M: Notify Observers (Views) */
	@Override
	public void removeFigure(Figure f) {
		if(figurelist.contains(f)) {
			figurelist.remove(f);
			f.removeFigureListener(this);
			notifyDrawModelChangeListener(f, Type.FIGURE_REMOVED);
		}
	}

	@Override
	public void addModelChangeListener(DrawModelListener listener) {
		if (listener != null && !mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	@Override
	public void removeModelChangeListener(DrawModelListener listener) {
		mListeners.remove(listener);
	}

	/** The draw command handler. Initialized here with a dummy implementation. */
	// TODO initialize with your implementation from the undo/redo-assignment.
	private DrawCommandHandler handler = new EmptyDrawCommandHandler();

	/**
	 * Retrieve the draw command handler in use.
	 * @return the draw command handler.
	 */
	public DrawCommandHandler getDrawCommandHandler() {
		return handler;
	}

	/** M: See interface definition in DrawModel. */
	@Override
	public void setFigureIndex(Figure f, int index) {
		// get a copy of figurelist to avoid concurrency problem
		List<Figure> figurelistCopy = (List<Figure>) getFigures();
		int pos = figurelistCopy.indexOf(index);

		if(!figurelistCopy.contains(f)) {
			throw new IllegalArgumentException();
		}

		if(index < 0 || index >= figurelistCopy.size()) {
			throw new IndexOutOfBoundsException();
		}

		if(pos != index){
			// remove element that needs repositioning
			figurelistCopy.remove(f);
			// add element at required position
			figurelistCopy.add(index, f);
			notifyDrawModelChangeListener(f, DrawModelEvent.Type.DRAWING_CHANGED);
		}
	}

	/** M: Remove all Listeners before clearing the list. */
	@Override
	public void removeAllFigures() {
		// get a copy of figurelist to avoid concurrency problem
		List<Figure> figurelistCopy = (List<Figure>) getFigures();

		Iterator i = figurelistCopy.iterator();

		while(i.hasNext()) {
			((Figure) i.next()).removeFigureListener(fl);
		}
		figurelistCopy.clear();

		// overwriting of the original list by the temporary one
		figurelist = figurelistCopy;

		// for event type DRAWING_CLEARED figure can be null (s. test documentation)
		notifyDrawModelChangeListener(null, Type.DRAWING_CLEARED);
	}

	/**
	 * Notification method
	 * This class is being observed by DrawView implementing classes
	 * This method notifies all observers (DataViews) on changes
	 * modelChanged is implemented in StdDrawView Ctor
	 */
	public void notifyDrawModelChangeListener(Figure f, DrawModelEvent.Type t) {
		DrawModelEvent dme = new DrawModelEvent(this, f, t);
		DrawModelListener[] copy = mListeners.toArray(new DrawModelListener[]{});
		for (DrawModelListener ml : copy) {
			ml.modelChanged(dme);
		}
	}

	@Override
	public void figureChanged(FigureEvent e) {
		notifyDrawModelChangeListener(e.getFigure(), DrawModelEvent.Type.FIGURE_CHANGED);
		
	}

}
