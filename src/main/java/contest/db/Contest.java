package contest.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table
@Entity
public class Contest extends BaseEntity {
	private static int KEY;
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
	private Integer key = KEY++;
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
    public Integer getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Organization getOrganization() {
        return organization;
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