package contest.db;

import java.io.Serializable;
import java.util.Objects;

public abstract class BaseEntity implements Serializable {

    protected abstract Serializable getKey();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseEntity other = (BaseEntity) obj;
        if (!Objects.equals(other.getKey(), getKey())) {
            return false;
        }
        return obj == this;
    }

    @Override
    public int hashCode() {
        if (getKey() == null) {
            return super.hashCode();
        }

        return Objects.hash(getKey());
    }
}
