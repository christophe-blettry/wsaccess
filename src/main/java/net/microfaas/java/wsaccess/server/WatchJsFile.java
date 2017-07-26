/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.microfaas.java.wsaccess.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author christophe
 */
public class WatchJsFile extends Thread {

	private int fileVersion = 0;
	private final File file;
	private final WatchService watcher;
	private final CountDownLatch countDownLatch = new CountDownLatch(1);

	public WatchJsFile(File file) throws IOException {
		this.file = file;
		this.watcher = FileSystems.getDefault().newWatchService();
		Path dir = Paths.get(file.getAbsolutePath()).getParent();
		WatchKey key = dir.register(watcher, ENTRY_MODIFY);
	}

	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}

	@Override
	public void run() {
		countDownLatch.countDown();
		for (;;) {
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();
				if (kind == ENTRY_MODIFY) {
					WatchEvent<Path> ev = cast(event);
					Path name = ev.context();
					if (name.toFile().getAbsolutePath().equals(file.getAbsolutePath())) {
						fileVersion++;
					}
				}
			}
		}
	}

	public int getFileVersion() {
		return fileVersion;
	}

	public File getFile() {
		return file;
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}
}
