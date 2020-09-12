package utils;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class ListItr<T> implements ListIterator<T> {
    private final List<T> customList;
    protected int cursor;
    protected int lastRet = -1;
    protected int expectedModCount;

    ListItr(List<T> customList, int index) {
        this.customList = customList;
        cursor = index;
    }

    @Override
    public void add(T e) {
        checkForComodification();
        RunnableEx.run(() -> {
            customList.add(cursor, e);
            lastRet = -1;
            cursor = cursor + 1;
            expectedModCount = 0;
        });
    }

    @Override
    public boolean hasNext() {
        return cursor != this.customList.size();
    }

    @Override
    public boolean hasPrevious() {
        return cursor != 0;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException(cursor + " reached " + this.customList.size());
        }
        checkForComodification();
        return SupplierEx.get(() -> {
            int i = cursor;
            T next = this.customList.get(i);
            lastRet = i;
            cursor = i + 1;
            return next;
        });
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
            T previous = this.customList.get(i);
            lastRet = cursor = i;
            return previous;
        });
    }

    @Override
    public int previousIndex() {
        return cursor - 1;
    }

    @Override
    public void remove() {
        if (lastRet < 0) {
            throw new IllegalStateException();
        }
        checkForComodification();
        RunnableEx.run(() -> {
            customList.remove(lastRet);
            if (lastRet < cursor) {
                cursor--;
            }
            lastRet = -1;
            expectedModCount = 0;
        });
    }

    @Override
    public void set(T e) {
        if (lastRet < 0) {
            throw new IllegalStateException();
        }
        checkForComodification();
        RunnableEx.run(() -> {
            customList.set(lastRet, e);
            expectedModCount = 0;
        });
    }

    protected final void checkForComodification() {
        if (0 != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

}