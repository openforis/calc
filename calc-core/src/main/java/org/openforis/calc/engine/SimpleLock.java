package org.openforis.calc.engine;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public final class SimpleLock {
	private boolean locked;
	
	SimpleLock() {
		this.locked = false;
	}
	
	synchronized
	public boolean isLocked() {
		return locked;
	}
	
	synchronized
	public boolean tryLock() {
		if ( locked ) {
			return false;
		}
		this.locked = true;
		return true;
	}
	
	synchronized
	public void unlock() {
		if ( !locked ) {
			throw new IllegalStateException("Already unlocked");
		}
		this.locked = false;
	}
}
