package rosario;

import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;

public class Medicamento {
	private String registro;
	private Integer codigo;
	private String nome;
	private String apresentacao;
	private String lote;
	private Integer quantidade;
	private BooleanBinding registroValido;
	private BooleanBinding loteValido;
	private BooleanBinding quantidadeValido;
	private BooleanBinding codigoValido;

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
	public String toString() {
		return registro + "," + nome + "," + codigo + "," + lote + "," + quantidade;
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
			registroValido = Bindings.createBooleanBinding(
					() -> medicamentos.stream().anyMatch(m -> Objects.equals(m.getRegistro(), registro)), medicamentos);
		}

		return registroValido;
	}

	public BooleanBinding loteValidoProperty(ObservableList<Medicamento> medicamentos) {
		if (loteValido == null) {
			loteValido = Bindings.createBooleanBinding(
					() -> medicamentos.stream().anyMatch(
							m -> Objects.equals(m.getRegistro(), registro) && Objects.equals(m.getLote(), lote)),
					medicamentos);
		}

		return loteValido;
	}

	public BooleanBinding quantidadeValidoProperty(ObservableList<Medicamento> medicamentos) {
		if (quantidadeValido == null) {
			quantidadeValido = Bindings.createBooleanBinding(
					() -> medicamentos.stream().anyMatch(m -> Objects.equals(m.getRegistro(), registro)
							&& Objects.equals(m.getLote(), lote) && Objects.equals(m.getQuantidade(), quantidade)),
					medicamentos);
		}

		return quantidadeValido;
	}

	public BooleanBinding quantidadeCodigoValidoProperty(ObservableList<Medicamento> medicamentos) {
		if (quantidadeValido == null) {
			quantidadeValido = Bindings.createBooleanBinding(() -> medicamentos.stream()
					.filter(m -> Objects.equals(Integer.valueOf(m.getCodigo()), Integer.valueOf(codigo)))
					.mapToInt(c -> c.getQuantidade()).sum() == quantidade, medicamentos);
		}

		return quantidadeValido;
	}

	public BooleanBinding codigoValidoProperty(ObservableList<Medicamento> medicamentos) {
		if (codigoValido == null) {
			codigoValido = Bindings.createBooleanBinding(
					() -> medicamentos.stream()
							.anyMatch(m -> Objects.equals(Integer.valueOf(m.getCodigo()), Integer.valueOf(codigo))),
					medicamentos);
		}

		return codigoValido;
	}

	public boolean isCodigoValido() {
		if (codigoValido == null) {
			return false;
		}

		return codigoValido.get();

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

	public Integer getCodigo() {
		return codigo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}
}