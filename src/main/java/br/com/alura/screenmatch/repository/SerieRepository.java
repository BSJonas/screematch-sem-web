package br.com.alura.screenmatch.repository;

import java.util.List;
import java.util.Optional;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SerieRepository extends JpaRepository<Serie, Long> {
	Optional<Serie> findByTituloContainingIgnoreCase(String nomeSerie);

	List<Serie> findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(String nomeAtor, Double avaliacao);

	List<Serie> findTop5ByOrderByAvaliacaoDesc();

	List<Serie> findByGenero(Categoria categoria);

	List<Serie> findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(int totalTemporadas, Double avaliacao);

	@Query(value = "SELECT s from Serie s WHERE s.totalTemporadas <= :totalTemporadas and s.avaliacao >= :avaliacao")
	List<Serie> seriesPorTemporadaEAvaliacao(int totalTemporadas, Double avaliacao);

	@Query("SELECT e FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:trechoEpisodio%")
	List<Episodio> episodiosPorTrecho(String trechoEpisodio);

	@Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie ORDER BY e.avaliacao DESC LIMIT 5")
	List<Episodio> TopEpisodiosPorSerie(Serie serie);

	@Query("SELECT e FROM Serie s JOIN s.episodios e WHERE s = :serie AND YEAR(e.dataLancamento) >= :anoLancamento")
	List<Episodio> EpisodiosPorSerieEAno(Serie serie, int anoLancamento);
}
