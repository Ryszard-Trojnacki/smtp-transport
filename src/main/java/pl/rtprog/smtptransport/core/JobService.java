package pl.rtprog.smtptransport.core;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

/**
 * Job processing (sending) service.<br/>
 * TODO: Add shutdown code
 * 
 * @author Ryszard Trojnacki
 */
public class JobService {
	/**
	 * Actually active workers
	 */
	private volatile int activeWorkers=0;
	
	private class JobThread extends Thread {
		private final Runnable job;
		
		public JobThread(Runnable job) {
			this.job=job;
		}
		
		@Override
		public void run() {
			try {
				try {
					job.run();
				}catch(Exception e) {
					// Log error?
				}finally {
					synchronized(lock) {
						--activeWorkers;	// decrease active workers
					}
					processQueue();
				}
			}finally {
			}
		}
	}
	
	@Inject
	private ConfigurationService cs;

	/** Object for synchronization */
	private final Object lock=new Object();

	/** Queued jobs */
	private LinkedList<JobThread> jobs=new LinkedList<>();
	
	private void processQueue() {
		int bg=cs.getConfiguration().getBackgroundThreads();
		if(bg==0) bg=1;	// if background mode disabled then execute queued jobs
		synchronized(lock) {
			for(;;) {
				if(jobs.isEmpty()) return;	// nothing left
				if(activeWorkers<bg) {
					JobThread task=jobs.removeFirst();
					task.start();
					++activeWorkers;
				} else return;	// workers left
			}
		}
	}
	
	public boolean isBackgroundMode() {
		return cs.getConfiguration().getBackgroundThreads()>0;
	}
	
	/**
	 * Runs job in foreground (if configured no background senders) and then throws {@link InvocationTargetException} when error
	 * or in background, when background senders is more than 0.
	 * @param job job to execute
	 */
	public void run(Runnable job) {
		int bg=cs.getConfiguration().getBackgroundThreads();
		if(bg==0) {	// run in foreground
			job.run();
		} else {
			JobThread j=new JobThread(job);
			synchronized(lock) {
				jobs.add(j);	
				processQueue();
			}
		}
	}
	
}
