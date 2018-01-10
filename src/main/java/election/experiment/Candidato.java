package election.experiment;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table
public class Candidato implements Serializable {

	@Id
	private String href;

	private String nome;

	private String nomeCompleto;

	private Integer numero;

	private Integer votos;

	private LocalDate nascimento;

	private String naturalidade;

	private String ocupacao;

	private String grauInstrucao;

	private String partido;

	@ManyToOne
	@JoinColumn
	private Cidade cidade;

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
		Candidato other = (Candidato) obj;

		return Objects.equals(other.href, href);
	}

	public Cidade getCidade() {
		return cidade;
	}

	public String getGrauInstrucao() {
		return grauInstrucao;
	}

	public String getHref() {
		return href;
	}

	public LocalDate getNascimento() {
		return nascimento;
	}

	public String getNaturalidade() {
		return naturalidade;
	}

	public String getNome() {
		return nome;
	}

	public String getNomeCompleto() {
		return nomeCompleto;
	}

	public Integer getNumero() {
		return numero;
	}

	public String getOcupacao() {
		return ocupacao;
	}

	public String getPartido() {
		return partido;
	}

	public Integer getVotos() {
		return votos;
	}

	@Override
	public int hashCode() {
		return Objects.hash(href);
	}

	public void setCidade(Cidade cidade) {
		this.cidade = cidade;
	}
	public void setGrauInstrucao(String grauInstrucao) {
		this.grauInstrucao = grauInstrucao;
	}
	public void setHref(String href) {
		this.href = href;
	}

	public void setNascimento(LocalDate nascimento) {
		this.nascimento = nascimento;
	}
	public void setNaturalidade(String naturalidade) {
		this.naturalidade = naturalidade;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	public void setNomeCompleto(String nomeCompleto) {
		this.nomeCompleto = nomeCompleto;
	}
	public void setNumero(Integer numero) {
		this.numero = numero;
	}
	public void setOcupacao(String ocupacao) {
		this.ocupacao = ocupacao;
	}

	public void setPartido(String partido) {
		this.partido = partido;
	}
	public void setVotos(Integer votos) {
		this.votos = votos;
	}

	/*
	 * 1° 1.25% 7,681 votos Eleito
	 * 
	 * Romildo Santos é Vereador Eleito de Guarulhos pelo DEM. Nome Romildo Virginio
	 * dos Santos Naturalidade São Paulo - SP Estado Civil Casado Ocupação Outros
	 * Grau de Instrução Superior Completo
	 */
}