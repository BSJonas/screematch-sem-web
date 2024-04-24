package br.com.alura.screenmatch.service;

import java.util.List;
import java.util.stream.Collectors;

import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SerieService {

	@Autowired
	private SerieRepository repositorio;

	private List<SerieDTO> converteDados(List<Serie> series) {
		return series.stream()
			.map(s -> new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster(), s.getSinopse()))
			.collect(Collectors.toList());
	}

	public List<SerieDTO> obterTodasAsSeries() {
		return converteDados(repositorio.findAll());

	}

	public List<SerieDTO> obterTop5Series() {
		return converteDados(repositorio.findTop5ByOrderByAvaliacaoDesc());

	}

}
