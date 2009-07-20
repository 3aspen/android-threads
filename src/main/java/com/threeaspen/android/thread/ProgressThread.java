package com.threeaspen.android.thread;


import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ProgressThread extends Thread {
	private static final Logger logger = Logger.getLogger("com.threeaspen.android.utils.ProgressThread");
	
    private Activity activity;
	private ProgressDialog progressDialog;
	/*
	private LinkedBlockingQueue<Work> work = new LinkedBlockingQueue<Work>();
	
	public interface Work extends Runnable {
		public void complete();
		public boolean handleException(Exception ex);
		
		public String getTitle();
		public String getProgressMessage();
		public boolean getProgressIndeterminate();
		public int getProgressMax();
		public int getProgress();
	}*/

	public ProgressThread() {
		super();
	}

	public ProgressThread(Runnable runnable, String threadName) {
		super(runnable, threadName);
	}

	public ProgressThread(Runnable runnable) {
		super(runnable);
	}

	public ProgressThread(String threadName) {
		super(threadName);
	}

	public ProgressThread(ThreadGroup group, Runnable runnable, String threadName,
			long stackSize) {
		super(group, runnable, threadName, stackSize);
	}

	public ProgressThread(ThreadGroup group, Runnable runnable, String threadName) {
		super(group, runnable, threadName);
	}

	public ProgressThread(ThreadGroup group, Runnable runnable) {
		super(group, runnable);
	}

	public ProgressThread(ThreadGroup group, String threadName) {
		super(group, threadName);
	}

	/*
	protected void submitWork(Work w) {
		work.put(w);
	}

	@Override
	public void run() {
		for (currentWork = work.take(); currentWork != null; currentWork = work.take()) {
			try {
				showProgressDialog();
				currentWork.run();
			} catch (final Exception ex) {
				logger.log(Level.SEVERE, "Exception running work", ex);
				runOnUiThread(new Runnable() {
					public void run() {
						if (!currentWork.handleException(ex)) {
							throw ex;
						}
					}
				});
			} finally {
				dismissProgressDialog();
			}
			runOnUiThread(new Runnable() {
				public void run() {
					currentWork.complete();
				}
			});
		}
	}*/
	
	/**
	 * This method must be called while the monitor is held on this object. 
	 * The monitor should not be released until work with the activity is done.
	 * 
	 * @return
	 */
	protected synchronized Activity getActivity() {
		while(activity == null) {
			try {
				wait();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "Interrupted while waiting for activity", e);
			}
		}
		return activity;
	}
	
	/**
	 * Convenience method to post a Runnable to the UI thread using getActivity()
	 * 
	 * @param r something to run
	 */
	protected void runOnUiThread(Runnable r) {
		Handler h;
		synchronized (this) {
			Activity a = getActivity();
			h = new Handler(a.getMainLooper());	
		}
		h.post(r);
	}
	
	/**
	 * Method will notify this thread if it is waiting in getActivity()
	 * 
	 * @param activity
	 */
	public synchronized void setActivity(Activity activity) {
		this.activity = activity;
		if (activity == null) {
			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
		} else if (isAlive()){
			showProgressDialog();
		}
		notifyAll();
	}
	
	protected void showProgressDialog() {
		if (!isAlive()) return;
		runOnUiThread(new Runnable() {
			public void run() {
				Activity a = getActivity();
				if (progressDialog == null) {
					progressDialog = new ProgressDialog(a);
			    	setupDialog(progressDialog);
				}
				progressDialog.show();
			}
		});
	}
	
	protected void setupDialog(ProgressDialog progressDialog) {
	}
	
	protected void dismissProgressDialog() {
		runOnUiThread(new Runnable() {
			public void run() {
				// we don't need the activity, but we do need to make sure it exists
				getActivity();
				if (progressDialog != null) {
					try {
						progressDialog.dismiss();
						progressDialog = null;
					} catch (IllegalArgumentException ex) {
						// probably multiple overlapping threads got started, for example if
						// multiple orientation changes were done quickly
						logger.log(Level.WARNING, "Error while dismissing progress dialog", ex);
					}
				}
			}
		});
	}


}
