package rosario;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;

public class Medicamento {
	private String registro;
	private String codigo;
	private String nome;
	private String apresentacao;
	private String lote;
	private Integer quantidade;
	private BooleanBinding registroValido;
	private BooleanBinding loteValido;
	private BooleanBinding quantidadeValido;

	public Medicamento clonar() {
		Medicamento medicamento2 = new Medicamento();
		medicamento2.setCodigo(codigo);
		medicamento2.setApresentacao(apresentacao);
		medicamento2.setNome(nome);
		medicamento2.setRegistro(registro);
		medicamento2.setLote(lote);
		medicamento2.setQuantidade(quantidade);

		return medicamento2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (lote == null ? 0 : lote.hashCode());
		result = prime * result
				+ (quantidade == null ? 0 : quantidade.hashCode());
		result = prime * result + (registro == null ? 0 : registro.hashCode());
		return result;
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
		Medicamento other = (Medicamento) obj;
		if (lote == null) {
			if (other.lote != null) {
				return false;
			}
		} else if (!lote.equals(other.lote)) {
			return false;
		}
		if (quantidade == null) {
			if (other.quantidade != null) {
				return false;
			}
		} else if (!quantidade.equals(other.quantidade)) {
			return false;
		}
		if (registro == null) {
			if (other.registro != null) {
				return false;
			}
		} else if (!registro.equals(other.registro)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return registro + "," + nome + "," + apresentacao + "," + lote
				+ "," + quantidade;
	}

	public String getRegistro() {
		return registro;
	}

	public boolean isRegistroValido() {
		if (registroValido == null) {
			return false;
		}

		return registroValido.get();
	}

	public boolean isLoteValido() {
		if (loteValido == null) {
			return false;
		}

		return loteValido.get();
	}

	public boolean isQuantidadeValido() {
		if (quantidadeValido == null) {
			return false;
		}

		return quantidadeValido.get();
	}

	public BooleanBinding registroValidoProperty(ObservableList<Medicamento> medicamentos) {
		if (registroValido == null) {
			registroValido = new BooleanBinding() {
				{
					bind(medicamentos);
				}

				@Override
				protected boolean computeValue() {
					return medicamentos.stream().anyMatch(m -> m.getRegistro().equals(registro));
				}
			};
		}

		return registroValido;
	}


	public BooleanBinding loteValidoProperty(ObservableList<Medicamento> medicamentos) {
		if (loteValido == null) {
			loteValido = new BooleanBinding() {
				{
					bind(medicamentos);
				}

				@Override
				protected boolean computeValue() {
					return medicamentos.stream().anyMatch(
							m -> m.getRegistro().equals(registro) && m.getLote().equals(lote));
				}
			};
		}

		return loteValido;
	}

	public BooleanBinding quantidadeValidoProperty(ObservableList<Medicamento> medicamentos) {
		if (quantidadeValido == null) {
			quantidadeValido = new BooleanBinding() {
				{
					bind(medicamentos);
				}

				@Override
				protected boolean computeValue() {
					return medicamentos.stream().anyMatch(
							m -> m.getRegistro().equals(registro) && m.getLote().equals(lote)
									&& m.getQuantidade().equals(quantidade));
				}
			};
		}

		return quantidadeValido;
	}

	public void setRegistro(String registro) {
		this.registro = registro;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getApresentacao() {
		return apresentacao;
	}

	public void setApresentacao(String apresentacao) {
		this.apresentacao = apresentacao;
	}

	public String getLote() {
		return lote;
	}

	public void setLote(String lote) {
		this.lote = lote;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
}