package election;

import japstudy.db.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table
public class Cidade extends BaseEntity {
	private String nome;
	private String estado;
	private Integer eleitores;
	@Id
	private String href;


	public Integer getEleitores() {
		return eleitores;
	}

	public String getEstado() {
		return estado;
	}

	public String getHref() {
		return href;
	}

	public String getNome() {
		return nome;
	}

	public void setEleitores(Integer eleitores) {
		this.eleitores = eleitores;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public String toString() {
		return String.format("Cidade[nome=%s,estado=%s,eleitores=%d,href=%s]", nome, estado, eleitores, href);
	}


    @Override
    protected String getKey() {
        return getHref();
    }
}
