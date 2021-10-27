package ru.hse.edu.aaarifkhanov192.controllers.dirchanges;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Looks after directory by a given path for file changes.
 * Thanks to https://dzone.com/articles/listening-to-fileevents-with-java-nio;
 */
public class WatchFileChanges {
    private final List<IFileChangeListener> listeners = new ArrayList<>();
    private static final List<WatchService> services = new ArrayList<>();
    private Thread thread;
    private final String strPath;
    private final File file;

    public WatchFileChanges(String path) {
        this.strPath = path;
        this.file = new File(path);
    }

    public void startObserve() {
        if (file.exists()) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try (WatchService ws = FileSystems.getDefault().newWatchService()) {
                        Paths.get(file.getAbsolutePath()).register(ws, ENTRY_MODIFY);
                        services.add(ws);
                        boolean reset = true;

                        while (reset && !thread.isInterrupted()) {
                            WatchKey wk;
                            try {
                                wk = ws.take();
                                Path p = (Path) wk.watchable();
                                for (WatchEvent<?> e : wk.pollEvents()) {
                                    notifyListeners(e.kind(), p.resolve((Path) e.context()).toFile());
                                }
                                reset = wk.reset();
                            } catch (InterruptedException | ClosedWatchServiceException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread = new Thread(run);
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stopObserve() {
        for (WatchService ws : services) {
            try {
                ws.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());;
            }
        }
        thread.interrupt();
    }

    private void notifyListeners(WatchEvent.Kind<?> kind, File file) {
        if (kind == ENTRY_MODIFY) {
            FileEvent e = new FileEvent(file);
            for (IFileChangeListener l : listeners)
                l.fileEdited(e);
        }
    }

    public WatchFileChanges addListener(IFileChangeListener listener) {
        listeners.add(listener);
        return this;
    }

    public static List<WatchService> getWatchServices(){
        return services;
    }
}
