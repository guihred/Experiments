package contest.db;

import japstudy.db.BaseEntity;
import java.util.Objects;
import javax.persistence.*;

@Table
@Entity
public class Contest extends BaseEntity {
//    private static int keyCounter;
	@Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer key;
    @Column
    private String name;
    @Column
    @Enumerated(EnumType.STRING)
    private Organization organization;

    public Contest() {
    }

    public Contest(Organization organization) {
        this.organization = organization;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && super.equals(obj) && ((Contest) obj).key == key;
    }

    @Override
    public Integer getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Organization getOrganization() {
        return organization;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public void setKey(Integer id) {
        key = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}