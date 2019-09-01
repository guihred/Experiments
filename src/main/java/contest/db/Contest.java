package contest.db;

import japstudy.db.BaseEntity;
import javax.persistence.*;

@Table
@Entity
public class Contest extends BaseEntity {
	@Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer key;
    @Column
    private String name;
    @Column
    private String job;
    @Column
    @Enumerated(EnumType.STRING)
    private Organization organization;

    public Contest() {
    }

    public Contest(Organization organization) {
        this.organization = organization;
    }


    public String getJob() {
        return job;
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


    public void setJob(String job) {
        this.job = job;
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