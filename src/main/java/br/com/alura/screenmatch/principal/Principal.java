package br.com.alura.screenmatch.principal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

public class Principal {

	private Scanner leitura = new Scanner(System.in);
	private ConsumoApi consumo = new ConsumoApi();
	private ConverteDados conversor = new ConverteDados();
	private final String ENDERECO = "https://www.omdbapi.com/?t=";
	private final String API_KEY = "&apikey=6585022c";
	private List<DadosSerie> dadosSeries = new ArrayList<>();

	private SerieRepository repositorio;

	List<Serie> series = new ArrayList<>();

	private Optional<Serie> serieBusca;

	public Principal(SerieRepository repositorio) {
		this.repositorio = repositorio;
	}

	public void exibeMenu() {
		var opcao = -1;
		while (opcao != 0) {
			var menu = """
				\n
				1 - Buscar séries
				2 - Buscar episódios
				3 - Listar séries buscadas
				4 - Buscar série por título
				5 - Buscar série por ator
				6 - Top 5 Séries
				7 - Buscar Séries por categoria
				8 - Buscar Séries por quantidade de temporadas e avaliação mínima
				9 - Buscar episódios por trecho
				10 - Top 5 Episódios por série
				11 - Buscar episódios a partir de uma data 

				0 - Sair                                 
				""";

			System.out.println(menu);
			opcao = leitura.nextInt();
			leitura.nextLine();

			switch (opcao) {
			case 1:
				buscarSerieWeb();
				break;
			case 2:
				buscarEpisodioPorSerie();
				break;
			case 3:
				buscarSeriePorTitulo();
				break;
			case 4:
				listarSeriesBuscadas();
				break;
			case 5:
				buscarSeriePorAtor();
				break;
			case 6:
				buscarTop5Series();
				break;
			case 7:
				buscarSeriePorCategoria();
				break;
			case 8:
				buscaPorQuantidadeTemporadaEAvaliacao();
				break;
			case 9:
				buscarEpisodioPorTrecho();
				break;
			case 10:
				topEpisodiosPorSerie();
				break;
			case 11:
				topEpisodiosDepoisDeUmaData();
				break;
			case 0:
				System.out.println("Saindo...");
				break;
			default:
				System.out.println("Opção inválida");
			}
		}

	}

	private void buscarSerieWeb() {
		DadosSerie dados = getDadosSerie();
		Serie serie = new Serie(dados);
		//dadosSeries.add(dados);
		repositorio.save(serie);
		System.out.println(dados);
	}

	private DadosSerie getDadosSerie() {
		System.out.println("Digite o nome da série para busca");
		var nomeSerie = leitura.nextLine();
		var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		return dados;
	}

	private void buscarEpisodioPorSerie() {
		listarSeriesBuscadas();
		System.out.println("Escolha uma série pelo nome: ");
		var nomeSerie = leitura.nextLine();
		List<DadosTemporada> temporadas = new ArrayList<>();

		//Optional<Serie> serie = series.stream()
		//	.filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
		//	.findFirst();

		Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

		if (serie.isPresent()) {

			var serieEncontrada = serie.get();
			for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
				var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
				DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
				temporadas.add(dadosTemporada);
			}
			temporadas.forEach(System.out::println);

			List<Episodio> episodios = temporadas.stream()
				.flatMap(d -> d.episodios().stream()
					.map(e -> new Episodio(d.numero(), e)))
				.collect(Collectors.toList());
			serieEncontrada.setEpisodios(episodios);
			repositorio.save(serieEncontrada);
		} else {
			System.out.println("Série não encontrada");
		}
	}

	private void listarSeriesBuscadas() {
		series = repositorio.findAll();
		series.stream()
			.sorted(Comparator.comparing(Serie::getGenero))
			.forEach(System.out::println);
	}

	private void buscarSeriePorTitulo() {
		System.out.println("Escolha uma série pelo nome: ");
		var nomeSerie = leitura.nextLine();
		serieBusca = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

		if (serieBusca.isPresent()) {
			System.out.println("Dados da série: " + serieBusca.get());

		} else {
			System.out.println("Série não encontrada");
		}

	}

	private void buscarSeriePorAtor() {
		System.out.println("Qual o nome para busca: ");
		var nomeAtor = leitura.nextLine();
		System.out.println("Avaliação a partir de que valor? ");
		var avaliacao = leitura.nextDouble();
		List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
		System.out.println("Séries em que " + nomeAtor + " Trabalhou");
		seriesEncontradas.forEach(s ->
			System.out.println(("Título: " + s.getTitulo() + " avaliação" + s.getAvaliacao())));

	}

	private void buscarTop5Series() {
		//		System.out.println("Buscar top 5 séries");
		//		var seriesTop = leitura.nextLine();
		List<Serie> seriesTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
		seriesTop.forEach(s ->
			System.out.println("Título: " + s.getTitulo() + " avaliação: " + s.getAvaliacao()));
	}

	private void buscarSeriePorCategoria() {
		System.out.println("Deseja buscar séries de que categoria/gênero? ");
		var nomeGenero = leitura.nextLine();
		Categoria categoria = Categoria.fromPortugues(nomeGenero);
		List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
		System.out.println("Séries da categoria " + nomeGenero);
		seriesPorCategoria.forEach(System.out::println);
	}

	private void buscaPorQuantidadeTemporadaEAvaliacao() {
		System.out.println("Série até quantas temporadas? ");
		var totalTemporadas = leitura.nextInt();
		leitura.nextLine();
		System.out.println("Avaliação a partir de qual valor? ");
		var avaliacao = leitura.nextDouble();
		leitura.nextLine();
		List<Serie> serieTemporadaAvaliacao = repositorio.seriesPorTemporadaEAvaliacao(totalTemporadas, avaliacao);
		System.out.println("Séries com " + totalTemporadas + " quantidade de temporadas, e avaliação mínima de: " + avaliacao);
		serieTemporadaAvaliacao.forEach(s ->
			System.out.println(s.getTitulo() + " - Avaliação: " + s.getAvaliacao()));

	}

	private void buscarEpisodioPorTrecho() {
		System.out.println("Qual o nome do episódio para busca?");
		var trechoEpisodio = leitura.nextLine();
		List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(trechoEpisodio);
		episodiosEncontrados.forEach(e ->
			System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
				e.getSerie().getTitulo(), e.getTemporada(),
				e.getNumeroEpisodio(), e.getTitulo()));
	}

	private void topEpisodiosPorSerie() {
		buscarSeriePorTitulo();
		if (serieBusca.isPresent()) {
			Serie serie = serieBusca.get();
			List<Episodio> topEpisodios = repositorio.TopEpisodiosPorSerie(serie);
			topEpisodios.forEach(e ->
				System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
					e.getSerie().getTitulo(), e.getTemporada(),
					e.getNumeroEpisodio(), e.getTitulo()));
		}
	}

	private void topEpisodiosDepoisDeUmaData() {
		buscarSeriePorTitulo();
		if (serieBusca.isPresent()) {
			Serie serie = serieBusca.get();
			System.out.println("Digite o ano limite de lançamento: ");
			var anoLancamento = leitura.nextInt();
			leitura.nextLine();
			List<Episodio> episodiosAno = repositorio.EpisodiosPorSerieEAno(serie, anoLancamento);
			episodiosAno.forEach(System.out::println);
		}
	}

}