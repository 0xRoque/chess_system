package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check;
	private boolean checkMate;
	private ChessPiece enPassantVulnerable;

	private List<Piece> piecesOnTheBoard = new ArrayList<>();;
	private List<Piece> capturedPieces = new ArrayList<>();

	// saber a dimensão do tab
	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		// para testar se roda
		initialSetUp();
	}

	public int getTurn() {
		return turn;
	}

	public Color getCurrentPlayer() {
		return currentPlayer;
	}

	public boolean getCheck() {
		return check;
	}

	public boolean getCheckMate() {
		return checkMate;
	}

	public ChessPiece getEnPassantVulnerable() {
		return enPassantVulnerable;
	}

	// retornar matriz de peças de chess
	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}
		return mat;
	}

	public boolean[][] possibleMoves(ChessPosition sourcePosition) {
		// converter a posição de xadrez para posição de matriz normal
		Position position = sourcePosition.toPosition();
		// mandar validar a posição
		validateSourcePosition(position);
		// retornar na tela os movs possiveis da peça nessa posição
		return board.piece(position).possibleMoves();
	}

	// Executar a jogadas
	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
		Position source = sourcePosition.toPosition();
		Position target = targetPosition.toPosition();
		validateSourcePosition(source);
		validateTargetPosition(source, target);
		Piece capturedPiece = makeMove(source, target);
		// testar se o mov colocou o jogador em cheque
		if (testCheck(currentPlayer)) {
			undoMove(source, target, capturedPiece);
			throw new ChessException("You can't put yourself in check");
		}

		ChessPiece movedPiece = (ChessPiece) board.piece(target);

		// Se o opponent ficou em cheque, atulizar a propriedade check
		check = (testCheck(opponent(currentPlayer))) ? true : false;
		// testar se a jogada feita deixar o oponente em checkmate o jogo termina
		if (testCheckMate(opponent(currentPlayer))) {
			checkMate = true;
		} else {
			// Se nao ficar em chequemate a partida continua e chama o proximo turno
			nextTurn();
		}

		// Special move En Passant
		if (movedPiece instanceof Pawn
				&& (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)) {
			enPassantVulnerable = movedPiece;

		} else {
			enPassantVulnerable = null;
		}

		return (ChessPiece) capturedPiece;
	}

	private Piece makeMove(Position source, Position target) {
		// tirar a peça de origem do tab
		ChessPiece p = (ChessPiece) board.removePiece(source);
		p.increaseMoveCount();
		// tirar do tab uma possivel peça capturada, que está na pos de target
		Piece capturedPiece = board.removePiece(target);
		// colocar na posicao target a peça que estava na posição de origem
		board.placePiece(p, target);
		// Se a peça capturada for diferente de null
		if (capturedPiece != null) {
			// Temos de remover a peça da lista de peças do tab
			piecesOnTheBoard.remove(capturedPiece);
			// adicionamos a peça na lista de peças capturadas
			capturedPieces.add(capturedPiece);
		}

		// Special Move castling King side Rook
		if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
			board.placePiece(rook, targetT);
			rook.increaseMoveCount();
		}
		// Special Move castling Queen side Rook
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
			board.placePiece(rook, targetT);
			rook.increaseMoveCount();
		}

		// Special move en passant
		if (p instanceof Pawn) {
			if (source.getColumn() != target.getColumn() && capturedPiece == null) {
				Position pawnPosition;
				if (p.getColor() == Color.WHITE) {
					pawnPosition = new Position(target.getRow() + 1, target.getColumn());
				} else {
					pawnPosition = new Position(target.getRow() - 1, target.getColumn());
				}
				capturedPiece = board.removePiece(pawnPosition);
				capturedPieces.add(capturedPiece);
				piecesOnTheBoard.remove(capturedPiece);
			}
		}

		return capturedPiece;
	}

	// Desfazer o mov, caso também se a pessoa se coloque em Cheque, este metodo não
	// vai deixar
	private void undoMove(Position source, Position target, Piece capturedPiece) {
		// tira a peça movida do destino
		ChessPiece p = (ChessPiece) board.removePiece(target);
		p.decreaseMoveCount();
		// peça retirada do destino, devolver para a posição de origem
		board.placePiece(p, source);
		// e se houve uma peça capturda? devolver para a posição de destino
		if (capturedPiece != null) {
			board.placePiece(capturedPiece, target);
			// tirar a peça da Lista de peças capturadas e colocar novamente na Lista de
			// peças do tab
			capturedPieces.remove(capturedPiece);
		}
		// Special Move castling King side Rook
		if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			board.placePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}
		// Special Move castling Queen side Rook
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			board.placePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}

		// Special move en passant
		if (p instanceof Pawn) {
			if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
				ChessPiece pawn = (ChessPiece) board.removePiece(target);
				Position pawnPosition;
				if (p.getColor() == Color.WHITE) {
					pawnPosition = new Position(3, target.getColumn());
				} else {
					pawnPosition = new Position(4, target.getColumn());
				}
				board.placePiece(pawn, pawnPosition);
			}
		}
	}

	private void validateSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece on source position");
		}
		// downcasting para chesspiece para pegar a cor
		if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
			throw new ChessException("The chosen piece is not yours");
		}
		if (!board.piece(position).isThereAnyPossibleMove()) {
			throw new ChessException("There is no possible moves for the chosen piece");
		}
	}

	private void validateTargetPosition(Position source, Position target) {
		// testar se para a peça de origem a possição possivel não é um mov possivel,
		// nao pode mexer para la
		if (!board.piece(source).possibleMove(target)) {
			throw new ChessException("The chosen piece can't move to target position");
		}
	}

	private void nextTurn() {
		// incrementar o turno, do 1 para o 2, etc
		turn++;
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}

	// Criar metodo opponent, devolve o opponent de uma cor
	private Color opponent(Color color) {
		// Se a cor passada como argumento for white, ENTÃO "?" retornar o color.black,
		// caso contrário ":" vai retornar color.white
		return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}

	// Metodo king color, este metodo é para localizar o rei de uma determinada cor
	private ChessPiece king(Color color) {
		/*
		 * Procurar na lista de peças em jogo, qual o rei da cor dada como argumento
		 * Filtar a lista, criar a lista e ela recebe as peças em jogo e vai procurar
		 * toda a peça x tal que a cor dessa peça x seja da cor que for passada como
		 * arguemento. mas esta lista é de piece e a piece não tem cor, o ChessPiece tem
		 * cor, então temos de fazer um downcasting
		 */
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());
		// Com a lista, vamos agora procurar, para cada peça p da minha lista list,
		// vamos testar o seguinte
		for (Piece p : list) {
			// se p for instanceof de King, encontramos o rei e retornamos p usando
			// downcasting
			if (p instanceof King) {
				return (ChessPiece) p;
			}
		}
		// Se esgostar o for e nao encontrar o rei, utilizamos o exceção
		throw new IllegalStateException("There is no " + color + " king on the board");
	}

	// testar se o rei desta cor está em check
	private boolean testCheck(Color color) {
		// Pegar a posição do rei no formato de matriz
		Position kingPosition = king(color).getChessPosition().toPosition();
		// Uma lista das peças do oponente dessa cor
		List<Piece> opponentPieces = piecesOnTheBoard.stream()
				.filter(x -> ((ChessPiece) x).getColor() == opponent(color)).collect(Collectors.toList());
		// Vamos então testar se para cada peça contida nesta lista, se existe algum mov
		// possivel que leve à posição do meu Rei
		for (Piece p : opponentPieces) {
			// Matriz de mov possiveis da peça adversária p
			boolean[][] mat = p.possibleMoves();
			// Se na matriz a posição correspondeste á posição do rei for true, o rei está
			// em check
			if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		// se esgostar o for, o rei não está em check
		return false;
	}

	// testar o checkMate
	private boolean testCheckMate(Color color) {
		/*
		 * eliminar a possibilidade de não estar em cheque se a cor nao estiver em
		 * cheque ele nã está em cheque-mate
		 */
		if (!testCheck(color)) {
			return false;
		}
		// Se todas as peças dessa cor, não tiver um mov possivel que o tire do check,
		// ele está em cheque-mate
		// pega as peças no tab e filtra todos os que foram igual a essa cor
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());
		// percorrer todas as peças p pertencentes à lista
		for (Piece p : list) {
			// se existir peça p que retira do check.. retorna falsa
			// mov possiveis da peça p
			boolean[][] mat = p.possibleMoves();
			// percorrer a matriz, linhas e colunas
			for (int i = 0; i < board.getRows(); i++) {
				for (int j = 0; j < board.getColumns(); j++) {
					// testar se for mov possivel
					if (mat[i][j]) {
						// Se tira do check é mov possivel e retorna falso
						Position source = ((ChessPiece) p).getChessPosition().toPosition();
						Position target = new Position(i, j);
						Piece capturedPiece = makeMove(source, target);
						// testar se ainda está em check depois do mov
						boolean testCheck = testCheck(color);
						// desfazer o mov
						undoMove(source, target, capturedPiece);
						if (!testCheck) {
							return false;
						}
					}
				}
			}

		}
		// Egostando o for é checkmate
		return true;
	}

	// Colocar peça e passar a posiçao nas cordenadas do xadrez
	private void placeNewPiece(char column, int row, ChessPiece piece) {
		// Alem de colocar a peça no tab...
		board.placePiece(piece, new ChessPosition(column, row).toPosition());
		// ..tambem colocamos a peça na lista de peças do board
		piecesOnTheBoard.add(piece);
	}

	// Este metodo inicia a partida e colocar peças no tab
	private void initialSetUp() {
		placeNewPiece('a', 1, new Rook(board, Color.WHITE));
		placeNewPiece('b', 1, new Knight(board, Color.WHITE));
		placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('d', 1, new Queen(board, Color.WHITE));
		placeNewPiece('e', 1, new King(board, Color.WHITE, this));
		placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('g', 1, new Knight(board, Color.WHITE));
		placeNewPiece('h', 1, new Rook(board, Color.WHITE));
		placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
		placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

		placeNewPiece('a', 8, new Rook(board, Color.BLACK));
		placeNewPiece('b', 8, new Knight(board, Color.BLACK));
		placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('d', 8, new Queen(board, Color.BLACK));
		placeNewPiece('e', 8, new King(board, Color.BLACK, this));
		placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('g', 8, new Knight(board, Color.BLACK));
		placeNewPiece('h', 8, new Rook(board, Color.BLACK));
		placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
		placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
	}

}
