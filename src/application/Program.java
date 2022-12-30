package application;

import java.util.InputMismatchException;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class Program {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		ChessMatch chessMatch = new ChessMatch();

		while (true) {
			try {
				// chamar metodo de limpar a tela
				UI.clearScreen();
				// imprimir a peça para mover
				UI.printBoard(chessMatch.getPieces());
				System.out.println();
				System.out.print("Source: ");
				ChessPosition source = UI.readChessPosition(sc);

				System.out.println();
				System.out.print("Target: ");
				ChessPosition target = UI.readChessPosition(sc);

				ChessPiece capturedPiece = chessMatch.performChessMove(source, target);

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
	}

}