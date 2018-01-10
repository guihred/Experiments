package election.experiment;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table
public class Cidade implements Serializable {
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

	@Override
	public int hashCode() {
		return Objects.hash(href);
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
		Cidade other = (Cidade) obj;

		return Objects.equals(other.href, href);
	}
}
