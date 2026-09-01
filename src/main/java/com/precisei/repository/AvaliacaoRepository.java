package com.precisei.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.precisei.model.Avaliacao;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    boolean existsBySolicitacaoId(Long solicitacaoId);

    @Query("""
            select s.profissional.id as profissionalId,
                   avg(a.nota) as media,
                   count(a) as quantidade
              from Avaliacao a
              join a.solicitacao s
             group by s.profissional.id
            """)
    List<ResumoProfissional> resumirPorProfissional();

    interface ResumoProfissional {
        Long getProfissionalId();
        Double getMedia();
        Long getQuantidade();
    }
}
