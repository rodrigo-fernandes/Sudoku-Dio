package br.com.dio;

import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.dio.model.Board;
import br.com.dio.model.Space;
import br.com.dio.util.BoardTemplate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

public class Main {

	private final static Scanner sc = new Scanner(System.in);
	private static Board board; 
	private final static int BOARD_LIMIT = 9;

	public static void main(String[] args) {

		// Converte args no formato: "0,0;5,true"
		final var positions = Stream.of(args)
				.filter(s -> s != null && s.contains(";"))
				.collect(Collectors.toMap(
						k -> k.split(";")[0],
						v -> v.split(";")[1]
				));

		var option = -1;
		while (true) {
			System.out.println("Selecione uma das opções a seguir");
			System.out.println("1 - Iniciar um novo Jogo");
			System.out.println("2 - Colocar um novo número");
			System.out.println("3 - Remover um número");
			System.out.println("4 - Visualizar jogo atual");
			System.out.println("5 - Verificar status do jogo");
			System.out.println("6 - Limpar jogo");
			System.out.println("7 - Finalizar jogo");
			System.out.println("8 - Sair");

			option = sc.nextInt();

			switch (option) {
				case 1 -> startGame(positions);
				case 2 -> inputNumber();
				case 3 -> removeNumber();
				case 4 -> showCurrentGame();
				case 5 -> showGameStatus();
				case 6 -> clearGame();
				case 7 -> finishGame();
				case 8 -> System.exit(0);
				default -> System.out.println("Opção inválida, selecione uma das opções do menu");
			}
		}
	}

	private static void startGame(Map<String, String> positions) {
		if (nonNull(board)) {
			System.out.println("O jogo já foi iniciado");
			return;
		}

		List<List<Space>> spaces = new ArrayList<>();

		for (int row = 0; row < BOARD_LIMIT; row++) {
			spaces.add(new ArrayList<>());

			for (int col = 0; col < BOARD_LIMIT; col++) {
				var key = "%s,%s".formatted(row, col);
				var positionConfig = positions.get(key);

				if (positionConfig == null) {
					// Preenche posição vazia com 0 não fixo
					positionConfig = "0,false";
				}

				var parts = positionConfig.split(",");
				var expected = Integer.parseInt(parts[0]);
				var fixed = Boolean.parseBoolean(parts[1]);

				spaces.get(row).add(new Space(expected, fixed));
			}
		}

		board = new Board(spaces);
		System.out.println("O jogo está pronto para começar");
	}

	private static void inputNumber() {
		if (isNull(board)) {
			System.out.println("O jogo ainda não foi iniciado");
			return;
		}

		System.out.println("Informe a coluna em que o numero será inserido: ");
		var col = runUntilGetValidNumber(0, 8);

		System.out.println("Informe a linha em que o numero será inserido: ");
		var row = runUntilGetValidNumber(0, 8);

		System.out.printf("Informe o número que vai entrar na posição [%s,%s]\n", col, row);
		var value = runUntilGetValidNumber(1, 9);

		if (!board.changeValue(col, row, value)) {
			System.out.printf("A posição [%s,%s] tem um valor fixo\n", col, row);
		}
	}

	private static void removeNumber() {
		if (isNull(board)) {
			System.out.println("O jogo ainda não foi iniciado");
			return;
		}

		System.out.println("Informe a coluna: ");
		var col = runUntilGetValidNumber(0, 8);

		System.out.println("Informe a linha: ");
		var row = runUntilGetValidNumber(0, 8);

		if (!board.clearValue(col, row)) {
			System.out.printf("A posição [%s,%s] tem um valor fixo\n", col, row);
		}
	}

	private static void showCurrentGame() {
		if (isNull(board)) {
			System.out.println("O jogo ainda não foi iniciado");
			return;
		}

		var args = new Object[81];
		int pos = 0;

		for (int row = 0; row < BOARD_LIMIT; row++) {
			for (int col = 0; col < BOARD_LIMIT; col++) {
				var space = board.getSpaces().get(row).get(col);
				args[pos++] = " " + (isNull(space.getActual()) ? " " : space.getActual());
			}
		}

		System.out.println("Seu jogo se encontra da seguinte forma:");
		System.out.printf(BoardTemplate.BOARD_TEMPLATE + "%n", args);
	}

	private static void showGameStatus() {
		if (isNull(board)) {
			System.out.println("O jogo ainda não foi iniciado");
			return;
		}

		System.out.printf("O jogo atualmente se encontra no status: %s\n", board.getStatus().getLabel());

		if (board.hasErrors()) {
			System.out.println("O jogo contém erros!");
		} else {
			System.out.println("O jogo não contém erros!");
		}
	}

	private static void clearGame() {
		if (isNull(board)) {
			System.out.println("O jogo ainda não foi iniciado");
			return;
		}

		System.out.println("Tem certeza que deseja limpar seu jogo e perder o progresso? (sim/não)");
		sc.nextLine(); // limpa buffer
		var confirm = sc.nextLine();

		while (!confirm.equalsIgnoreCase("sim") && !confirm.equalsIgnoreCase("não")) {
			System.out.println("Informe 'sim' ou 'não'");
			confirm = sc.nextLine();
		}

		if (confirm.equalsIgnoreCase("sim")) {
			board.reset();
		}
	}

	private static void finishGame() {
		if (isNull(board)) {
			System.out.println("O jogo ainda não foi iniciado");
			return;
		}

		if (board.gameIsFinished()) {
			System.out.println("Parabéns, você concluiu o jogo!");
			showCurrentGame();
			board = null;
		} else if (board.hasErrors()) {
			System.out.println("Seu jogo contém erros, verifique as posições incorretas.");
		} else {
			System.out.println("O jogo não está completo. Ainda há espaços vazios.");
		}
	}

	private static int runUntilGetValidNumber(final int min, final int max) {
		while (true) {
			if (!sc.hasNextInt()) {
				System.out.println("Digite apenas números.");
				sc.next();
				continue;
			}

			int current = sc.nextInt();
			if (current < min || current > max) {
				System.out.printf("Informe um número entre %s e %s: ", min, max);
				continue;
			}

			return current;
		}
	}
}
