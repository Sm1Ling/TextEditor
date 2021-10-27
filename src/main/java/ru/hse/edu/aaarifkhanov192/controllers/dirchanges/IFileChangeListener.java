package ru.hse.edu.aaarifkhanov192.controllers.dirchanges;

/**
 * Все слушатели директории реализуют данный интерфейс.
 */
public interface IFileChangeListener {
    /**
     * Метод будет выполняться, когда произойдут изменения в файлах директории.
     *
     * @param event Тип <code>{@link FileEvent}</code>.
     */
    void fileEdited(FileEvent event);
}
