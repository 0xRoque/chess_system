package boardgame;


public class Piece {

	protected Position position;
	private Board board;

	public Piece(Board board) {
		super();
		// não tem a peça criada pq a posição dela inicialmente é nula
		this.board = board;
		position = null;
	}

	protected Board getBoard() {
		return board;
	}
}
