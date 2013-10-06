/*
 * Copyright (c) 2000-2013 Fachhochschule Nordwestschweiz (FHNW)
 * All Rights Reserved. 
 */

package jdraw.std;

import java.awt.Dimension;
import java.util.ArrayList;
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
public class StdDrawModel implements DrawModel {

	/** C: The view's selection. */
	private List<Figure> figurelist = new LinkedList<Figure>();

	/** C: Send changes to this listener. */
	private FigureListener fl;

	/** M: Register all DrawModelListeners in a list. */
	private List<DrawModelListener> mListeners = new ArrayList<DrawModelListener>();

	/** Creates a new StdDrawModel. */
	public StdDrawModel() {
		fl = new FigureListener() {
			// figureChanged(e) is the UPDATE method of this observer
			@Override
			public void figureChanged(FigureEvent e) {
				//System.out.println("Geaenderte Figur: " +e.getFigure());
				notifyDrawModelChangeListener(e.getFigure(), Type.FIGURE_CHANGED);
			}
		};
	}

	/** M: Only add FigureListener if f is being added to the list. */
	@Override
	public void addFigure(Figure f) {
		if(f != null && !figurelist.contains(f)) {
			figurelist.add(f);
			f.addFigureListener(fl);
			notifyDrawModelChangeListener(f, Type.FIGURE_ADDED);
			//System.out.println(f + ", Index: " + figurelist.indexOf(f));
		}
	}

	/** M: Return a copy of the current figurelist to avoid concurrency problem. */
	@Override
	public Iterable<Figure> getFigures() {
		return new LinkedList<Figure>(figurelist);
	}

	/** M: Only remove FigureListener if f is contained in list.  
	 *  M: Remove Observer
	 *  M: Notify Observers (Views) */
	@Override
	public void removeFigure(Figure f) {
		if(figurelist.contains(f)) {
			figurelist.remove(f);
			f.removeFigureListener(fl);
			notifyDrawModelChangeListener(f, Type.FIGURE_REMOVED);
		}
	}

	@Override
	public void addModelChangeListener(DrawModelListener listener) {
		mListeners.add(listener);
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

		if(!figurelistCopy.contains(f)) {
			throw new IllegalArgumentException();
		}

		if(index < 0 || index >= figurelistCopy.size()) {
			throw new IndexOutOfBoundsException();
		}

		if(figurelistCopy.indexOf(f) != index){

			// remove element that needs repositioning
			figurelistCopy.remove(f);

			// prepare a new list that will contain the new order
			List<Figure> figurelistNew = new LinkedList<Figure>();
			List<Figure> sl1 = new LinkedList<Figure>();
			List<Figure> sl2 = new LinkedList<Figure>();

			// divide current list at the index position in two sublists
			sl1 = figurelistCopy.subList(0, index);
			sl2 = figurelistCopy.subList(index, figurelistCopy.size());

			// add up sublists and the element that needs repositioning
			figurelistNew.addAll(sl1);
			figurelistNew.add(f);
			figurelistNew.addAll(sl2);

			// to check the new positions
			/*Iterator y = ((LinkedList<Figure>) figurelistNew).iterator();
				while (y.hasNext()) {
					Figure temp = (Figure) y.next();
					System.out.println(temp + ", Index: " + figurelistNew.indexOf(temp));
				}*/

			// overwriting of the original list by the temporary one
			figurelist = figurelistNew;
			notifyDrawModelChangeListener(f, Type.DRAWING_CHANGED);
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
	public void notifyDrawModelChangeListener(Figure f, Type t){		
		for (DrawModelListener ml : mListeners) {
			ml.modelChanged(new DrawModelEvent(this, f, t));
		}
	}

}
