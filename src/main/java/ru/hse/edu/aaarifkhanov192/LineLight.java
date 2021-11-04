package ru.hse.edu.aaarifkhanov192;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для хранения и работы с линиями, которые были выделены.
 */
public class LineLight {
    private final List<Integer> stack = new ArrayList<>();
    private boolean isReversed = false;

    /**
     * Возвращает индексы линий, которые были выделены.
     *
     * @return Возвращает индексы линий, которые были выделены.
     */
    public List<Integer> getLines() {
        return stack;
    }

    /**
     * Возвращает <code>boolean</code> происходит выделение снизу вверх или сверху вниз.
     *
     * @return Возвращает <code>boolean</code> происходит выделение снизу вверх или сверху вниз.
     */
    public boolean isReversed() {
        return isReversed;
    }

    /**
     * Возвращает число выделенных линий.
     *
     * @return Возвращает число выделенных линий.
     */
    public int size() {
        return stack.size();
    }

    /**
     * Обрабатывает значение <code>value</code>, добавляет его или удаляет из списка линий.
     *
     * @param value Индекс новой выделенной линии.
     */
    public void process(int value) {
        if (!this.put(value)) {
            this.delete();
        }
    }

    /**
     * Сбрасывает все данные.
     */
    public void reload() {
        stack.removeAll(stack);
        isReversed = false;
    }

    /**
     * Добавляет значение <code>value</code> в список линий.
     *
     * @param value Индекс новой выделенной линии.
     * @return Возвращает <code>true</code>, если операция успешно выполнена, иначе <code>false.</code>
     */
    private boolean put(int value) {
        //Обработка если идет выделение сверху вниз.
        if ((this.size() == 1 && stack.get(0) > value)) {
            isReversed = true;
        } else if ((this.size() == 1 && stack.get(0) < value)) {
            isReversed = false;
        }

        //Если в обычном порядке, но передается значение меньше.
        if (!isReversed && !stack.isEmpty() && stack.get(this.size() - 1) > value) {
            return false;
        }

        //Если реверснуто, но передается значение больше.
        if (isReversed && !stack.isEmpty() && stack.get(this.size() - 1) < value) {
            return false;
        }

        if (!stack.isEmpty() && stack.get(this.size() - 1) == value) {
            return true;
        }

        //Если вдруг как-то пропустили линии.
        if (!stack.isEmpty() && Math.abs(stack.get(this.size() - 1) - value) > 1) {
            if (!isReversed) {
                for (int i = stack.get(this.size() - 1) + 1; i <= value; i++) {
                    stack.add(i);
                }
            } else {
                for (int i = stack.get(this.size() - 1) - 1; i >= value; i--) {
                    stack.add(i);
                }
            }

            return true;
        }

        stack.add(value);
        return true;
    }

    /**
     * Удаляет текущую выделенную линию.
     */
    private void delete() {
        stack.remove(this.size() - 1);
    }

    @Override
    public String toString() {
        return stack.toString();
    }
}
