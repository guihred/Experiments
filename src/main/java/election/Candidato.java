package election;

import japstudy.db.BaseEntity;
import java.time.LocalDate;
import javax.persistence.*;

@Entity
@Table
public class Candidato extends BaseEntity {

    private String cargo;

    @ManyToOne
    @JoinColumn
    private Cidade cidade;

    private String fotoUrl;

    private String grauInstrucao;
    @Id
    private String href;

    private LocalDate nascimento;

    private String naturalidade;

    private String nome;

    private String nomeCompleto;

    private Integer numero;
    private String ocupacao;

    private String partido;

    private Integer votos;
    private Boolean eleito = false;

    public String getCargo() {
        return cargo;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public Boolean getEleito() {
        return eleito;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getGrauInstrucao() {
        return grauInstrucao;
    }

    public String getHref() {
        return href;
    }

    @Override
    public String getKey() {
        return getHref();
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

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }

    public void setEleito(Boolean eleito) {
        this.eleito = eleito;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
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

    @Override
    public String toString() {
        return String.format("[%d, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %d, %s]", numero, cargo, cidade,
            fotoUrl, grauInstrucao, href, nascimento, naturalidade, nome, nomeCompleto, ocupacao, partido, votos,
            eleito);
    }

}