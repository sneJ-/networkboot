package uk.ac.lsbu.networkboot.frontend;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * PhaseListener that logs a short message before and after every phase.
 * 
 * @author Michael Kurz
 */
public class DebugPhaseListener implements PhaseListener {
    private static final long serialVersionUID = 28697126271609506L;

    public void afterPhase(PhaseEvent event) {
		System.out.println("After phase: " + event.getPhaseId());
	}

	public void beforePhase(PhaseEvent event) {
		System.out.println("Before phase: " + event.getPhaseId());
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
