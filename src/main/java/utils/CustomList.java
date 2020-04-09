package utils;

import java.util.*;

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
        Iterator<T> iterator = iterator();
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

        if (!(o instanceof Set) && !(o instanceof List)) {
            return false;
        }
        Collection<?> c = (Collection<?>) o;
        if (c.size() != size()) {
            return false;
        }
        return PredicateEx.makeTest(this::containsAll).test(c);
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
        return new ListItr(0);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new ListItr(index);
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

    private class Itr implements Iterator<T> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor = 0;

        /**
         * Index of element returned by most recent call to next or previous. Reset to
         * -1 if this element is deleted by a call to remove.
         */
        int lastRet = -1;

        /**
         * The modCount value that the iterator believes that the backing List should
         * have. If this expectation is violated, the iterator has detected concurrent
         * modification.
         */
        int expectedModCount = 0;

        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public T next() {
            checkForComodification();
            return SupplierEx.get(() -> {
                int i = cursor;
                T next = get(i);
                lastRet = i;
                cursor = i + 1;
                return next;
            });
        }

        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            checkForComodification();

            RunnableEx.run(() -> {
                CustomList.this.remove(lastRet);
                if (lastRet < cursor) {
                    cursor--;
                }
                lastRet = -1;
                expectedModCount = 0;
            });
        }

        final void checkForComodification() {
            if (0 != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class ListItr extends Itr implements ListIterator<T> {
        ListItr(int index) {
            cursor = index;
        }

        @Override
        public void add(T e) {
            checkForComodification();

            RunnableEx.run(() -> {

                int i = cursor;
                CustomList.this.add(i, e);
                lastRet = -1;
                cursor = i + 1;
                expectedModCount = 0;
            });
        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public T previous() {
            checkForComodification();
            return SupplierEx.get(() -> {
                int i = cursor - 1;
                T previous = get(i);
                lastRet = cursor = i;
                return previous;
            });
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void set(T e) {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            checkForComodification();
            RunnableEx.run(() -> {
                CustomList.this.set(lastRet, e);
                expectedModCount = 0;
            });
        }

    }
}