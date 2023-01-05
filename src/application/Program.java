package application;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class Program {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		ChessMatch chessMatch = new ChessMatch();
		List<ChessPiece> captured = new ArrayList<>();

		while (!chessMatch.getCheckMate()) {
			try {
				// chamar metodo de limpar a tela
				UI.clearScreen();
				// imprimir a peça para mover
				UI.printMatch(chessMatch, captured);
				System.out.println();
				System.out.print("Source: ");
				ChessPosition source = UI.readChessPosition(sc);

				boolean[][] possibleMoves = chessMatch.possibleMoves(source);
				UI.clearScreen();
				// Imprime um novo board mas com os possiveis movs das peças (colorido)
				UI.printBoard(chessMatch.getPieces(), possibleMoves);

				System.out.println();
				System.out.print("Target: ");
				ChessPosition target = UI.readChessPosition(sc);

				ChessPiece capturedPiece = chessMatch.performChessMove(source, target);

				// Sempre que executar um mov e capturar uma peça
				if (capturedPiece != null) {
					// add na lista de peças capturadas
					captured.add(capturedPiece);
				}

				if (chessMatch.getPromoted() != null) {
					System.out.print("Enter piece for promotion (B/N/R/Q): ");
					String type = sc.nextLine();
					chessMatch.replacePromotedPiece(type);
				}

			} catch (ChessException e) {
				System.out.println(e.getMessage());
				// nextline serve para o programa aguardar carregar no enter
				sc.nextLine();
			} catch (InputMismatchException e) {
				System.out.println(e.getMessage());
				// nextline serve para o programa aguardar carregar no enter
				sc.nextLine();
			}
		}
		UI.clearScreen();
		// if o checkmate for feito, limpar a tela e mostrar a partida finalizada
		UI.printMatch(chessMatch, captured);
	}
}
