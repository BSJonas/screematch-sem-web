package br.com.alura.screenmatch.controller;

import java.util.List;

import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/series")
public class SerieController {

	@Autowired
	private SerieService servico;

	@GetMapping
	public List<SerieDTO> obterSeries() {
		return servico.obterTodasAsSeries();
	}

	@GetMapping("/top5")
	public List<SerieDTO> obterTop5Series() {
		return servico.obterTop5Series();
	}

}