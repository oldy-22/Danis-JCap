package com.watsonnet.jcap;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/*
 * File: CloseableDialog.java
 * Created for Projekt: DaniBibo
 * Created on 07.11.2007 by Daniel Enke
 */

/**
 * Klasse zum ... / mit folgenden Eigenschaften
 * @author Daniel Enke
 */
public class CloseableDialog extends Dialog implements WindowListener {

	/**
	 * @param owner
	 */
	public CloseableDialog(Dialog owner) {
		super(owner);
		this.addWindowListener(this);
	}

	/**
	 * @param owner
	 */
	public CloseableDialog(Frame owner) {
		super(owner);
		this.addWindowListener(this);
	}

	/**
	 * @param owner
	 * @param modal
	 */
	public CloseableDialog(Frame owner, boolean modal) {
		super(owner, modal);
		this.addWindowListener(this);
	}

	/**
	 * @param owner
	 * @param title
	 */
	public CloseableDialog(Dialog owner, String title) {
		super(owner, title);
		this.addWindowListener(this);
	}

	/**
	 * @param owner
	 * @param title
	 * @param modal
	 */
	public CloseableDialog(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		this.addWindowListener(this);
	}

	/**
	 * @param owner
	 * @param title
	 */
	public CloseableDialog(Frame owner, String title) {
		super(owner, title);
		this.addWindowListener(this);
	}

	/**
	 * @param owner
	 * @param title
	 * @param modal
	 */
	public CloseableDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		this.addWindowListener(this);
	}

	/**
	 * @param owner
	 * @param title
	 * @param modal
	 * @param gc
	 */
	public CloseableDialog(
		Dialog owner,
		String title,
		boolean modal,
		GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		this.addWindowListener(this);
	}

	/**
	 * @param owner
	 * @param title
	 * @param modal
	 * @param gc
	 */
	public CloseableDialog(
		Frame owner,
		String title,
		boolean modal,
		GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		this.addWindowListener(this);
	}

	public void windowClosing(WindowEvent e) { this.dispose(); }
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}

}
