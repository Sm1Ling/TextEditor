package ru.hse.edu.aaarifkhanov192.supportiveclasses.dirchanges;

import javafx.concurrent.Task;

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
    private String observedFileName;
    private final File file;

    /**
     * Constructor.
     *
     * @param path Path to root directory of the file.
     */
    public WatchFileChanges(String path) {
        this.file = new File(path);
    }

    public String getObservedFileName() {
        return observedFileName;
    }

    public void setObservedFileName(String filename) {
        this.observedFileName = filename;
    }

    public static List<WatchService> getWatchServices() {
        return services;
    }

    /**
     * Запускает новый <code>{@link Thread}</code> и начинает следить за директорией <code>file</code>.
     */
    public void startObserve() {
        if (file.exists()) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try (WatchService ws = FileSystems.getDefault().newWatchService()) {
                        Paths.get(file.getAbsolutePath()).register(ws, ENTRY_MODIFY);
                        services.add(ws);
                        boolean reset = true;

                        while (reset && !thread.isInterrupted()) {
                            WatchKey wk;
                            try {
                                wk = ws.take();
                                Path p = (Path) wk.watchable();
                                Thread.sleep(50);   //Stop get 2 events in a row.
                                for (WatchEvent<?> e : wk.pollEvents()) {
                                    var path = (Path) e.context();
                                    if (observedFileName == null ||
                                            path.toFile().getAbsolutePath().equals(observedFileName)) {
                                        notifyListeners(e.kind(), p.resolve(path).toFile());
                                    }
                                }
                                reset = wk.reset();
                            } catch (InterruptedException | ClosedWatchServiceException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }
            };

            thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Останавливает поток и завершает все <code>{@link WatchService}</code>.
     */
    public void stopObserve() {
        for (WatchService ws : services) {
            try {
                ws.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        thread.interrupt();
    }

    /**
     * Уведомляет слушаетей об изменениях в директории.
     * @param kind Тип изменения.
     * @param file Файл.
     */
    private void notifyListeners(WatchEvent.Kind<?> kind, File file) {
        if (kind == ENTRY_MODIFY) {
            FileEvent e = new FileEvent(file);
            for (IFileChangeListener l : listeners) {
                l.fileEdited(e);
            }
        }
    }

    /**
     * Добавляет слушателя директории.
     * @param listener Новый слушатель.
     */
    public WatchFileChanges addListener(IFileChangeListener listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * Прерван ли поток.
     */
    public boolean isInterrupted() {
        return thread.isInterrupted();
    }
}
