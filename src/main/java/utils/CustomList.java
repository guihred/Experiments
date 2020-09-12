
package utils;

import java.util.*;
import utils.ex.PredicateEx;

public class CustomList<T> extends LinkedHashSet<T> implements List<T> {

    @Override
    public void add(int index, T element) {
        Iterator<T> iterator = iterator();
        LinkedHashSet<T> removed = new LinkedHashSet<>();
        for (int i = 0; iterator.hasNext(); i++) {
            T next = iterator.next();
            if (i >= index) {
                this.remove(next);
                removed.add(next);
            }
        }
        add(element);
        addAll(removed);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        Iterator<T> iterator = listIterator();
        LinkedHashSet<T> removed = new LinkedHashSet<>();
        for (int i = 0; iterator.hasNext(); i++) {
            T next = iterator.next();
            if (i >= index) {
                this.remove(next);
                removed.add(next);
            }
        }
        return removed.addAll(c) && addAll(removed);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Collection)) {
            return false;
        }
        Collection<?> c = (Collection<?>) o;
        if (c.size() != size()) {
            return false;
        }
        return PredicateEx.test(this::containsAll, c);
    }

    @Override
    public T get(int index) {
        Iterator<T> iterator = iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            T next = iterator.next();
            if (i == index) {
                return next;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        int h = 0;
        Iterator<T> i = iterator();
        while (i.hasNext()) {
            T obj = i.next();
            if (obj != null) {
                h += obj.hashCode();
            }
        }
        return h;
    }

    @Override
    public int indexOf(Object o) {
        Iterator<T> it = iterator();
        int i = 0;
        while (it.hasNext()) {
            if (o.equals(it.next())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return new ListItr<>(this, 0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new ListItr<>(this, index);
    }

    @Override
    public T remove(int index) {
        Iterator<T> iterator = iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            T next = iterator.next();
            if (i == index) {
                iterator.remove();
                return next;
            }
        }
        return null;
    }

    @Override
    public T set(int index, T element) {
        Iterator<T> iterator = iterator();
        T removedT = null;
        LinkedHashSet<T> removed = new LinkedHashSet<>();
        for (int i = 0; iterator.hasNext(); i++) {
            T next = iterator.next();
            if (i >= index) {
                iterator.remove();
                if (i > index) {
                    removed.add(next);
                } else {
                    removedT = next;
                }
            }
        }
        add(element);
        addAll(removed);
        return removedT;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        Iterator<T> iterator = iterator();
        ArrayList<T> removed = new ArrayList<>();
        for (int i = 0; iterator.hasNext(); i++) {
            T next = iterator.next();
            if (i >= fromIndex && i < toIndex) {
                removed.add(next);
            }
        }
        return removed;
    }
}