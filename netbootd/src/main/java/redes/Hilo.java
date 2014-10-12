package redes;

import java.util.Observable;
import java.util.Observer;

/**
 * Implementacion para manejo de hilos en patorn observers
 * @author Diego
 *
 */
public abstract class Hilo extends Observable implements Runnable {
	
	private boolean pausado = false;
	private Object seguro = null;
	private boolean abortado = false;
	protected Thread thread = null;
			
	public Hilo() {
		seguro = new Object();
	}
	
	public Hilo(Observer o) {
		this();
		addObserver(o);
	}
	
	public void aborte() {
		abortado = true;
	}
	
	protected boolean continueTarea() {
		while (pausado) {
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				setChanged();
				notifyObservers(e);
			}
		}
		
		if (abortado) {
			return false;
		}
		
		return true;
	}
	
	public boolean estaAbortado() {
		return abortado;
	}
	
	public boolean estaPausado() {
		return pausado;
	}
	
	public boolean estaCorriendo() {
		return thread != null && thread.isAlive();
	}
	
	public void pausa(boolean p) {
		pausado = p;
	}
	
	public abstract void run();
	
	public void setNombre(String n) {
		if (n != null && estaCorriendo())
			thread.setName(n);
	}
	
	public void inicie() {
		if (estaCorriendo()) {
			return;
		}
		
		synchronized (seguro) {
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void esperaHastaFinal() {
		espereHastaFinal(-1);
	}
	
	public void espereHastaFinal(int max) {
		int zeit = 0;
		
		if (max == 0)
			zeit = -1;
		
		while (estaCorriendo() && zeit < max) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				setChanged();
				notifyObservers(e);
			}
			;
		}
	}
}
