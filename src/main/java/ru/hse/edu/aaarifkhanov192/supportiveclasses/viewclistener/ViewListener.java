package ru.hse.edu.aaarifkhanov192.supportiveclasses.viewclistener;

/**
 * Интерфейс, уведомляющий о том, что была нажата кнопка "ДА" в окне выбора загрузки файла.
 */
public interface ViewListener {
    void onChoose(ViewInfo data);
}
